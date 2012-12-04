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

import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Triples;
import gr.forth.ics.swkm.model2.Transitively;

/**
 * A helper for supporting "object view" access methods (defined in
 * <a href="{@docRoot}/gr/forth/ics/swkm/model2/views/package-summary.html">
 * gr.forth.ics.swkm.model2.views</a>).
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface ObjectViewSupport {
    /**
     * Returns all individuals of a class.
     *
     * @param clazz the class
     * @return all individuals of a class
     */
    Iterable<? extends RdfNode> findIndividuals(Resource clazz);
    
    /**
     * Returns all properties with a given domain.
     *
     * @param domainClass the domain
     * @return all properties with a given domain
     */
    Iterable<? extends RdfNode> findPropertiesWithDomain(Resource domainClass);
    
    /**
     * Returns all properties with a given range.
     *
     * @param rangeClass the range
     * @return all properties with a given range
     */
    Iterable<? extends RdfNode> findPropertiesWithRange(Resource rangeClass);
    
    /**
     * Returns the metaclasses of a class.
     *
     * @param clazz the class
     * @return the metaclasses of a class
     */
    Iterable<? extends RdfNode> findMetaclassesOfClass(Resource clazz);

    /**
     * Returns the metaproperties of a property.
     *
     * @param property the property
     * @return the metaproperties of a property
     */
    Iterable<? extends RdfNode> findMetapropertiesOfProperty(Resource property);

    /**
     * Returns the property instances of a property.
     * 
     * @param property the property
     * @return the property instances of a property
     */
    Triples findPropertyInstances(Resource property);
    
    /**
     * Returns the classes of an individual.
     *
     * @param individual the individual
     * @return the classes of an individual
     */
    Iterable<? extends RdfNode> findClassesOfIndividual(ObjectNode individual);
    
    /**
     * Returns the ancestors of a schema resource. A schema resource can be a class,
     * a metaclass, a property or a metaproperty.
     *
     * @param schemaResource the schema resource
     * @param transitively if true, all ancestors recursively must be returned, otherwise just the direct ones
     * @return the ancestors of a schema resource
     */
    Iterable<? extends RdfNode> findAncestors(Resource schemaResource, Transitively transitively);
    
    /**
     * Returns the descendants of a schema resource. A schema resource can be a class,
     * a metaclass, a property or a metaproperty.
     *
     * @param schemaResource the schema resource
     * @param transitively if true, all descendants recursively must be returned, otherwise just the direct ones
     * @return the descendants of a schema resource
     */
    Iterable<? extends RdfNode> findDescendants(Resource schemaResource, Transitively transitively);
    
    /**
     * Returns the classes of a metaclass.
     *
     * @param metaclass the metaclass
     * @return the classes of a metaclass
     */
    Iterable<? extends RdfNode> findClassesOfMetaclass(Resource metaclass);
    
    /**
     * Returns the properties of a metaproperty.
     *
     * @param metaproperty the metaproperty
     * @return the properties of a metaproperty
     */
    Iterable<? extends RdfNode> findPropertiesOfMetaproperty(Resource metaproperty);
    
    /**
     * Returns the domains of a property.
     *
     * @param property the property
     * @return the domains of a property
     */
    Iterable<? extends RdfNode> findDomainsOfProperty(Resource property);
    
    /**
     * Returns the ranges of a property.
     *
     * @param property the property
     * @return the ranges of a property
     */
    Iterable<? extends RdfNode> findRangesOfProperty(Resource property);
    
    /**
     * Returns true if the first resource is an ancestor of the second resource (or if they are the same), false otherwise.
     * 
     * @param maybeAncestor the first resource, which is tested whether it is an ancestor of the second
     * @param maybeDescendant the second resource, which is tested whether it is a descendant of the first
     * @return true if the first resource is an ancestor of the second resource, false otherwise
     */
    boolean isFirstAncestorOfSecond(Resource maybeAncestor, Resource maybeDescendant);
    
    /**
     * Returns the members of the specified container.
     *
     * @param container the container (of type {@code rdf:Bag}, {@code rdf:Alt} or {@code rdf:Seq})
     * of which to return the members
     * @return the members of the specified container
     */
    Iterable<? extends RdfNode> findMembersOfContainer(ObjectNode container);
}
