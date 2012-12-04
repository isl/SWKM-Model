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


package gr.forth.ics.swkm.model2;

import gr.forth.ics.swkm.model2.event.TypeChange;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import gr.forth.ics.swkm.model2.Uri.UriFormatException;
import gr.forth.ics.swkm.model2.event.RdfNodeListener;
import gr.forth.ics.swkm.model2.event.TripleListener;
import gr.forth.ics.swkm.model2.index.ModelIndexer;
import gr.forth.ics.swkm.model2.index.ModelView;
import gr.forth.ics.swkm.model2.index.ObjectViewSupport;
import gr.forth.ics.swkm.model2.index.TripleDeletionListener;
import gr.forth.ics.swkm.model2.labels.LabelManager;
import gr.forth.ics.swkm.model2.labels.LabelManagers;
import gr.forth.ics.swkm.model2.labels.Labelers;
import gr.forth.ics.swkm.model2.labels.PredefinedLabels;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.Assert;

/**
 * A Model implementation.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ModelImpl implements Model {
    private final Map<Literal, LiteralNodeImpl> literals =
            new MapMaker().weakValues().makeMap();

    private final Map<Uri, ResourceImpl> resources =
            new MapMaker().weakValues().makeMap();
    
    private final Map<String, BlankNodeImpl> blankNodes =
            new MapMaker().weakValues().makeMap();
    
    private final Map<Set<ResourceImpl>, Set<ResourceImpl>> usedGraphsCombinations;
    
    /**
     * Helper for handling type events.
     */
    private final RdfNodeListenerSupport rdfNodeListenerSupport = new RdfNodeListenerSupport();
    private final TripleListenerSupport tripleListenerSupport = new TripleListenerSupport();

    private final ModelIndexer indexer;
    private final ObjectViewSupport objectViewSupport;

    private final TypeInferenceStrategy typeInferenceStrategy;
    private final UriValidationStrategy uriValidationStrategy;
    
    private final Resource defaultNamedGraph;
    private LabelManager labelManager;

    ModelImpl(ModelIndexer indexer) {
        this(indexer, RdfSuite.DEFAULT_GRAPH_URI);
    }

    ModelImpl(ModelIndexer indexer, Uri defaultNamedGraphUri) {
        this(indexer, TypeInferenceStrategy.WITH_TYPING, 
                UriValidationStrategy.WITH_VALIDATION, defaultNamedGraphUri);
    }

    ModelImpl(ModelIndexer indexer, TypeInferenceStrategy typeInferenceStrategy,
            UriValidationStrategy uriValidationStrategy, Uri defaultNamedGraphUri) {
        this.indexer = Preconditions.checkNotNull(indexer);
        this.typeInferenceStrategy = typeInferenceStrategy;
        this.uriValidationStrategy = uriValidationStrategy;

        this.defaultNamedGraph = mapResource(Preconditions.checkNotNull(defaultNamedGraphUri));
        indexer.setModelView(new ModelViewImpl());
        indexer.setModel(this);
        this.objectViewSupport = indexer.objectViewSupport();
        this.labelManager = new LabelManagers.NonIncrementalMainMemoryManager(
                this, Labelers.newDefault());
        this.usedGraphsCombinations = new HashMap<Set<ResourceImpl>, Set<ResourceImpl>>();
    }

    public Resource defaultNamedGraph() {
        return defaultNamedGraph;
    }
    
    final ObjectViewSupport objectViewSupport() {
        return objectViewSupport;
    }
    
    //called by any node of this model when its type changes
    void handleTypeChange(TypeChange change) {
        RdfNodeImpl node = (RdfNodeImpl)change.node();
        node.setIndex(indexer.indexFor(node, change));
        if (node.type() == change.oldType()) {
            return;
        }
        rdfNodeListenerSupport.fireTypeChange(change);
    }

    public boolean delete(Triple triple) {
        return deleteTriple((TripleImpl)triple, true);
    }

    public boolean delete(Iterable<Triple> triplesToRemove) {
        return deleteTriples(triplesToRemove, true);
    }

    private boolean deleteTriples(Iterable<Triple> triplesToRemove, boolean notifyIndexer) {
        boolean changed = false;
        for (Triple triple : triplesToRemove) {
            changed |= deleteTriple((TripleImpl)triple, notifyIndexer);
        }
        return changed;
    }

    private boolean deleteTriple(TripleImpl t, boolean notifyIndexer) {
        if (notifyIndexer && !indexer.containsTriple(t)) {
            return false;
        }
        decrementCounters(t);
        
        //removing named graphs one by one
        Set<ResourceImpl> graphs = t.namedGraphs;
        t.namedGraphs = Collections.emptySet();
        for (Iterator<ResourceImpl> i = graphs.iterator(); i.hasNext(); ) {
            ResourceImpl namedGraph = i.next();
            namedGraph.decrementCounter();
            tripleListenerSupport.fireTripleDeletion(namedGraph, t);
        }
        
        if (notifyIndexer) {
            indexer.delete(t);
        }
        labelManager.tripleDeleted(t);
        return true;
    }

    public void retypeNodes() {
        for (ObjectNodeImpl objectNode : Iterables.concat(blankNodes.values(), resources.values())) {
            if (objectNode.hasTriples()) {
                objectNode.resetType();
            }
        }
        for (Triple t : triples().fetch()) {
            for (Resource ng : t.graphs()) {
                TypeInference.applyTypingRulesForNamedGraph((ResourceImpl)ng);
            }
            TypeInference.applyTypingRules((TripleImpl)t);
        }
    }

    enum TypeInferenceStrategy {
        WITH_TYPING() {
            void perhapsDoTyping(TripleImpl triple) {
                TypeInference.applyTypingRules(triple);
            }
        },

        NO_TYPING() {
            void perhapsDoTyping(TripleImpl triple) {
                //do nothing
            }
        };

        abstract void perhapsDoTyping(TripleImpl triple);
    }

    enum UriValidationStrategy {
        WITH_VALIDATION() {
            void perhapsValidate(Uri uri) {
                if (!uri.isAbsolute()) {
                    throw new IllegalArgumentException(
                            "Expected an absolute URI. Found relative instead: \"" + uri + "\"");
                }
            }
        },

        WITHOUT_VALIDATION() {
            void perhapsValidate(Uri uri) {
                //do nothing
            }
        };

        abstract void perhapsValidate(Uri uri);
    }

    public AddContext add() {
        return new TripleBuilder();
    }
    
    public Triple add(Resource namedGraph, ObjectNode subject, Resource predicate, RdfNode object) {
        if (namedGraph == null) {
            namedGraph = defaultNamedGraph();
        }
        return addQuad(namedGraph, subject, predicate, object);
    }
    
    TripleImpl addQuad(Resource namedGraph,
            ObjectNode subject, Resource predicate, RdfNode object) {
        TripleImpl triple = getTriple(subject, predicate, object);
        boolean addInIndexer = false;

        if (triple == null) {
            addInIndexer = true;
            triple = new TripleImpl(
                    (ObjectNodeImpl) subject,
                    (ResourceImpl) predicate,
                    (RdfNodeImpl) object);
        }

        ResourceImpl ng = (ResourceImpl)namedGraph;
        TypeInference.applyTypingRulesForNamedGraph(ng);
        if (triple.namedGraphs.contains(ng)) {
            return triple; //exit early; nothing to do
        }
        incrementCounters(ng, triple);
        
        addNamedGraph(ng, triple);
        if (addInIndexer) {
            indexer.add(namedGraph, triple);
        } else {
            indexer.addInNamedGraph(namedGraph, triple);
        }

        tripleListenerSupport.fireTripleAddition(namedGraph, triple);
        
        labelManager.tripleAdded(triple);

        //this call *must* be after the insertion of the triple in indexer; typing depends on indexes!
        //it is the last call because it can fail; in that case, the triple remains in the model;
        //the user may delete it
        typeInferenceStrategy.perhapsDoTyping(triple);
        return triple;
    }
    
    private void addNamedGraph(ResourceImpl ng, TripleImpl triple) {
        updateNamedGraph(true, ng, triple);
    }
    
    private void removeNamedGraph(ResourceImpl ng, TripleImpl triple) {
        updateNamedGraph(false, ng, triple);
        ng.decrementCounter();
    }
    
    private void updateNamedGraph(boolean add, ResourceImpl ng, TripleImpl triple) {
        //actual namedGraphs Set must be copied: it is used by other triples, too
        Set<ResourceImpl> namedGraphs = new HashSet<ResourceImpl>(triple.namedGraphs);
        if (add) {
            namedGraphs.add(ng);
        } else {
            namedGraphs.remove(ng);
        }
        Set<ResourceImpl> usedSet = usedGraphsCombinations.get(namedGraphs);
        if (usedSet == null) {
            usedGraphsCombinations.put(namedGraphs, namedGraphs);
            usedSet = namedGraphs;
        }
        triple.namedGraphs = usedSet;
    }

    private TripleImpl getTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        checkOwned(subject, true);
        checkOwned(predicate, true);
        checkOwned(object, true);
        
        Iterator<Triple> iterator = indexer.findTriples(
                null, subject, predicate, object);
        
        return iterator.hasNext() ? (TripleImpl) iterator.next() : null;
    }

    public LiteralNode mapLiteral(Literal literal) {
        Assert.notNull(literal);
        LiteralNodeImpl literalNode = literals.get(literal);
        if (literalNode == null) {
            literalNode = new LiteralNodeImpl(this, literal);
            literals.put(literal, literalNode);
        }
        return literalNode;
    }
    
    public LiteralNode mapLiteral(String literal) {
        return mapLiteral(Literal.parse(literal));
    }

    public BlankNode mapBlankNode(String id) {
        Assert.notNull(id);
        BlankNodeImpl blankNode = blankNodes.get(id);
        if (blankNode == null) {
            blankNode = new BlankNodeImpl(this, id);
            blankNodes.put(id, blankNode);
        }
        return blankNode;
    }

    public Resource mapResource(Resource resource) {
        if (resource.owner() == this) {
            return resource;
        }
        return mapResource(resource.getUri(), false);
    }
    
    private Resource mapResource(Uri uri, boolean validate) {
        Assert.notNull(uri);
        if (validate) {
            uriValidationStrategy.perhapsValidate(uri);
        }
        ResourceImpl resource = resources.get(uri);
        if (resource == null) {
            resource = new ResourceImpl(this, uri);
            resources.put(uri, resource);
        }
        return resource;    
    }

    public Resource mapResource(Uri uri) {
        return mapResource(uri, true);
    }
    
    public Resource mapResource(String uri) {
        return mapResource(Uri.parse(uri));
    }

    public ObjectNode mapObjectNode(String uriOrId) {
        Uri uri = Uri.tryParse(uriOrId);
        //if this is definitely not a Uri, treat it as blank node
        if (uri == null) {
            return mapBlankNode(uriOrId);
        }
        //if it is a Uri, check existing resource
        ObjectNode node = resources.get(uri);
        if (node != null) {
            //found existing resource
            return node;
        }
        //check existing blank node
        node = blankNodes.get(uriOrId);
        if (node != null) {
            //found existing blank node
            return node;
        }
        return mapResource(uri);
    }

    public RdfNode map(String uriOrIdOrLiteral) {
        Literal literal = Literal.tryParse(uriOrIdOrLiteral);
        if (literal != null) {
            return mapLiteral(literal);
        }
        return mapObjectNode(uriOrIdOrLiteral);
    }

    private boolean checkOwned(RdfNode node, boolean throwException) {
        if (node == null) {
            if (!throwException) {
                return false;
            }
            throw new IllegalArgumentException("null");
        }
        if (node.owner() != this) {
            if (!throwException) {
                return false;
            }
            throw new IllegalArgumentException("Object [" + node + "] is not owned by this " +
                    Model.class.getSimpleName()); //just to be refactorable
        }
        return true;
    }

    public QueryBuilder triples() {
        return new QueryBuilderImpl();
    }

    private Iterable<RdfNode> findNodes(final RdfType type) {
        return new Iterable<RdfNode>() {
            public Iterator<RdfNode> iterator() {
                return indexer.find(Preconditions.checkNotNull(type));
            }
        };
    }
    
    public Iterable<RdfNode> findNodes(RdfType firstType, RdfType... restTypes) {
        List<Iterable<RdfNode>> iterables = Lists.newLinkedList();
        iterables.add(findNodes(firstType));
        for (RdfType type : restTypes) {
            iterables.add(findNodes(type));
        }
        return Iterables.concat(iterables);
    }

    private Iterable<Resource> findSchemaNodes(final Uri namespace, final RdfType type) {
        if (!type.isSchema()) {
            throw new IllegalArgumentException("Requested type: " + type + " is not schema");
        }
        if (namespace.getLocalName().length() > 0) {
            throw new IllegalArgumentException("Uri: '" + namespace + "' is not purely a namespace; it also contains a local part");
        }
        return new Iterable<Resource>() {
            public Iterator<Resource> iterator() {
                return indexer.findInNamespace(
                            type,
                            namespace);
            }
        };
    }

    public Iterable<Resource> findSchemaNodes(Uri namespace, RdfType firstType, RdfType... restTypes) {
        List<Iterable<Resource>> iterables = Lists.newLinkedList();
        iterables.add(findSchemaNodes(namespace, firstType));
        for (RdfType type : restTypes) {
            iterables.add(findSchemaNodes(namespace, type));
        }
        return Iterables.concat(iterables);
    }
    
    public Iterable<Resource> namedGraphs() {
        return new Iterable<Resource>() {
            @SuppressWarnings("unchecked") //only resources can be named graphs
            public Iterator<Resource> iterator() {
                Iterator iterator = indexer.find(RdfType.NAMED_GRAPH);
                return iterator;
            }
        };
    }

    private static final Predicate<RdfNode> isActiveNode = new Predicate<RdfNode>() {
        public boolean apply(RdfNode node) {
            return node.hasTriples();
        }
    };

    @SuppressWarnings("unchecked") //every ResourceImpl is a Resource
    public Iterable<Resource> resources() {
        return (Iterable)Iterables.filter(resources.values(), isActiveNode);
    }

    @SuppressWarnings("unchecked") //every LiteralNodeImpl is a LiteralNode
    public Iterable<LiteralNode> literals() {
        return (Iterable)Iterables.filter(literals.values(), isActiveNode);
    }

    @SuppressWarnings("unchecked") //every BlankNodeImpl is a BlankNode
    public Iterable<BlankNode> blankNodes() {
        return (Iterable)Iterables.filter(blankNodes.values(), isActiveNode);
    }
    
    /**
     * Returns all available namespaces.
     *
     * @return all available namespaces
     */
    public Set<Uri> namespaces() {
        return indexer.namespaces();
    }

    public boolean isInferable(ObjectNode s, Resource p, RdfNode o) {
        checkOwned(s, true);
        checkOwned(p, true);
        checkOwned(o, true);

        if (triples().s(s).p(p).o(o).fetch().iterator().hasNext()) {
            return true;
        }
        if (p.is(RdfSchema.SUBCLASSOF)
                || p.is(RdfSchema.SUBPROPERTYOF)) {
            return s.asInheritable().isDescendantOf(o);
        } else if (p.is(Rdf.TYPE)) {
            for (RdfNode type : triples().s(s).p(p).fetch().objects()) {
                if (type.asInheritable().isDescendantOf(o)) {
                    return true;
                }
            }
        } else {
            for (Resource predicate : triples().s(s).o(o).fetch().predicates()) {
                if (predicate.asInheritable().isDescendantOf(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (Triple t : triples().fetch()) {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }

    private void incrementCounters(ResourceImpl namedGraph, TripleImpl triple) {
        namedGraph.incrementCounter();
        triple.subject().incrementCounter();
        triple.predicate().incrementCounter();
        triple.object().incrementCounter();
    }

    /**
     * Counter of named graph is decremented in {@link TripleImpl#removeFromNamedGraph(Resource)}.
     */
    private void decrementCounters(TripleImpl triple) {
//        for (ResourceImpl ng : triple.namedGraphs) {
//            ng.decrementCounter();

            triple.subject().decrementCounter();
            triple.predicate().decrementCounter();
            triple.object().decrementCounter();
//        }
    }

    /**
     * Called by RdfNodeImpl#decrementCounter().
     */
    void onDeletedNode(RdfNodeImpl node) {
        rdfNodeListenerSupport.fireNodeDeletion(node);
    }

    /**
     * Called by RdfNodeImpl#incrementCounter().
     */
    void onAddedNode(RdfNodeImpl node) {
        rdfNodeListenerSupport.fireNodeAddition(node);
    }

    private class QueryBuilderImpl implements QueryBuilder {
        private boolean hasGraph = false;
        private Resource namedGraph;

        private boolean hasSubject = false;
        private ObjectNode subject;

        private boolean hasPredicate = false;
        private Resource predicate;

        private boolean hasObject = false;
        private RdfNode object;
        
        private void checkGraphAndSet(Resource namedGraph) {
            if (hasGraph) {
                throw new IllegalStateException("Named graph has already been set");
            }
            checkOwned(namedGraph, true);
            hasGraph = true;
            this.namedGraph = namedGraph;
        }

        private void checkSubjectAndSet(ObjectNode subject) {
            if (hasSubject) {
                throw new IllegalStateException("Subject has already been set");
            }
            checkOwned(subject, true);
            hasSubject = true;
            this.subject = subject;
        }

        private void checkPredicateAndSet(Resource predicate) {
            if (hasPredicate) {
                throw new IllegalStateException("Predicate has already been set");
            }
            checkOwned(predicate, true);
            hasPredicate = true;
            this.predicate = predicate;
        }

        private void checkObjectAndSet(RdfNode object) {
            if (hasObject) {
                throw new IllegalStateException("Object has already been set");
            }
            checkOwned(object, true);
            hasObject = true;
            this.object = object;
        }
        
        public QueryBuilder g(String namedGraph) {
            if (namedGraph == null) {
                g(defaultNamedGraph());
            }
            return g(mapResource(Uri.parse(namedGraph)));
        }

        public QueryBuilder g(Uri namedGraph) {
            if (namedGraph == null) {
                g(defaultNamedGraph());
            }
            return g(mapResource(namedGraph));
        }

        public QueryBuilder g(Resource namedGraph) {
            if (namedGraph == null) {
                this.namedGraph = defaultNamedGraph();
            }
            checkGraphAndSet(namedGraph);
            return this;
        }
        
        public QueryBuilder s(String uriOrId) {
            return s(mapObjectNode(uriOrId));
        }

        public QueryBuilder s(Uri uri) {
            return s(mapResource(uri));
        }

        public QueryBuilder s(ObjectNode node) {
            Assert.notNull(node);
            checkSubjectAndSet(node);
            return this;
        }

        public QueryBuilder p(String uri) {
            return p(mapResource(Uri.parse(uri)));
        }

        public QueryBuilder p(Uri uri) {
            return p(mapResource(uri));
        }

        public QueryBuilder p(Resource node) {
            Assert.notNull(node);
            checkPredicateAndSet(node);
            return this;
        }

        public QueryBuilder o(String uriOrIdOrLiteral) {
            return o(map(uriOrIdOrLiteral));
        }

        public QueryBuilder o(Uri uri) {
            return o(mapResource(uri));
        }

        public QueryBuilder o(Literal literal) {
            return o(mapLiteral(literal));
        }

        public QueryBuilder o(RdfNode node) {
            Assert.notNull(node);
            checkObjectAndSet(node);
            return this;
        }

        public Triples fetch() {
            return new Triples(new Iterable<Triple>() {
                public Iterator<Triple> iterator() {
                    return indexer.findTriples(namedGraph, subject, predicate, object);
                }
            });
        }
        
        public boolean delete() {
            final List<Triple> toDelete = new ArrayList<Triple>();
            final List<Triple> toDeleteAndNotifyIndexer = new ArrayList<Triple>();
            final boolean[] changed = new boolean[1];
            indexer.deleteTriples(namedGraph, subject, predicate, object, new TripleDeletionListener() {
                public void tripleDeleted(Triple triple) {
                    toDelete.add(triple);
                    //no need to fire an event here, it will be fired from deleteTriples
                    changed[0] = true;
                }

                public void tripleRemovedFromNamedGraph(Resource namedGraph, Triple triple) {
                    //first remove from named graph and then fire event
                    removeNamedGraph((ResourceImpl)namedGraph, (TripleImpl)triple);
                    tripleListenerSupport.fireTripleDeletion(namedGraph, triple);

                    if (triple.graphs().isEmpty()) {
                        toDeleteAndNotifyIndexer.add(triple);
                    }
                    changed[0] = true;
                }
            });
            ModelImpl.this.deleteTriples(toDelete, false);
            ModelImpl.this.deleteTriples(toDeleteAndNotifyIndexer, true);
            return changed[0];
        }
    }

    private class ModelViewImpl implements ModelView {
        public Iterable<LiteralNode> allLiterals() {
            return Collections.<LiteralNode>unmodifiableCollection(literals.values());
        }

        public Iterable<Resource> allResources() {
            return Collections.<Resource>unmodifiableCollection(resources.values());
        }

        public Iterable<BlankNode> allBlankNodes() {
            return Collections.<BlankNode>unmodifiableCollection(blankNodes.values());
        }
    }
    
    private class TripleBuilder implements AddContext, AddSubject, AddPredicate, AddObject {
        private Resource namedGraph;
        private ObjectNode subject;
        private Resource predicate;
        private RdfNode object;
        
        public AddSubject g(Resource namedGraph) {
            checkOwned(namedGraph, true);
            this.namedGraph = namedGraph;
            return this;
        }

        public AddSubject g(Uri uri) {
            return g(mapResource(uri));
        }

        public AddSubject g(String uri) {
            return g(mapResource(Uri.parse(uri)));
        }

        public AddPredicate s(ObjectNode subject) {
            checkOwned(subject, true);
            this.subject = subject;
            return this;
        }

        public AddPredicate s(Uri uri) {
            return s(mapResource(uri));
        }

        public AddPredicate s(String uriOrId) {
            return s(mapObjectNode(uriOrId));
        }

        public AddObject p(Resource predicate) {
            checkOwned(predicate, true);
            this.predicate = predicate;
            return this;
        }

        public AddObject p(Uri uri) {
            return p(mapResource(uri));
        }

        public AddObject p(String uri) {
            return p(mapResource(Uri.parse(uri)));
        }

        public Triple o(RdfNode object) {
            checkOwned(object, true);
            this.object = object;
            return add(namedGraph, subject, predicate, object);
        }

        public Triple o(Uri uri) {
            return o(mapResource(uri));
        }

        public Triple o(Literal literal) {
            return o(mapLiteral(literal));
        }

        public Triple o(String uriOrIdOrLiteral) {
            return o(map(uriOrIdOrLiteral));
        }

        public Resource newClass(String classUri) throws UriFormatException {
            return (Resource)s(classUri).p(RdfSchema.SUBCLASSOF).o(RdfSchema.RESOURCE).subject();
        }

        public Resource newClass(Uri classUri) {
            return (Resource)s(classUri).p(RdfSchema.SUBCLASSOF).o(RdfSchema.RESOURCE).subject();
        }

        public Resource newMetaclass(String metaclassUri) throws UriFormatException {
            return (Resource)s(metaclassUri).p(RdfSchema.SUBCLASSOF).o(RdfSchema.CLASS).subject();
        }

        public Resource newMetaclass(Uri metaclassUri) {
            return (Resource)s(metaclassUri).p(RdfSchema.SUBCLASSOF).o(RdfSchema.CLASS).subject();
        }

        public Resource newProperty(String propertyUri) throws UriFormatException {
            return (Resource)s(propertyUri).p(Rdf.TYPE).o(Rdf.PROPERTY).subject();
        }

        public Resource newProperty(Uri propertyUri) {
            return (Resource)s(propertyUri).p(Rdf.TYPE).o(Rdf.PROPERTY).subject();
        }

        public Resource newMetaproperty(String metapropertyUri) throws UriFormatException {
            return (Resource)s(metapropertyUri).p(RdfSchema.SUBCLASSOF).o(Rdf.PROPERTY).subject();
        }

        public Resource newMetaproperty(Uri metapropertyUri) {
            return (Resource)s(metapropertyUri).p(RdfSchema.SUBCLASSOF).o(Rdf.PROPERTY).subject();
        }

        public Resource newIndividual(String individualUri) throws UriFormatException {
            return (Resource)s(mapResource(individualUri)).p(Rdf.TYPE).o(RdfSchema.RESOURCE).subject();
        }

        public Resource newIndividual(Uri individualUri) {
            return (Resource)s(individualUri).p(Rdf.TYPE).o(RdfSchema.RESOURCE).subject();
        }

        public Resource newNamedGraph(String namedGraphUri) throws UriFormatException {
            return (Resource)s(namedGraphUri).p(Rdf.TYPE).o(RdfSuite.GRAPH).subject();
        }

        public Resource newNamedGraph(Uri namedGraphUri) {
            return (Resource)s(namedGraphUri).p(Rdf.TYPE).o(RdfSuite.GRAPH).subject();
        }

        public Resource newAlt(String altUri) throws UriFormatException {
            return (Resource)s(mapResource(altUri)).p(Rdf.TYPE).o(Rdf.ALT).subject();
        }

        public Resource newAlt(Uri altUri) {
            return (Resource)s(altUri).p(Rdf.TYPE).o(Rdf.ALT).subject();
        }

        public Resource newBag(String bagUri) throws UriFormatException {
            return (Resource)s(mapResource(bagUri)).p(Rdf.TYPE).o(Rdf.BAG).subject();
        }

        public Resource newBag(Uri bagUri) {
            return (Resource)s(bagUri).p(Rdf.TYPE).o(Rdf.BAG).subject();
        }

        public Resource newSeq(String seqUri) throws UriFormatException {
            return (Resource)s(mapResource(seqUri)).p(Rdf.TYPE).o(Rdf.SEQ).subject();
        }

        public Resource newSeq(Uri seqUri) {
            return (Resource)s(seqUri).p(Rdf.TYPE).o(Rdf.SEQ).subject();
        }

        public Resource newStatement(String statementUri) throws UriFormatException {
            return (Resource)s(mapResource(statementUri)).p(Rdf.TYPE).o(Rdf.STATEMENT).subject();
        }

        public Resource newStatement(Uri statementUri) {
            return (Resource)s(statementUri).p(Rdf.TYPE).o(Rdf.STATEMENT).subject();
        }
    }
    
    public void setLabelManager(LabelManager labelManager) {
        if (labelManager == null) {
            throw new NullPointerException();
        }
        if (labelManager.getTargetModel() != this) {
            throw new IllegalArgumentException("The specified label manager does not target this model");
        }
        this.labelManager = labelManager;
    }

    LabelManager getLabelManager() {
        return labelManager;
    }

    public void updateLabels() {
        labelManager.updateLabels(PredefinedLabels.swkmPredefinedLabels());
    }

    public void updateLabels(PredefinedLabels predefinedLabels) {
        labelManager.updateLabels(PredefinedLabels.swkmPredefinedLabels().merge(predefinedLabels));
    }

    public void addRdfNodeListener(RdfNodeListener rdfNodeListener) {
        rdfNodeListenerSupport.addRdfNodeListener(rdfNodeListener);
    }

    public void removeRdfNodeListener(RdfNodeListener rdfNodeListener) {
        rdfNodeListenerSupport.removeRdfNodeListener(rdfNodeListener);
    }

    private static class RdfNodeListenerSupport {
        private final Map<RdfNodeListener, RdfNodeListener> listeners =
                new MapMaker().weakKeys().initialCapacity(0).makeMap();

        private void addRdfNodeListener(RdfNodeListener typeListener) {
            listeners.put(Preconditions.checkNotNull(typeListener), typeListener);
        }

        private void removeRdfNodeListener(RdfNodeListener typeListener) {
            listeners.remove(typeListener);
        }

        void fireTypeChange(TypeChange change) {
            for (RdfNodeListener listener : listeners.keySet()) {
                listener.onTypeChange(change);
            }
        }

        void fireNodeDeletion(RdfNode node) {
            for (RdfNodeListener listener : listeners.keySet()) {
                listener.onNodeDeletion(node);
            }
        }

        void fireNodeAddition(RdfNode node) {
            for (RdfNodeListener listener : listeners.keySet()) {
                listener.onNodeAddition(node);
            }
        }
    }

    public void addTripleListener(TripleListener tripleListener) {
        tripleListenerSupport.addTripleListener(tripleListener);
    }

    public void removeTripleListener(TripleListener tripleListener) {
        tripleListenerSupport.removeTripleListener(tripleListener);
    }

    private static class TripleListenerSupport {
        private final Map<TripleListener, TripleListener> listeners =
                new MapMaker().weakKeys().initialCapacity(0).makeMap();

        private void addTripleListener(TripleListener typeListener) {
            listeners.put(Preconditions.checkNotNull(typeListener), typeListener);
        }

        private void removeTripleListener(TripleListener typeListener) {
            listeners.remove(typeListener);
        }

        void fireTripleAddition(Resource namedGraph, Triple triple) {
            for (TripleListener listener : listeners.keySet()) {
                listener.onTripleAddition(namedGraph, triple);
            }
        }

        void fireTripleDeletion(Resource namedGraph, Triple triple) {
            for (TripleListener listener : listeners.keySet()) {
                listener.onTripleDeletion(namedGraph, triple);
            }
        }
    }
    
    public int tripleCount() {
        return indexer.tripleCount();
    }
}
