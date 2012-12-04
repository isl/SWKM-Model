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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.Inference;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Uri.Delimiter;
import gr.forth.ics.swkm.model2.importer.DbSynchronizer.KnownDbState;
import gr.forth.ics.swkm.model2.importer.DbSynchronizer.UpdatedLabels;
import gr.forth.ics.swkm.model2.labels.Interval;
import gr.forth.ics.swkm.model2.labels.Label;
import gr.forth.ics.swkm.model2.validation.Validator;
import gr.forth.ics.swkm.model2.vocabulary.Owl;
import gr.forth.ics.swkm.model2.util.NamespaceDependenciesIndex;
import gr.forth.ics.swkm.model2.Triples;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import javax.sql.DataSource;
import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.event.TripleListener;
import java.util.EnumSet;
import java.util.Map.Entry;
import org.springframework.util.Assert;

/**
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
abstract class AbstractStore implements RdfStore {

    protected final Jdbc jdbc;
    protected final Tables commonTables;
    protected final Configurer conf;
    private final RdfStores.Representation representation;
    private final int DEFAULT_GRAPHSET_ID = 200000;
    public AbstractStore(RdfStores.Representation representation,
            DataSource dataSource, Configurer conf) {
        Assert.notNull(representation, "Representation");
        Assert.notNull(dataSource, "DataSource");
        Assert.notNull(conf, "Configurer");
        this.representation = representation;
        this.jdbc = new Jdbc(dataSource);
        this.commonTables = new Tables(conf);
        this.conf = conf;
    }

    boolean isSchemaInitialized() {
        try {
            Jdbc.execute("SELECT * from %s", commonTables.representation.getName());
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public final void initializeSchemaIfNeeded() throws SQLException {
        jdbc.doInConnection(new ConnectionTask<Void>() {

            @Override
            public Void execute() throws SQLException {
                Connection connection = Jdbc.connection();
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(true);
                if (isSchemaInitialized()) {
                    return null;
                }
                connection.setAutoCommit(false);
                initialize();
                connection.commit();
                return null;
            }
        });
    }

    protected String createUpdate(Map<Interval, Interval> oldIntervalToNew, String tabName, String wattName, String... uattNames) {
        StringBuilder sb = new StringBuilder();
        for (Entry<Interval, Interval> e : oldIntervalToNew.entrySet()) {
            Interval oldInterval = e.getKey();
            Interval newInterval = e.getValue();
            sb.append("update ");
            sb.append(tabName);
            sb.append(" set ");
            boolean ufirst = true;
            for (String attName : uattNames) {
                if (!ufirst) {
                    sb.append(",");
                } else {
                    ufirst = false;
                }
                sb.append(attName);
                sb.append("=");
                sb.append(newInterval.getPost());
            }
            sb.append(" where ");
            sb.append(wattName);
            sb.append("=");
            sb.append(oldInterval.getPost());
            sb.append(";");
        }
        return sb.toString();
    }

    private void updateClassPropOrMetaclassTable(Map<Interval, Interval> oldIntervalToNew, RdfType type) throws SQLException {
        if (type == RdfType.CLASS) {
            StringBuilder t2 = new StringBuilder();
            StringBuilder t35 = new StringBuilder();

            String t1 = createUpdate(oldIntervalToNew, "t1000000000", "att0", "att0");
            t2.append(createUpdate(oldIntervalToNew, "t2000000000", "att3", "att3"));
            t2.append(createUpdate(oldIntervalToNew, "t2000000000", "att4", "att4"));
            t35.append(createUpdate(oldIntervalToNew, "t35", "att3", "att3"));
            t35.append(createUpdate(oldIntervalToNew, "t35", "att4", "att4"));

            jdbc.execute(t1);
            jdbc.execute(t2.toString());
            jdbc.execute(t35.toString());
        //            String query = "update t1000000000 set att0=" + newInterval.getPost() + " where att0=" + oldInterval.getPost();
//            String t35Att3Query = "update t35 set att3=" + newInterval.getPost() + " where att3=" + oldInterval.getPost();
//            String t35Att4Query = "update t35 set att4=" + newInterval.getPost() + " where att4=" + oldInterval.getPost();
//            String t2000000000Att3Query = "update t2000000000 set att3=" + newInterval.getPost() + " where att3=" + oldInterval.getPost();
//            String t2000000000Att4Query = "update t2000000000 set att4=" + newInterval.getPost() + " where att4=" + oldInterval.getPost();
        }
        if (type == RdfType.PROPERTY) {
            String upd = createUpdate(oldIntervalToNew, "t2000000000", "att0", "att0");
            jdbc.execute(upd);
//            String query = "update t2000000000 set att0=" + newInterval.getPost() + " where att0=" + oldInterval.getPost();
        }
        if ((type == RdfType.METACLASS) || (type == RdfType.METAPROPERTY)) {
            String upd = createUpdate(oldIntervalToNew, "metaclass", "att0", "att0");
            jdbc.execute(upd);
//            String query = "update metaclass set att0=" + newInterval.getPost() + " where att0=" + oldInterval.getPost();
        }
    }

    protected abstract void updateInstanceTable(Map<Interval, Interval> oldIntervalToNew, RdfType type) throws SQLException;

    private void updateSubclassTable(Map<Interval, Interval> oldIntervalToNew, RdfType type) {
        String spanTreeTable = null;
        String nonSpanTreeTable = null;
        if (type == RdfType.CLASS) {
            spanTreeTable = "subclass";
            nonSpanTreeTable = "class_anc";
        } else if (type == RdfType.PROPERTY) {
            spanTreeTable = "subproperty";
            nonSpanTreeTable = "property_anc";
        } else if (type == RdfType.METACLASS) {
            spanTreeTable = "submetaclass";
            nonSpanTreeTable = "metaclass_anc";
        } else {
            spanTreeTable = "submetaproperty";
            nonSpanTreeTable = "metaproperty_anc";
        }

        String q1 = createUpdate(oldIntervalToNew, spanTreeTable, "att0", "att0", "att2");
        String q2 = createUpdate(oldIntervalToNew, spanTreeTable, "att1", "att1");
//        String query = "update " + spanTreeTable + " set att0=" + newInterval.getPost() + ", att2=" + newInterval.getIndex() + " where att0=" + oldInterval.getPost();
//        String query2 = "update " + spanTreeTable + " set att1=" + newInterval.getPost() + " where att1=" + oldInterval.getPost();

//        storageParameters.executeUpdate(query);
//        storageParameters.executeUpdate(query2);


        /*** In case of DAGs I have to update the class_anc table***/
