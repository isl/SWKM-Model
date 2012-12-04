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


package gr.forth.ics.swkm.model2.index;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.event.RdfNodeListener;
import gr.forth.ics.swkm.model2.index.common.CollectionFactory;
import gr.forth.ics.swkm.model2.index.common.IteratorChooser;
import gr.forth.ics.swkm.model2.event.TypeChange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An implementation of ModelIndexer using {@code RdfNodes}' {@code Index}
 * to store information about triples.
 *
 * <p>Each {@code RdfNode} knows all related triples and stores them in three
 * different {@code List}s depending on wether the node is subject, predicate or
 * object of the triples.</p>
 *
 * <p>Additional {@code List}s are kept when the {@code RdfNode} is a named graph
 * or when it is a schema node (Class, Metaclass, Property, Metaproperty). In case
 * of named graphs, all triples it contains are kept seperately. In case of schema
 * nodes, information about ancestors and descendants of {@code Resource}s are kept
 * and thus, can be easily retrieved.</p>
 *
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
class NodeListsModelIndexer extends AbstractModelIndexer implements ModelIndexer {
    private final List<Triple> allTriples;

    private static final TripleDeletionListener doNothingDeletionListener =
            new TripleDeletionListener() {
        public void tripleDeleted(Triple triple) {}

        public void tripleRemovedFromNamedGraph(Resource namedGraph, Triple triple) {}
    };

    public NodeListsModelIndexer() {
        this(CollectionFactory.arrayListFactory);
    }

    public NodeListsModelIndexer(CollectionFactory<List<Triple>> factory) {
        super(new NodeListsNamedGraphIndexer());
        NodeIndex.factory = factory;
        allTriples = new ArrayList<Triple>(128);
    }

    @Override
    public void setModel(Model model) {
        super.setModel(model);
        model.addRdfNodeListener((NodeListsNamedGraphIndexer) super.graphIndexer);
    }

    @Override
    public void add(Resource namedGraph, Triple triple) {
        graphIndexer.add(namedGraph, triple);

        if (containsTriple(triple)) {
            return;
        }
        allTriples.add(triple);
        NodeIndex.getIndex(triple.subject()).addToSubjects(triple);
        NodeIndex.getIndex(triple.predicate()).addToPredicates(triple);
        NodeIndex.getIndex(triple.object()).addToObjects(triple);
    }

    public void addInNamedGraph(Resource namedGraph, Triple triple) {
        graphIndexer.add(namedGraph, triple);
    }


    private NodeIndex newIndex(RdfNode node) {
        if (node.type().isNamedGraph()) {
            return new NamedGraphIndex(node);
        }

        if (node.isBlankNode()) {
            return new BlankNodeIndex(node);
        }
        if (node.isLiteral()) {
            return new LiteralNodeIndex(node);
        }
        // if (node.isResource())
        return new ResourceIndex(node);
    }

    @Override
    public Index indexFor(RdfNode node, TypeChange typeChange) {
        NodeIndex currentIndex = NodeIndex.getIndex(node);
        if (currentIndex == null) {
            return newIndex(node);
        }

        RdfType currentType = node.type();
        RdfType oldType;
        if (typeChange == null ||
                (oldType = typeChange.oldType()).equals(currentType) ||
                oldType.isSchema() ||
                oldType.isNamedGraph()) {
            return currentIndex;
        }

        if (currentType == RdfType.NAMED_GRAPH  &&
                !(currentIndex instanceof NamedGraphIndex)) {
            return new NamedGraphIndex((ResourceIndex)node.getIndex());
        }
        return currentIndex;
    }

    public boolean containsTriple(Triple triple) {
        return findTriples((Resource)null, triple.subject(), triple.predicate(),
                triple.object()).hasNext();
    }

    @Override
    public Iterator<Triple> findTriples(Resource g, ObjectNode s, Resource p, RdfNode o) {
        return findTriples(g, s, p, o, doNothingDeletionListener);
    }

    private Collection<Triple> smallestList(Collection<Triple> c1, Collection<Triple> c2) {
        return c1.size() < c2.size() ? c1 : c2;
    }

