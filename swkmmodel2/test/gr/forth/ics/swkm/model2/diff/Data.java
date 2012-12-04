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

/**
 *
 * @author Papavasileiou Vicky
 */
class Data {

    private String name;
    private String version1, version2;
    private String ns1, ns2;
    private String addedExplicit, deletedExplicit;
    private String addedClosure, deletedClosure;
    private String addedDense, deletedDense;

    public Data(String name,String version1, String version2,
            String ns1, String ns2,
            String addedExplicit, String deletedExplicit,
            String addedClosure, String deletedClosure,            
            String addedDense, String deletedDense
            ) {
        this.name = name;
        this.version1 = version1;
        this.version2 = version2;
        this.ns1 = ns1;
        this.ns2 = ns2;
        this.addedExplicit = addedExplicit;
        this.deletedExplicit = deletedExplicit;
        this.addedClosure = addedClosure;
        this.deletedClosure = deletedClosure;
        this.addedDense = addedDense;
        this.deletedDense = deletedDense;

    }

    public String getAddedDense() {
        return addedDense;
    }



    public String getDeletedDense() {
        return deletedDense;
    }

    public String getNs1() {
        return ns1;
    }

    public String getNs2() {
        return ns2;
    }

    public String getName() {
        return name;
    }

    public String getAddedClosure() {
        return addedClosure;
    }

    public String getAddedExplicit() {
        return addedExplicit;
    }

    public String getDeletedClosure() {
        return deletedClosure;
    }

    public String getDeletedExplicit() {
        return deletedExplicit;
    }

    public String getVersion1() {
        return version1;
    }

    public String getVersion2() {
        return version2;
    }

}