//        String query3 = "update " + nonSpanTreeTable + " set att0=" + newInterval.getPost() + ", att2=" + newInterval.getIndex() + " where att0=" + oldInterval.getPost();
//        storageParameters.executeUpdate(query3);
//        stmt.execute(query3);
//        String query4 = "update " + nonSpanTreeTable + " set att1=" + newInterval.getPost() + " where att1=" + oldInterval.getPost();
//        storageParameters.executeUpdate(query4);
//        stmt.execute(query4);
        String q3 = createUpdate(oldIntervalToNew, nonSpanTreeTable, "att0", "att0", "att2");
        String q4 = createUpdate(oldIntervalToNew, nonSpanTreeTable, "att1", "att1");
    }

    class Tables {

        protected final Namespace namespace;
        protected final Graphspace graphspace;
        protected final Graphset graphset;
        protected final NamespaceDependencies namespaceDependencies;
        protected final GraphspaceDependencies graphspaceDependencies;
        protected final Metaclass metaclass;
        protected final SubMetaclass subMetaclass;
        protected final SubMetaproperty subMetaproperty;
        protected final MetaclassAncestry metaclassAncestry;
        protected final MetapropertyAncestry metapropertyAncestry;
        //protected final Graph graph;
        protected final GraphTriples graphTriples;
        protected final LiteralType literalType;
        protected final Thesaurus thesaurus;
        protected final Enumeration enumeration;
        protected final SubClass subClass;
        protected final SubProperty subProperty;
        protected final ClassAncestry classAncestry;
        protected final PropertyAncestry propertyAncestry;
        protected final Generator generator;
        protected final SequenceTable namespaceIdGenerator;
        protected final SequenceTable graphspaceIdGenerator;
        protected final SequenceTable graphsetIdGenerator;
        protected final SequenceTable tripleIdGenerator;
        protected final SequenceTable anonIdGenerator;
        protected final Representation representation;
        protected final MaxPostOrder maxPostOrder;
        protected final SequenceTypes sequenceTypes;
        protected final BagTypes bagTypes;
        protected final AnonIds anonIds;
        protected final ClassComment classComment;
        protected final ClassSeeAlso classSeeAlso;
        protected final ClassLabel classLabel;
        protected final ClassIsDefinedBy classIsDefinedBy;
        protected final PropertyComment propertyComment;
        protected final PropertySeeAlso propertySeeAlso;
        protected final PropertyLabel propertyLabel;
        protected final PropertyIsDefinedBy propertyIsDefinedBy;
        protected final ResourceComment resourceComment;
        protected final ResourceSeeAlso resourceSeeAlso;
        protected final ResourceLabel resourceLabel;
        protected final ResourceIsDefinedBy resourceIsDefinedBy;

        protected Tables(Configurer conf) {
            namespace = new Namespace(conf);
            graphspace = new Graphspace(conf);
            graphset = new Graphset(conf);
            namespaceDependencies = new NamespaceDependencies(conf);
            graphspaceDependencies = new GraphspaceDependencies(conf);
            metaclass = new Metaclass(conf);
            subMetaclass = new SubMetaclass(conf);
            subMetaproperty = new SubMetaproperty(conf);
            metaclassAncestry = new MetaclassAncestry(conf);
            metapropertyAncestry = new MetapropertyAncestry(conf);
            //graph = new Graph(conf);
            graphTriples = new GraphTriples(conf);
            literalType = new LiteralType(conf);
            thesaurus = new Thesaurus(conf);
            enumeration = new Enumeration(conf);
            subClass = new SubClass(conf);
            subProperty = new SubProperty(conf);
            classAncestry = new ClassAncestry(conf);
            propertyAncestry = new PropertyAncestry(conf);
            generator = new Generator(conf);
            namespaceIdGenerator = new SequenceTable("nsid_generator", 6);
            graphspaceIdGenerator = new SequenceTable("gsid_generator", 2);
            graphsetIdGenerator = new SequenceTable("graphsetid_generator", 200001);
            tripleIdGenerator = new SequenceTable("tripleid_generator", 1);
            anonIdGenerator = new SequenceTable("anonid_generator", 1);
            representation = new Representation(conf);
            maxPostOrder = new MaxPostOrder(conf);
            sequenceTypes = new SequenceTypes(conf);
            bagTypes = new BagTypes(conf);
            anonIds = new AnonIds(conf);

            classComment = new ClassComment(conf);
            classSeeAlso = new ClassSeeAlso(conf);
            classLabel = new ClassLabel(conf);
            classIsDefinedBy = new ClassIsDefinedBy(conf);

            propertyComment = new PropertyComment(conf);
            propertySeeAlso = new PropertySeeAlso(conf);
            propertyLabel = new PropertyLabel(conf);
            propertyIsDefinedBy = new PropertyIsDefinedBy(conf);

            resourceComment = new ResourceComment(conf);
            resourceSeeAlso = new ResourceSeeAlso(conf);
            resourceLabel = new ResourceLabel(conf);
            resourceIsDefinedBy = new ResourceIsDefinedBy(conf);
        }

        private void createTablePerClassField() throws SQLException {
            Table.createAllTablesDeclaredAsFields(this);
        }

        //creates all common commonTables
        private void createCommonTables(RdfStores.Representation repr) throws SQLException {
            createTablePerClassField();

            graphspace.insert(graphspace.id, graphspace.uri).values(1, RdfSuite.DEFAULT_GRAPH_URI.toString(Delimiter.WITH));
            Jdbc.execute("INSERT INTO %s (%s, %s) VALUES (%d, ARRAY[1])", graphset.getName(), graphset.id.getName(), graphset.set.getName(), DEFAULT_GRAPHSET_ID);

            namespace.insert(namespace.id, namespace.uri).values(1, Rdf.NAMESPACE.toString(Uri.Delimiter.WITH)).insert(namespace.id, namespace.uri).values(2, RdfSchema.NAMESPACE.toString(Uri.Delimiter.WITH)).insert(namespace.id, namespace.uri).values(3, XmlSchema.NAMESPACE.toString(Uri.Delimiter.WITH)).insert(namespace.id, namespace.uri).values(4, RdfSuite.NAMESPACE.toString(Uri.Delimiter.WITH)).insert(namespace.id, namespace.uri).values(5, Owl.NAMESPACE.toString(Uri.Delimiter.WITH));

            metaclass.insertRow(RdfSchema.CLASS, 2).insertRow(RdfSuite.DATA_PROPERTY, 1).insertRow(Rdf.PROPERTY, 1).insertRow(RdfSchema.LITERAL, 4).insertRow(RdfSuite.THESAURUS, 4).insertRow(RdfSuite.ENUMERATION, 4);

            subMetaclass.insert(subMetaclass.subClassId, subMetaclass.postOrder).values(DbConstants.getIdFor(RdfSchema.CLASS),
                    DbConstants.getPostOrderFor(RdfSchema.CLASS));

            subMetaproperty.insert(subMetaproperty.subPropertyId, subMetaproperty.postOrder).values(DbConstants.getIdFor(Rdf.PROPERTY),
                    DbConstants.getPostOrderFor(Rdf.PROPERTY));

            generator.insert(generator.namespaceCounter, generator.anonymousCounter, generator.thesaurusCounter).values(4, 0, DbConstants.MAX_INT);

            subClass.insert(subClass.subId, subClass.index).values(DbConstants.getIdFor(RdfSchema.RESOURCE), DbConstants.getPostOrderFor(RdfSchema.RESOURCE));

            representation.insert(representation.type).values(repr.getCode());

            maxPostOrder.insert(maxPostOrder.metaclassPostOrder, maxPostOrder.metapropertyPostOrder,
                    maxPostOrder.classPostOrder, maxPostOrder.propertyPostOrder).values(DbConstants.INIT_POST_ORDER, DbConstants.MAX_INT / 2 + 2,
                    DbConstants.INIT_POST_ORDER, DbConstants.INIT_POST_ORDER);


            commonTables.literalType.insert(
                    commonTables.literalType.id,
                    commonTables.literalType.namespaceId,
                    commonTables.literalType.localPart,
                    commonTables.literalType.kind).values(DbConstants.getIdFor(RdfSuite.GRAPH), 2, "Literal", DbConstants.getIdFor(RdfSuite.GRAPH));

            insertXmlTypes();
        }

        private void insertXmlTypes() throws SQLException {
            for (Uri uri : XmlSchema.getAllTypes()) {
                literalType.insertXmlType(uri);
            }
        }

        private void createCommonIndexes() throws SQLException {
            namespace.clusteredIndexOn("ns", namespace.id);

            namespaceDependencies.indexOn("ns_deps_idx", namespaceDependencies.att0, namespaceDependencies.att1);

            //graph.
            //        clusteredIndexOn("g1", graph.id);

            subClass.clusteredIndexOn("cls_sub_idx", subClass.subId).
                    indexOn("cls_super_idx", subClass.superId).
                    indexOn("cls_index_idx", subClass.index);

            subProperty.clusteredIndexOn("p_sub_idx", subProperty.subId).
                    indexOn("p_super_idx", subProperty.superId).
                    indexOn("p_index_idx", subProperty.index);

            subMetaclass.indexOn("sm_att0", subMetaclass.subClassId).
                    indexOn("sm_att1", subMetaclass.superClassId).
                    indexOn("sm_att2", subMetaclass.postOrder);

            subMetaproperty.indexOn("smp_att0", subMetaproperty.subPropertyId).
                    indexOn("smp_att1", subMetaproperty.superPropertyId).
                    indexOn("smp_att2", subMetaproperty.postOrder);

            classAncestry.clusteredIndexOn("ca_att0", classAncestry.classId).
                    indexOn("ca_att1", classAncestry.namespaceId).
                    indexOn("ca_att2", classAncestry.ancestorClassId);

            propertyAncestry.clusteredIndexOn("pa_att0", propertyAncestry.propertyId).
                    indexOn("pa_att1", propertyAncestry.namespaceId).
                    indexOn("pa_att2", propertyAncestry.ancestorPropertyId);

            metaclassAncestry.indexOn("ma_att0", metaclassAncestry.childPostOrder).
                    indexOn("ma_att1", metaclassAncestry.parentPostOrder).
                    indexOn("ma_att2", metaclassAncestry.childIndex);

            metapropertyAncestry.indexOn("mpa_att0", metapropertyAncestry.childPostOrder).
                    indexOn("mpa_att1", metapropertyAncestry.parentPostOrder).
                    indexOn("mpa_att2", metapropertyAncestry.childIndex);

            sequenceTypes.indexOn("seq1_idx", sequenceTypes.att0).
                    indexOn("seq2_idx", sequenceTypes.att1).
                    indexOn("seq3_idx", sequenceTypes.att2).
                    indexOn("seq4_idx", sequenceTypes.att3);

            bagTypes.indexOn("bag1_idx", bagTypes.att0).
                    indexOn("bag2_idx", bagTypes.att1).
                    indexOn("bag3_idx", bagTypes.att2);

            enumeration.indexOn("emum_id_idx", enumeration.att0);
        }
    }

    private void initialize() throws SQLException {
        commonTables.createCommonTables(representation);
        commonTables.createCommonIndexes();
        initializeRepresentation();
    }

    abstract void initializeRepresentation() throws SQLException;

    static class Namespace extends Table {

        public final Attribute id;
        public final Attribute uri;

        private Namespace(Configurer conf) {
            super("NameSpace");
            id = newAttribute("att0", "INTEGER PRIMARY KEY");
            uri = newAttribute("att1", "VARCHAR(%s) NOT NULL UNIQUE", conf.getMaxPrefixLength());
        }
        private static final Function<Uri, String> toStringWithHash = new Function<Uri, String>() {

            public String apply(Uri uri) {
                return uri.toString(Uri.Delimiter.WITH);
            }
        };

        private Map<Uri, Integer> createNewNamespaceIds(Set<Uri> namespaces) throws SQLException {
            namespaces = Sets.newHashSet(namespaces);
            ResultSet rs = Jdbc.prepared("SELECT att1" +
                    " FROM " + getName() +
                    " WHERE att1 = ANY(?)",
                    Jdbc.connection().createArrayOf("text",
                    Iterables.toArray(Iterables.transform(namespaces, toStringWithHash), String.class))).executeQuery();
            try {
                while (rs.next()) {
                    final Uri uri = Uri.parse(rs.getString(1));
                    namespaces.remove(uri);
                }
            } finally {
                rs.close();
            }
            //at this point, namespaces set contain only brand new namespaces

            InsertHelper insertHelper = new InsertHelper(
                    "INSERT INTO NAMESPACE VALUES(nextval('nsid_generator'), ?)", "nsid_generator");

            Map<Uri, Integer> map = Maps.newHashMap();
            for (Uri ns : namespaces) {
                int id = insertHelper.executeAndGetId(ns.toString(Uri.Delimiter.WITH));
                map.put(ns, id);
            }
            return map;
        }
    }

    static class NamespaceDependencies extends Table {

        public final Attribute att0; //TODO: clarify attribute
        public final Attribute att1; //TODO: clarify attribute

        public NamespaceDependencies(Configurer conf) {
            super("Namespace_Dependencies");
            att0 = newAttribute("att0", "INTEGER");
            att1 = newAttribute("att1", "INTEGER");
        }
    }

    static class Graphspace extends Table {

        public final Attribute id;
        public final Attribute uri;

        private Graphspace(Configurer conf) {
            super("NamedGraph");
            //super("Graphspace"); // used only to run the old importer test
            id = newAttribute("att0", "INTEGER PRIMARY KEY");
            uri = newAttribute("att1", "VARCHAR(%s) NOT NULL UNIQUE", conf.getMaxPrefixLength());
        }
    }

    static class Graphset extends Table {

        public final Attribute id;
        public final Attribute set;

        private Graphset(Configurer conf) {
            super("Graphset");
            id = newAttribute("att0", "INTEGER PRIMARY KEY");
            set = newAttribute("att1", "INTEGER[]");
        }
    }

    static class GraphspaceDependencies extends Table {

        public final Attribute att0; //TODO: clarify attribute
        public final Attribute att1; //TODO: clarify attribute
        public final Attribute att2; //TODO: clarify attribute

        public GraphspaceDependencies(Configurer conf) {
            super("Graphspace_Dependencies");
            att0 = newAttribute("att0", "INTEGER");
            att1 = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class Metaclass extends Table {

        public final Attribute classId; //this is the post order too
        public final Attribute namespaceId;
        public final Attribute localPart;
        public final Attribute kind;
        public final Attribute att4; //TODO: clarify attribute

        private Metaclass(Configurer conf) {
            super("MetaClass");
            classId = newAttribute("att0", "INTEGER PRIMARY KEY");
            namespaceId = newAttribute("att1", "INTEGER NOT NULL");
            localPart = newAttribute("att2", "VARCHAR(%s) NOT NULL", conf.getMaxLocalPartLength());
            kind = newAttribute("att3", "INTEGER NOT NULL");
            att4 = newAttribute("att4", "INTEGER");
        }

        public Metaclass insertRow(Uri uri, int nsId) throws SQLException {
            insert(classId, namespaceId, localPart, kind).
                    values(DbConstants.getIdFor(uri),
                    nsId,
                    DbConstants.getNameFor(uri),
                    DbConstants.getRqlKindFor(uri));
            return this;
        }

        void storeMetaclasses(ImportContext context) throws SQLException {
            storeMetatypes(context, RdfType.METACLASS);
        }

        void storeMetaproperties(ImportContext context) throws SQLException {
            storeMetatypes(context, RdfType.METAPROPERTY);
        }

        private void storeMetatypes(ImportContext context, RdfType metatype) throws SQLException {
            //this is similar to the code that stores classes, in HybridStore, but no time to consolidate it
            InsertHelper insertHelper = new InsertHelper(
                    "INSERT INTO " + getName() + " VALUES (?, ?, ?, ?, nextval('tripleid_generator'))",
                    "tripleid_generator");
            Map<Uri, Label> newLabels = context.updatedLabels.getNewLabels(metatype);
            final Uri target;
            switch (metatype) {
                case METACLASS:
                    target = RdfSchema.CLASS;
                    break;
                case METAPROPERTY:
                    target = Rdf.PROPERTY;
                    break;
                default:
                    throw new AssertionError();
            }

            for (Entry<Uri, Integer> newNamespaceEntry : context.namespaceIds.entrySet()) {
                Uri namespace = newNamespaceEntry.getKey();
                Integer nsId = newNamespaceEntry.getValue();
                for (Resource clazz : context.model.findSchemaNodes(namespace, metatype)) {
                    Uri uri = clazz.getUri();

                    Triples isaTriples = context.model.triples().s(clazz).p(RdfSchema.SUBCLASSOF).o(target).fetch();
                    if (isaTriples.iterator().hasNext() == false) {
                        //we just need a triple object to associate with a triple id.
                        //for metaclasses/metaproperties, I arbitrarily put <X rdf:type rdfs:Class>, but this
                        //should not be observable by anywhere (and will get cleaned up after the import)
                        context.model.add().g(RdfSuite.IMPORTER_SIDE_EFFECTS).s(clazz).p(Rdf.TYPE).o(target);
                        context.model.add().g(RdfSuite.DEFAULT_GRAPH_URI).s(clazz).p(Rdf.TYPE).o(target);
                    }

                    for (Triple t : isaTriples) {
                        //the triple itself is ignored, it just gets associated with a triple id
                        Integer nodeId = newLabels.get(uri).getTreeLabel().getPost();
                        int tripleId = insertHelper.executeAndGetId(nodeId, nsId, uri.getLocalName(),
                                metatype == RdfType.METACLASS ? 2 : 3); //bonus points for another pair of magic numbers!
                        //the last part obviously belongs to DbConstants.
                        //But that's a huge mess, Vassilis designed it, Vassilis should clean it up :-)
                        context.tripleIds.put(tripleId, t);
                    }
                }
            }
        }
    }

    static class SubMetaclass extends IsaTable {

        public final Attribute subClassId;
        public final Attribute superClassId;
        public final Attribute postOrder;
        public final Attribute att3; //TODO: clarify attribute

        private SubMetaclass(Configurer conf) {
            super("SubMetaClass", RdfType.METACLASS);
            subClassId = newAttribute("att0", "INTEGER NOT NULL");
            superClassId = newAttribute("att1", "INTEGER");
            postOrder = newAttribute("att2", "INTEGER NOT NULL");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class SubMetaproperty extends IsaTable {

        public final Attribute subPropertyId;
        public final Attribute superPropertyId;
        public final Attribute postOrder;
        public final Attribute att3; //TODO: clarify field

        private SubMetaproperty(Configurer conf) {
            super("SubMetaProperty", RdfType.METAPROPERTY);
            subPropertyId = newAttribute("att0", "INTEGER NOT NULL");
            superPropertyId = newAttribute("att1", "INTEGER");
            postOrder = newAttribute("att2", "INTEGER NOT NULL");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class MetaclassAncestry extends PropagatedIsaTable {

        public final Attribute childPostOrder;
        public final Attribute parentPostOrder;
        public final Attribute childIndex;
        public final Attribute isDirectChild;
        public final Attribute att4; //TODO: clarify field

        private MetaclassAncestry(Configurer conf) {
            super("Metaclass_Anc", RdfType.METACLASS);
            childPostOrder = newAttribute("att0", "INTEGER NOT NULL");
            parentPostOrder = newAttribute("att1", "INTEGER");
            childIndex = newAttribute("att2", "INTEGER NOT NULL");
            isDirectChild = newAttribute("att3", "boolean NOT NULL");
            att4 = newAttribute("att4", "INTEGER");
        }
    }

    static class MetapropertyAncestry extends PropagatedIsaTable {

        public final Attribute childPostOrder;
        public final Attribute parentPostOrder;
        public final Attribute childIndex;
        public final Attribute isDirectChild;
        public final Attribute att4; //TODO: clarify field

        private MetapropertyAncestry(Configurer conf) {
            super("MetaProperty_Anc", RdfType.METAPROPERTY);
            childPostOrder = newAttribute("att0", "INTEGER NOT NULL");
            parentPostOrder = newAttribute("att1", "INTEGER");
            childIndex = newAttribute("att2", "INTEGER NOT NULL");
            isDirectChild = newAttribute("att3", "boolean NOT NULL");
            att4 = newAttribute("att4", "INTEGER");
        }
    }

    static class Graph extends Table {
    public final Attribute uri;
    public final Attribute id;

    private Graph(Configurer conf) {
    super("Graph");
    uri = newAttribute("att0", "VARCHAR(%s) NOT NULL", conf.getMaxPrefixLength());
    id = newAttribute("att1", "INTEGER NOT NULL");
    }
    }
    static class GraphTriples extends Table {
        //public final Attribute uri;
        public final Attribute graphsetId;
        public final Attribute tripleId;

        private GraphTriples(Configurer conf) {
            super("GraphTriples");
            graphsetId = newAttribute("att0", "INTEGER NOT NULL");
            tripleId = newAttribute("att1", "INTEGER NOT NULL");
        }
    }

    static class LiteralType extends Table {

        public final Attribute id;
        public final Attribute namespaceId;
        public final Attribute localPart;
        public final Attribute kind;

        private LiteralType(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSchema.LITERAL));
            id = newAttribute("att0", "INTEGER PRIMARY KEY");
            namespaceId = newAttribute("att1", "INTEGER NOT NULL");
            localPart = newAttribute("att2", "VARCHAR(%s) NOT NULL", conf.getMaxLocalPartLength());
            kind = newAttribute("att3", "INTEGER UNIQUE");
        }

        public LiteralType insertXmlType(Uri uri) throws SQLException {
            insert(id, namespaceId, localPart, kind).
                    values(DbConstants.getIdFor(uri), 3, uri.getLocalName(), DbConstants.getRqlKindFor(uri));
            return this;
        }
    }

    static class Thesaurus extends Table {

        public final Attribute id;
        public final Attribute att1; //TODO: clarify field
        public final Attribute att2; //TODO: clarify field


        private Thesaurus(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSuite.THESAURUS));
            id = newAttribute("att0", "INTEGER PRIMARY KEY");
            att1 = newAttribute("att1", "INTEGER NOT NULL");
            att2 = newAttribute("att2", "INTEGER NOT NULL");
        }
    }

    static class Enumeration extends Table {

        public final Attribute att0; //TODO: clarify field
        public final Attribute att1; //TODO: clarify field

        private Enumeration(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSuite.ENUMERATION));
            att0 = newAttribute("att0", "INTEGER NOT NULL");
            att1 = newAttribute("att1", "VARCHAR(%s) NOT NULL", conf.getMaxLiteralLength());
        }
    }

    static abstract class IsaTable extends Table {

        private final RdfType type;

        public IsaTable(String name, RdfType type) {
            super(name);
            this.type = type;
        }

        void storeIsaRelationships(ImportContext context) throws SQLException {
            UpdatedLabels labels = context.updatedLabels;
            InsertHelper insertHelper = new InsertHelper(
                    "INSERT INTO " + getName() + " VALUES (?, ?, ?, nextval('tripleid_generator'))",
                    "tripleid_generator");
            System.out.println("storeISaRelationships: RDF TYPE = " + type);
            String findUriTablename = "";
            String triplePredicate = "";
            // I don' t like the following check but...
            if (type.equals(RdfType.CLASS))
            {
                findUriTablename = "t1000000000";
                triplePredicate = RdfSchema.SUBCLASSOF.toString();
            }
            else if (type.equals(RdfType.PROPERTY))
            {
                findUriTablename = "t2000000000";
                triplePredicate = RdfSchema.SUBPROPERTYOF.toString();
            }
            else if (type.equals(RdfType.METACLASS))
            {
                findUriTablename = "metaclass";
                triplePredicate = RdfSchema.SUBCLASSOF.toString();
            }
            else if (type.equals(RdfType.METAPROPERTY))
            {
                findUriTablename = "metaclass";
                triplePredicate = RdfSchema.SUBPROPERTYOF.toString();
            }
            Map<Uri, Label> labelsToStore = labels.getNewLabels(type);
            Map<Integer, Integer> kidsToParents = labels.getNewKidsToParents(type);
            for (Entry<Uri, Label> labelEntry : labels.getNewLabels(type).entrySet()) {
                Uri uri = labelEntry.getKey();

                if (!context.isNewNamespace(uri.getNamespaceUri())) {
                    continue;
                }

                Label label = labelEntry.getValue();
                System.out.println("storeIsaRelationships: Uri = " + uri.toString() + " Label = " + label.toString());
                final Interval interval = label.getTreeLabel();
    
                System.out.println("Saving in " + this.getName() + " the ISA relationship between " + interval.getPost() + " and " + kidsToParents.get(interval.getPost()));
                Map <Integer, Integer> map = Maps.newHashMap();
                map.put(interval.getPost(), kidsToParents.get(interval.getPost()));
                Triple relationshipTriple = labels.getRelationshipTriple(map);
                if (relationshipTriple == null)
                {
                    System.out.println("Not found triple for the ISA relationship between " + interval.getPost() + " and " + kidsToParents.get(interval.getPost()));
                    if (kidsToParents.get(interval.getPost())!= null)
                    {
                        Uri parentUri = context.findSchemaUrifromDB(findUriTablename, kidsToParents.get(interval.getPost()).intValue());
                        for (Uri namedGraph : new Uri[] { RdfSuite.IMPORTER_SIDE_EFFECTS, RdfSuite.DEFAULT_GRAPH_URI} )
                                context.model.add().g(namedGraph).s(uri).p(triplePredicate).o(parentUri);
                        Iterator triplesIterator = context.model.triples().g(RdfSuite.DEFAULT_GRAPH_URI).s(uri).p((type.equals(RdfType.CLASS))? RdfSchema.SUBCLASSOF : RdfSchema.SUBPROPERTYOF ).o(parentUri).fetch().iterator();
                        relationshipTriple = (Triple)triplesIterator.next();
                        System.out.println("ADDED triple: " + relationshipTriple);
                    }
                }
                else
                    System.out.println("FOUND triple: " +  relationshipTriple);
           
                int id = insertHelper.executeAndGetId(
                        interval.getPost(),
                        kidsToParents.get(interval.getPost()),
                        interval.getIndex());
                context.tripleIds.put(id, relationshipTriple);

                for (Interval directDagInterval : label.getPropagatedLabels(true)) { //also store direct propagated labels
                            System.out.println("Saving in " + this.getName() + " the propagated ISA relationship between " + directDagInterval.getPost() + " and " + interval.getPost());
                            Map <Integer, Integer> map2 = Maps.newHashMap();
                            map2.put(directDagInterval.getPost(), interval.getPost());
                            Triple relationshipTripleProp = labels.getRelationshipTriple(map2);
                            if (relationshipTripleProp == null)
                            {
                                System.out.println("DIRECT: Not found triple for the ISA relationship between " + directDagInterval.getPost() + " and " + interval.getPost());
                                if (directDagInterval.getPost()!= 0)
                                {
                                    Uri childUri = context.findSchemaUrifromDB(findUriTablename, directDagInterval.getPost());
                                    for (Uri namedGraph : new Uri[] { RdfSuite.IMPORTER_SIDE_EFFECTS, RdfSuite.DEFAULT_GRAPH_URI} )
                                        context.model.add().g(namedGraph).s(childUri).p(triplePredicate).o(uri);
                                    Iterator triplesIterator = context.model.triples().g(RdfSuite.DEFAULT_GRAPH_URI).s(childUri).p((type.equals(RdfType.CLASS))? RdfSchema.SUBCLASSOF : RdfSchema.SUBPROPERTYOF ).o(uri).fetch().iterator();
                                    relationshipTripleProp = (Triple)triplesIterator.next();
                                    System.out.println("DIRECT - ADDED triple: " + relationshipTripleProp);
                                }
                            }
                            else
                                System.out.println("DIRECT: FOUND triple: " +  relationshipTripleProp);
                            int id2 = insertHelper.executeAndGetId(
                            directDagInterval.getPost(),
                            interval.getPost(),
                            directDagInterval.getIndex());
                            context.tripleIds.put(id2, relationshipTripleProp);
                }
            }
        }
    }

    static abstract class PropagatedIsaTable extends Table {

        private final RdfType type;

        PropagatedIsaTable(String name, RdfType type) {
            super(name);
            this.type = type;
        }

        void storePropagated(ImportContext context) throws SQLException {
            UpdatedLabels labels = context.updatedLabels;
            InsertHelper insertHelper = new InsertHelper(
                    "INSERT INTO " + getName() + " VALUES (?, ?, ?, ?, nextval('tripleid_generator'))",
                    "tripleid_generator");
            
            Map<Uri, Label> newLabels = labels.getNewLabels(type);
            for (boolean direct : new boolean[]{true, false}) {
                for (Entry<Uri, Label> labelEntry : newLabels.entrySet()) {
                    Uri uri = labelEntry.getKey();
                    Label parentLabel = labelEntry.getValue();
                    Interval parentInterval = parentLabel.getTreeLabel();
                    for (Interval interval : parentLabel.getPropagatedLabels(direct)) {
                        Map <Integer, Integer> map = Maps.newHashMap();
                        map.put(interval.getPost(), parentInterval.getPost());
                        Triple relationshipTriple = labels.getRelationshipTriple(map);
                        if (relationshipTriple == null)
                        {
                            //Uri childUri = context.findSchemaUrifromDB(type == RdfType.CLASS ? "t1000000000" : "t2000000000", interval.getPost());
                            System.out.println("PROPAGATED: Not found triple for the Propagated ISA relationship between " + interval.getPost() + " and " + parentInterval.getPost());
                            /*for (Uri namedGraph : new Uri[] { RdfSuite.IMPORTER_SIDE_EFFECTS, RdfSuite.DEFAULT_GRAPH_URI} )
                                    context.model.add().g(namedGraph).s(childUri).p((type.equals(RdfType.CLASS))? RdfSchema.SUBCLASSOF : RdfSchema.SUBPROPERTYOF ).o(uri);
                            Iterator triplesIterator = context.model.triples().g(RdfSuite.DEFAULT_GRAPH_URI).s(childUri).p((type.equals(RdfType.CLASS))? RdfSchema.SUBCLASSOF : RdfSchema.SUBPROPERTYOF ).o(uri).fetch().iterator();
                            relationshipTriple = (Triple)triplesIterator.next();*/
                        }
                        else
                            System.out.println("PROPAGATED: FOUND triple: " +  relationshipTriple);
                        int id = insertHelper.executeAndGetId(
                                interval.getPost(),
                                parentInterval.getPost(),
                                interval.getIndex(),
                                direct);
                        context.tripleIds.put(id, relationshipTriple);
                    }
                }
            }
        }
    }

    static abstract class RdfSchemaDocumentationTable extends Table {

        private final RdfType type;
        private final Uri schemaUri;

        public RdfSchemaDocumentationTable(String name, RdfType type, Uri uri) {
            super(name);
            this.type = type;
            this.schemaUri = uri;
        }

        void storeRdfSchemaDocumentationTriples(ImportContext context) throws SQLException {
            InsertHelper insertHelper = null;
            boolean boolCaseResource = false;
            String att0 = "";
            System.out.println("storeRdfSchemaDocumentationTriples: type = " +
                        type.toString() + ", schemaUri = " +
                        this.schemaUri.toString());
            if (type.equals(RdfType.CLASS) || type.equals(RdfType.PROPERTY)) {
                insertHelper = new InsertHelper(
                        "INSERT INTO " + getName() + " VALUES (?, ?, nextval('tripleid_generator'))",
                        "tripleid_generator");
            } else {
                insertHelper = new InsertHelper(
                        "INSERT INTO " + getName() + " VALUES (?, ?, nextval('tripleid_generator'), ?)",
                        "tripleid_generator");
                boolCaseResource = true;
            }

            for (Triple triple : context.model.triples().p(schemaUri).fetch()) {
                System.out.println("storeRdfSchemaDocumentationTriples: triple.subject = " +
                        triple.subject().getIdentifier() + ", triple.predicate = " +
                        triple.predicate().getIdentifier() + ", triple.object = " +
                        (triple.object().isResource() ? ((Resource)triple.object()).getUri().toString() : ((LiteralNode) triple.object()).getLiteral().getValue()));
                String att1="";
                int newTripleId = 0;
                if (!triple.subject().type().equals(type)) {
                    continue;
                }
                if (boolCaseResource) {
                    att0 = ((gr.forth.ics.swkm.model2.Resource) triple.subject()).getUri().toString();
                } else {
                    att0 = String.valueOf(context.updatedLabels.getLabel(type, ((gr.forth.ics.swkm.model2.Resource) triple.subject()).getUri()).getTreeLabel().getPost());
                }

                if (triple.object().type().equals(RdfType.CLASS) || triple.object().type().equals(RdfType.PROPERTY))
                    att1 = String.valueOf(context.updatedLabels.getLabel(triple.object().type(), ((gr.forth.ics.swkm.model2.Resource) triple.object()).getUri()).getTreeLabel().getPost());
                else if (triple.object().type().equals(RdfType.INDIVIDUAL))
                    att1 = ((gr.forth.ics.swkm.model2.Resource) triple.object()).getUri().toString();
                else
                    att1 = ((LiteralNode) triple.object()).getLiteral().getValue();
                if (!checkExistenceDocumentationDB(this.getName(), boolCaseResource, att0, att1)) {
                    if (!boolCaseResource) {
                        newTripleId = insertHelper.executeAndGetId(
                                Integer.parseInt(att0),
                                triple.object().type().equals(RdfType.CLASS) || triple.object().type().equals(RdfType.PROPERTY) ?
                                    Integer.parseInt(att1) :
                                    ((LiteralNode) triple.object()).getLiteral().getValue());
                        context.tripleIds.put(newTripleId, triple);
                    } else {
                        Triples resourceTypeTriples = context.model.triples().s((gr.forth.ics.swkm.model2.Resource) triple.subject()).p(Rdf.TYPE).fetch();
                        if (!resourceTypeTriples.iterator().hasNext()) {
                            context.model.add().g(RdfSuite.IMPORTER_SIDE_EFFECTS).s((gr.forth.ics.swkm.model2.Resource) triple.subject()).p(Rdf.TYPE).o(RdfSchema.RESOURCE);
                            context.model.add().g(RdfSuite.DEFAULT_GRAPH_URI).s((gr.forth.ics.swkm.model2.Resource) triple.subject()).p(Rdf.TYPE).o(RdfSchema.RESOURCE);
                        }

                        for (Triple t : resourceTypeTriples) {
                            newTripleId = insertHelper.executeAndGetId(
                                    att0,
                                    att1,
                                    context.updatedLabels.getLabel(RdfType.CLASS, ((gr.forth.ics.swkm.model2.Resource) t.object()).getUri()).getTreeLabel().getPost());
                            context.tripleIds.put(newTripleId, triple);
                        }
                    }
                }
            }
        }

        private boolean checkExistenceDocumentationDB(String tablename, boolean boolAtt0String, String att0, String att1) throws SQLException {
            java.sql.PreparedStatement pstmt = Jdbc.prepared(String.format("SELECT 1 FROM %s where att0 = ? and att1 = ?", tablename));

            if (boolAtt0String)
                pstmt.setString(1, att0);
            else
                pstmt.setInt(1, Integer.parseInt(att0));

            pstmt.setString(2, att1);

            //String sql = "SELECT 1 FROM %s WHERE att0 = ";
            //sql += boolAtt0String ? "'%s'" : "%d";
            //sql += " AND att1 = '%s'";
            //ResultSet rs = Jdbc.query(String.format(sql, tablename, boolAtt0String ? att0 : Integer.parseInt(att0), att1));

            ResultSet rs = pstmt.executeQuery();
            try {
                return rs.next();
            } finally {
                rs.close();
            }
        }
    }

    static class SubClass extends IsaTable {

        public final Attribute subId;
        public final Attribute superId;
        public final Attribute index;
        public final Attribute tripleId;

        private SubClass(Configurer conf) {
            super("SubClass", RdfType.CLASS);
            subId = newAttribute("att0", "INTEGER NOT NULL");
            superId = newAttribute("att1", "INTEGER");
            index = newAttribute("att2", "INTEGER NOT NULL");
            tripleId = newAttribute("att3", "INTEGER");
        }
    }

    static class SubProperty extends IsaTable {

        public final Attribute subId;
        public final Attribute superId;
        public final Attribute index;
        public final Attribute tripleId;

        private SubProperty(Configurer conf) {
            super("SubProperty", RdfType.PROPERTY);
            subId = newAttribute("att0", "INTEGER NOT NULL");
            superId = newAttribute("att1", "INTEGER");
            index = newAttribute("att2", "INTEGER NOT NULL");
            tripleId = newAttribute("att3", "INTEGER");
        }
    }

    static class ClassAncestry extends PropagatedIsaTable {

        public final Attribute classId;
        public final Attribute namespaceId;
        public final Attribute ancestorClassId;
        public final Attribute isDirectChild;
        public final Attribute tripleId;

        private ClassAncestry(Configurer conf) {
            super("Class_Anc", RdfType.CLASS);
            classId = newAttribute("att0", "INTEGER NOT NULL");
            namespaceId = newAttribute("att1", "INTEGER");
            ancestorClassId = newAttribute("att2", "INTEGER NOT NULL");
            isDirectChild = newAttribute("att3", "boolean NOT NULL");
            tripleId = newAttribute("att4", "INTEGER");
        }
    }

    static class PropertyAncestry extends PropagatedIsaTable {

        public final Attribute propertyId;
        public final Attribute namespaceId;
        public final Attribute ancestorPropertyId;
        public final Attribute isDirectChild;
        public final Attribute tripleId;

        private PropertyAncestry(Configurer conf) {
            super("Property_Anc", RdfType.PROPERTY);
            propertyId = newAttribute("att0", "INTEGER NOT NULL");
            namespaceId = newAttribute("att1", "INTEGER");
            ancestorPropertyId = newAttribute("att2", "INTEGER NOT NULL");
            isDirectChild = newAttribute("att3", "boolean NOT NULL");
            tripleId = newAttribute("att4", "INTEGER");
        }
    }

    static class Generator extends Table {

        public final Attribute namespaceCounter;
        public final Attribute anonymousCounter;
        public final Attribute thesaurusCounter;

        private Generator(Configurer conf) {
            super("Generator");
            namespaceCounter = newAttribute("att0", "INTEGER NOT NULL");
            anonymousCounter = newAttribute("att1", "INTEGER NOT NULL");
            thesaurusCounter = newAttribute("att2", "INTEGER NOT NULL");
        }
    }

    static class Representation extends Table {

        public final Attribute type;

        private Representation(Configurer conf) {
            super("REPRESENTATION");
            type = newAttribute("att0", "INTEGER NOT NULL");
        }
    }

    static class MaxPostOrder extends Table {

        public final Attribute metaclassPostOrder;
        public final Attribute metapropertyPostOrder;
        public final Attribute classPostOrder;
        public final Attribute propertyPostOrder;

        private MaxPostOrder(Configurer conf) {
            super("max_postorder");
            metaclassPostOrder = newAttribute("att0", "INTEGER NOT NULL");
            metapropertyPostOrder = newAttribute("att1", "INTEGER NOT NULL");
            classPostOrder = newAttribute("att2", "INTEGER NOT NULL");
            propertyPostOrder = newAttribute("att3", "INTEGER NOT NULL");
        }
    }

    static class SequenceTypes extends Table {

        public final Attribute att0; //TODO: clarify attribute
        public final Attribute att1; //TODO: clarify attribute
        public final Attribute att2; //TODO: clarify attribute
        public final Attribute att3; //TODO: clarify attribute

        private SequenceTypes(Configurer conf) {
            super("seqtypes");
            att0 = newAttribute("att0", "INTEGER");
            att1 = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class BagTypes extends Table {

        public final Attribute att0; //TODO: clarify attribute
        public final Attribute att1; //TODO: clarify attribute
        public final Attribute att2; //TODO: clarify attribute

        private BagTypes(Configurer conf) {
            super("bagtypes");
            att0 = newAttribute("att0", "INTEGER");
            att1 = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static abstract class ClassNote extends RdfSchemaDocumentationTable {

        public final Attribute classId;

        private ClassNote(String name, Uri uri) {
            super(name, RdfType.CLASS, uri);
            classId = newAttribute("att0", "INTEGER");
        }
    }

    static abstract class PropertyNote extends RdfSchemaDocumentationTable {

        public final Attribute propertyId;

        private PropertyNote(String name, Uri uri) {
            super(name, RdfType.PROPERTY, uri);
            propertyId = newAttribute("att0", "INTEGER");
        }
    }

    static abstract class ResourceNote extends RdfSchemaDocumentationTable {

        public final Attribute resourceUri;

        private ResourceNote(String name, Uri uri, Configurer conf) {
            super(name, RdfType.INDIVIDUAL, uri);
            resourceUri = newAttribute("att0", "VARCHAR(%d)", conf.getMaxUriLength());
        }
    }

    static class ClassComment extends ClassNote {

        public final Attribute comment;
        public final Attribute att2; //TODO: clarify attribute

        private ClassComment(Configurer conf) {
            super("ClassComment", RdfSchema.COMMENT);
            comment = newAttribute("att1", "TEXT");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class PropertyComment extends PropertyNote {

        public final Attribute comment;
        public final Attribute att2; //TODO: clarify attribute

        private PropertyComment(Configurer conf) {
            super("PropertyComment", RdfSchema.COMMENT);
            comment = newAttribute("att1", "TEXT");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class ResourceComment extends ResourceNote {

        public final Attribute comment;
        public final Attribute att2; //TODO: clarify attribute
        public final Attribute att3; //TODO: clarify attribute

        private ResourceComment(Configurer conf) {
            super("ResourceComment", RdfSchema.COMMENT, conf);
            comment = newAttribute("att1", "TEXT");
            att2 = newAttribute("att2", "INTEGER");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class ClassLabel extends ClassNote {

        public final Attribute label;
        public final Attribute att2; //TODO: clarify attribute

        private ClassLabel(Configurer conf) {
            super("ClassLabel", RdfSchema.LABEL);
            label = newAttribute("att1", "VARCHAR(%d)", conf.getSmallMaxLiteralLength());
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class PropertyLabel extends PropertyNote {

        public final Attribute label;
        public final Attribute att2; //TODO: clarify attribute

        private PropertyLabel(Configurer conf) {
            super("PropertyLabel", RdfSchema.LABEL);
            label = newAttribute("att1", "VARCHAR(%d)", conf.getSmallMaxLiteralLength());
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class ResourceLabel extends ResourceNote {

        public final Attribute label;
        public final Attribute att2; //TODO: clarify attribute
        public final Attribute att3; //TODO: clarify attribute

        private ResourceLabel(Configurer conf) {
            super("ResourceLabel", RdfSchema.LABEL, conf);
            label = newAttribute("att1", "VARCHAR(%d)", conf.getSmallMaxLiteralLength());
            att2 = newAttribute("att2", "INTEGER");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class ClassSeeAlso extends ClassNote {

        public final Attribute seeAlso;
        public final Attribute att2; //TODO: clarify attribute

        private ClassSeeAlso(Configurer conf) {
            super("ClassSeeAlso", RdfSchema.SEEALSO);
            seeAlso = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class PropertySeeAlso extends PropertyNote {

        public final Attribute seeAlso;
        public final Attribute att2; //TODO: clarify attribute

        private PropertySeeAlso(Configurer conf) {
            super("PropertySeeAlso", RdfSchema.SEEALSO);
            seeAlso = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class ResourceSeeAlso extends ResourceNote {

        public final Attribute seeAlso;
        public final Attribute att2; //TODO: clarify attribute
        public final Attribute att3; //TODO: clarify attribute

        private ResourceSeeAlso(Configurer conf) {
            super("ResourceSeeAlso", RdfSchema.SEEALSO, conf);
            seeAlso = newAttribute("att1", "VARCHAR(%d)", conf.getSmallMaxLiteralLength());
            att2 = newAttribute("att2", "INTEGER");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class ClassIsDefinedBy extends ClassNote {

        public final Attribute isDefinedBy;
        public final Attribute att2; //TODO: clarify attribute

        private ClassIsDefinedBy(Configurer conf) {
            super("ClassIsDefinedBy", RdfSchema.ISDEFINEDBY);
            isDefinedBy = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class PropertyIsDefinedBy extends PropertyNote {

        public final Attribute isDefinedBy;
        public final Attribute att2; //TODO: clarify field

        private PropertyIsDefinedBy(Configurer conf) {
            super("PropertyIsDefinedBy", RdfSchema.ISDEFINEDBY);
            isDefinedBy = newAttribute("att1", "INTEGER");
            att2 = newAttribute("att2", "INTEGER");
        }
    }

    static class ResourceIsDefinedBy extends ResourceNote {

        public final Attribute isDefinedBy;
        public final Attribute att2; //TODO: clarify attribute
        public final Attribute att3; //TODO: clarify attribute

        private ResourceIsDefinedBy(Configurer conf) {
            super("ResourceIsDefinedBy", RdfSchema.ISDEFINEDBY, conf);
            isDefinedBy = newAttribute("att1", "VARCHAR(%d)", conf.getSmallMaxLiteralLength());
            att2 = newAttribute("att2", "INTEGER");
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    static class AnonIds extends Table {

        public final Attribute att0; //TODO: clarify attribute

        private AnonIds(Configurer conf) {
            super("AnonIds");
            att0 = newAttribute("att0", "INTEGER PRIMARY KEY");
        }
    }

    public void store(final Model model) throws SQLException {
        jdbc.doInConnection(new ConnectionTask<Void>() {

            public Void execute() throws SQLException {
                Jdbc.connection().setAutoCommit(false);
                storeImpl(model);
                Jdbc.connection().commit();
                return null;
            }
        });
    }

    private void storeImpl(Model model) throws SQLException {
        final Multimap<Resource, Triple> triples = HashMultimap.create();
        TripleListener listener = new TripleListener() {

            public void onTripleAddition(Resource namedGraph, Triple triple) {
            }

            public void onTripleDeletion(Resource namedGraph, Triple triple) {
                //System.out.println(triple);
                triples.put(namedGraph, triple);
            }
        };
        model.addTripleListener(listener);
        try {
            Inference.reduce(model);
            try {
                DbSynchronizer synchronizer = DbSynchronizer.forModel(model);
                KnownDbState dbState = synchronizer.synchronize(RdfSuite.IMPORTER_SIDE_EFFECTS);
                System.out.println("MODEL INSIDE STORE: " + model);
                Validator.defaultValidator().validateAndFailOnFirstError(model);

                UpdatedLabels labels = dbState.recalculateLabels();

                storeModel(model, labels);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            } finally {
                for (Triple t : model.triples().g(RdfSuite.IMPORTER_SIDE_EFFECTS).fetch()) {
                    t.delete();
                }
            }
        } finally {
            model.removeTripleListener(listener);
            for (Entry<Resource, Triple> entry : triples.entries()) {
                Triple t = entry.getValue();
                model.add(entry.getKey(), t.subject(), t.predicate(), t.object());
            }
        }
    }

    protected final void storeModel(Model model, UpdatedLabels labels) throws SQLException {
        ImportContext context = new ImportContext(model, labels);

        updateExistingLabels(context);

        storeClasses(context);
        storeClassHierarchy(context);

        storeProperties(context);
        storePropertyHierarchy(context);

        storeMetaclasses(context);
        storeMetaclassHierarchy(context);

        storeMetaproperties(context);
        storeMetapropertyHierarchy(context);

        storeNamespaceDependencies(context);

        storePropertyInstances(context);

        storeRdfSchemaComment(context);
        storeRdfSchemaIsDefinedBy(context);
        storeRdfSchemaLabel(context);
        storeRdfSchemaSeeAlso(context);

        storeResources(context);
        storeNamedGraphs(context);
    }

    private void updateExistingLabels(ImportContext context) throws SQLException {
        UpdatedLabels updatedLabels = context.updatedLabels;

        Map<Interval, Interval> oldIntervalToNew = Maps.newHashMap();
        Map<Interval, RdfType> oldIntervalToType = Maps.newHashMap();
        final SetMultimap<RdfType, Interval> typeToOldInterval = HashMultimap.create();
        //Two cases:

        //1) Nodes for which we have their URI.
        for (Uri oldResource : updatedLabels.getOldLabels().getResourcesWithPredefinedLabels()) {
            RdfType type = context.model.mapResource(oldResource).type();
            Label oldLabel = updatedLabels.getOldLabels().predefinedLabelOf(oldResource);

            //Do we have a new label for this URI?
            Label newLabel = updatedLabels.getNewLabels(type).get(oldResource);
            if (newLabel != null) {
                oldIntervalToNew.put(oldLabel.getTreeLabel(), newLabel.getTreeLabel());
                oldIntervalToType.put(oldLabel.getTreeLabel(), type);
                typeToOldInterval.put(type, oldLabel.getTreeLabel());
            }
        }

        //2) Nodes for which we only know their old tree interval: all of them need to be updated
        for (RdfType type : EnumSet.of(RdfType.CLASS, RdfType.METACLASS, RdfType.PROPERTY, RdfType.METAPROPERTY)) {
            Map<Interval, Label> map = updatedLabels.getUpdatedLabelsByInterval(type);
            for (Entry<Interval, Label> entry : map.entrySet()) {
                Interval oldLabel = entry.getKey();
                Label newLabel = entry.getValue();
                oldIntervalToNew.put(oldLabel, newLabel.getTreeLabel());
                oldIntervalToType.put(oldLabel, type);
                typeToOldInterval.put(type, oldLabel);
            }
        }
        for (final RdfType type : EnumSet.of(RdfType.CLASS, RdfType.METACLASS, RdfType.PROPERTY, RdfType.METAPROPERTY)) {
            Predicate<Interval> predicate = new Predicate<Interval>() {

                public boolean apply(Interval interval) {
                    return typeToOldInterval.get(type).contains(interval);
                }
            };
            Map<Interval, Interval> filtered = Maps.filterKeys(oldIntervalToNew, predicate);
            if (!filtered.isEmpty()) {
                updateResourceLabels(filtered, type);
            }
        }
    }

    protected void updateResourceLabels(Map<Interval, Interval> oldIntervalToNew, RdfType type) throws SQLException {
        updateClassPropOrMetaclassTable(oldIntervalToNew, type);
        updateSubclassTable(oldIntervalToNew, type);
        if ((type == RdfType.CLASS) || (type == RdfType.PROPERTY)) {
            updateInstanceTable(oldIntervalToNew, type);
        }
    }

    protected void storeClassHierarchy(ImportContext context) throws SQLException {
        commonTables.subClass.storeIsaRelationships(context);
        commonTables.classAncestry.storePropagated(context);
    }

    protected void storeClasses(ImportContext context) throws SQLException {
        throw new UnsupportedOperationException();
    }

    protected void storePropertyHierarchy(ImportContext context) throws SQLException {
        commonTables.subProperty.storeIsaRelationships(context);
        commonTables.propertyAncestry.storePropagated(context);
    }

    protected void storeProperties(ImportContext context) throws SQLException {
        throw new UnsupportedOperationException();
    }

    protected void storeMetaclassHierarchy(ImportContext context) throws SQLException {
        commonTables.subMetaclass.storeIsaRelationships(context);
        commonTables.metaclassAncestry.storePropagated(context);
    }

    protected void storeMetaclasses(ImportContext context) throws SQLException {
        commonTables.metaclass.storeMetaclasses(context);
    }

    protected void storeMetapropertyHierarchy(ImportContext context) throws SQLException {
        commonTables.subMetaproperty.storeIsaRelationships(context);
        commonTables.metapropertyAncestry.storePropagated(context);
    }

    protected void storeMetaproperties(ImportContext context) throws SQLException {
        commonTables.metaclass.storeMetaproperties(context);
    }

    protected void storeNamedGraphs(ImportContext context) throws SQLException {
        int graphsetId = 0;
        int graphId = 0;
        Map<Uri, Integer> mapNamedGraphToGraphsetId = Maps.newHashMap();
        InsertHelper insertHelper = new InsertHelper(
                "INSERT INTO " + this.commonTables.graphset.getName() + " VALUES (nextval('graphsetid_generator'), ARRAY[?])",
                "graphsetid_generator");
        InsertHelper insertHelperNamedGraph = new InsertHelper(
                "INSERT INTO " + this.commonTables.graphspace.getName() + " VALUES (nextval('gsid_generator'), ?)",
                "gsid_generator");
        //System.out.println("storeNamedGraphs: context.tripleids = " + context.tripleIds.size());
        
        for (gr.forth.ics.swkm.model2.Resource resource : context.model.namedGraphs()) {
            //System.out.println("storeNamedGraphs: Named Graph = " + resource.getUri().toString(Uri.Delimiter.WITH));
            if (!resource.is(RdfSuite.IMPORTER_SIDE_EFFECTS)) {
                Iterator triplesIterator = context.model.triples().g(resource).fetch().iterator();

                if (resource.equals(context.model.defaultNamedGraph())) {
                    graphId = 1;
                    graphsetId = DEFAULT_GRAPHSET_ID;
                } else {
                    if (!checkExistenceNamedGraphDB(resource.getUri())) //this.commonTables.graphspace.insert(this.commonTables.graphspace.id, this.commonTables.graphspace.uri).values(this.commonTables.graphspaceIdGenerator.nextValue(), resource.getUri().toString(Uri.Hash.WITH));
                    {
                        graphId = insertHelperNamedGraph.executeAndGetId(resource.getUri().toString(Uri.Delimiter.WITH));
                    } else {
                        graphId = findNamedGraphIDfromDB(resource.getUri());
                    }

                    if (triplesIterator.hasNext() && !checkExistenceGraphsetDB(graphId)) {
                        graphsetId = insertHelper.executeAndGetId(graphId);
                    }
                }
                mapNamedGraphToGraphsetId.put(resource.getUri(), graphsetId);
            }
        }
                
        Iterator contextTriples = context.tripleIds.keySet().iterator();
        while(contextTriples.hasNext())
        {
            int tripleId = (Integer)contextTriples.next();
            Triple triple = context.tripleIds.get(tripleId);
            if (triple!= null)
            {
                for (gr.forth.ics.swkm.model2.Resource resource : triple.graphs())
                    if (!resource.is(RdfSuite.IMPORTER_SIDE_EFFECTS))
                        this.commonTables.graphTriples.insert(this.commonTables.graphTriples.graphsetId, this.commonTables.graphTriples.tripleId).values(mapNamedGraphToGraphsetId.get(resource.getUri()), tripleId);
            }
            else
                this.commonTables.graphTriples.insert(this.commonTables.graphTriples.graphsetId, this.commonTables.graphTriples.tripleId).values(DEFAULT_GRAPHSET_ID, tripleId);
        }
    }

    protected void storeNamespaceDependencies(ImportContext context) throws SQLException {
        NamespaceDependenciesIndex index = new NamespaceDependenciesIndex(context.model);
        Iterator nsIterator = context.model.namespaces().iterator();
        while (nsIterator.hasNext()) {
            Uri ns = (Uri) nsIterator.next();
            Iterator nsDependencies = index.getDependencies(ns, gr.forth.ics.swkm.model2.Transitively.NO).iterator();
            while (nsDependencies.hasNext()) {
                Uri nsDependency = (Uri) nsDependencies.next();
                System.out.println("storeNamespaceDependencies: namespace = " + ns.toString(Uri.Delimiter.WITH) +
                        ", dependent on namespace = " + nsDependency.toString(Uri.Delimiter.WITH));
                if (context.namespaceIds.containsKey(ns)) {
                    // the examined namespace is a new namespace...
                    if (context.namespaceIds.containsKey(nsDependency)) // the namespace nsDependency which the ns namespace depends on is a new namespace too..
                    {
                        this.commonTables.namespaceDependencies.insert(this.commonTables.namespaceDependencies.att0,
                                this.commonTables.namespaceDependencies.att1).values(context.namespaceIds.get(ns),
                                context.namespaceIds.get(nsDependency));
                    } else // the namespace nsDependency which the ns namespace depends on is NOT a new namespace..
                    // I must search it in the DB
                    {
                        this.commonTables.namespaceDependencies.insert(this.commonTables.namespaceDependencies.att0,
                                this.commonTables.namespaceDependencies.att1).values(context.namespaceIds.get(ns),
                                findNamespaceIDfromDB(nsDependency));
                    }
                } else {
                    // the examined namespace is NOT a new namespace
                    int nsId = findNamespaceIDfromDB(ns);
                    if (context.namespaceIds.containsKey(nsDependency)) // the namespace nsDependency which the ns namespace depends on is a new namespace..
                    {
                        this.commonTables.namespaceDependencies.insert(this.commonTables.namespaceDependencies.att0,
                                this.commonTables.namespaceDependencies.att1).values(nsId,
                                context.namespaceIds.get(nsDependency));
                    } else {
                        // the namespace nsDependency which the ns namespace depends on is NOT a new namespace..
                        // I must search it in the DB
                        int nsDependencyId = findNamespaceIDfromDB(nsDependency);
                        // save the dependency only if it doesnt' already exist in DB
                        if (!checkExistenceNamespaceDependencyDB(nsId, nsDependencyId)) {
                            this.commonTables.namespaceDependencies.insert(this.commonTables.namespaceDependencies.att0,
                                    this.commonTables.namespaceDependencies.att1).values(nsId,
                                    nsDependencyId);
                        }
                    }
                }
            }
        }
    }

    protected void storePropertyInstances(ImportContext context) throws SQLException {
        throw new UnsupportedOperationException();
    }

    protected void storeResources(ImportContext context) throws SQLException {
        throw new UnsupportedOperationException();
    }

    protected void storeRdfSchemaComment(ImportContext context) throws SQLException {
        commonTables.classComment.storeRdfSchemaDocumentationTriples(context);
        commonTables.propertyComment.storeRdfSchemaDocumentationTriples(context);
        commonTables.resourceComment.storeRdfSchemaDocumentationTriples(context);
    }

    protected void storeRdfSchemaIsDefinedBy(ImportContext context) throws SQLException {
        commonTables.classIsDefinedBy.storeRdfSchemaDocumentationTriples(context);
        commonTables.propertyIsDefinedBy.storeRdfSchemaDocumentationTriples(context);
        commonTables.resourceIsDefinedBy.storeRdfSchemaDocumentationTriples(context);
    }

    protected void storeRdfSchemaLabel(ImportContext context) throws SQLException {
        commonTables.classLabel.storeRdfSchemaDocumentationTriples(context);
        commonTables.propertyLabel.storeRdfSchemaDocumentationTriples(context);
        commonTables.resourceLabel.storeRdfSchemaDocumentationTriples(context);
    }

    protected void storeRdfSchemaSeeAlso(ImportContext context) throws SQLException {
        commonTables.classSeeAlso.storeRdfSchemaDocumentationTriples(context);
        commonTables.propertySeeAlso.storeRdfSchemaDocumentationTriples(context);
        commonTables.resourceSeeAlso.storeRdfSchemaDocumentationTriples(context);
    }

    private boolean checkExistenceNamedGraphDB(Uri uri) throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM %s WHERE att1 = '%s'", this.commonTables.graphspace.getName(), uri.toString(Uri.Delimiter.WITH)));
        try {
            return rs.next();
        } finally {
            rs.close();
        }
    }

    private boolean checkExistenceGraphsetDB(int graphId) throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM %s WHERE att1 = ARRAY[%d]", this.commonTables.graphset.getName(), graphId));
        try {
            return rs.next();
        } finally {
            rs.close();
        }
    }

    private boolean checkExistenceNamespaceDependencyDB(int nsID, int nsDependentID) throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM %s WHERE att0 = %d AND att1 = %d", this.commonTables.namespaceDependencies.getName(), nsID, nsDependentID));
        try {
            return rs.next();
        } finally {
            rs.close();
        }
    }

    private int findNamespaceIDfromDB(Uri uri) throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT att0 FROM %s WHERE att1 = '%s'", this.commonTables.namespace.getName(), uri.toString(Uri.Delimiter.WITH)));
        try {
            rs.next();
            return rs.getInt(1);
        } finally {
            rs.close();
        }
    }

    private int findNamedGraphIDfromDB(Uri uri) throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT att0 FROM %s WHERE att1 = '%s'", this.commonTables.graphspace.getName(), uri.toString(Uri.Delimiter.WITH)));
        try {
            rs.next();
            return rs.getInt(1);
        } finally {
            rs.close();
        }
    }

    protected class ImportContext {

        protected final Model model;
        protected final Map<Integer, Triple> tripleIds = Maps.newHashMap();
        protected final UpdatedLabels updatedLabels;
        /**
         * Only contains the namespace to-be-stored ids, not existing ones.
         * Importer should not store into preexisting namespaces (all universe will break).
         */
        protected final Map<Uri, Integer> namespaceIds;

        protected ImportContext(Model model, UpdatedLabels updatedLabels) throws SQLException {
            this.model = model;
            this.updatedLabels = updatedLabels;
            this.namespaceIds = commonTables.namespace.createNewNamespaceIds(model.namespaces());
        }

        protected boolean isNewNamespace(Uri namespace) {
            return namespaceIds.containsKey(namespace);
        }
        protected Uri findSchemaUrifromDB(String tablename, int id) throws SQLException
        {
            ResultSet rs = Jdbc.query(String.format("SELECT tb1.att2, tb2.att1 FROM %s tb1, %s tb2 WHERE tb1.att0 = %d and tb1.att1 = tb2.att0", tablename, commonTables.namespace.getName(), id));
            try {
                rs.next();
                return Uri.parse(rs.getString(2)+ rs.getString(1));
            }
            finally
            {
                rs.close();
            }
        }
    }
}
