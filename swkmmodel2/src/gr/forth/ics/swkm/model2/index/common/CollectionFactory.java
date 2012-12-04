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



package gr.forth.ics.swkm.model2.index.common;

import gr.forth.ics.swkm.model2.Triple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Vouzoukidou
 */
public interface CollectionFactory<E extends Collection<Triple>> {
    /**
     * Creates and returns a new {@code E<Triple>} object.
     * @return a new {@code E<Triple>} object.
     */
    E newInstance();

    /**
     * A {@code CollectionFactory} object that generates instances of 
     * {@code LinkedList&lt;Triple&gt}.
     */
    public static final CollectionFactory<List<Triple>> linkedListFactory = new CollectionFactory<List<Triple>>() {
        public LinkedList<Triple> newInstance() {
            return new LinkedList<Triple>();
        }
    };
    
    /**
     * A {@code CollectionFactory} object that generates instances of 
     * {@code ArrayList&lt;Triple&gt}.
     */
    public static final CollectionFactory<List<Triple>> arrayListFactory = new CollectionFactory<List<Triple>>() {
        public List<Triple> newInstance() {
            return new ArrayList<Triple>(2);
        }
    };
    
    /**
     * A {@code CollectionFactory} object that generates instances of 
     * {@code HashSet&lt;Triple&gt}.
     */
    public static final CollectionFactory<Set<Triple>> hashSetFactory = new CollectionFactory<Set<Triple>>() {
        public HashSet<Triple> newInstance() {
            return new HashSet<Triple>();
        }
    };
    
    /**
     * A {@code CollectionFactory} object that generates instances of 
     * {@code LinkedHashSet&lt;Triple&gt}.
     */
    public static final CollectionFactory<Set<Triple>> linkedHashSetFactory = new CollectionFactory<Set<Triple>>() {
        public LinkedHashSet<Triple> newInstance() {
            return new LinkedHashSet<Triple>();
        }
    };
}
