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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Papavasileiou Vicky
 */
public class DiffComparisons {

    public static void assertEqual(String name, Collection<?> c1, Collection<?> c2) {
        Set<Object> set1 = new HashSet<Object>(c1);
        Set<Object> set2 = new HashSet<Object>(c2);
        Set<Object> swap = new HashSet<Object>(c1);
        set1.removeAll(set2);
//        System.out.println(set1);
//        System.out.println("-------------------------------------");
        set2.removeAll(swap);
//        System.out.println(set2);
        if (!set1.isEmpty() || !set2.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (!set1.isEmpty()) {
                sb.append("\nExtra elements in first collection of " + name + " : \n ").append(set1);
            }
            if (!set2.isEmpty()) {
                sb.append("\nExtra elements in second collection of " + name + " : \n ").append(set2);
            }
            throw new AssertionError(sb.toString());
        }
    }
}
