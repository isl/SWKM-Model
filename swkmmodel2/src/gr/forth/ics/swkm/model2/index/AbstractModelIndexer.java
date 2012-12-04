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

import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.index.common.IteratorChooser;
import gr.forth.ics.swkm.model2.index.common.IteratorSpecializationChooser;
import gr.forth.ics.swkm.model2.index.common.SuperClassIterator;
import java.util.Collection;
import java.util.Iterator;
import gr.forth.ics.swkm.model2.event.TypeChange;
import gr.forth.ics.swkm.model2.index.common.EmptyIndex;
import java.util.Set;

/**
 * An abstract implementation of {@code ModelIndexer}.
 * 
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
abstract class AbstractModelIndexer implements ModelIndexer {
    private Model model;
    private ModelView modelView;
    private NamespaceIndexer namespaceIndexer;
    protected NamedGraphIndexer graphIndexer;

    public AbstractModelIndexer(NamedGraphIndexer graphIndexer) {
        this.graphIndexer = graphIndexer;
    }

    protected Model model() {
        return model;
    }

    protected ModelView modelView() {
        return modelView;
    }

    protected NamespaceIndexer namespaceIndexer() {
        return namespaceIndexer;
    }
    
    @Override
    public void setModelView(ModelView modelView) {
        this.modelView = modelView;
    }
    
    @Override
    public void setModel(Model model) {
        this.model = model;
        this.namespaceIndexer = new NamespaceIndexer(model);
    }
    
    @Override
    public void removeTripleFromNamedGraph(Resource namedGraph, Triple triple) {
        graphIndexer.removeTriple(namedGraph, triple);
    }
    
    @Override
    public Iterator<RdfNode> find(RdfType type) {
        if (type.isLiteral()) {
            return findRdfNodes(modelView.allLiterals());
        }
        if (type == RdfType.NAMED_GRAPH) {
            return findNamedGraphs(graphIndexer);
        }
        return findResource(modelView.allResources(), type);
    }

    private Iterator<RdfNode> findRdfNodes(
            Iterable<LiteralNode> iterable) {
        return new SuperClassIterator<RdfNode, LiteralNode>(iterable.iterator());
    }

    private Iterator<RdfNode> findNamedGraphs(
            Iterable<Resource> iterable) {
        return new SuperClassIterator<RdfNode, Resource>(iterable.iterator());
    }

    private Iterator<RdfNode> findResource(
            Iterable<Resource> allResources, final RdfType type) {
        return new IteratorSpecializationChooser<RdfNode, Resource>(allResources.iterator()) {
            @Override
            protected boolean accept(RdfNode element) {
                return element.type() == type  &&
                        element.hasTriples();
            }

            @Override
            protected RdfNode specialize(Resource element) {
                return element;
            }
        };
    }

    public Index indexFor(RdfNode node, TypeChange typeChange) {
        return EmptyIndex.instance;
    }

    public Iterator<Resource> findInNamespace(RdfType type, Uri namespace) {
        return namespaceIndexer.findInNamespace(type, namespace);
    }
    
    protected static Iterator<Triple> findTriples(final Collection<Triple> matchingSPO,
            final Collection<Triple> matchingNG, final Resource g,
            final ObjectNode s, final Resource p, final RdfNode o) {
        if (matchingSPO.size() < matchingNG.size()) {
            return findTriples(matchingSPO, g);
        }
        return findTriples(matchingNG, s, p, o);
    }
    
    protected static Iterator<Triple> findTriples(final Iterable<Triple> triples,
            final Resource g) {
        return new IteratorChooser<Triple>(triples.iterator()) {
            @Override
            protected boolean accept(Triple triple) {
                return triple.graphs().contains(g);
            }
        };
    }
    
    protected static Iterator<Triple> findTriples(final Iterable<Triple> triples,
            final ObjectNode s, final Resource p, final RdfNode o) {
        return new IteratorChooser<Triple>(triples.iterator()) {
            protected boolean accept(Triple triple) {
                return AbstractModelIndexer.match(s, p, o, triple);
            }
        };
    }
    

    public ObjectViewSupport objectViewSupport() {
        return new TripleBasedObjectViewSupport(this, model);
    }
    
    /**
     * Deletes the triple with the given subject, predicate and object from the 
     * given named graph. If some of the subject, predicate and object are null,
     * their value is indifferent. Parameter g cannot be null.
     * @param g the named graph
     * @param s the subject of the triple or null if indifferent
     * @param p the predicate of the triple or null if indifferent
     * @param o the object of the triple or null if indifferent
     * @param l the TripleDeletionListener
     */
    protected void deleteFromNamedGraph(Resource g, ObjectNode s,
            Resource p, RdfNode o, TripleDeletionListener l) {
        for (Iterator<Triple> iterator = graphIndexer.get(g).iterator(); 
                iterator.hasNext(); ) {
            Triple triple = iterator.next();
            if (match(s, p, o, triple)) {
                iterator.remove();
                l.tripleRemovedFromNamedGraph(g, triple);
            }
        }
    }
    
    protected static boolean match(ObjectNode s, Resource p, RdfNode o, Triple triple) {
        return (s == null || s == triple.subject()) &&
                (p == null || p == triple.predicate()) &&
                (o == null || o == triple.object());
    }
    
    /**
     * Returns all available namespaces.
     *
     * @return all available namespaces
     */
    public Set<Uri> namespaces() {
        return namespaceIndexer.namespaces();
    }
}
