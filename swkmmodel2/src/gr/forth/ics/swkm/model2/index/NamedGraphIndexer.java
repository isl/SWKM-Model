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

import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import java.util.Collection;

/**
 * An indexer for triples in named graphs. Adding or removing named graphs is not
 * allowed. Named graphs are created by adding triples in new named graphs.
 * 
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
interface NamedGraphIndexer extends Iterable<Resource> {
    /**
     * Adds the triple in the given named graph.
     * @param namedGraph the named graph where the triple is to be added
     * @param triple the triple to be added
     */
    void add(Resource namedGraph, Triple triple);
    
    /**
     * Returns the {@code Collection} of triples contained in the given named 
     * graph, or an empty {@code Collection} if the named graph does not exists.
     * 
     * @param namedGraph returned triples will belong in this named graph 
     * @return the {@code Collection} of triples contained in the given named graph
     */
    Collection<Triple> get(Resource namedGraph);

    /**
     * Removes the given triple from all named graphs.
     * @param triple the triple to be removed
     */
    void removeTriple(Triple triple);

    /**
     * Removes the triple from the given named graphs.
     * @param namedGraph the named graph the triple will be removed from
     * @param triple the triple to be removed
     */
    void removeTriple(Resource namedGraph, Triple triple);
}
