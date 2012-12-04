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

import gr.forth.ics.swkm.model2.Transitively;

/**
 * A object-oriented view of a metaclass.
 *
 * <p>All methods of this type throw {@linkplain IllegalStateException} if the
 * type of this node (i.e. {@code type()}) is no longer an metaclass.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface MetaclassView extends Inheritable {
    /**
     * Returns the (direct or transitive) supermetaclasses of this metaclass.
     * Direct supermetaclasses are requested with {@code Transitively#NO}
     * while transitive ones with {@code Transitively#YES}.
     *
     * @param transitively whether to return non-direct (i.e. transitive) supermetaclasses too
     * @return the direct or transitive supermetaclasses of this metaclass
     */
    Iterable<MetaclassView> superMetaclasses(Transitively transitively);

    /**
     * Returns the (direct or transitive) submetaclasses of this metaclass.
     * Direct submetaclasses are requested with {@code Transitively#NO}
     * while transitive ones with {@code Transitively#YES}.
     *
     * @param transitively whether to return non-direct (i.e. transitive) submetaclasses too
     * @return the direct or transitive submetaclasses of this metaclass
     */
    Iterable<MetaclassView> subMetaclasses(Transitively transitively);
    
    /**
     * Returns the classes that are types of this metaclasses.
     *
     * @return the classes that are types of this metaclasses
     */
    Iterable<ClassView> classes();
}
