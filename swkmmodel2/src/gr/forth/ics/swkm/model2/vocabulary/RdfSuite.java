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
 * A list of terms from the OWL ({@code http://139.91.183.30:9090/RDF/rdfsuite.rdfs}) namespace.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class RdfSuite {
    private RdfSuite() {
    }
    
    public static final Uri NAMESPACE = new Uri("http://139.91.183.30:9090/RDF/rdfsuite.rdfs#", "");
    public static final Uri GRAPH = new Uri(NAMESPACE.getNamespace(), "Graph");
    
    public static final Uri DATA_PROPERTY = new Uri(NAMESPACE.getNamespace(), "DataProperty");
    public static final Uri THESAURUS = new Uri(NAMESPACE.getNamespace(), "Thesaurus");
    public static final Uri ENUMERATION = new Uri(NAMESPACE.getNamespace(), "Enumeration");
    
    public static final Uri METACLASS = new Uri(NAMESPACE.getNamespace(), "Metaclass");
    public static final Uri CLASS = new Uri(NAMESPACE.getNamespace(), "Class");

    public static final Uri DEFAULT_GRAPH_URI = new Uri(
            "http://139.91.183.30:9090/RDF/rdfsuite.rdfs#", "DEFAULT");
    
    public static final Uri IMPORTER_SIDE_EFFECTS = new Uri(NAMESPACE.getNamespace(), "ImporterSideEffects");
}
