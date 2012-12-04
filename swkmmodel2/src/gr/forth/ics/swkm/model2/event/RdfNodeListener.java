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


package gr.forth.ics.swkm.model2.event;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import java.util.List;

/**
 * A listener for {@link RdfNode} events, fired by a {@link Model}.
 *
 * @see Model#addRdfNodeListener(RdfNodeListener)
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface RdfNodeListener {
    /**
     * Called when a node changes type. 
     * 
     * @param change an object representing the occurred type change
     */
    void onTypeChange(TypeChange change);

    /**
     * Called when a node loses all its triples, which can be thought as deleting that node.
     * Note that this might be called several times for a single node if the node loses all its
     * triples (i.e. deleted), but reused to add some triples back, and loses those triples again.
     *
     * @param node the node that lost all its triples
     */
    void onNodeDeletion(RdfNode node);

    /**
     * Called when a node acquires its first triple, which can be thought as adding that node.
     * Note that this might be called several times for a single node if the node loses all its
     * triples (i.e. deleted), but reused to add some triples back, and loses those triples again.
     *
     * @param node the node that acquired its first triple
     */
    void onNodeAddition(RdfNode node);
}