    private IteratorChooser<Triple> findTriple(final Resource g, final ObjectNode s,
            final Resource p, final RdfNode o, final TripleDeletionListener listener) {
        Collection<Triple> triples = NodeIndex.getIndex(s).subjects();
        triples = smallestList(NodeIndex.getIndex(p).predicates(), triples);
        triples = smallestList(NodeIndex.getIndex(o).objects(), triples);

        Triple triple = null;

        for (Triple t : triples) {
            if (t.subject() == s &&
                    t.predicate() == p &&
                    t.object() == o) {
                triple = t;
                break;
            }
        }
        if (triple == null ||
                (g != null && !triple.graphs().contains(g))) {
            return IteratorChooser.empty();
        }
        final Triple t = triple;
        return new IteratorChooser<Triple>(Collections.singleton(triple).iterator()) {
            @Override
            protected boolean accept(Triple element) {
                return true;
            }

            @Override
            public void removeAll() {
                delete(t, true);
                listener.tripleDeleted(t);
            }
        };
    }

    private IteratorChooser<Triple> findTriples(final Resource g, final ObjectNode s,
            final Resource p, final RdfNode o, final TripleDeletionListener listener) {
        Collection<Triple> triples = allTriples;

        if (s != null) {
            if (p != null && o != null) {
                return findTriple(g, s, p, o, listener);
            }
            triples = smallestList(NodeIndex.getIndex(s).subjects(), triples);
        }
        if (p != null) {
            triples = smallestList(NodeIndex.getIndex(p).predicates(), triples);
        }
        if (o != null) {
            triples = smallestList(NodeIndex.getIndex(o).objects(), triples);
        }
        if (g != null) {
            triples = smallestList(graphIndexer.get(g), triples);
        }
        return new IteratorChooser<Triple>(triples.iterator()) {
            @Override
            protected boolean accept(Triple t) {
                return (g == null || t.graphs().contains(g)) &&
                        (s == null || s == t.subject()) &&
                        (p == null || p == t.predicate()) &&
                        (o == null || o == t.object());
            }

            @Override
            public void removeAll() {
                if (next != null) {
                    iterator.remove();
                    delete(next, true);
                    listener.tripleDeleted(next);
                }
                while (iterator.hasNext()) {
                    Triple t = iterator.next();
                    if (accept(t)) {
                        iterator.remove();
                        delete(t, true);
                        listener.tripleDeleted(t);
                    }
                }
            }
        };
    }

    @Override
    public void delete(Triple triple) {
        delete(triple, false);
    }

    private void delete(Triple triple, boolean tryDeleteFromAll) {
        if (!tryDeleteFromAll && !containsTriple(triple)) {
            return;
        }
        allTriples.remove(triple);

        NodeIndex index = NodeIndex.getIndex(triple.subject());
        index.removeFrom(index.subjects(), triple);

        index = NodeIndex.getIndex(triple.predicate());
        index.removeFrom(index.predicates(), triple);

        index = NodeIndex.getIndex(triple.object());
        index.removeFrom(index.objects(), triple);

        graphIndexer.removeTriple(triple);
    }

    public void deleteTriples(Resource g, ObjectNode s,
            Resource p, RdfNode o, TripleDeletionListener listener) {
        if (g != null) {
            deleteFromNamedGraph(g, s, p, o, listener);
            return;
        }
        // g == null
        findTriples(g, s, p, o, listener).removeAll();
    }

    public int tripleCount() {
        return allTriples.size();
    }
}


/**
 * The {@code Index} implementation for the {@code NodeListsModelIndexer}.
 *
 * <p>Each {@code NodeIndex} holds Collections of all the triples where the
 * {@code RdfNode} is subject, predicate or object. </p>
 *
 * <p>It provides methods to add triples to the RdfNode, remove them or retrieve
 * them.</p>
 *
 * @author Vouzoukidou Nelly
 */
abstract class NodeIndex implements Index {
    protected RdfNode node;

    protected static CollectionFactory<List<Triple>> factory =
            CollectionFactory.arrayListFactory;

    NodeIndex(RdfNode node) {
        this.node = node;
    }

    /* Triples retrieval */

    protected List<Triple> getList(List<Triple> triples) {
        return triples == null ? EmptyTriplesList.instance() : triples;
    }

    List<Triple> subjects() {
        return EmptyTriplesList.instance();
    }

    List<Triple> predicates() {
        return EmptyTriplesList.instance();
    }

    List<Triple> objects() {
        return EmptyTriplesList.instance();
    }

    List<Triple> namedGraphs() {
        return EmptyTriplesList.instance();
    }


    /* Adding Triples */

    protected List<Triple> addTo(List<Triple> list, Triple triple) {
        if (list == null) {
            list = factory.newInstance();
        }
        list.add(triple);
        return list;
    }

