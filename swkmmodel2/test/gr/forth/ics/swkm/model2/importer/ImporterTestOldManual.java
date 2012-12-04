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


package gr.forth.ics.swkm.model2.importer;

import gr.forth.ics.swkm.model2.importer.RdfStores.Representation;
import gr.forth.ics.rdfsuite.services.db.JdbcMonitor;
import gr.forth.ics.rdfsuite.swkm.model.db.impl.RDFDB_Model;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkmclient.utils.test.ModelStub;
import gr.forth.ics.swkmclient.utils.test.Models;
import gr.forth.ics.taskmonitor.Monitor;
import java.sql.SQLException;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ImporterTestOldManual extends TestCase {
    private final String name;
    private final ModelStub modelStub;
    private final Representation repr;
    private static final boolean ENABLE_JDBC_MONITORING = true;

    public ImporterTestOldManual(String testName, ModelStub modelStub, Representation repr,
            String name) {
        super(testName);
        this.modelStub = modelStub;
        this.repr = repr;
        this.name = name;
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
//        for (ModelStub model : Models.allCultureModels()) {
//            suite.addTest(new ImporterTest("test", model));
//        }
//        for (Representation r : Representation.values()) {
        suite.addTest(new ImporterTestOldManual("test", Models.culture(), Representation.HYBRID, "Culture schema+data"));
//        }
        return suite;
    }
    
    private static final Monitor monitor = new Monitor();
    
    private static void startWatching() {
        if (ENABLE_JDBC_MONITORING) {
            try {
                JdbcMonitor.startWatching(monitor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static void stopWatching() {
        if (ENABLE_JDBC_MONITORING) {
            JdbcMonitor.stopWatching();
        }
    }
    
    static {
        startWatching();
        stopWatching();
    }
    
    @Override
    protected void setUp() throws Exception {
        stopWatching();
        Database[] dbs = { /* Database.db1, */ Database.db2  };
        for (Database db : dbs) {
            db.erase();
            RdfStore store = RdfStores.get(repr,
                    db.getDataSource(), Configurers.newDefault());
            store.initializeSchemaIfNeeded();
        }
        startWatching();
    }

    public void test() throws SQLException {
        setName(getName() + "_" + name + " with " + repr);
//        System.out.println("Running: " + getName());
        
//        System.out.println("Storing OLD");
        storeOld(Database.db2, repr);
        
//        System.out.println("\n\n\nStoring NEW");
//        storeNew(Database.db2, repr);
        stopWatching();
//        Database.assertEqual(Database.db1, Database.db2);
    }
    
    private void storeOld(Database db, Representation repr) throws SQLException {
        RDFDB_Model model = db.newModel(repr);
        modelStub.createModel(model);
        String cultureUri = "http://cultureUri#";
        String whateverUri = "http://whateverUri#";

        String class1 = cultureUri + "CLASS1";
        String class2 = cultureUri + "CLASS2";
        String class3 = cultureUri + "CLASS3";
        String class4 = cultureUri + "CLASS4";
        
        model.addStatement(class1, Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);
        model.addStatement(class2, Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);
        model.addStatement(class3, Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);
        model.addStatement(class4, Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);
        model.addStatement("http://whateverUri#NewClass", Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);
        model.addStatement("http://cultureUri#Destroyer", Rdf.TYPE.toString(), RdfSchema.CLASS.toString(), cultureUri, null, null);

        model.addStatement(class1, RdfSchema.SUBCLASSOF.toString(), class2, cultureUri, null, null);
        model.addStatement(class1, RdfSchema.SUBCLASSOF.toString(), class3, cultureUri, null, null);
        model.addStatement(class4, RdfSchema.SUBCLASSOF.toString(), class1, cultureUri, null, null);
        model.addStatement("http://cultureUri#Destroyer", RdfSchema.SUBCLASSOF.toString(), "http://cultureUri#Artist", cultureUri, null, null);

        model.addStatement("http://cultureUri#destroys", Rdf.TYPE.toString(), Rdf.PROPERTY.toString(), cultureUri, null, null);
        model.addStatement("http://cultureUri#destroys", RdfSchema.DOMAIN.toString(), "http://cultureUri#Destroyer", whateverUri, null, null);
        model.addStatement("http://cultureUri#destroys", RdfSchema.RANGE.toString(), "http://cultureUri#Artifact", whateverUri, null, null);
        model.addStatement("http://whateverUri#NewProperty", Rdf.TYPE.toString(), Rdf.PROPERTY.toString(), whateverUri, null, null);
        model.addStatement("http://whateverUri#NewProperty", RdfSchema.DOMAIN.toString(), RdfSchema.RESOURCE.toString(), whateverUri, null, null);
        model.addStatement("http://whateverUri#NewProperty", RdfSchema.RANGE.toString(), RdfSchema.RESOURCE.toString(), whateverUri, null, null);

        try {
            model.store(true);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void storeNew(Database db, Representation repr) throws SQLException {
        RdfStore store = RdfStores.get(repr, db.getDataSource(), Configurers.newDefault());
        throw new UnsupportedOperationException();
    }
}
