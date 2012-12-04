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

import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class CompoundIntervalTest extends TestCase {
    
    public CompoundIntervalTest(String testName) {
        super(testName);
    }

    public void testContains() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 5));
        assert combo.contains(2) == false;
        assert combo.contains(3) == true;
        assert combo.contains(4) == true;
        assert combo.contains(5) == true;
        assert combo.contains(6) == false;
    }
    
    public void testContainsInterval() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 5));
        assert combo.contains(new Interval(2, 4)) == false;
        assert combo.contains(new Interval(3, 5)) == true;
        assert combo.contains(new Interval(4, 4)) == true;
        assert combo.contains(new Interval(4, 6)) == false;
        assert combo.contains(new Interval(4, 3)) == false;
    }
    
    public void testContainsCompoundInterval() {
        CompoundInterval combo1 = new CompoundInterval();
        combo1.add(new Interval(0, 10));
        combo1.add(new Interval(20, 30));
        
        CompoundInterval combo2 = new CompoundInterval();
        combo2.add(new Interval(0, 5));
        assert combo1.contains(combo2);
        
        combo2.add(new Interval(6, 7));
        assert combo1.contains(combo2);
        
        combo2.add(new Interval(0, -10)); //this will not be added
        assert combo1.contains(combo2); //so this will still be true
        
        combo2.add(new Interval(22, 26));
        assert combo1.contains(combo2);
        
        combo2.add(new Interval(27, 32));
        assert !combo1.contains(combo2);
    }
    
    public void testOverlapsWithInterval() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(0, 10));
        
        assert combo.overlapsWith(new Interval(-5, 5));
        assert combo.overlapsWith(new Interval(5, 15));
        assert combo.overlapsWith(new Interval(5, 5));
        assert !combo.overlapsWith(new Interval(-10, -5));
        assert !combo.overlapsWith(new Interval(15, 20));
        assert !combo.overlapsWith(new Interval(5, 3));
    }
    
    public void testOverlapsWithCompoundInterval() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(0, 5));
        combo.add(new Interval(7, 10));
        
        assert combo.overlapsWith(new CompoundInterval(new Interval(-5, 5)));
        assert combo.overlapsWith(new CompoundInterval(new Interval(5, 15)));
        assert combo.overlapsWith(new CompoundInterval(new Interval(5, 5)));
        assert !combo.overlapsWith(new CompoundInterval(new Interval(-10, -5)));
        assert !combo.overlapsWith(new CompoundInterval(new Interval(15, 20)));
        assert !combo.overlapsWith(new CompoundInterval(new Interval(5, 3)));
    }
    
    public void testAddDisjointIntervals() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 3));
        assert combo.contains(3);
        combo.add(new Interval(5, 5));
        assert !combo.contains(4);
        assert combo.contains(5);
    }
    
    public void testAddIntersectedIntervals1() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 5));
        combo.add(new Interval(4, 7));
        assert combo.contains(new Interval(3, 5));
        assert combo.contains(new Interval(4, 7));
        assert !combo.contains(2);
        assert !combo.contains(8);
    }

    public void testAddIntersectedIntervals2() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 5));
        combo.add(new Interval(1, 7));
        assert combo.contains(new Interval(1, 7));
        assert !combo.contains(0);
        assert !combo.contains(8);
    }

    public void testAddNotContains() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(3, 5));
        combo.add(new Interval(0, 3));
        combo.add(new Interval(7, 15));
        assert !combo.contains(new Interval(-1, 5));
        assert !combo.contains(new Interval(0, 6));
        assert !combo.contains(new Interval(0, 7));
        assert !combo.contains(new Interval(6, 15));
        assert !combo.contains(new Interval(7, 16));
    }
    
    public void testAddCompoundInterval() {
        CompoundInterval combo1 = new CompoundInterval();
        combo1.add(new Interval(3, 5));
        
        CompoundInterval combo2 = new CompoundInterval();
        combo2.add(new Interval(0, 2));
        combo2.add(new Interval(6, 10));
        
        combo1.add(combo2);
        assert combo1.contains(-1) == false;
        assert combo1.contains(new Interval(0, 2)) == true;
        assert combo1.contains(new Interval(3, 5)) == true;
        assert combo1.contains(new Interval(6, 10)) == true;
        assert combo1.contains(11) == false;
    }

    public void testRemoveInterval() {
        CompoundInterval combo = new CompoundInterval();
        combo.add(new Interval(0, 5));
        combo.add(new Interval(6, 15));
        assert combo.remove(new Interval(4, 7)) == false;
        assert combo.contains(new Interval(0, 5));
        assert combo.contains(new Interval(6, 15));
        assert combo.remove(new Interval(0, 5));
        assert combo.contains(new Interval(0, 5)) == false;
    }
    
    public void testIsEmpty() {
        CompoundInterval combo = new CompoundInterval();
        assert combo.isEmpty();
        combo.add(Interval.empty());
        assert combo.isEmpty();
        combo.add(new Interval(1, 2));
        assert !combo.isEmpty();
        combo.remove(new Interval(1, 2));
        assert combo.isEmpty();
    }
    
    public void testEquality() {
        CompoundInterval combo1 = new CompoundInterval();
        combo1.add(new Interval(1, 15));
        
        CompoundInterval combo2 = new CompoundInterval();
        combo2.add(new Interval(1, 15));
        
        assert combo1.equals(combo2);
        
        combo1.add(new Interval(16, 16));
        assert !combo1.equals(combo2);
        
        combo2.add(new Interval(16, 16));
        assert combo1.equals(combo2);
        
        assert combo1.hashCode() == combo2.hashCode();
    }
    
    public void testSet() {
        CompoundInterval combo1 = new CompoundInterval();
        combo1.add(new Interval(1, 15));
        
        combo1.set(new Interval(4, 5));
        assert !combo1.contains(3);
        assert combo1.contains(4);
        assert combo1.contains(5);
        assert !combo1.contains(6);
        
        combo1.set(new Interval(5, 3));
        assert combo1.isEmpty();
        
        combo1.set(new Interval(4, 5));
        combo1.set(null);
        assert combo1.isEmpty();
    }
    
    public void testMaxInterval() {
        CompoundInterval combo = new CompoundInterval(new Interval(1, 15));
        assert combo.maxInterval().equals(new Interval(1, 15));
        
        combo.add(new Interval(16, 40));
        assert combo.maxInterval().equals(new Interval(16, 40));
        
        combo.remove(new Interval(16, 40));
        assert combo.maxInterval().equals(new Interval(1, 15));
    }
}
