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
import gr.forth.ics.rdfsuite.services.util.Args;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Model.QueryBuilder;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author papavas
 */
public class TripleDelta implements Delta{
    private Model delta;
    private static final Uri added = Uri.parse("http://addedTriples#");
    private static final Uri deleted = Uri.parse("http://deletedTriples#");

    /**
     * Creates a new delta that consists of textual triples.
     */
    public TripleDelta() {
        delta = ModelBuilder.newSparse().build();
        delta.add().g(added);
        delta.add().g(deleted);
    }

    /**
     * Creates a new delta that constists of the specified added and deleted
     * textual triples.
     * @param added The added triples of the delta
     * @param deleted The deleted triples of the delta
     */
    public TripleDelta(Set<Triple> addedTriples, Set<Triple> deletedTriples){
        Args.notNull(addedTriples,deletedTriples);
        delta = ModelBuilder.newSparse().build();
        delta.add().g(added);
        delta.add().g(deleted);
        for(Triple t: addedTriples){
            delta.add().g(added).
                    s(t.subject().mappedTo(delta)).
                    p(t.predicate().mappedTo(delta))
                    .o(t.object().mappedTo(delta));
        }
        for(Triple t: deletedTriples){
            delta.add().g(deleted).
                    s(t.subject().mappedTo(delta)).
                    p(t.predicate().mappedTo(delta)).
                    o(t.object().mappedTo(delta));
        }
    }

    public Set<Triple> getAddedSet() {
        return Sets.newHashSet(delta.triples().g(added).fetch());
    }

    public Set<Triple> getDeletedSet() {
        return Sets.newHashSet(delta.triples().g(deleted).fetch());
    }

    public Model getTripleDelta(){
        return delta;
    }

    public void insertAddedTriples(Set<Triple> toAdd) {
        for(Triple t: toAdd){
            delta.add().g(added).
                    s(t.subject().mappedTo(delta)).
                    p(t.predicate().mappedTo(delta)).
                    o(t.object().mappedTo(delta));
        }
    }

    public void insertDeletedTriples(Set<Triple> toDelete) {
        for(Triple t: toDelete){
            delta.add().g(deleted).
                    s(t.subject().mappedTo(delta)).
                    p(t.predicate().mappedTo(delta)).
                    o(t.object().mappedTo(delta));
        }
    }


    public void insertAddedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        delta.add().g(added).
                s(subject.mappedTo(delta)).
                p(predicate.mappedTo(delta)).
                o(object.mappedTo(delta));
    }


    public void insertDeletedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        delta.add().g(deleted).
                s(subject.mappedTo(delta)).
                p(predicate.mappedTo(delta)).
                o(object.mappedTo(delta));
    }

    public boolean removeAddedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
            return delta.triples().g(added).
                    s(subject.mappedTo(delta)).
                    p(predicate.mappedTo(delta)).
                    o(object.mappedTo(delta)).
                    delete();        
    }

    public boolean removeDeletedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
            return delta.triples().g(deleted).
                    s(subject.mappedTo(delta)).
                    p(predicate.mappedTo(delta)).
                    o(object.mappedTo(delta)).
                    delete();        
    }

    public boolean removeAddedTriple(ObjectNode subject, Uri predicate, RdfNode object) {
        return delta.triples().g(added).
                s(subject.mappedTo(delta)).
                p(predicate).
                o(object.mappedTo(delta)).
                delete();
    }

    public boolean removeDeletedTriple(ObjectNode subject, Uri predicate, RdfNode object) {
        return delta.triples().g(deleted).
                s(subject.mappedTo(delta)).
                p(predicate).
                o(object.mappedTo(delta)).
                delete();
    }

    public boolean removeAddedTriple(Uri subject, Uri predicate, Uri object) {
        return delta.triples().g(added).
                s(delta.mapResource(subject)).
                p(delta.mapResource(predicate)).
                o(delta.mapResource(object)).
                delete();
        
    }

    public boolean removeDeletedTriple(Uri subject, Uri predicate, Uri object) {
        return delta.triples().g(deleted).
                s(delta.mapResource(subject)).
                p(delta.mapResource(predicate)).
                o(delta.mapResource(object)).
                delete();        
    }

    public void removeDeletedTriple(Triple t) {
        delta.delete(t);
    }

    public boolean isAddedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        return delta.triples().g(added).
                s(subject.mappedTo(delta)).
                p(predicate.mappedTo(delta)).
                o(object.mappedTo(delta)).fetch().iterator().hasNext();
    }

    public boolean isDeletedTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        return delta.triples().g(deleted).
                s(subject.mappedTo(delta)).
                p(predicate.mappedTo(delta)).
                o(object.mappedTo(delta)).fetch().iterator().hasNext();
    }

    public boolean isAddedTriple(Uri subject, Uri predicate, Uri object) {
        return delta.triples().g(added).s(subject).p(predicate).o(object).fetch().iterator().hasNext();
    }

    public boolean isDeletedTriple(Uri subject, Uri predicate, Uri object) {
        return delta.triples().g(deleted).s(subject).p(predicate).o(object).fetch().iterator().hasNext();
    }

    public QueryBuilder getDeletedTriples(){
        return delta.triples().g(deleted);
    }

    public QueryBuilder getAddedTriples(){
        return delta.triples().g(added);
    }

    /**
     *
     * @return String representation of delta.
     */
    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append("Added Triples:\n");
        Iterator<Triple> it1 = delta.triples().g(added).fetch().iterator();
        while (it1.hasNext())
        {
            Triple next = it1.next();
            result.append(next.toSimpleString() + "\n");
        }
        result.append("\nDeleted Triples:\n");
        Iterator<Triple> it2 = delta.triples().g(deleted).fetch().iterator();
        while (it2.hasNext())
        {
            Triple next = it2.next();
            result.append(next.toSimpleString() + "\n");
        }
        return result.toString();
    }

}
