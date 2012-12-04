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
public class TripleTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public TripleTest(String testName) {
        super(testName);
    }

    public void testGetSubject() {
    }

    public void testGetPredicate() {
    }

    public void testGetObject() {
    }

    public void testNonLiterals() {
    }

    public void testNodes() {
    }

    public void testNamedGraphs() {
    }

    public void testMappedTo() {
    }

    public void testRemove() {
        Model model = ModelBuilder.newSparse().build();
        Triple t = model.add().s(ns + "a").p(ns + "b").o(ns + "c");
        
        assert t.delete();
        
        assert model.triples().
                s(t.subject()).
                p(t.predicate()).
                o(t.object()).
                fetch().iterator().hasNext() == false;
        
        assert !t.delete();
    }

}
