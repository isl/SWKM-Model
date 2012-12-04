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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import gr.forth.ics.swkm.model2.index.EmptyTriplesList;
import java.util.Collections;
import java.util.Iterator;

/**
 * An iterable over triples. Apart from iterating over Triple instances, it can additionally
 * return iterators over the subjects, predicates or objects of them, which is slightly
 * more convenient than iterating the triples and extracting the desired triple part manually.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Triples implements Iterable<Triple> {
    private static final Triples empty = new Triples(EmptyTriplesList.instance());
    
    private final Iterable<Triple> triples;
    
    /**
     * Creates a Triples instance that will used the specified iterable as a source of triples.
     * 
     * @param triples the iterable that this Triples instance will iterate over when requested
     * @throws NullPointerException if {@code triples} is null
     */
    public Triples(Iterable<Triple> triples) {
        if (triples == null) {
            throw new NullPointerException();
        }
        this.triples = triples;
    }

    /**
     * Returns an iterable over the subjects of the triples this Triples instance iterates over.
     * 
     * @return an iterable over the subjects of the triples this Triples instance iterates over
     */
    public Iterable<ObjectNode> subjects() {
        return Iterables.transform(triples, new Function<Triple, ObjectNode>() {
            public ObjectNode apply(Triple triple) {
                return triple.subject();
            }
        });
    }
    
    /**
     * Returns an iterable over the predicates of the triples this Triples instance iterates over.
     * 
     * @return an iterable over the predicates of the triples this Triples instance iterates over
     */
    public Iterable<Resource> predicates() {
        return Iterables.transform(triples, new Function<Triple, Resource>() {
            public Resource apply(Triple triple) {
                return triple.predicate();
            }
        });
    }

    /**
     * Returns an iterable over the objects of the triples this Triples instance iterates over.
     * 
     * @return an iterable over the objects of the triples this Triples instance iterates over
     */
    public Iterable<RdfNode> objects() {
        return Iterables.transform(triples, new Function<Triple, RdfNode>() {
            public RdfNode apply(Triple triple) {
                return triple.object();
            }
        });
    }

    
    /**
     * Returns an iterator over triples.
     * 
     * @return an iterator over triples
     */
    public Iterator<Triple> iterator() {
        return triples.iterator();
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return Joiner.on("\n").join(this);
    }
    
    /**
     * Returns a shared, empty Triples object.
     * 
     * @return a shared, empty Triples object
     */
    public static Triples empty() {
        return empty;
    }

    /**
     * Returns a Triples object containing only the specified Triple
     * 
     * @param triple the sole object to be held in the returned Triples object
     * @return a Triples object containing only the specified Triple
     */
    public static Triples singleton(Triple triple) {
        return new Triples(Collections.singletonList(triple));
    }

    /**
     * Deletes these triples from the model that contains them.
     *
     * @return whether any triples were actually deleted
     */
    public boolean delete() {
        boolean changed = false;
        for (Triple t : this) {
            changed |= t.delete();
        }
        return changed;
    }
}
