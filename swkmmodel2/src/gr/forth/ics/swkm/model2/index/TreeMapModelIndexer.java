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

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Triples;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.views.ClassView;
import gr.forth.ics.swkm.model2.views.ContainerView;
import gr.forth.ics.swkm.model2.views.IndividualView;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.views.MetaclassView;
import gr.forth.ics.swkm.model2.views.MetapropertyView;
import gr.forth.ics.swkm.model2.views.PropertyView;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
class TreeMapModelIndexer extends AbstractModelIndexer {
    private TreeSet<Triple> spo;
    private TreeMultiMap po;
    private TreeMultiMap os;
    
    public TreeMapModelIndexer() {
        super(new NamedGraphIndexerImpl());
        
        spo = new TreeSet<Triple>(TriplesComparator.instance);
        po = new TreeMultiMap(new TriplesComparator() {
            @Override
            public int compare(Triple t1, Triple t2) {
                int dif = compare(t1.predicate(), t2.predicate());
                if (dif != 0) {
                    return dif;
                }
                return compare(t1.object(), t2.object());
            }
        });
        os = new TreeMultiMap(new TriplesComparator() {
            @Override
            public int compare(Triple t1, Triple t2) {
                int dif = compare(t1.object(), t2.object());
                if (dif != 0) {
                    return dif;
                }
                return compare(t1.subject(), t2.subject());
            }
        });
    }

    private Iterable<Triple> findTriplesFromSPO(ObjectNode subject, Resource predicate, RdfNode object) {
        if (subject == null) { // ?s, ?p, ?o
            return spo.descendingSet();
        }
        QueryTriple fromElement = new QueryTriple(subject, predicate, object);
        QueryTriple toElement = new QueryTriple(subject, predicate, object);
        if (object == null) {
            // s, ?p, ?o  |  s, p, ?o
            fromElement.setObject(ResourceAny.minimum);
            toElement.setObject(ResourceAny.maximum); 
            if (predicate == null) { // s, ?p, ?o
                fromElement.setPredicate(ResourceAny.minimum);
                toElement.setPredicate(ResourceAny.maximum);
            }
        }
        // if it weren't inclusive it wouldn't work for "s, p, o"
        return spo.subSet(fromElement, true, toElement, true);
    }
    
    private Iterable<Triple> findTriplesFromPO(Resource predicate) {
        return po.subSet(poQueryTriple(predicate, ResourceAny.minimum),
                poQueryTriple(predicate, ResourceAny.maximum));
    }
    
    private Iterable<Triple> findTriplesFromOS(RdfNode object) {
        return os.subSet(osQueryTriple(object, ResourceAny.minimum),
                osQueryTriple(object, ResourceAny.maximum));
    }
    
    public Iterable<Triple> findTriples(ObjectNode s, Resource p,
            RdfNode o) {
        int count = 0;
        if (s != null) {
            count |= 4;
        }
        if (p != null) {
            count |= 2;
        }
        if (o != null) {
            count |= 1;
        }
        
        switch (count) {
            case 0: // ?s, ?p, ?o
                return spo;
            case 4: case 6: case 7: // s, ?p, ?o  |  s, p, ?o  |  s, p, o  
                return findTriplesFromSPO(s, p, o);
            case 2: // ?s, p, ?o
                return findTriplesFromPO(p);
            case 3: // ?s, p, o
                return po.get(new QueryTriple(ResourceAny.minimum, p, o));
            case 1: // ?s, ?p, o
                return findTriplesFromOS(o);
            default: // case 5: -- s, ?p, o
                return os.get(new QueryTriple(s, ResourceAny.minimum, o));
        }
    }
    
    public Iterator<Triple> findTriples(Resource namedGraph, ObjectNode subject,
            Resource predicate, RdfNode object) {
        Iterable<Triple> triples = findTriples(subject, predicate, object);
        if (triples == null) {
            return Iterators.emptyIterator();
        }
        if (namedGraph == null) {
            return triples.iterator();
        }
        // namedGraph != null
        if (triples == spo) {
            // all the triples are selected
            return graphIndexer.get(namedGraph).iterator();
        }
        return AbstractModelIndexer.findTriples(triples, namedGraph);
    }

