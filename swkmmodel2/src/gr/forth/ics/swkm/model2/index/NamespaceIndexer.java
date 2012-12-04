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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.event.TypeChange;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.event.RdfNodeListener;
import gr.forth.ics.swkm.model2.index.ModelIndexer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An indexer for namespaces, meant to support {@link ModelIndexer#findInNamespace(RdfType, Uri)}
 * query.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class NamespaceIndexer {
    private final Map<Uri, Multimap<RdfType, Resource>> index = Maps.newHashMap();

    NamespaceIndexer(Model model) {
        model.addRdfNodeListener(new NamespaceIndexUpdater());
    }

    Set<Uri> namespaces() {
        return Collections.unmodifiableSet(index.keySet());
    }

    Iterator<Resource> findInNamespace(RdfType type, Uri namespace) {
        //assuming type is only schema, and namespace does not have a local part
        //the relevant checks have moved to ModelImpl#findSchemaNodes
        Multimap<RdfType, Resource> map = index.get(namespace);
        if (map == null) {
            return Iterators.emptyIterator();
        }
        return Iterators.unmodifiableIterator(map.get(type).iterator());
    }

    private void addEntry(Resource resource) {
        if (!resource.type().isSchema()) {
            return;
        }
        Uri namespace = resource.getUri().getNamespaceUri();
        Multimap<RdfType, Resource> map = index.get(namespace);
        if (map == null) {
            index.put(namespace, map = HashMultimap.create());
        }
        map.put(resource.type(), resource);
    }

    private void removeEntry(Resource resource, RdfType type) {
        if (!type.isSchema()) {
            return;
        }
        Uri namespace = resource.getUri().getNamespaceUri();
        Multimap<RdfType, Resource> map = index.get(namespace);
        if (map == null) {
            return;
        }
        map.remove(type, resource);
        if (map.isEmpty()) {
            index.remove(namespace);
        }
    }

    private class NamespaceIndexUpdater implements RdfNodeListener {
        public void onNodeAddition(RdfNode node) {
            if (!node.isResource()) {
                return;
            }
            addEntry((Resource)node);
        }

        public void onNodeDeletion(RdfNode node) {
            if (!node.isResource()) {
                return;
            }
            removeEntry((Resource)node, node.type());
        }

        public void onTypeChange(TypeChange change) {
            RdfNode node = change.node();
            if (!node.isResource()) {
                return;
            }
            Resource resource = (Resource)node;

            removeEntry(resource, change.oldType());
            addEntry(resource);
        }
    }
    
    
    void clear() {
        index.clear();
    }
}
