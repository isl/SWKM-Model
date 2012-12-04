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

import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Triple;
import java.util.Set;

/**
 * Delta utility methods
 * @author Papavasileiou Vicky
 */
public class DeltaUtils {

    private DeltaUtils(){
    }

    /**
     * Creates a new Delta object from two models containing the added and deleted triples.
     * @param addedTriples The model that contains the added triples
     * @param deletedTriples The models that contains the deleted triples
     * @return The delta created by the triples contained in the models given
     */
    public static Delta createfromModels(Model addedTriples, Model deletedTriples){
        Delta delta = new TripleDelta();
        for(Triple t: addedTriples.triples().fetch()){
            ((TripleDelta)delta).insertAddedTriple(t.subject(), t.predicate(), t.object());
        }
        for(Triple t: deletedTriples.triples().fetch()){
            ((TripleDelta)delta).insertDeletedTriple(t.subject(), t.predicate(), t.object());
        }
        return delta;
    }

    /**
     * Creates a new Delta object from two sets containing the added and deleted triples.
     * Since a triple is always related to an owner model, the triples are first read
     * into two models.
     * @param addedTriples The added triples
     * @param deletedTriples The deleted triples
     * @return The delta created by two seta of triples
     */
    public static Delta createFromSets(Set<Triple> addedTriples, Set<Triple> deletedTriples){
        Delta delta = new TripleDelta();
        for(Triple t: addedTriples){
            ((TripleDelta)delta).insertAddedTriple(t.subject(), t.predicate(), t.object());
        }
        for(Triple t: deletedTriples){
            ((TripleDelta)delta).insertDeletedTriple(t.subject(), t.predicate(), t.object());
        }
        return delta;
    }

    /**
     * Applies a delta to the given model.
     * Removes from the model all the deleted triples of the delta and adds all
     * the added triples of the delta.
     * @param delta The delta that will be applied
     * @param m The model on which the delta will be applied
     */
    public static void applyToModel(Delta delta, Model m){
        assert(delta != null);
        assert(m != null);
        for(Triple t: delta.getAddedSet()){
            m.add().s(t.subject().mappedTo(m)).p(t.predicate().mappedTo(m)).o(t.object().mappedTo(m));
        }
        for(Triple t: delta.getDeletedSet()){
            if(m.triples().
                    s(t.subject().mappedTo(m)).
                    p(t.predicate().mappedTo(m)).
                    o(t.object().mappedTo(m)).
                    fetch().iterator().hasNext()){
                Triple del = m.triples().
                        s(t.subject().mappedTo(m)).
                        p(t.predicate().mappedTo(m)).
                        o(t.object().mappedTo(m)).
                        fetch().iterator().next();
                m.delete(del);
            }
        }
    }

    /**
     * Checks whether two deltas are compatible.
     * Two deltas are compatible if the set of added and deleted triples are disjoint.
     * @param delta1 The first delta
     * @param delta The second delta
     * @return true if the two given deltas are compatible else false
     */
    public boolean areCompatible(Delta delta1,Delta delta2){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Computes the union of the two given deltas.
     * Creates a new delta that contains as the set of added triples the union of
     * the added triples of the two deltas and as the set of deleted triples the union
     * of the deleted triples of the two deltas.
     * @param delta1 The first delta
     * @param delta2 The second delta
     * @return A new delta that is the union of the two deltas
     */
    public static Delta union(Delta delta1, Delta delta2){
        assert(delta1 != null);
        assert(delta2 != null);
        Model result = ModelBuilder.newSparse().withoutTypeInference().build();
        result.add().g("http://delta1_added");
        result.add().g("http://delta1_deleted");
        result.add().g("http://delta2_added");
        result.add().g("http://delta2_deleted");
        for(Triple t: delta1.getAddedSet()){
            result.add().g("http://delta1_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta1.getDeletedSet()){
            result.add().g("http://delta1_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getAddedSet()){
            result.add().g("http://delta2_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getDeletedSet()){
            result.add().g("http://delta2_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        Set<Triple> added = Sets.newHashSet(result.triples().g("http://delta1_added").fetch());
        added.addAll(Sets.newHashSet(result.triples().g("http://delta2_added").fetch()));
        Set<Triple> deleted = Sets.newHashSet(result.triples().g("http://delta1_deleted").fetch());
        deleted.addAll(Sets.newHashSet(result.triples().g("http://delta2_deleted").fetch()));

        return new TripleDelta(added,deleted);
    }

    /**
     * Computes the intersection of the two given deltas.
     * Creates a new delta that contains as the set of added triples the intersection of
     * the added triples of the two deltas and as the set of deleted triples the intersection
     * of the deleted triples of the two deltas.
     * @param delta1 The first delta
     * @param delta2 The second delta
     * @return A new delta that is the intersection of the two deltas
     */
    public static Delta intersection(Delta delta1, Delta delta2) {
        assert(delta1 != null);
        assert(delta2 != null);
        Model result = ModelBuilder.newSparse().withoutTypeInference().build();
        result.add().g("http://delta1_added");
        result.add().g("http://delta1_deleted");
        result.add().g("http://delta2_added");
        result.add().g("http://delta2_deleted");
        for(Triple t: delta1.getAddedSet()){
            result.add().g("http://delta1_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta1.getDeletedSet()){
            result.add().g("http://delta1_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getAddedSet()){
            result.add().g("http://delta2_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getDeletedSet()){
            result.add().g("http://delta2_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }

        Set<Triple> added = Sets.newHashSet(result.triples().g("http://delta1_added").fetch());
        added.retainAll(Sets.newHashSet(result.triples().g("http://delta2_added").fetch()));
        Set<Triple> deleted = Sets.newHashSet(result.triples().g("http://delta1_deleted").fetch());
        deleted.retainAll(Sets.newHashSet(result.triples().g("http://delta2_deleted").fetch()));

        return new TripleDelta(added,deleted);
    }

    /**
     * Computes the difference of the two given deltas.
     * Creates a new delta that contains as the set of added triples the difference of
     * the added triples of the two deltas and as the set of deleted triples the difference
     * of the deleted triples of the two deltas.
     * @param delta1 The first delta
     * @param delta2 The second delta
     * @return A new delta that is the difference of the two deltas
     */
    public static Delta difference(Delta delta1, Delta delta2) {
        assert(delta1 != null);
        assert(delta2 != null);
        Model result = ModelBuilder.newSparse().withoutTypeInference().build();
        result.add().g("http://delta1_added");
        result.add().g("http://delta1_deleted");
        result.add().g("http://delta2_added");
        result.add().g("http://delta2_deleted");
        for(Triple t: delta1.getAddedSet()){
            result.add().g("http://delta1_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta1.getDeletedSet()){
            result.add().g("http://delta1_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getAddedSet()){
            result.add().g("http://delta2_added").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }
        for(Triple t: delta2.getDeletedSet()){
            result.add().g("http://delta2_deleted").
                    s(t.subject().mappedTo(result)).
                    p(t.predicate().mappedTo(result)).
                    o(t.object().mappedTo(result));
        }

        Set<Triple> added = Sets.newHashSet(result.triples().g("http://delta1_added").fetch());
        added.removeAll(Sets.newHashSet(result.triples().g("http://delta2_added").fetch()));
        Set<Triple> deleted = Sets.newHashSet(result.triples().g("http://delta1_deleted").fetch());
        deleted.removeAll(Sets.newHashSet(result.triples().g("http://delta2_deleted").fetch()));

        return new TripleDelta(added,deleted);
    }


}
