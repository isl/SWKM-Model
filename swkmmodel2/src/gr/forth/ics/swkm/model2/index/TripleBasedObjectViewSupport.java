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
import com.google.common.collect.Maps;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Triples;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Map;

/**
 * An default implementation of {@link ObjectViewSupport}. All methods are implemented
 * in terms of triple queries.
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class TripleBasedObjectViewSupport implements ObjectViewSupport {
    protected final ModelIndexer indexer;
    protected final Model model;
    
    public TripleBasedObjectViewSupport(ModelIndexer indexer, Model model) {
        this.indexer = indexer;
        this.model = model;
    }

    public Iterable<? extends RdfNode> findAncestors(Resource schemaResource, Transitively transitively) {
        return findRelatives(schemaResource, transitively, true);
    }

    public Iterable<? extends RdfNode> findDescendants(Resource schemaResource, Transitively transitively) {
        return findRelatives(schemaResource, transitively, false);
    }

    private Iterable<? extends RdfNode> findRelatives(Resource schemaResource,
            Transitively transitively, boolean upward) {
        return transitively.collect(schemaResource,
                createIsaExplorer(getIsaPropertyFor(schemaResource), upward));
    }
    
    private Resource getIsaPropertyFor(Resource schemaResource) {
        Uri transitivePropertyUri = schemaResource.type().isProperty() ?
            RdfSchema.SUBPROPERTYOF : RdfSchema.SUBCLASSOF;
        return model.mapResource(transitivePropertyUri);
    }

    @SuppressWarnings("unchecked") //relatives are all Resources, so its ok to cast from RdfNode to Resource
    protected Iterable<? extends Resource> findDirectRelatives(Resource schemaResource,
            Resource transitiveProperty, boolean upward) {
        return (Iterable)(upward ?
            model.triples().s(schemaResource).p(transitiveProperty).fetch().objects() :
            model.triples().p(transitiveProperty).o(schemaResource).fetch().subjects());
    }
    
    /**
     * Creates a function that explores the given transitive property, upward or downward.
     * "Upward" means that the function maps from a subject to all objects that can
     * be reached from it through the property. The opposite is mapping an object to all
     * subjects that can be reached from it through the property.
     */
    private Function<Resource, Iterable<? extends Resource>> createIsaExplorer(
            final Resource property, final boolean upward) {
        return new Function<Resource, Iterable<? extends Resource>>() {
            public Iterable<? extends Resource> apply(Resource resource) {
                return findDirectRelatives(resource, property, upward);
            }
        };
    }
    
    public Iterable<? extends RdfNode> findClassesOfIndividual(ObjectNode individual) {
        return model.triples().s(individual).p(Rdf.TYPE).fetch().objects();
    }

    public Iterable<? extends RdfNode> findClassesOfMetaclass(Resource metaclass) {
        return model.triples().p(Rdf.TYPE).o(metaclass).fetch().subjects();
    }

    public Iterable<? extends RdfNode> findIndividuals(Resource clazz) {
        return model.triples().p(Rdf.TYPE).o(clazz).fetch().subjects();
    }

    public Iterable<? extends RdfNode> findMetaclassesOfClass(Resource clazz) {
        return model.triples().s(clazz).p(Rdf.TYPE).fetch().objects();
    }

    public Iterable<? extends RdfNode> findMetapropertiesOfProperty(Resource property) {
        return model.triples().s(property).p(Rdf.TYPE).fetch().objects();
    }

    public Iterable<? extends RdfNode> findPropertiesOfMetaproperty(Resource metaproperty) {
        return model.triples().p(Rdf.TYPE).o(metaproperty).fetch().subjects();
    }

    public Iterable<? extends RdfNode> findPropertiesWithDomain(Resource domainClass) {
        return model.triples().p(RdfSchema.DOMAIN).o(domainClass).fetch().subjects();
    }

    public Iterable<? extends RdfNode> findPropertiesWithRange(Resource domainClass) {
        return model.triples().p(RdfSchema.RANGE).o(domainClass).fetch().subjects();
    }

    public Triples findPropertyInstances(Resource property) {
        return model.triples().p(property).fetch();
    }

    public Iterable<? extends RdfNode> findDomainsOfProperty(Resource property) {
        return model.triples().s(property).p(RdfSchema.DOMAIN).fetch().objects();
    }

    public Iterable<? extends RdfNode> findRangesOfProperty(Resource property) {
        return model.triples().s(property).p(RdfSchema.RANGE).fetch().objects();
    }

    public boolean isFirstAncestorOfSecond(Resource maybeAncestor, Resource maybeDescendant) {
        if (maybeAncestor == maybeDescendant) {
            return true;
        }
        for (RdfNode descendant : findDescendants(maybeAncestor, Transitively.YES)) {
            if (maybeDescendant == descendant) {
                return true;
            }
        }
        return false;
    }

    public Iterable<? extends RdfNode> findMembersOfContainer(ObjectNode container) {
        Map<Integer, RdfNode> members = container.type() == RdfType.SEQ ?
            Maps.<Integer, RdfNode>newTreeMap() : Maps.<Integer, RdfNode>newHashMap();
        for (Triple t : model.triples().s(container).fetch()) {
            Uri uri = t.predicate().getUri();
            try {
                if (uri.hasEqualNamespace(Rdf.NAMESPACE) && uri.getLocalName().startsWith("_")) {
                    int index = Integer.parseInt(uri.getLocalName().substring(1));
                    members.put(index, t.object());
                }
            } catch (NumberFormatException e) {
                continue; //ignore
            }
        }

        return members.values();
    }
}