    void addToSubjects(Triple triple) {
        throw new UnsupportedOperationException(node.getClass().getCanonicalName() +
                " cannot be subject of a triple");
    }

    void addToPredicates(Triple triple) {
        throw new UnsupportedOperationException(node.getClass().getCanonicalName() +
                " cannot be predicate of a triple");
    }

    void addToObjects(Triple triple) {
        throw new UnsupportedOperationException(node.getClass().getCanonicalName() +
                " cannot be object of a triple");
    }

    void addToNamedGraphs(Triple triple) {
        throw new UnsupportedOperationException(node.getClass().getCanonicalName() +
                " is not a named graph.");
    }


    /* Removing Triples */

    void removeFrom(List<Triple> list, Triple triple) {
        if (list != null) {
            list.remove(triple);
        }
    }

    public RdfNode getNode() {
        return node;
    }

    static NodeIndex getIndex(RdfNode node) {
        return (NodeIndex) node.getIndex();
    }
}


class LiteralNodeIndex extends NodeIndex {
    private List<Triple> objects;

    LiteralNodeIndex(RdfNode node) {
        super(node);
    }

    LiteralNodeIndex(LiteralNodeIndex index) {
        super(index.getNode());
        objects = index.objects;
    }

    @Override
    List<Triple> objects() {
        return getList(objects);
    }

    @Override
    void addToObjects(Triple triple) {
        objects = addTo(objects, triple);
    }

}
class BlankNodeIndex extends LiteralNodeIndex {
    private List<Triple> subjects;

    BlankNodeIndex(RdfNode node) {
        super(node);
    }

    BlankNodeIndex(BlankNodeIndex index) {
        super(index);
        subjects = index.subjects;
    }

    @Override
    List<Triple> subjects() {
        return getList(subjects);
    }

    @Override
    void addToSubjects(Triple triple) {
        subjects = addTo(subjects, triple);
    }
}

class ResourceIndex extends BlankNodeIndex {
    private List<Triple> predicates;

    ResourceIndex(RdfNode node) {
        super(node);
    }

    ResourceIndex(ResourceIndex index) {
        super(index);
        predicates = index.predicates;
    }

    @Override
    List<Triple> predicates() {
        return getList(predicates);
    }

    @Override
    void addToPredicates(Triple triple) {
        predicates = addTo(predicates, triple);
    }
}

class NamedGraphIndex extends ResourceIndex {
    private List<Triple> namedGraphs;

    NamedGraphIndex(RdfNode node) {
        super(node);
    }

    NamedGraphIndex(ResourceIndex index) {
        super(index);
    }

    @Override
    List<Triple> namedGraphs() {
        return getList(namedGraphs);
    }

    @Override
    void addToNamedGraphs(Triple triple) {
        namedGraphs = addTo(namedGraphs, triple);
    }
}


/**
 * An implementation of {@code NamedGraphIndexer} storing information about
 * triples in named graphs inside the nodes (named graphs). Using this NamedGraphIndexer
 * requires updating named graph Resources' Index.
 *
 * @author Vouzoukidou Nelly
 */
class NodeListsNamedGraphIndexer implements NamedGraphIndexer, RdfNodeListener {
    Set<Resource> namedGraphs;

    public NodeListsNamedGraphIndexer() {
        namedGraphs = new HashSet<Resource>(4);
    }

    public void add(Resource namedGraph, Triple triple) {
        NodeIndex.getIndex(namedGraph).addToNamedGraphs(triple);
    }

    public Collection<Triple> get(Resource namedGraph) {
        return NodeIndex.getIndex(namedGraph).namedGraphs();
    }

    public void removeTriple(Triple triple) {
        for (Resource ng : triple.graphs()) {
            removeTriple(ng, triple);
        }
    }

    public void removeTriple(Resource namedGraph, Triple triple) {
        NodeIndex index = NodeIndex.getIndex(namedGraph);
        index.removeFrom(index.namedGraphs(), triple);
    }

    public Iterator<Resource> iterator() {
        return namedGraphs.iterator();
    }

    public void onTypeChange(TypeChange change) {
        addNamedGraph(change.node());
    }


    public void onNodeAddition(RdfNode node) {
        addNamedGraph(node);
    }

    private void addNamedGraph(RdfNode node) {
        if (node.type().isNamedGraph()) {
            namedGraphs.add((Resource) node);
        }
    }

    public void onNodeDeletion(RdfNode node) {
        if (node.type().isNamedGraph()) {
            namedGraphs.remove(node);
        }
    }
}
