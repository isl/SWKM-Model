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

import gr.forth.ics.swkm.model2.importer.Database;
import gr.forth.ics.swkm.model2.importer.RdfStores;
import gr.forth.ics.rdfsuite.swkm.model.db.impl.RDFDB_Model;
import gr.forth.ics.swkmclient.utils.test.ModelStub;
import gr.forth.ics.swkmclient.utils.test.Models;
import junit.framework.TestCase;

public class SwkmLabelingTest extends TestCase {
    private static final Database db = Database.db1;
    
    public SwkmLabelingTest(String testName) {
        super(testName);
    }
    
    public void testCulture() throws Exception {
        db.erase();
//        JdbcMonitor.startWatching(new Monitor());
        ModelStub modelStub = Models.culture();
        RDFDB_Model model = db.newModel(RdfStores.Representation.HYBRID);
        modelStub.createModel(model);
        model.createLabels();
//        model.store(true);
    }
}