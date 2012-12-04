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


package gr.forth.ics.swkm.model2.index;

import com.google.common.collect.Iterators;
import gr.forth.ics.swkm.model2.Triple;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>An empty List of Triples.</p>
 * 
 * <p>The only instance of this class can be retrieved by calling static method
 * {@code instance()}.</p>
 * 
 * <p>The difference between this object and {@code EMPTY_LIST} object in 
 * {@linkplain java.util.Collections} class, is that in {@code EmptyTriplesList},
 * {@code iterator()} method returns the same {@linkplain Iterator} object each
 * time it is called.</p>
 * 
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
public class EmptyTriplesList extends AbstractList<Triple> {
    private static final EmptyTriplesList instance = new EmptyTriplesList();
    
    // this class should only have one instance
    private EmptyTriplesList() {}
    
    
    /**
     * Returns the only instance of this class.
     * @return the only instance of this class.
     */
    public static List<Triple> instance() {
        return instance;
    }
    
    public int size() {
        return 0;
    }

    public boolean contains(Triple triple) {
        return false;
    }

    public Triple get(int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Iterator<Triple> iterator() {
        return Iterators.emptyIterator();
    }
}