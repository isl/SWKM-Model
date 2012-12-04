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

import gr.forth.ics.swkm.model2.Uri.Delimiter;
import gr.forth.ics.swkm.model2.Uri.UriFormatException;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class UriTest extends TestCase {
    private final UriStub uri;
    
    public UriTest(String testName, UriStub uriStub) {
        super(testName);
        this.uri = uriStub;
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        UriStub[] uris = {
            new UriStub("base#local/local", "base#", "local/local", true),
            new UriStub("base/base#local", "base/base#", "local", true),
            new UriStub("base#local", "base#", "local", true),
            new UriStub("base:local", "base:", "local", true),
            new UriStub("base#", "base#", "", true),
            new UriStub("base/", "base/", "", true),
            new UriStub("#local", "#", "local", true),
            new UriStub("/local", "/", "local", true),
            new UriStub("_#id", "_#", "id", true),
            new UriStub("_/id", "_/", "id", true),
            new UriStub("base#base2#local", "", "", false)
        };
        for (UriStub uri : uris) {
            suite.addTest(new UriTest("testParse", uri));
            if (uri.isValid) {
                suite.addTest(new UriTest("testGetNamespace", uri));
                suite.addTest(new UriTest("testAppendNamespace", uri));
                suite.addTest(new UriTest("testGetLocalName", uri));
                suite.addTest(new UriTest("testAppendLocalName", uri));
                suite.addTest(new UriTest("testToString", uri));
                suite.addTest(new UriTest("testToStringAppendable", uri));
                suite.addTest(new UriTest("testWithNamespace", uri));
                suite.addTest(new UriTest("testWithLocalName", uri));
            }
        }
        suite.addTest(new UriTest("testHasEqualNamespaceAndLocalName", null));
        suite.addTest(new UriTest("testValidateCharacters", null));
        return suite;
    }

    public void testParse() {
        if (uri.isValid) {
            assertNotNull(Uri.tryParse(uri.full));
            Uri.parse(uri.full);
        } else {
            try {
                assertNull(Uri.tryParse(uri.full));
                Uri.parse(uri.full);
                fail("Parsed invalid URI");
            } catch (UriFormatException ok) {
            }
        }
    }

    public void testGetNamespace() {
        Uri u = Uri.parse(uri.full);
        assertEquals(uri.base, u.getNamespace());
    }

    public void testAppendNamespace() {
        StringBuilder sb = new StringBuilder();
        Uri u = Uri.parse(uri.full);
        u.appendNamespace(sb);
        assertEquals(uri.base, sb.toString());
    }

    public void testGetLocalName() {
        Uri u = Uri.parse(uri.full);
        assertEquals(uri.local, u.getLocalName());
    }

    public void testAppendLocalName() {
        StringBuilder sb = new StringBuilder();
        Uri u = Uri.parse(uri.full);
        u.appendLocalName(sb);
        assertEquals(uri.local, sb.toString());
    }

    public void testToString() {
        Uri u = Uri.parse(uri.full);
        assertEquals(uri.full, u.toString(Delimiter.WITH));
    }
    
    public void testToStringAppendable() {
        StringBuilder sb = new StringBuilder();
        Uri u = Uri.parse(uri.full);
        u.toString(sb, Delimiter.WITH);
        assertEquals(uri.full, sb.toString());
    }
    
    public void testWithLocalName() {
        Uri uri1 = Uri.parse(uri.full);
        Uri uri2 = uri1.withLocalName("newLocal");
        assertNotSame(uri1, uri2);
        assertEquals(uri1.getNamespace(), uri2.getNamespace());
        assertEquals("newLocal", uri2.getLocalName());
    }
    
    public void testWithNamespace() {
        Uri uri1 = Uri.parse(uri.full);
        Uri uri2 = uri1.withNamespace("newNamespace#");
        assertNotSame(uri1, uri2);
        assertEquals("newNamespace#", uri2.getNamespace());
        assertEquals(uri1.getLocalName(), uri2.getLocalName());
    }
    
    public void testHasEqualNamespaceAndLocalName() {
        Uri uri1 = Uri.parse("ns#localPart");
        Uri uri2 = Uri.parse("ns#localPart2");
        Uri uri3 = Uri.parse("ns2#localPart");
        assert uri1.hasEqualNamespace(uri2);
        assert !uri1.hasEqualLocalName(uri2);
        
        assert !uri1.hasEqualNamespace(uri3);
        assert uri1.hasEqualLocalName(uri3);
        
        assert !uri2.hasEqualNamespace(uri3);
        assert !uri2.hasEqualLocalName(uri3);
    }

    public void testValidateCharacters() {
        assertTrue(Uri.parse("normal#normal").validateCharacters());
        assertFalse(Uri.parse("4notNormal#normal").validateCharacters());
        assertFalse(Uri.parse("normal#4notNormal").validateCharacters());
        assertTrue(Uri.parse("normal4#normal4").validateCharacters());
        assertTrue(Uri.parse("normal4").validateCharacters());
        assertTrue(Uri.parse("normal4_-/?:.~#a4_-/?:.~").validateCharacters());

        char[] invalidChars = { '$', '%', '`', '@', '^', '&', '*', '(', ')', '[', ']', '{', '}', '\"', '\'' };
        for (char invalidChar : invalidChars) {
            assertFalse(Uri.parse("notNormal" + invalidChar + "#").validateCharacters());
            assertFalse(Uri.parse("notNormal" + invalidChar).validateCharacters());
            assertFalse(Uri.parse("notNormal#a" + invalidChar).validateCharacters());
        }
    }
    
    private static class UriStub {
        final String full;
        final String base;
        final String local;
        final boolean isValid;
        
        UriStub(String full, String base, String local, boolean isValid) {
            this.full = full;
            this.base = base;
            this.local = local;
            this.isValid = isValid;
        }
    }
}
