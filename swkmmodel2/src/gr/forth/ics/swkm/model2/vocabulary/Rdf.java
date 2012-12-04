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
 * A list of terms from the RDF ({@code http://www.w3.org/1999/02/22-rdf-syntax-ns}) namespace.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Rdf {
    private Rdf() {
    }
    
    public static final Uri NAMESPACE = new Uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "");
    public static final Uri PROPERTY = new Uri(NAMESPACE.getNamespace(), "Property");
    public static final Uri TYPE = new Uri(NAMESPACE.getNamespace(), "type");
    public static final Uri STATEMENT = new Uri(NAMESPACE.getNamespace(), "Statement");
    public static final Uri BAG = new Uri(NAMESPACE.getNamespace(), "Bag");
    public static final Uri SEQ = new Uri(NAMESPACE.getNamespace(), "Seq");
    public static final Uri ALT = new Uri(NAMESPACE.getNamespace(), "Alt");
    public static final Uri PREDICATE = new Uri(NAMESPACE.getNamespace(), "predicate");
    public static final Uri SUBJECT = new Uri(NAMESPACE.getNamespace(), "subject");
    public static final Uri OBJECT = new Uri(NAMESPACE.getNamespace(), "object");
    public static final Uri VALUE = new Uri(NAMESPACE.getNamespace(), "value");
    public static final Uri ID = new Uri(NAMESPACE.getNamespace(), "ID");
    public static final Uri BAG_ID = new Uri(NAMESPACE.getNamespace(), "bagID");
    public static final Uri ABOUT = new Uri(NAMESPACE.getNamespace(), "about");
    public static final Uri DESCRIPTION = new Uri(NAMESPACE.getNamespace(), "Description");
    public static final Uri RESOURCE = new Uri(NAMESPACE.getNamespace(), "resource");
    public static final Uri LI = new Uri(NAMESPACE.getNamespace(), "li");
    public static final Uri LIST = new Uri(NAMESPACE.getNamespace(), "List");
    public static final Uri FIRST = new Uri(NAMESPACE.getNamespace(), "first");
    public static final Uri REST = new Uri(NAMESPACE.getNamespace(), "rest");
    public static final Uri NIL = new Uri(NAMESPACE.getNamespace(), "nil");
    public static final Uri DATATYPE = new Uri(NAMESPACE.getNamespace(), "datatype");
    public static final Uri NODE_ID = new Uri(NAMESPACE.getNamespace(), "nodeID");
    public static final Uri XML_LITERAL = new Uri(NAMESPACE.getNamespace(), "XMLLiteral");
}
