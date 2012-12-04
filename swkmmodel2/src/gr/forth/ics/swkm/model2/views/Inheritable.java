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


package gr.forth.ics.swkm.model2.views;

import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Transitively;

/**
 * The supertype of all schema-related views.
 * 
 * <p>All methods of this type throw {@linkplain IllegalStateException} if the
 * type of this node (i.e. {@code type()}) is no longer a class or
 * a property or a metaclass or a metaproperty.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface Inheritable extends View {
    /**
     * Returns the (direct or transitive) ancestors of this class.
     * Direct ancestors are requested with {@code Transitively#NO}
     * while transitive ones with {@code Transitively#YES}.
     *
     * @param transitively whether to return non-direct (i.e. transitive) ancestors too
     * @return the direct or transitive ancestors of this node
     */
    Iterable<? extends Inheritable> ancestors(Transitively transitively);

    /**
     * Returns the (direct or transitive) descendants of this class.
     * Direct descendants are requested with {@code Transitively#NO}
     * while transitive ones with {@code Transitively#YES}.
     *
     * @param transitively whether to return non-direct (i.e. transitive) descendants too
     * @return the direct or transitive descendants of this node
     */
    Iterable<? extends Inheritable> descendants(Transitively transitively);

    /**
     * Returns whether this is a class.
     * 
     * @return whether this is a class
     */
    boolean isClass();

    /**
     * Returns whether this is a property.
     *
     * @return whether this is a property
     */
    boolean isProperty();

    /**
     * Returns whether this is a metaclass.
     *
     * @return whether this is a metaclass
     */
    boolean isMetaclass();

    /**
     * Returns whether this is a metaproperty.
     *
     * @return whether this is a metaproperty
     */
    boolean isMetaproperty();

    /**
     * Returns a class view of this node.
     *
     * @return a class view of this node
     * @throws IllegalStateException if {@code  isClass() == false}
     */
    ClassView asClass() throws IllegalStateException;

    /**
     * Returns a propertry view of this node.
     *
     * @return a property view of this node
     * @throws IllegalStateException if {@code  isProperty() == false}
     */
    PropertyView asProperty();

    /**
     * Returns a metaclass view of this node.
     *
     * @return a metaclass view of this node
     * @throws IllegalStateException if {@code  isClass() == false}
     */
    MetaclassView asMetaclass();

    /**
     * Returns a metapropertry view of this node.
     *
     * @return a metaproperty view of this node
     * @throws IllegalStateException if {@code  isMetaproperty() == false}
     */
    MetapropertyView asMetaproperty();

    /**
     * Returns the least (in the hierarchy) that this node and the specified one
     * have as a common ancestor.
     * 
     * @param node the node, combined with this one, to find the least common ancestor of
     * @return the least common ancestor of this node and the specified one
     */
    Inheritable leastCommonAncestorWith(RdfNode node);

    /**
     * Returns whether this is an (direct or indirect) ancestor of the specified node.
     *
     * @param node the node to check whether it is a (direct or indirect) descendant of this one
     * @return whether this is an (direct or indirect) ancestor of the specified node
     */
    boolean isAncestorOf(RdfNode node);

    /**
     * Returns whether this is an (direct or indirect) descendant of the specified node.
     *
     * @param node the node to check whether it is a (direct or indirect) ancestor of this one
     * @return whether this is an (direct or indirect) descendant of the specified node
     */
    boolean isDescendantOf(RdfNode node);
}
