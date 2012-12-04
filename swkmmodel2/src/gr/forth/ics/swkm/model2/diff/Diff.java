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

import com.google.common.collect.BiMap;
import gr.forth.ics.swkm.model2.Model;

/**
 * The implementation of the diff algorithm
 * @author papavas
 */

public interface Diff {

    /**
     * Computes the differences between two models.
     * @param m1 The first model
     * @param m2 The second model
     * @param map The mapping of the namespaces of the first model to the namespaces
     * of the second model
     * @return A delta that contains the added and deleted triples
     */
    Delta diff(Model m1, Model m2, BiMap<String,String> map);

    /**
     * Computes the differences between two models using the default mapping between
     * the namespaces of the two models.
     * The default mapping is the mapping created by this method.
     * @param m1 The first model
     * @param m2 The second model
     * @return A delta that contains the added and deleted triples
     */
    Delta diff(Model m1, Model m2);

}
