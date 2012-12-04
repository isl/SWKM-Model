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

import gr.forth.ics.swkm.model2.event.TypeChange;

/**
 * Thrown when there is no valid typing scheme for a given model (typically, when a triple which
 * produces a typing contradiction with regard to RDF typing rules is added to a model).
 *
 * @see Model
 * @see <a href="../../../../../docs/ClassPropertyResource.pdf">"Discussion on the semantics of rdfs:Resource, rdfs:Class, rdf:Property" for a reference of supported RDF typing rules.</a>
 * @see <a href="../../../../../gr/forth/ics/swkm/model2/RdfNode.html#nodeTyping">Node typing section in RdfNode</a>
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
public class RdfTypeException extends RuntimeException {
    private TypeChange change;
    private String changeToString;

    RdfTypeException(String message) {
        super(message);
    }

    void setTypeChange(TypeChange change, String changeToString) {
        this.change = change;
        this.changeToString = changeToString;
    }

    /**
     * Returns the change that caused the error.
     *
     * @return the change that caused the error
     */
    public TypeChange getTypeChange() {
        return change;
    }

    @Override public String toString() {
        String msg = super.toString();
        if (changeToString != null) {
            msg = msg + "\n" + changeToString;
        }
        return msg;
    }
}
