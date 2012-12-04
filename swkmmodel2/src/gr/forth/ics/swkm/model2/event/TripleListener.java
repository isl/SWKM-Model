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
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;

/**
 * A listener for {@link Triple} events, fired by a {@link Model}.
 *
 * @see Model#addTripleListener(TripleListener)
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface TripleListener {
    /**
     * Called when a triple is added.
     *
     * <p>Note that if {@code triple.graphs().size() == 1}, then the Triple instance is a newly
     * created and added exactly to the {@code namedGraph} named graph (which is also the
     * single element of {@code triple.graphs()} set). The same triple may subsequently be added
     * to several other named graphs.
     * 
     * @param namedGraph the namedGraph in which the triple was added
     * @param triple the triple that was added to a named graph
     * @see Triple
     */
    void onTripleAddition(Resource namedGraph, Triple triple);

    /**
     * Called when a triple is deleted.
     *
     * <p>Note that if {@code triple.graphs().size() == 0}, then the Triple instance is completely
     * deleted (i.e. a triple must belong at least to a single named graph).
     *
     * @param namedGraph the namedGraph from which the triple was deleted
     * @param triple the triple that was deleted from a named graph
     * @see Triple
     */
    void onTripleDeletion(Resource namedGraph, Triple triple);
}
