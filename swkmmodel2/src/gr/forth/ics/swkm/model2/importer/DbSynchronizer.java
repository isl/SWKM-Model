/* 
 *  COPYRIGHT (c) 2008-2009 by Institute of Computer Science, 
 *  Foundation for Research and Technology - Hellas
 *  Contact: 
 *      POBox 1385, Heraklio Crete, GR-700 13 GREECE
 *      Tel:+30-2810-391632
 *      Fax: +30-2810-391638
 *      E-mail: isl@ics.forth.gr
 *      http://www.ics.forth.gr/isl
 *
 *   Authors  :  Dimitris Andreou, Nelly Vouzoukidou.
 *
 *   This file is part of SWKM model APIs (see also http://athena.ics.forth.gr:9090/SWKM/).
 *
 *    SWKM model APIs is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *   SWKM model APIs is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SWKM model APIs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   SWKM has been partially supported by EU project KP-Lab (IP IST-27490) kp-lab.org
 */


package gr.forth.ics.swkm.model2.importer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import gr.forth.ics.graph.Node;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Uri.Delimiter;
import gr.forth.ics.swkm.model2.labels.Interval;
import gr.forth.ics.swkm.model2.labels.Label;
import gr.forth.ics.swkm.model2.labels.Labeler;
import gr.forth.ics.swkm.model2.labels.Labelers;
import gr.forth.ics.swkm.model2.labels.PredefinedLabels;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import gr.forth.ics.swkm.model2.Triple;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Synchronizes a main-memory model with the contents of the currently connected database.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class DbSynchronizer {
    private final Model model;
    private final Map<TypedId, StoredResource> storedResources;
    private final Map<Integer, StoredProperty> storedProperties;
    private final Map<Integer, Uri> virtualResources;

    private DbSynchronizer(
            Model model,
            Map<TypedId, StoredResource> storedResources,
            Map<Integer, StoredProperty> storedProperties,
            Map<Integer, Uri> virtualResources) {
        this.model = model;
        this.storedResources = storedResources;
        this.storedProperties = storedProperties;
        this.virtualResources = virtualResources;
    }

    KnownDbState synchronize(Uri sideEffectsNamedGraph) {
        final Resource graph = model.mapResource(sideEffectsNamedGraph);
        final Resource rdfType = model.mapResource(Rdf.TYPE);
        final Resource rdfClass = model.mapResource(RdfSchema.CLASS);
        final Resource rdfSubClassOf = model.mapResource(RdfSchema.SUBCLASSOF);
        final Resource rdfDomain = model.mapResource(RdfSchema.DOMAIN);
        final Resource rdfRange = model.mapResource(RdfSchema.RANGE);
        final Resource rdfResource = model.mapResource(RdfSchema.RESOURCE);
        final Resource rdfProperty = model.mapResource(Rdf.PROPERTY);

        Map<Uri, Label> labels = Maps.newHashMap();

        for (StoredResource resource : storedResources.values()) {
            final Resource obj;
            switch (resource.type) {
                case CLASS: obj = rdfResource; break;
                case METACLASS: obj = rdfClass; break;
                case METAPROPERTY: obj = rdfProperty; break;
                default: throw new AssertionError(resource.type);
            }
            labels.put(resource.uri, new Label(resource.interval));
            final Resource subj = model.mapResource(resource.uri);
            if (subj == obj) {
                //disallow <rdfs:Resource rdfs:subClassOf rdfs:Resource>
                //and <rdfs:Class rdfs:subClassOf rdfs:Class>
                continue;
            }
            model.add(graph, subj, rdfSubClassOf, obj);
        }

        for (StoredProperty property : storedProperties.values()) {
            Resource resource = model.mapResource(property.uri);
            model.add(graph, resource, rdfType, rdfProperty);
            model.add(graph, resource, rdfDomain, model.mapResource(
                    storedResources.get(property.domain).uri));

            if (virtualResources.containsKey(property.range.postId)) {
                model.add(graph, resource, rdfRange, model.mapResource(virtualResources.get(property.range.postId)));
            } else {
                model.add(graph, resource, rdfRange, model.mapResource(
                        storedResources.get(property.range).uri));
            }

            labels.put(property.uri, new Label(property.interval));
        }

        PredefinedLabels predefinedLabels = PredefinedLabels.fromUriMap(labels);
        return new KnownDbState(predefinedLabels);
    }

    static DbSynchronizer forModel(Model model) throws SQLException {
        Collection<Uri> resources = Sets.newHashSet();
        for (Resource r : model.resources()) {
            resources.add(r.getUri());
        }
        resources.add(RdfSchema.RESOURCE);
        resources.add(RdfSchema.CLASS);
        
        /*
         * To find a set of resources, three steps are taken:
         * 1) Load matching classes, metaclasses, metaproperties
         * 2) Load matching properties
         * 3) Load domains and ranges of loaded properties
         */
        
        //multiple post Ids can be mapped to different resources, because labels
        //are disjoint for different types, for example both a class *and* a property
        //may exist where both have their post Ids = 405
        Map<TypedId, StoredResource> storedResources = Maps.newHashMap();
        Multimap<String, String> namesBySpace = HashMultimap.create();
        for (Uri uri : resources) {
            namesBySpace.put(uri.getNamespaceUri().toString(Delimiter.WITH), uri.getLocalName());
        }

        //Step 1: Load matching classes, metaclasses, metaproperties

        /*
         * Note: SUBCLASS table, as well the other similar ones, have multiple rows per id:
         * one row per direct ancestor of that resource. So when we query that table to find
         * the resource's index, multiple rows can be returned, and we convert the result
         * from bag-semantics to set-semantics.
         */

        //Input needed: [localNames, ns] * 3
        PreparedStatement classesOrMetatypes = Jdbc.prepared(
                //classes
                "SELECT DISTINCT c.att2, sc.att2, c.att0, 1 " + //localName, index, post
                "FROM t" + DbConstants.getIdFor(RdfSchema.CLASS) + " c, NAMESPACE ns, SUBCLASS sc " +
                "WHERE c.att2 = ANY(?) AND c.att1 = ns.att0 AND ns.att1 = ? AND c.att0 = sc.att0 " +
                "UNION ALL " +
                //metaclasses
                "SELECT DISTINCT mc.att2, smc.att2, mc.att0, 2 " + //localName, index, post
                "FROM METACLASS mc, NAMESPACE ns, SUBMETACLASS smc " +
                "WHERE mc.att2 = ANY(?) AND mc.att1 = ns.att0 AND ns.att1 = ? AND smc.att0 = mc.att0 " +
                "UNION ALL " +
                //metaproperties
                "SELECT DISTINCT mc.att2, smp.att2, mc.att0, 3 " + //localName, index, post
                "FROM METACLASS mc, NAMESPACE ns, SUBMETAPROPERTY smp " +
                "WHERE mc.att2 = ANY(?) AND mc.att1 = ns.att0 AND ns.att1 = ? AND smp.att0 = mc.att0");

        Iterator<String> nsIterator = namesBySpace.keySet().iterator();
        while (nsIterator.hasNext()) {
            String ns = nsIterator.next();
            Collection<String> localNames = namesBySpace.get(ns);
            Array array = Jdbc.connection().createArrayOf("text", localNames.toArray());
            for (int i = 1; i < 6; i += 2) {
                classesOrMetatypes.setArray(i, array);
                classesOrMetatypes.setString(i + 1, ns);
            }

            ResultSet rs = classesOrMetatypes.executeQuery();
            while (rs.next()) {
                final String localName = rs.getString(1);
                final int index = rs.getInt(2);
                final int post = rs.getInt(3);
                final RdfType type;
                switch (rs.getInt(4)) {
                    case 1: type = RdfType.CLASS; break;
                    case 2: type = RdfType.METACLASS; break;
                    case 3: type = RdfType.METAPROPERTY; break;
                    default: throw new AssertionError();
                }
                storedResources.put(new TypedId(type, post), new StoredResource(
                        new Uri(ns, localName),
                        new Interval(index, post),type));

                //don't search this localName in properties, we already found it here
                //caution: if localNames becomes empty, it will remove its entry,
                //thus would interfere with the keySet iteration
                if (localNames.size() == 1 && localNames.contains(localName)) {
                    nsIterator.remove();
                    break; //done with this namespace
                }
                localNames.remove(localName);
            }
            rs.close();
        }

        //Step 2: Load matching properties
        Map<Integer, StoredProperty> storedProperties = Maps.newHashMap();

        //Input needed: [localNames, ns]
        PreparedStatement properties = Jdbc.prepared(
                //localName, index, post, domainId, domainKind, rangeId, rangeKind
                "SELECT DISTINCT p.att2, sp.att2, p.att0, p.att3, p.att5, p.att4, p.att6 " +
                "FROM t" + DbConstants.getIdFor(Rdf.PROPERTY) + " p, NAMESPACE ns, SUBPROPERTY sp " +
                "WHERE p.att2 = ANY(?) AND p.att1 = ns.att0 AND ns.att1 = ? AND p.att0 = sp.att0");

        //storedResources are only given as an optimization
        KindsAnalyzer kindsAnalyzer = new KindsAnalyzer(storedResources);

        for (String ns : namesBySpace.keySet()) {
            Collection<String> localNames = namesBySpace.get(ns);
            Array array = Jdbc.connection().createArrayOf("text", localNames.toArray());
            properties.setArray(1, array);
            properties.setString(2, ns);

            ResultSet rs = properties.executeQuery();
            while (rs.next()) {
                final String localName = rs.getString(1);
                final int index = rs.getInt(2);
                final int post = rs.getInt(3);

                final int domainId = rs.getInt(4);
                final int domainKind = rs.getInt(5);

                final int rangeId = rs.getInt(6);
                final int rangeKind = rs.getInt(7);

                storedProperties.put(post, new StoredProperty(
                        new Uri(ns, localName),
                        new Interval(index, post),
                        domainId, domainKind, rangeId, rangeKind));

                kindsAnalyzer.recordResourceIdAndKind(domainId, domainKind);
                kindsAnalyzer.recordResourceIdAndKind(rangeId, rangeKind);
            }
            rs.close();
        }

        //Step 3: Load domains and ranges of loaded properties

        if (!kindsAnalyzer.getClassIds().isEmpty()) {
            //load classes which were domain or range of some property
            //Input needed: [id]
            PreparedStatement classesById = Jdbc.prepared(
                "SELECT DISTINCT ns.att1, c.att2, sc.att2, c.att0 " + //ns, localName, index, post
                "FROM t" + DbConstants.getIdFor(RdfSchema.CLASS) + " c, NAMESPACE ns, SUBCLASS sc " +
                "WHERE c.att1 = ns.att0 AND c.att0 = ANY(?) AND sc.att0 = c.att0",
                Jdbc.connection().createArrayOf("int", kindsAnalyzer.getClassIds().toArray()));

            ResultSet rs = classesById.executeQuery();
            while (rs.next()) {
                final String ns = rs.getString(1);
                final String localName = rs.getString(2);
                final int index = rs.getInt(3);
                final int post = rs.getInt(4);
                storedResources.put(new TypedId(RdfType.CLASS, post),
                        new StoredResource(new Uri(ns, localName), new Interval(index, post), RdfType.CLASS));
            }
            rs.close();
        }
        if (!kindsAnalyzer.getMetatypeIds().isEmpty()) {
            //load metaclasses/metaproperties which were domain or range of some property
            //Input needed: [id]
            PreparedStatement metaclassesById = Jdbc.prepared(
                "SELECT DISTINCT ns.att1, c.att2, sc.att2, c.att0, 1 " + //ns, localName, index, post
                "FROM METACLASS c, NAMESPACE ns, SUBMETACLASS sc " +
                "WHERE c.att1 = ns.att0 AND c.att0 = ANY(?) AND sc.att0 = c.att0 " +
                "UNION ALL " +
                "SELECT DISTINCT ns.att1, c.att2, sc.att2, c.att0, 2 " + //ns, localName, index, post
                "FROM METACLASS c, NAMESPACE ns, SUBMETAPROPERTY sc " +
                "WHERE c.att1 = ns.att0 AND c.att0 = ANY(?) AND sc.att0 = c.att0 ");
            Array ids = Jdbc.connection().createArrayOf("int", kindsAnalyzer.getMetatypeIds().toArray());
            metaclassesById.setArray(1, ids);
            metaclassesById.setArray(2, ids);

            ResultSet rs = metaclassesById.executeQuery();
            while (rs.next()) {
                final String ns = rs.getString(1);
                final String localName = rs.getString(2);
                final int index = rs.getInt(3);
                final int post = rs.getInt(4);
                final RdfType type;
                switch (rs.getInt(5)) {
                    case 1: type = RdfType.METACLASS; break;
                    case 2: type = RdfType.METAPROPERTY; break;
                    default: throw new AssertionError();
                }
                storedResources.put(new TypedId(type, post), new StoredResource(new Uri(ns, localName), new Interval(index, post), type));
            }
            rs.close();
        }

        return new DbSynchronizer(model, storedResources, storedProperties, kindsAnalyzer.getVirtualIds());
    }

    private static class KindsAnalyzer {
        private final Set<Integer> classes = Sets.newHashSet();
        private final Set<Integer> metatypes = Sets.newHashSet();

        private final Map<Integer, Uri> virtualResources = Maps.newHashMap();

        private final Map<TypedId, StoredResource> storedResources;

        KindsAnalyzer(Map<TypedId, StoredResource> storedResources) {
            this.storedResources = storedResources;
        }

        private static final Map<Integer, Uri> literalIds = Maps.newHashMap(); static {
            for (Entry<Uri, Integer> entry : DbConstants.getRqlIds().entrySet()) {
                Uri uri = entry.getKey();
                if (uri.hasEqualNamespace(XmlSchema.NAMESPACE)) {
                    literalIds.put(entry.getValue(), uri);
                }
            }
        }

        void recordResourceIdAndKind(int id, int kind) {
            RdfType type = TypedId.fromRqlKind(kind);
            if (findLoadedResource(type, id)) {
                //this is an optimization, so not to load again resources that have already been loaded.
                return;
            }
            switch (type) {
                case CLASS:
                    classes.add(id);
                    break;
                case METACLASS:
                    metatypes.add(id);
                    break;
                case LITERAL:
                    Uri literalType = literalIds.get(id);
                    virtualResources.put(id, literalType);
                    break;
                default:
                    throw new AssertionError("Unknown kind: " + kind + ", which is associated with post id: " + id);
            }
        }

        private boolean findLoadedResource(RdfType type, int id) {
            return storedResources.containsKey(new TypedId(type, id));
        }

        Set<Integer> getClassIds() {
            return Collections.unmodifiableSet(classes);
        }

        Set<Integer> getMetatypeIds() {
            return Collections.unmodifiableSet(metatypes);
        }

        Map<Integer, Uri> getVirtualIds() {
            return Collections.unmodifiableMap(virtualResources);
        }
    }

    private static class StoredResource {
        final Uri uri;
        final Interval interval;
        final RdfType type;

        StoredResource(Uri uri, Interval interval, RdfType type) {
            this.uri = uri;
            this.interval = interval;
            this.type = type;
        }

        @Override
        public String toString() {
            return "[" + uri + "=" + type + " with " + interval + "]";
        }
    }

    private static class StoredProperty extends StoredResource {
        final TypedId domain;
        final TypedId range;

        StoredProperty(Uri uri, Interval interval, int domainId, int domainKind, int rangeId, int rangeKind) {
            super(uri, interval, RdfType.PROPERTY);
            this.domain = new TypedId(TypedId.fromRqlKind(domainKind), domainId);
            this.range = new TypedId(TypedId.fromRqlKind(rangeKind), rangeId);
        }

    }

    private static class TypedId {
        private final int postId;
        private final RdfType type;

        TypedId(RdfType type, int postId) {
            this.type = type;
            this.postId = postId;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypedId)) {
                return false;
            }
            final TypedId other = (TypedId) obj;
            return (this.postId == other.postId && normalized(this.type) == normalized(other.type));
        }

        private static RdfType normalized(RdfType type) {
            if (type == RdfType.METAPROPERTY) {
                return RdfType.METACLASS;
            }
            return type;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.postId;
            hash = 37 * hash + normalized(type).hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "[" + type + ", " + postId + "]";
        }

        private static final int CLASS_KIND = DbConstants.getRqlKindFor(RdfSchema.CLASS);
        private static final int METATYPE_KIND = DbConstants.getRqlKindFor(RdfSuite.METACLASS);
        private static final int LITERAL_KIND = DbConstants.getRqlKindFor(RdfSchema.LITERAL);

        static RdfType fromRqlKind(int rqlKind) {
            if (rqlKind == CLASS_KIND) {
                return RdfType.CLASS;
            } else if (rqlKind == METATYPE_KIND) {
                return RdfType.METACLASS;
            } else if (rqlKind == LITERAL_KIND) {
                return RdfType.LITERAL;
            } else {
                throw new AssertionError("Unexpected kind: " + rqlKind);
            }
        }
    }

    /**
     * Associates hierarchies of the various schema types with specific fields in the table MAX_POSTORDER
     */
    private static final Map<RdfType, String> fieldOfMaxPostOrderByType; static {
                Map<RdfType, String> map = new EnumMap<RdfType, String>(RdfType.class);
                map.put(RdfType.METACLASS, "att0");
                map.put(RdfType.METAPROPERTY, "att1");
                map.put(RdfType.CLASS, "att2");
                map.put(RdfType.PROPERTY, "att3");
                fieldOfMaxPostOrderByType = Collections.unmodifiableMap(map);
            }

    class KnownDbState {
        private final PredefinedLabels existingLabels;

        private KnownDbState(PredefinedLabels existingLabels) {
            this.existingLabels = PredefinedLabels.swkmPredefinedLabels().merge(existingLabels);
        }

        UpdatedLabels recalculateLabels() throws SQLException {
            //Note: we do not lock MAX_POSTORDER just yet. Because this import
            //might not contain any schema at all, and we wouldn't want to block all other
            //imports (of schema data) for no reason.

            //We will issue the lock in DbHierarchy#getIndexForNewHierarchy() call.
            //So this might end up sending 4 requests to lock the table (which is no problem)
            //but it may also be called zero times, if no hierarchy is to be stored.

            //Note that I don't load all 4 DbHierarchies in memory at once, so there is less probability to get OOMEs.
            //Also note that the order that hierarchies are listed *matters*, since I use this order
            //to update MAX_POSTORDER table.
            Collection<Callable<DbHierarchy>> hierarchyFactories = Lists.newArrayListWithExpectedSize(4);
            hierarchyFactories.add(new Callable<DbHierarchy>() { public DbHierarchy call() {
                return DbHierarchy.newMetaclassHierarchy(model, existingLabels);
            } });
            hierarchyFactories.add(new Callable<DbHierarchy>() { public DbHierarchy call() {
                return DbHierarchy.newMetapropertyHierarchy(model, existingLabels);
            } });
            hierarchyFactories.add(new Callable<DbHierarchy>() { public DbHierarchy call() {
                return DbHierarchy.newClassHierarchy(model, existingLabels);
            } });
            hierarchyFactories.add(new Callable<DbHierarchy>() { public DbHierarchy call() {
                return DbHierarchy.newPropertyHierarchy(model, existingLabels);
            } });

            Map<RdfType, Map<Uri, Label>> resourcesToLabelsByType = Maps.newHashMapWithExpectedSize(128);
            Map<RdfType, Map<Interval, Label>> unknownResourcesToLabels = Maps.newHashMap();
            final Map<RdfType, Map<Integer, Integer>> kidsToParents = Maps.newHashMap();
            Map<Map<Integer, Integer>, Triple> tripleToRelationships = Maps.newHashMap();

            Labeler labeler = Labelers.newDefault();
            for (Callable<DbHierarchy> hierarchyFactory : hierarchyFactories) {
                try {
                    final DbHierarchy hierarchy = hierarchyFactory.call();
                    if (hierarchy == null) {
                        continue;
                    }
                    final Map<Node, Node> kidsToParentsNodes = Maps.newHashMap();
                    labeler.assignLabels(hierarchy, new Labeler.TreeBuilder() {
                        public void treeEdge(Node kid, Node parent) {
                            if (!kidsToParentsNodes.containsKey(kid)) {
                                kidsToParentsNodes.put(kid, parent); //storing the nodes because
                            //post id is not yet available
                            }
                            //System.out.println("DbSynchronizer: treeEdge - kid: " + kid.toString() + " parent: " + parent.toString());
                        }
                    });

                    Map<Integer, Integer> mapPerType = Maps.newHashMap();
                    kidsToParents.put(hierarchy.getType(), mapPerType);
                    for (Entry<Node, Node> entry : kidsToParentsNodes.entrySet()) {
                        //System.out.println("DbSynchronizer: put in kidsToParents - kid: " + hierarchy.getLabelOf(entry.getKey()).getTreeLabel().getPost() + " parent: " + hierarchy.getLabelOf(entry.getValue()).getTreeLabel().getPost());
                        mapPerType.put(
                            hierarchy.getLabelOf(entry.getKey()).getTreeLabel().getPost(),
                            hierarchy.getLabelOf(entry.getValue()).getTreeLabel().getPost());
                        Triple t = (Triple)hierarchy.exploredGraph().anEdge(entry.getKey(), entry.getValue()).getValue();
                        //System.out.println("DbSynchronizer: treeEdge - triple: " + t);
                        Map<Integer, Integer> kidParent = Maps.newHashMap();
                        kidParent.put(
                            hierarchy.getLabelOf(entry.getKey()).getTreeLabel().getPost(),
                            hierarchy.getLabelOf(entry.getValue()).getTreeLabel().getPost());
                        tripleToRelationships.put(kidParent, t);
                    }
                    resourcesToLabelsByType.put(hierarchy.getType(), Collections.unmodifiableMap(
                            hierarchy.findUpdatedResourcesWithKnownUri()));

                    unknownResourcesToLabels.put(hierarchy.getType(), Collections.unmodifiableMap(
                            hierarchy.findUpdatedResourcesWithUnknownUri()));

                    hierarchy.recalculateIndexForNewHierarchy();
                    Jdbc.execute("UPDATE MAX_POSTORDER SET %s=%d",
                            fieldOfMaxPostOrderByType.get(hierarchy.getType()), //field
                            hierarchy.getIndexForNewHierarchy()); //next max post id
                } catch (RuntimeException e) {
                    throw e;
                } catch (SQLException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return new UpdatedLabels(
                    existingLabels,
                    resourcesToLabelsByType,
                    unknownResourcesToLabels,
                    kidsToParents,
                    tripleToRelationships);
        }
    }

    static class UpdatedLabels {
        /**
         * Labels already stored in database (possibly to be updated).
         */
        private final PredefinedLabels existingLabels;

        /**
         * Updated labels by URI, for nodes where their URI was known.
         */
        private final Map<RdfType, Map<Uri, Label>> urisToLabelsByType;

        /**
         * Updated labels by type and interval, when we didn't know the URI of the node.
         */
        private final Map<RdfType, Map<Interval, Label>> intervalsToLabels;

        /**
         * Defines the parent of a node in the tree (not in the dag), by type and post id.
         */
        private final Map<RdfType, Map<Integer, Integer>> kidsToParents;

        /**
         * Defines the parent of a node in the tree (not in the dag), by type and post id.
         */
        private final Map<Map<Integer, Integer>, Triple> tripleToRelationships;
        private UpdatedLabels(
                PredefinedLabels existingLabels,
                Map<RdfType, Map<Uri, Label>> urisToLabelsByType,
                Map<RdfType, Map<Interval, Label>> intervalsToLabels,
                Map<RdfType, Map<Integer, Integer>> kidsToParents,
                Map<Map<Integer, Integer>, Triple> tripleToRelationships) {
            this.existingLabels = existingLabels;
            this.urisToLabelsByType = urisToLabelsByType;
            this.intervalsToLabels = intervalsToLabels;
            this.kidsToParents = kidsToParents;
            this.tripleToRelationships = tripleToRelationships;
        }

        //returns the updated labels for resources that we don't know their URI
        Map<Interval, Label> getUpdatedLabelsByInterval(RdfType type) {
            Map<Interval, Label> result = intervalsToLabels.get(type);
            if (result == null) return Collections.emptyMap();
            return result;
        }

        PredefinedLabels getOldLabels() {
            return existingLabels;
        }

        Map<Uri, Label> getNewLabels(RdfType type) {
            Map<Uri, Label> newLabels = urisToLabelsByType.get(type);
            if (newLabels == null) {
                return Collections.emptyMap();
            }
            return newLabels;
        }

        /**
         * Returns the new or old (if not updated) label of a URI.
         */
        Label getLabel(RdfType type, Uri uri) {
            Label label = null;
            Map<Uri, Label> map = urisToLabelsByType.get(type);
            if (map != null) {
                label = map.get(uri);
            }
            if (label == null) {
                return existingLabels.predefinedLabelOf(uri);
            }
            return label;
        }

        Map<Integer, Integer> getNewKidsToParents(RdfType type) {
            return kidsToParents.get(type);
        }
        Triple getRelationshipTriple(Map<Integer, Integer> map) {
            return tripleToRelationships.get(map);
        }
    }
}
