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


package gr.forth.ics.swkm.model2.labels;

import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class IntervalTest extends TestCase {
    
    public IntervalTest(String testName) {
        super(testName);
    }

    public void testGetIndex() {
        assert new Interval(5, 10).getIndex() == 5;
    }

    public void testGetPost() {
        assert new Interval(5, 10).getPost() == 10;
    }

    public void testIsEmpty() {
        assert new Interval(5, 10).isEmpty() == false;
        assert new Interval(5, 4).isEmpty();
        assert Interval.empty().isEmpty();
    }
    
    public void testEquals() {
        assert new Interval(5, 10).equals(new Interval(5, 10));
        assert !(new Interval(5, 10).equals(new Interval(10, 5)));
    }
    
    public void testHashCode() {
        assert new Interval(5, 10).hashCode() == new Interval(5, 10).hashCode();
    }

    public void testIntersection() {
        assert new Interval(5, 10).intersection(new Interval(6, 9)).equals(new Interval(6, 9));
        assert new Interval(5, 10).intersection(new Interval(1, 4)).isEmpty();
        assert new Interval(5, 10).intersection(new Interval(1, 7)).equals(new Interval(5, 7));
        assert new Interval(0, -10).intersection(new Interval(-10, 0)).isEmpty();
        assert new Interval(-10, 0).intersection(new Interval(0, -10)).isEmpty();
    }
    
    public void testUnion() {
        assert new Interval(5, 10).union(new Interval(6, 9)).equals(new Interval(5, 10));
        assert new Interval(5, 10).union(new Interval(1, 4)).equals(new Interval(1, 10));
        assert new Interval(5, 10).union(new Interval(1, 7)).equals(new Interval(1, 10));
        assert new Interval(0, -10).union(new Interval(-10, 0)).equals(new Interval(-10, 0));
        assert new Interval(-10, 0).union(new Interval(0, -10)).equals(new Interval(-10, 0));
        assert new Interval(5, 10).union(new Interval(3, 2)).equals(new Interval(5, 10));
        assert new Interval(3, 2).union(new Interval(5, 10)).equals(new Interval(5, 10));
    }
    
    public void testOverlapsWith() {
        assert new Interval(5, 10).overlapsWith(new Interval(6, 11)) == true;
        assert new Interval(5, 10).overlapsWith(new Interval(4, 5)) == true;
        assert new Interval(5, 10).overlapsWith(new Interval(3, 4)) == false;
        assert new Interval(5, 10).overlapsWith(new Interval(7, 6)) == false;
        assert new Interval(5, 10).overlapsWith(new Interval(11, 12)) == false;
        assert new Interval(4, 3).overlapsWith(new Interval(0, 10)) == false;
        assert new Interval(5, 10).overlapsWith(new Interval(0, 15)) == true;
    }

    public void testContains() {
        assert new Interval(1, 3).contains(0) == false;
        assert new Interval(1, 3).contains(1) == true;
        assert new Interval(1, 3).contains(2) == true;
        assert new Interval(1, 3).contains(3) == true;
        assert new Interval(1, 3).contains(4) == false;
    }

    public void testContainsInterval() {
        assert new Interval(1, 3).contains(new Interval(0, 1)) == false;
        assert new Interval(1, 3).contains(new Interval(0, 2)) == false;
        assert new Interval(1, 3).contains(new Interval(1, 2)) == true;
        assert new Interval(1, 3).contains(new Interval(1, 3)) == true;
        assert new Interval(1, 3).contains(new Interval(2, 1)) == false;
    }

    public void testWithIndex() {
        assert new Interval(1, 3).withIndex(999).equals(new Interval(999, 3));
    }

    public void testWithPost() {
        assert new Interval(1, 3).withPost(999).equals(new Interval(1, 999));
    }
    
    public void testLength() {
        assert new Interval(5, 10).length() == 6;
        assert new Interval(5, 5).length() == 1;
        assert new Interval(5, 4).length() == 0;
    }
    
    public void testCanBeMerged() {
        assert new Interval(1, 5).canBeMergedWith(new Interval(3, 7)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(5, 7)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(6, 7)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(-2, 2)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(-2, 1)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(-2, 0)) == true;
        
        assert new Interval(1, 5).canBeMergedWith(new Interval(7, 7)) == false;
        assert new Interval(1, 5).canBeMergedWith(new Interval(-1, -1)) == false;
        
        assert new Interval(1, 5).canBeMergedWith(new Interval(3, 2)) == true;
        assert new Interval(1, 5).canBeMergedWith(new Interval(-1, -4)) == true;
    }
}
