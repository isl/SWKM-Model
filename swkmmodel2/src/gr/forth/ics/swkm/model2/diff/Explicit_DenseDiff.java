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


package gr.forth.ics.swkm.model2.diff;

import com.google.common.collect.BiMap;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;

/**
 *
 * @author Papavasileiou Vicky
 */
class Explicit_DenseDiff  implements Diff{
    private BiMap<String,String> namespaceMap;
    private Model m1, m2;
    static final Diff INSTANCE = new Explicit_DenseDiff();
    private Delta delta;

    public Delta diff(Model m1, Model m2, BiMap<String,String> mapping) {
        this.namespaceMap = mapping;
        this.m1 = m1;
        this.m2 = m2;
        this.delta = new TripleDelta();
        return compareTriples();

    }

    public Delta diff(Model m1, Model m2) {
        NamespaceMapping nsMap = new NamespaceMapping();
        nsMap.populateMapping(m1, m2);
        return diff(m1, m2, nsMap.getNamespaceMap());
    }

    private Explicit_DenseDiff() {
        
    }


    private Delta compareTriples() {
        
        for(Triple triple1: m1.triples().fetch()){
            ObjectNode mappedS = null;
            RdfNode mappedO = null;
            Resource mappedP = null;

            if(triple1.subject().isResource()){
                Resource s = (Resource)triple1.subject();
                mappedS = (ObjectNode)DiffUtils.mapNodeToAnotherModel(s, m2, namespaceMap);
            }
            if(triple1.object().isResource()){
                Resource o = (Resource)triple1.object();
                mappedO = DiffUtils.mapNodeToAnotherModel(o, m2, namespaceMap);
            }else if(triple1.object().isLiteral()){
                if(m2.map(triple1.object().toString()).hasTriples()){
                    mappedO = m2.map(triple1.object().toString());
                }
            }
            if(triple1.predicate().isResource()){
                Resource p = (Resource)triple1.predicate();
                mappedP = (Resource)DiffUtils.mapNodeToAnotherModel(p, m2, namespaceMap);
            }
            if(mappedS == null || mappedP == null || mappedO == null){
                ((TripleDelta)delta).insertDeletedTriple(
                        triple1.subject(),
                        triple1.predicate(),
                        triple1.object());
            }else if(!m2.isInferable(mappedS,mappedP,mappedO)){
                ((TripleDelta)delta).insertDeletedTriple(
                        triple1.subject(),
                        triple1.predicate(),
                        triple1.object());
            }
        }
        for(Triple triple2: m2.triples().fetch()){
            ObjectNode mappedS = null;
            RdfNode mappedO = null;
            Resource mappedP = null;

            if(triple2.subject().isResource()){
                Resource s = (Resource)triple2.subject();
                mappedS = (ObjectNode)DiffUtils.mapNodeToAnotherModel(s, m1, namespaceMap);
            }
            if(triple2.object().isResource()){
                Resource o = (Resource)triple2.object();
                mappedO =DiffUtils.mapNodeToAnotherModel(o, m1, namespaceMap);
            }else if(triple2.object().isLiteral()){
                if(m1.map(triple2.object().toString()).hasTriples()){
                    mappedO = m1.map(triple2.object().toString());
                }
            }
            if(triple2.predicate().isResource()){
                Resource p = (Resource)triple2.predicate();
                mappedP = (Resource)DiffUtils.mapNodeToAnotherModel(p, m1, namespaceMap);
            }
            if(mappedS == null || mappedP == null || mappedO == null){
                ((TripleDelta)delta).insertAddedTriple(
                        triple2.subject(),
                        triple2.predicate(),
                        triple2.object());
            }else if(!m1.triples().s(mappedS).p(mappedP).o(mappedO).fetch().iterator().hasNext()){
                ((TripleDelta)delta).insertAddedTriple(
                        triple2.subject(),
                        triple2.predicate(),
                        triple2.object());
            }
        }
        return delta;
    }

}
