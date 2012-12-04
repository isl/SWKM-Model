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

import java.util.Collection;

/**
 * An RDF triple. A triple must always be contained in at least <em>one</em> named graph,
 * or else it is deleted from its model. 
 * 
 * <p>A triple imposes some constraints on its parts:
 * <ul>
 * <li>The <em>subject</em> can be either a {@linkplain Resource} or a {@linkplain BlankNode}
 * <li>The <em>predicate</em> can only be a {@linkplain Resource}
 * <li>The <em>object</em> can be either a {@linkplain Resource}, a {@linkplain BlankNode},
 * or a {@linkplain LiteralNode}.
 * </ul>
 * So, the subject is modelled as an {@linkplain ObjectNode} (which is the super type of Resources and BlankNodes),
 * the predicate is modelled as a {@linkplain Resource}, and the object is modelled as an {@linkplain RdfNode}
 * (which is the super type of Resources, BlankNodes and LiteralNodes).
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface Triple extends Ownable {
    /**
     * Returns the subject of this triple.
     * 
     * @return the subject of this triple
     */
    ObjectNode subject();
    
    /**
     * Returns the predicate of this triple.
     * 
     * @return the predicate of this triple
     */
    Resource predicate();
    
    /**
     * Returns the object of this triple.
     * 
     * @return the object of this triple
     */
    RdfNode object();

    /**
     * Returns the subject, the predicate and the object of this triple as an immutable Iterable.
     * 
     * @return the subject, the predicate and the object of this triple as an immutable Iterable
     */
    Iterable<RdfNode> nodes();
    
    /**
     * Returns the (read-only) set of named graphs that contain this triple.
     * 
     * @return the (read-only) set of named graphs that contain this triple
     */
    Collection<Resource> graphs();
    
    /**
     * Deletes this triple from its owner model.
     * 
     * <p><strong>Important:</strong> After triple deletions,
     * the typing of the nodes is guarranteed to remain valid and consistent,
     * but it is no longer guarranteed to be <em>minimal</em>. Strongly consider
     * invoking {@link Model#retypeNodes()} after triple deletions (see the method specification
     * for further details).
     * 
     * @return true if this triple was actually contained in the model
     */
    boolean delete();

    /**
     * Returns a simple representation of this triple, that is one that only includes the subject,
     * the predicate and the object (not the named graphs).
     *
     * @return a simple representation of this triple, that is one that only includes the subject,
     * the predicate and the object (not the named graphs)
     */
    String toSimpleString();
}
