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


package gr.forth.ics.swkm.model2.diff;

import java.io.Serializable;

/**
 * Textual representation of a triple
 * @author Papavasileiou Vicky
 */


class TextualTriple{

    private String subject;
    private String predicate;
    private String object;

    /**
     * Creates a textual triple representation, with the specified subject, predicate, and object.
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @throws NullPointerException if any argument is null
     */
    public TextualTriple(String subject, String predicate, String object) {
        if (subject == null || predicate == null || object == null) {
            throw new NullPointerException("One parameter is null: " +
                    "subject=" + subject + ", predicate=" + predicate +
                    ", object=" + object);
        }
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String subjectText() {
        return this.subject;
    }

    public String predicateText() {
        return this.predicate;
    }

    public String objectText() {
        return this.object;
    }

    /**
     * Two DefaultTextualTriples are equal if the their subject/predicate/object are equal
     * @param o the object to be check for equality
     * @return if the two DefaultTextualTriples are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TextualTriple)) {
            return false;
        }
        TextualTriple that = (TextualTriple)o;
        return areEqual(this.subject, that.subject) &&
                areEqual(this.predicate, that.predicate) &&
                areEqual(this.object, that.object);
    }

    private boolean areEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * Returns a hash code value for the object
     * @return the hash code for the TripleValue object
     */
    @Override
    public int hashCode() {
        return hash(subject) + hash(predicate) + hash(object);
    }

    private int hash(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    /**
     * A String representation of the DefaultTextualTriples
     * @return A String representation of the DefaultTextualTriples
     */
    @Override
    public String toString() {
        return "{'" + subject + "', '" + predicate +"', '" + object+ "'}";
    }


}
