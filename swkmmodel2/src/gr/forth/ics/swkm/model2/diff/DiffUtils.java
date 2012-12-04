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

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.algo.transitivity.Closure;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Uri.Delimiter;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods
 * @author Papavasileiou Vicky
 */
class DiffUtils {

    static RdfNode mapNodeToAnotherModel(Resource node, Model m,BiMap<String,String> namespaceMap){
        RdfNode mappedNode = null;
        if(namespaceMap.containsKey(node.getUri().getNamespace())){
            Uri u = new Uri(namespaceMap.get(node.getUri().getNamespace()),
                node.getUri().getLocalName());
            if(m.mapResource(u).hasTriples()){
                mappedNode = m.mapResource(u);
            }
        }else if(namespaceMap.containsValue(node.getUri().getNamespace())){
            Uri u = new Uri(namespaceMap.inverse().get(node.getUri().getNamespace()),
                node.getUri().getLocalName());
            if(m.mapResource(u).hasTriples()){
                mappedNode = m.mapResource(u);
            }
        }else{
            if(m.mapResource(node.getIdentifier()).hasTriples()){
                mappedNode = m.mapResource(node.getIdentifier());
            }
        }
        return mappedNode;
    }

    /**
     * Return all the nodes that are ancestors of the given node.
     * @param m The model that contains the node
     * @param n The node whose ancestor we want to find
     * @param closure The edges and nodes that form the closure of the model
     * @param map Used to map RdfNodes of the model to Nodes of the graph
     * @return The set of the nodes that are ancestors of the given node
     */
    static Set<RdfNode> getAncestors(
            Model m, RdfNode n,Closure closure, Map<RdfNode,Node> map){
        
        if(map.get(n) != null){
            return Sets.newHashSet(Iterables.transform(
                    closure.successorsOf(map.get(n)),new Transformer()));
        }
        return Sets.newHashSet();
    }

    /**
     * Return all the nodes that the given node is type of considering also inferred relations.
     * @param m The model that contains the node
     * @param n The node whose types we want to find
     * @param closure The edges and nodes that form the closure of the model
     * @param map Used to map RdfNodes of the model to Nodes of the graph
     * @return The set of the nodes that the given node is type of
     */
    static Set<RdfNode> getSuperTypes(
            Model m , RdfNode n,Closure closure, Map<RdfNode,Node> map){

        Set<RdfNode> ancestors = new HashSet<RdfNode>();
        for(Triple triple: m.triples().s(((Resource)n)).p(Rdf.TYPE).fetch()){
            ancestors.add(triple.object());
            if(map.get(triple.object()) != null){
                ancestors.addAll(Sets.newHashSet(Iterables.transform(
                        closure.successorsOf(map.get(triple.object())),new Transformer())));
            }
        }
        return ancestors;
    }

    /**
     * Given a set of inffered neighboors of a resource regarding the predicate given
     * find all the neighboors that are not contained in the second model.
     * @param r The resource whose neighboors we want to compare
     * @param mappedS The uri of the resource after considering the namespace map
     * @param predicate The predicate that is used to find all the inferred neighboors
     * @param set The set of inferred neighboors of the first model
     * @param m2 The second model
     * @param namespaceMap The map that contains the mapping between the namespaces of the
     * two models
     * @return The set of the inferred neighboors that have been deleted in the
     * second model.
     */
    static Set<Triple> findDeletedNeighboors(
            Model m1, Model m2, Resource r1, Resource r2, Resource predicate,
            Set<RdfNode> set,
            BiMap<String,String> namespaceMap, Delta delta ){

        Set<Triple> deletedTriples = Sets.newHashSet(); 
        Iterator<RdfNode> it = set.iterator();
        while(it.hasNext()){
            Resource next = (Resource)it.next();
            Resource mappedO = (Resource)DiffUtils.mapNodeToAnotherModel(next, m2, namespaceMap);

            if(mappedO != null && mappedO.mappedTo(m2).hasTriples()){
                if(!m2.isInferable(r2,predicate.mappedTo(m2),mappedO)){
                    ((TripleDelta)delta).insertDeletedTriple(r1, predicate, next);
                }
            }else{
                ((TripleDelta)delta).insertDeletedTriple(r1, predicate, next);
            }
        }
        return deletedTriples;
    }

    /**
     * Given a set of inffered neighboors of a resource regarding the predicate given
     * find all the neighboors that are not contained in the first model.
     * @param r The resource whose neighboors we want to compare
     * @param mappedS The uri of the resource after considering the namespace map
     * @param predicate The predicate that is used to fingd all the inferred neighboors
     * @param set The set of inferred neighboors of the second model
     * @param m2 The second model
     * @param namespaceMap The map that contains the mapping between the namespaces of the
     * two models
     * @return The set of the inferred neighboors that have been added in the
     * second model.
     */
    static Set<Triple> findAddedNeighboors(
            Model m1, Model m2, Resource r1, Resource r2, Resource predicate,
            Set<RdfNode> set,
            BiMap<String,String> namespaceMap,Delta delta){

        Set<Triple> addedTriples = Sets.newHashSet();
        Iterator<RdfNode> it = set.iterator();
        while(it.hasNext()){
            Resource next = (Resource)it.next();
            Resource mappedO = (Resource)DiffUtils.mapNodeToAnotherModel(next, m1, namespaceMap);

            if(mappedO != null){
                if(!m1.isInferable(r1,predicate.mappedTo(m1),mappedO)){
                    ((TripleDelta)delta).insertAddedTriple(r2, predicate, next);
                }
            }else{
               ((TripleDelta)delta).insertAddedTriple(r2, predicate, next); 
            }
        }
        return addedTriples;
    }

    private static class Transformer implements Function<Node,RdfNode>{

        public RdfNode apply(Node from) {
            return (RdfNode)from.getValue();
        }

    }
    
    private static class TripleCompare implements Comparator{

        public int compare(Object arg0, Object arg1) {
            if(arg0 instanceof Triple && arg1 instanceof Triple){
                if(((Triple)arg0).subject().toString().equals(((Triple)arg1).subject().toString()) &&
                    ((Triple)arg0).predicate().toString().equals(((Triple)arg1).predicate().toString()) &&
                       ((Triple)arg0).object().toString().equals(((Triple)arg1).object().toString())) {
                            return 0;
                } 
            }
            return -1;
        }
        
    }

    static Collection<Triple> removeAll(Collection<Triple> c1, Collection<Triple> c2){
        TripleCompare comparator = new TripleCompare();
        Iterator<Triple> it = c1.iterator();
        while(it.hasNext()){
            Triple t1 = it.next();
            for(Triple t2: c2){
                if(comparator.compare(t1, t2) == 0){
                    it.remove();
                }
            }
        }

        return c1;
    }
    
    static Collection<Triple> retainAll(Collection<Triple> c1, Collection<Triple> c2){
        TripleCompare comparator = new TripleCompare();
        for(Triple t1: c1){
            for(Triple t2: c2){
                if(comparator.compare(t1, t2) != 0){
                    c1.remove(t1);
                }
            }
        }

        return c1;
    }


}
