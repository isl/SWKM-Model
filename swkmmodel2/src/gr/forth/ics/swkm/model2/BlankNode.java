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

/**
 * A blank node. A resource is an {@linkplain RdfNode} that is identified
 * by a blank node identifier, which is not globally unique but unique in the scope
 * of the document where it is defined.
 *
 * <p><strong>Important: </strong>A blank node can never be <em>schema</em>,
 * i.e. a class, a property, a metaclass, or a metaproperty. For example, a triple of the form:
 * {@code <subject rdf:type _bnode>}, where {@code _bnode} is a blank node, will always
 * throw a validation exception upon its addition to a {@linkplain Model}, since such a triple would
 * imply that {@code _bnode} is a class, or a metaclass, or a metaproperty.
 * 
 * @see <a href="http://www.w3.org/TR/rdf-concepts/#section-blank-nodes">The specification of blank nodes</a>
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface BlankNode extends ObjectNode {
    /**
     * Returns the identifier of this blank node.
     * @return the identifier of this blank node
     */
    String getId();
    
    /**
     * {@inheritDoc}
     */
    BlankNode mappedTo(Model model);
}
