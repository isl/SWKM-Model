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


package gr.forth.ics.swkm.model2.event;

import gr.forth.ics.swkm.model2.*;

/**
 * A change of the type of an RDF node. Models a transition of a node from an old
 * type to a new type. This is triggered by a triple.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface TypeChange {
    /**
     * Returns the node that its type changed. The new type can be accessed by {@link RdfNode#type()}.
     *
     * @return the node that its type changed
     */
    RdfNode node();

    /**
     * Returns the old type of the node. The new type can be accessed by {@link RdfNode#type()}.
     *
     * @return the old type of the node
     */
    RdfType oldType();

    /**
     * Returns the triple that caused this change.
     *
     * @return the triple that caused this change
     */
    Triple cause();

    /**
     * The change that resulted in this subsequent change, if any.
     *
     * @return the change that resulted in this subsequent change, if any, or null if this change had
     * no previous changes causing it.
     */
    TypeChange parentChange();

    /**
     * Returns the root change which resulted in this change. This is equivalent to:
     * <p>
     *<pre>{@code
     *  TypeChange change = ...;
     *  while (change.parentChange() != null) {
     *    change = change.parentChange();
     *  }
     * //rootCause == change
     *}</pre>
     * <p> This can be used to easily find the initial {@link Triple} object that caused this
     * change chain, i.e.: {@code rootChange().cause()}.
     * @return
     */
    TypeChange rootChange();
}
