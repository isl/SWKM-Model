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

import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class LiteralTest extends TestCase {
    
    public LiteralTest(String testName) {
        super(testName);
    }

    public void testParse() {
        assertExpected("value", "lang", null, Literal.parse("\"value\"@lang"));
        assertExpected("value", null, "type", Literal.parse("\"value\"^^type"));
        assertExpected("value", null, null, Literal.parse("\"value\""));
        
        try {
            Literal.parse("value");
            fail("Should fail because of missing quotations");
        } catch (Exception ok) { }
    }

    public void testCreate() {
        assertExpected("value", null, null, Literal.create("value"));
        assertExpected("\"value\"", null, null, Literal.create("\"value\""));
    }

    public void testCreateWithLanguage() {
        assertExpected("value", "lang", null, Literal.createWithLanguage("value", "lang"));
        assertExpected("\"value\"", "lang", null, Literal.createWithLanguage("\"value\"", "lang"));
    }

    public void testCreateWithType() {
        assertExpected("value", null, "type", Literal.createWithType("value", Uri.parse("type")));
        assertExpected("\"value\"", null, "type", Literal.createWithType("\"value\"", Uri.parse("type")));
    }
    
    public void testWithStartingSpace() {
        assertExpected("value", null, null, Literal.parse("  \"value\""));
    }

    public void testMultilineLiteral() {
        assertExpected("value1\nvalue2", "en", null, Literal.parse("  \"value1\nvalue2\"@en"));
    }

    private static void assertExpected(String literal, String lang, String type, Literal actual) {
        assertEquals(literal, actual.getValue());
        assertEquals(lang, actual.getLanguage());
        assertEquals(type, actual.getType() != null ? actual.getType().toString() : null);
    }
}
