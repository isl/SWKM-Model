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
public class LabelTest extends TestCase {
    
    public LabelTest(String testName) {
        super(testName);
    }

    public void testGetTreeLabel() {
        Label label = new Label(new Interval(3, 5));
        assert label.getTreeLabel().equals(new Interval(3, 5));
    }

    public void testSetTreeLabel() {
        Label label = new Label(new Interval(3, 5));
        label.setTreeLabel(new Interval(6, 10));
        assert label.getTreeLabel().equals(new Interval(6, 10));
    }

    public void testGetPropagatedLabel() {
        Label label = Label.newEmpty();
        assert label.getPropagatedLabels(true).isEmpty();
        assert label.getPropagatedLabels(false).isEmpty();
        
        label.addPropagatedLabel(new Interval(5, 10), true);
        assert label.getPropagatedLabels(true).getIntervals().iterator().next().equals(new Interval(5, 10));
        assert label.getPropagatedLabels(false).isEmpty();
    }

    public void testNotNullTreeLabel() {
        assert Label.newEmpty().getTreeLabel() != null;
    }
    
    public void testContainsLabel() {
        Label label = new Label(new Interval(0, 10));
        label.addPropagatedLabel(new Interval(15, 20), true);
        label.addPropagatedLabel(new Interval(25, 30), false);
        
        assert label.contains(new Label(new Interval(0, 5))) == true;
        assert label.contains(new Label(new Interval(7, 15))) == false;
        assert label.contains(new Label(new Interval(15, 20))) == true;
        assert label.contains(new Label(new Interval(23, 23))) == false;
        assert label.contains(new Label(new Interval(25, 30))) == true;
        assert label.contains(new Label(new Interval(31, 31))) == false;
    }
}