    public boolean containsTriple(Triple triple) {
        return spo.contains(triple);
    }
    
    public void add(Resource namedGraph, Triple triple) {
        graphIndexer.add(namedGraph, triple);
        
        spo.add(triple);
        po.put(poQueryTriple(triple.predicate(), triple.object()), triple);
        os.put(osQueryTriple(triple.object(), triple.subject()), triple);
    }

    public void addInNamedGraph(Resource namedGraph, Triple triple) {
        graphIndexer.add(namedGraph, triple);
    }
    
    private QueryTriple poQueryTriple(Resource predicate, RdfNode object) {
        return new QueryTriple(ResourceAny.minimum, predicate, object);
    }
    
    private QueryTriple osQueryTriple(RdfNode object, ObjectNode subject) {
        return new QueryTriple(subject, ResourceAny.minimum, object);
    }

    public ObjectViewSupport objectViewSupport(Model model) {
        return new TripleBasedObjectViewSupport(this, model);
    }

    private void delete(Triple triple, boolean tryDeleteFromAll) {
        if (!spo.remove(triple) && !tryDeleteFromAll) {
            return;
        }
        po.remove(poQueryTriple(triple.predicate(), triple.object()), triple);
        os.remove(osQueryTriple(triple.object(), triple.subject()), triple);

        graphIndexer.removeTriple(triple);
    }
    
    public void delete(Triple triple) {
        delete(triple, false);
    }
    
    public void deleteTriples(Resource g, ObjectNode s,
            Resource p, RdfNode o, TripleDeletionListener listener) {
        if (g != null) {
            deleteFromNamedGraph(g, s, p, o, listener);
            return;
        }
        // g == null
        for (Iterator<Triple> triples = findTriples(s, p, o).iterator();
                triples.hasNext(); ) {
            Triple triple = triples.next();
            triples.remove();
            delete(triple, true);
            listener.tripleDeleted(triple);
        }
    }
    
    public int tripleCount() {
        return spo.size();
    }
}

class TreeMultiMap {
    private final TreeMap<QueryTriple, Collection<Triple>> map;
    
    TreeMultiMap() {
        this(TriplesComparator.instance);
    }
    
    TreeMultiMap(Comparator<Triple> comparator) {
        map = new TreeMap<QueryTriple, Collection<Triple>>(comparator);
    }

    int size() {
        return map.size();
    }

    boolean isEmpty() {
        return map.isEmpty();
    }

    boolean containsKey(QueryTriple key) {
        return map.containsKey(key);
    }

    void put(QueryTriple key, Triple value) {
        Collection<Triple> collection = map.get(key);
        if (collection == null) {
            collection = new HashSet<Triple>();
            map.put(key, collection);
        }
        collection.add(value);
    }

    public void clear() {
        map.clear();
    }

    public Collection<Triple> get(QueryTriple key) {
        return map.get(key);
    }

    Set<QueryTriple> keySet() {
        return map.keySet();
    }
    
    boolean remove(QueryTriple key, Triple value) {
        Collection<Triple> collection = map.get(key);
        if (collection == null) {
            return false;
        }
        return collection.remove(value);
    }
    
    Iterable<Triple> subSet(QueryTriple fromElement, QueryTriple toElement) {
        Collection<Collection<Triple>> results = map.subMap(fromElement, toElement).values();
        return Iterables.concat(results);
    }
}

class TriplesComparator implements Comparator<Triple> {
    static final TriplesComparator instance = new TriplesComparator() {};
    
    TriplesComparator() {}
    
    public int compare(Triple t1, Triple t2) {
        int dif = compare(t1.subject(), t2.subject());
        if (dif != 0) {
            return dif;
        }
        dif = compare(t1.predicate(), t2.predicate());
        if (dif != 0) {
            return dif;
        }
        return compare(t1.object(), t2.object());
    }

