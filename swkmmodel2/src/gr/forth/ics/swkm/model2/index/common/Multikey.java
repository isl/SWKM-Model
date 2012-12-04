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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.forth.ics.swkm.model2.index.common;

/**
 *
 * @author vuzukid
 */
public class Multikey {
    private Object[] keys;
    private int hash;
    
    // for quicker creation: not coping the array
    public Multikey(final Object... keys) {
        this.keys = keys;
        hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Multikey) {
            return equals((Multikey) other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        hash = 7;
        for (Object key : keys) {
            hash = 79 * hash + key.hashCode();
        }
        return hash;
    }

    
    public boolean equals(Multikey other) {
        if (other.keys.length != keys.length) {
            return false;
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != other.keys[i]) {
                return false;
            }
        }
        return true;
    }
}
