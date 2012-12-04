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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.index.common.Multikey;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An implementation of ModelIndexer using Multimaps to store triples.
 * 
 * <p>All possible combinations of Subject/Predicate/Object (SPO) queries corresponds
 * to a different map, having the given arguments of the query as key and a {@code List}
 * of matching triples as value.</p>
 * 
 * <p>This means that for each triple seven references are saved. Consequently,
 * this implementation offers a very fast SPO queries response. However, it also has
 * high memory requirements.</p>
 * 
 * <p>Named graphs are handled using {@code NamedGraphIndexerImpl}.</p>
 * 
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
class MultimapsModelIndexer extends AbstractModelIndexer {
    private Multimap<ObjectNode, Triple> subjects;
    private Multimap<Resource, Triple> predicates;
    private Multimap<RdfNode, Triple> objects;
    
    private Multimap<Multikey, Triple> subjectPredicates;
    private Multimap<Multikey, Triple> predicateObjects;
    private Multimap<Multikey, Triple> subjectObjects;
    
    private Map<Multikey, Triple> subjectPredicateObjects;
    
    public MultimapsModelIndexer() {
        super(new NamedGraphIndexerImpl());
        
        subjects = LinkedListMultimap.create();
        predicates = LinkedListMultimap.create();
        objects = LinkedListMultimap.create();
        
        subjectPredicates = LinkedListMultimap.create();
        subjectObjects = LinkedListMultimap.create();
        predicateObjects = LinkedListMultimap.create();
        
        subjectPredicateObjects = new HashMap<Multikey, Triple>();
    }
    
    public boolean containsTriple(Triple triple) {
        return subjectPredicateObjects.containsKey(
                new Multikey(triple.subject(), triple.predicate(), triple.object()));
    }

    
    public Iterator<Triple> findTriples(final Resource g, final ObjectNode s, final Resource p,
            final RdfNode o) {
        final Collection<Triple> triples = findTriples(s, p, o);
        if (g == null) {
            return triples.iterator();
        }
        // g != null
        final Collection<Triple> ngTriples = graphIndexer.get(g);
        return AbstractModelIndexer.findTriples(triples, ngTriples, g, s, p, o);
    }
    
    private Collection<Triple> findTriples(ObjectNode subject, Resource predicate, RdfNode object) {
        int count = 0;
        if (subject != null) {
            count |= 4;
        }
        if (predicate != null) {
            count |= 2;
        }
        if (object != null) {
            count |= 1;
        }
        
        Collection<Triple> triples;
        switch (count) {
            case 0: // s?, p?, o?
                triples = subjectPredicateObjects.values();
                break;
            case 1: // s?, p?, o
                triples = objects.get(object);
                break;
            case 2: // s?, p,  o?
                triples = predicates.get(predicate);
                break;
            case 3: // s?, p,  o
                triples = predicateObjects.get(new Multikey(predicate, object));
                break;
            case 4: // s,  p?, o?
                triples = subjects.get(subject);
                break;
            case 5: // s,  p?, o
                triples = subjectObjects.get(new Multikey(subject, object));
                break;
            case 6: // s,  p,  o?
                triples = subjectPredicates.get(new Multikey(subject, predicate));
                break;
            default: //case 7: // s,  p,  o
                Triple triple = subjectPredicateObjects.get(new Multikey(subject, predicate, object));
                triples = triple == null ? EmptyTriplesList.instance() : singletonList  (triple);
        }
        return triples == null ? EmptyTriplesList.instance() : triples;
    }
    
    /* Cannot use Collections.singletonList() because remove method of the
     * iterator should be implemented.
     * Remove method now deletes the triple from the subjectPredicateObjects 
     * hashMap
     */
    private Collection<Triple> singletonList(final Triple triple) {
        return new AbstractList<Triple>() {
            @Override
            public Triple get(int index) {
                switch (index) {
                    case 0:
                        return triple;
                    default:
                        throw new IndexOutOfBoundsException("Index: " + index);
                }
            }

            @Override
            public int size() {
                return 1;
            }
            
            @Override
            public Iterator<Triple> iterator() {
                return new Iterator<Triple>() {
                    boolean returned = false;
                    
                    public boolean hasNext() {
                        return !returned;
                    }

                    public Triple next() {
                        if (returned) {
                            throw new NoSuchElementException();
                        }
                        returned = true;
                        return triple;
                    }

                    public void remove() {
                        subjectPredicateObjects.remove(triple);
                    }
                };
            }
        };
    }

    /**
     * {@inheritDoc }
     */
    public void add(Resource namedGraph, Triple triple) {
        ObjectNode s = triple.subject();
        Resource p = triple.predicate();
        RdfNode o = triple.object();
        
        graphIndexer.add(namedGraph, triple);
        
        subjectPredicateObjects.put(new Multikey(s, p, o), triple);
        
        subjects.put(s, triple);
        predicates.put(p, triple);
        objects.put(o, triple);
        
        subjectPredicates.put(new Multikey(s, p), triple);
        subjectObjects.put(new Multikey(s, o), triple);
        predicateObjects.put(new Multikey(p, o), triple);
    }

    public void addInNamedGraph(Resource namedGraph, Triple triple) {
        graphIndexer.add(namedGraph, triple);
    }
    
    private void delete(Triple triple, boolean tryDeleteFromAll) {
        ObjectNode s = triple.subject();
        Resource p = triple.predicate();
        RdfNode o = triple.object();
        
        if (subjectPredicateObjects.remove(new Multikey(s, p, o)) == null  &&
                !tryDeleteFromAll) {
            return;
        }
        
        subjects.remove(s, triple);
        predicates.remove(p, triple);
        objects.remove(o, triple);
        
        subjectPredicates.remove(new Multikey(s, p), triple);
        subjectObjects.remove(new Multikey(s, o), triple);
        predicateObjects.remove(new Multikey(p, o), triple);
        
        graphIndexer.removeTriple(triple);
    }
    
    public void delete(Triple triple) {
        delete(triple, false);
    }

    public void deleteTriples(Resource g, ObjectNode s, Resource p, RdfNode o,
            TripleDeletionListener listener) {
        if (g != null) {
            deleteFromNamedGraph(g, s, p, o, listener);
            return;
        }
        // g == null
        for (Iterator<Triple> triples = findTriples(s, p, o).iterator();
                triples.hasNext(); ) {
            Triple triple = triples.next();
            triples.remove();
            delete(triple, false);
            listener.tripleDeleted(triple);
        }
    }
    
    public int tripleCount() {
        return subjectPredicateObjects.size();
    }
}