    protected int compare(RdfNode n1, RdfNode n2) {
        if (n1 == n2) {
            return 0;
        }
        int h1 = n1.hashCode();
        int h2 = n2.hashCode();
        // case of a hash collision
        if (h1 == h2) {
            return n1.toString().compareTo(n2.toString());
        }
        return h1 > h2 ? 1 : -1;
    }
}

class QueryTriple implements Triple {
    private ObjectNode subject;
    private Resource predicate;
    private RdfNode object;
    
    QueryTriple(ObjectNode subject, Resource predicate, RdfNode object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    void setSubject(ObjectNode subject) {
        this.subject = subject;
    }
    
    void setPredicate(Resource predicate) {
        this.predicate = predicate;
    }
    
    void setObject(RdfNode object) {
        this.object = object;
    }
    
    public ObjectNode subject() {
        return subject;
    }

    public Resource predicate() {
        return predicate;
    }

    public RdfNode object() {
        return object;
    }

    public Iterable<? extends ObjectNode> nonLiterals() {
        throw new UnsupportedOperationException("Not supported in QueryTriple.");
    }

    public Iterable<RdfNode> nodes() {
        throw new UnsupportedOperationException("Not supported in QueryTriple.");
    }

    public Set<Resource> graphs() {
        throw new UnsupportedOperationException("Not supported in QueryTriple.");
    }

    public boolean delete() {
        throw new UnsupportedOperationException("Not supported in QueryTriple.");
    }

    public String toSimpleString() {
        return "<" + subject + " " + predicate + " " + object + ">";
    }

    public Model owner() {
        throw new UnsupportedOperationException("Not supported in QueryTriple.");
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(object instanceof Triple)) {
            return false;
        }
        Triple triple = (Triple) other;
        return subject.equals(triple.subject()) &&
                predicate.equals(triple.predicate()) &&
                object.equals(triple.object());
    }

    @Override
    public int hashCode() {
        return 7 + 961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode();
    }
}
abstract class ResourceAny implements Resource {
    static final ResourceAny minimum = new ResourceAny() {
        @Override
        public int hashCode() {
            return Integer.MIN_VALUE;
        }
    };
    
    static final ResourceAny maximum = new ResourceAny() {
        @Override
        public int hashCode() {
            return Integer.MAX_VALUE;
        }
    };
    
    private ResourceAny() {}

    public ContainerView asContainer() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Uri getUri() {
        throw new UnsupportedOperationException();
    }

    public Resource mappedTo(Model model) {
        throw new UnsupportedOperationException();
    }

    public String getIdentifier() {
        throw new UnsupportedOperationException();
    }

    public boolean isResource() {
        throw new UnsupportedOperationException();
    }

    public boolean isLiteral() {
        throw new UnsupportedOperationException();
    }

    public boolean isBlankNode() {
        throw new UnsupportedOperationException();
    }

    public boolean isObjectNode() {
        throw new UnsupportedOperationException();
    }

    public boolean is(Uri uri) {
        throw new UnsupportedOperationException();
    }

    public RdfType type() {
        throw new UnsupportedOperationException();
    }

    public boolean hasConstantType() {
        throw new UnsupportedOperationException();
    }

    public Index getIndex() {
        throw new UnsupportedOperationException();
    }

    public Triples subjectTriples() {
        throw new UnsupportedOperationException();
    }

    public Triples predicateTriples() {
        throw new UnsupportedOperationException();
    }

    public Triples objectTriples() {
        throw new UnsupportedOperationException();
    }

    public ClassView asClass() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public PropertyView asProperty() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public MetaclassView asMetaclass() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public MetapropertyView asMetaproperty() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public IndividualView asIndividual() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public Inheritable asInheritable() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public Model owner() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract int hashCode();
    
    @Override
    public boolean equals(Object other) {
        return other instanceof RdfNode;
    }
    
    public boolean equals(RdfNode other) {
        return true;
    }

    public boolean hasTriples() {
        throw new UnsupportedOperationException();
    }
}