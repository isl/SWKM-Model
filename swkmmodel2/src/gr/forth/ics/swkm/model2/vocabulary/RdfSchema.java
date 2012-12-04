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


package gr.forth.ics.swkm.model2.vocabulary;

import gr.forth.ics.swkm.model2.Uri;

/**
 * A list of terms from the RDF/S ({@code http://www.w3.org/1999/02/22-rdf-syntax-ns}) namespace.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class RdfSchema {
    private RdfSchema() {
    }
    
    public static final Uri NAMESPACE = new Uri("http://www.w3.org/2000/01/rdf-schema#", "");
    
    public static final Uri CLASS = new Uri(NAMESPACE.getNamespace(), "Class");
    public static final Uri SUBCLASSOF = new Uri(NAMESPACE.getNamespace(), "subClassOf");
    public static final Uri SUBPROPERTYOF = new Uri(NAMESPACE.getNamespace(), "subPropertyOf");
    public static final Uri DOMAIN = new Uri(NAMESPACE.getNamespace(), "domain");
    public static final Uri RANGE = new Uri(NAMESPACE.getNamespace(), "range");
    public static final Uri COMMENT = new Uri(NAMESPACE.getNamespace(), "comment");
    public static final Uri LABEL = new Uri(NAMESPACE.getNamespace(), "label");
    public static final Uri SEEALSO = new Uri(NAMESPACE.getNamespace(), "seeAlso");
    public static final Uri ISDEFINEDBY = new Uri(NAMESPACE.getNamespace(), "isDefinedBy");
    public static final Uri LITERAL = new Uri(NAMESPACE.getNamespace(), "Literal");
    public static final Uri CONTAINER = new Uri(NAMESPACE.getNamespace(), "Container");
    public static final Uri CONTAINER_MEMBERSHIP_PROPERTY = new Uri(NAMESPACE.getNamespace(), "ContainerMembershipProperty");
    public static final Uri MEMBER = new Uri(NAMESPACE.getNamespace(), "member");
    public static final Uri RESOURCE = new Uri(NAMESPACE.getNamespace(), "Resource");
    public static final Uri CONSTRAINT_RESOURCE = new Uri(NAMESPACE.getNamespace(), "ConstraintResource");
    public static final Uri CONSTRAINT_PROPERTY = new Uri(NAMESPACE.getNamespace(), "ConstraintProperty");
    public static final Uri DATATYPE = new Uri(NAMESPACE.getNamespace(), "Datatype");
}
