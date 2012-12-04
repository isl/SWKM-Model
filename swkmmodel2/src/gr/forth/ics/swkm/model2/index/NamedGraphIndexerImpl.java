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
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@code NamedGraphIndexer} using a Map to store
 * information about which triples belong to existing named graphs.
 * 
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
class NamedGraphIndexerImpl implements NamedGraphIndexer {
    private final Map<Resource, List<Triple>> graphToTriples;

    public NamedGraphIndexerImpl() {
        graphToTriples = new IdentityHashMap<Resource, List<Triple>>();
    }

    public Iterator<Resource> iterator() {
        return graphToTriples.keySet().iterator();
    }
    
    public void add(Resource namedGraph, Triple triple) {
        List<Triple> triples = graphToTriples.get(namedGraph);
        if (triples == null) {
            triples = new ArrayList<Triple>();
            graphToTriples.put(namedGraph, triples);
        }
        triples.add(triple);
    }
    
    public Collection<Triple> get(Resource namedGraph) {
        Collection<Triple> triples = graphToTriples.get(namedGraph);
        return triples == null ? EmptyTriplesList.instance() : triples;
    }

    public void removeTriple(Triple triple) {
        for (Resource namedGraph : triple.graphs()) {
            removeTriple(namedGraph, triple);
        }
    }

    public void removeTriple(Resource namedGraph, Triple triple) {
        List<Triple> triples = graphToTriples.get(namedGraph);
        if (triples == null) {
            return;
        }
        triples.remove(triple);
    }
}
