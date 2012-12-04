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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import gr.forth.ics.swkm.model2.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.event.TypeChange;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A ModelIndexer implementation, used only for testing.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class HorizontalModelIndexer implements ModelIndexer {
    private final Set<Triple> triples = Sets.newLinkedHashSet();
    private Model model;
    private ModelView modelView;

    public void add(Resource namedGraph, Triple triple) {
        triples.add(triple);
    }

    public void addInNamedGraph(Resource namedGraph, Triple triple) {
    }

    public Index indexFor(RdfNode node, TypeChange typeChange) {
        return new IndexImpl(node);
    }

    public void delete(Triple triple) {
        triples.remove(triple);
    }

    public void removeTripleFromNamedGraph(Resource namedGraph, Triple triple) {
        //do nothing
    }
    
    public ObjectViewSupport objectViewSupport() {
        return new TripleBasedObjectViewSupport(this, model);
    }
    
    public boolean containsTriple(Triple triple) {
        return triples.contains(triple);
    }
    
    public Iterator<Triple> findTriples(Resource g, ObjectNode s, Resource p, RdfNode o) {
        List<Triple> results = Lists.newArrayList();
        for (Triple triple : triples) {
            if ((g == null || triple.graphs().contains(g))
                    && (s == null || s == triple.subject())
                    && (p == null || p == triple.predicate())
                    && (o == null || o == triple.object())) {
                results.add(triple);
            }
        }
        return results.iterator();
    }

    public void deleteTriples(Resource g, ObjectNode s, Resource p, RdfNode o, TripleDeletionListener listener) {
        for (Iterator<Triple> i = triples.iterator(); i.hasNext(); ) {
            Triple triple = i.next();
            if ((g == null || triple.graphs().contains(g))
                    && (s == null || s == triple.subject())
                    && (p == null || p == triple.predicate())
                    && (o == null || o == triple.object())) {
                if (g != null) {
                    listener.tripleRemovedFromNamedGraph(g, triple);
                } else {
                    listener.tripleDeleted(triple);
                    i.remove();
                }
            }
        }
    }

    public Iterator<RdfNode> find(RdfType type) {
        Set<RdfNode> results = Sets.newHashSet();
        for (Triple triple : triples) {
            if (triple.subject().type() == type) {
                results.add(triple.subject());
            }
            if (triple.predicate().type() == type) {
                results.add(triple.predicate());
            }
            if (triple.object().type() == type) {
                results.add(triple.object());
            }
            if (type == RdfType.NAMED_GRAPH) {
                results.addAll(triple.graphs());
            }
        }
        return results.iterator();
    }

    public void setModelView(ModelView modelView) {
        this.modelView = modelView;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }

    private static final Function<Resource, Uri> namespaceExtractor = new Function<Resource, Uri> () {
        public Uri apply(Resource resource) {
            return resource.getUri().getNamespaceUri();
        }
    };

    private static final Function<Triple, Iterable<Resource>> tripleResources = new Function<Triple, Iterable<Resource>>() {
        public Iterable<Resource> apply(Triple triple) {
            List<Resource> list = Lists.newArrayListWithExpectedSize(3);
            for (RdfNode n : triple.nodes()) {
                if (n instanceof Resource) {
                    list.add((Resource) n);
                }
            }
            return list;
        }
    };

    private Iterable<Resource> allResources() {
        return Iterables.concat(Iterables.transform(triples, tripleResources));
    }

    public Iterator<Resource> findInNamespace(final RdfType type, final Uri namespace) {
        if (!type.isSchema()) {
            throw new IllegalArgumentException("Requested type: " + type + " is not schema");
        }
        if (namespace.getLocalName().length() > 0) {
            throw new IllegalArgumentException("Uri: '" + namespace + "' is not purely a namespace; it also contains a local part");
        }
        return Iterators.filter(allResources().iterator(), new Predicate<Resource>() {
            public boolean apply(Resource resource) {
                return resource.type() == type && resource.getUri().hasEqualNamespace(namespace);
            }
        });
    }

    public Set<Uri> namespaces() {
        return Sets.newHashSet(Iterables.transform(
            Iterables.filter(allResources(), new Predicate<Resource>() {
                public boolean apply(Resource resource) {
                    return resource.type().isSchema();
                }
        }), namespaceExtractor));
    }

    private static class IndexImpl implements Index {
        final RdfNode node;

        IndexImpl(RdfNode node) {
            this.node = node;
        }

        public RdfNode getNode() {
            return node;
        }
    }
    
    public int tripleCount() {
        return triples.size();
    }
}