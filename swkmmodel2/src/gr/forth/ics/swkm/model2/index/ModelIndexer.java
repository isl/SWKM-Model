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
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.event.TypeChange;
import java.util.Iterator;
import java.util.Set;

/**
 * An entity providing indexing services for an {@link Model RDF model}.
 * 
 * <p>A ModelIndexer implementation 
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface ModelIndexer {
    /**
     * Returns an Iterator with all the triples that match the given triple
     * pattern. If any of the subject, predicate or object are {@code null}, then the value
     * of the given node of the triple is not taken into acount.
     * <p>
     * Must not report duplicate triples, if these are contained in more than one named graph
     * and parameter {@code namedGraph} is {@code null}.
     *
     * @param namedGraph the named graph to match, or {@code null} to match any
     * @param subject the subject to match, or {@code null} to match any
     * @param predicate the predicate to match, or {@code null} to match any
     * @param object the object to match, or {@code null} to match any
     * @return an Iterable with all the triples that match the given triple pattern
     */
    Iterator<Triple> findTriples(Resource namedGraph, ObjectNode subject, Resource predicate, RdfNode object);

    /**
     * Deletes from this indexer all the triples that match the given triple
     * pattern. If any of the subject, predicate or object are {@code null}, then the value
     * of the given node of the triple is not taken into acount.
     *
     * @param namedGraph the named graph to match, or {@code null} to match any
     * @param subject the subject to match, or {@code null} to match any
     * @param predicate the predicate to match, or {@code null} to match any
     * @param object the object to match, or {@code null} to match any
     * @param listener a listener that is notified for every deleted triple
     */
    void deleteTriples(Resource namedGraph,
            ObjectNode subject, Resource predicate, RdfNode object,
            TripleDeletionListener listener);

    /**
     * Returns all nodes with a given type.
     *
     * @param type the type of the nodes to return
     * @return all nodes with a given type
     */
    Iterator<RdfNode> find(RdfType type);

    /**
     * Returns all nodes with a given type, which also have a URI with a specified namespace.
     *
     * @param type the type of the nodes to return
     * @param namespace the namespace of the nodes to return
     * @return all nodes with a given type, which also have a URI with a specified namespace
     */
    Iterator<Resource> findInNamespace(RdfType type, Uri namespace);
    
    /**
     * Returns all available namespaces.
     *
     * @return all available namespaces
     */
    Set<Uri> namespaces();

    /**
     * Sets the ModelView that can be used to access to the (indexed) Model's node collections.
     *
     * @param modelView the ModelView that provides access to
     * the (indexed) Model's node collections
     */
    void setModelView(ModelView modelView);

    /**
     * Sets the Model that this indexer works on. It also adds all Model connected
     * Listeners (if any) to the Model, so it should be called by the constractor
     * of the Model, to ensure no information is lost.
     * 
     * @param model the model that this indexer works on
     */
    void setModel(Model model);
    
    /**
     * Adds a triple in this indexer.
     *
     * @param namedGraph the named graph (non-null) that the triple is added into
     * @param triple the triple to add to this indexer
     *
     */
    void add(Resource namedGraph, Triple triple);

    /**
     * Adds a triple in this indexer.
     *
     * @param namedGraph the named graph (non-null) that the triple is added into
     * @param triple the triple to add to this indexer
     *
     */
    void addInNamedGraph(Resource namedGraph, Triple triple);

    /**
     * Returns an implementation-specific {@linkplain Index}, appropriate
     * for an {@linkplain RdfNode}, of which the type just changed. This is called by
     * the indexed {@linkplain Model} whenever the type of an {@code RdfNode}
     * changes, or when the {@code RdfNode} is initially created.
     *
     * <p>The Index can encapsulate data structures to be used by this indexer in
     * order to accelerate queries which involve the specified node.
     * 
     * <p>The returned Index can be then accessed by {@link RdfNode#getIndex()}. 
     *
     * @param node the node that obtained a new type
     * @param typeChange the type change that possibly requires a new index for the changed node.
     *  May be null if there is no triple that caused this type change (i.e. when a node gets its
     *  initial {@link RdfType#UNKNOWN} type
     * @return a (not necessarily new) appropriate Index for this node and its current type,
     * that can help with queries involving this node, which can subsequently be accessed by
     * {@link RdfNode#getIndex()}
     */
    Index indexFor(RdfNode node, TypeChange typeChange);

    /**
     * Removes a triple from a named graph. The triple itself is not deleted, unless
     * {@link #delete(Triple)} is called.
     * 
     * @param namedGraph the named graph from which to remove the specified triple
     * @param triple the triple to remove from a named graph
     */
    void removeTripleFromNamedGraph(Resource namedGraph, Triple triple);
    
    /**
     * Deletes a triple from this indexer.
     *
     * @param triple the triple to delete from this indexer
     */
    void delete(Triple triple);

    /**
     * Returns an implementation of methods supporting the "object view".
     * 
     * @return an implementation of methods supporting the "object view"
     */
    ObjectViewSupport objectViewSupport();
    
    /**
     * Retruns true if the ModelIndexer cointains the specified triple.
     * 
     * @param triple the given 
     * @return true if the ModelIndexer cointains the specified triple; false 
     * otherwise
     */
    boolean containsTriple(Triple triple);
    
    /**
     * Retruns the number of triples contained in the indexer
     * 
     * @return the number of triples contained in the indexer
     */
    int tripleCount();
}
