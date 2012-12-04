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

import gr.forth.ics.rdfsuite.services.RdfDocument;
import gr.forth.ics.rdfsuite.services.db.JdbcMonitor;
import gr.forth.ics.swkm.model2.importer.RdfStores.Representation;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkmclient.utils.test.ModelStub;
import gr.forth.ics.swkmclient.utils.test.Models;
import gr.forth.ics.taskmonitor.Monitor;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ImporterTestNewManual extends TestCase {
    private final String name;
    private final ModelStub modelStub;
    private final Representation repr;
    private static final boolean ENABLE_JDBC_MONITORING = true;

    public ImporterTestNewManual(String testName, ModelStub modelStub, Representation repr,
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
        for (Representation r : Arrays.asList(Representation.HYBRID)) {
            suite.addTest(new ImporterTestNewManual("test",
                    Models.cultureData(), r, "Culture schema+data"));
        }
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

    private final boolean withAdditions = false;
    @Override
    protected void setUp() throws Exception {
        stopWatching();
        Database[] dbs = { Database.db1 };
        for (Database db : dbs) {
            if (!withAdditions) {
                db.erase();
            }
            RdfStore store = RdfStores.get(repr,
                    db.getDataSource(), Configurers.newDefault());
            store.initializeSchemaIfNeeded();
        }
        startWatching();
    }

    public void test() throws Exception {
        setName(getName() + "_" + name + " with " + repr);
//        System.out.println("Running: " + getName());

//        System.out.println("\n\n\nStoring NEW");
        storeNew(Database.db1, repr);
        stopWatching();
    }

    private void storeNew(Database db, Representation repr) throws Exception {
        RdfStore store = RdfStores.get(repr, db.getDataSource(), Configurers.newDefault());

        store.store(createModel());
    }

    private Model createModel() throws IOException {
        Model model = ModelBuilder.newSparse().build();
        for (RdfDocument doc : modelStub.getDocuments()) {
            RdfIO.read(doc.getContent(), Format.valueOf(
                    doc.getFormat().toString().replaceAll("_", ""))).
                    withBase(doc.getURI()).into(model);
        }
        if (withAdditions) {
            model.add()
                    .s("http://cultureUri#destroys")
                    .p(RdfSchema.SUBPROPERTYOF)
                    .o("http://cultureUri#creates");
            model.add()
                    .s("http://whateverUri#NewClass")
                    .p(Rdf.TYPE)
                    .o(RdfSchema.CLASS);
            model.add()
                    .s("http://whateverUri#NewProperty")
                    .p(Rdf.TYPE)
                    .o(Rdf.PROPERTY);
            model.add()
                    .s("http://cultureUri#Destroyer")
                    .p(RdfSchema.SUBCLASSOF)
                    .o("http://cultureUri#Artist");
            model.add()
                    .s("http://cultureUri#destroys")
                    .p(RdfSchema.DOMAIN)
                    .o("http://cultureUri#Destroyer");

            model.add()
                    .s("http://cultureUri#class1")
                    .p(RdfSchema.SUBCLASSOF)
                    .o("http://cultureUri#class2");
            model.add()
                    .s("http://cultureUri#class1")
                    .p(RdfSchema.SUBCLASSOF)
                    .o("http://cultureUri#class3");
            model.add()
                    .s("http://cultureUri#class4")
                    .p(RdfSchema.SUBCLASSOF)
                    .o("http://cultureUri#class1");
        /*
        model.add()
                .s("http://cultureUri#class5")
                .p(RdfSchema.SUBCLASSOF)
                .o("http://cultureUri#class1");
        model.add()
                .s("http://cultureUri#class6")
                .p(RdfSchema.SUBCLASSOF)
                .o("http://cultureUri#class5");
        model.add()
                .s("http://cultureUri#class1")
                .p(RdfSchema.SUBCLASSOF)
                .o("http://cultureUri#class7");
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#classEl1")
                .p(Rdf.TYPE)
                .o(RdfSchema.CLASS);
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#classEl2")
                .p(Rdf.TYPE)
                .o(RdfSchema.CLASS);
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl1")
                .p(Rdf.TYPE)
                .o(Rdf.PROPERTY);
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl1")
                .p(RdfSchema.DOMAIN)
                .o("http://cultureUri#classEl1");
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl1")
                .p(RdfSchema.RANGE)
                .o("http://cultureUri#classEl2");

        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl2")
                .p(Rdf.TYPE)
                .o(Rdf.PROPERTY);
        model.add()
                .s("http://cultureUri#propertyEl2")
                .p(RdfSchema.SUBPROPERTYOF)
                .o("http://cultureUri#propertyEl1");
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl2")
                .p(RdfSchema.DOMAIN)
                .o("http://cultureUri#classEl1");
        model.add()
                .g("http://cultureUri#ngEl1")
                .s("http://cultureUri#propertyEl2")
                .p(RdfSchema.RANGE)
                .o("http://cultureUri#classEl2"); */

            model.add()
                    .g("http://cultureUri#ngEl1")
                    .s("http://cultureUri#classEl1")
                    .p(Rdf.TYPE)
                    .o(RdfSchema.CLASS);
            model.add()
                    .g("http://cultureUri#ngEl1")
                    .s("http://cultureUri#classEl2")
                    .p(Rdf.TYPE)
                    .o(RdfSchema.CLASS);
            model.add()
                    .g("http://cultureUri#ngEl1")
                    .s("http://cultureUri#propertyEl1")
                    .p(Rdf.TYPE)
                    .o(Rdf.PROPERTY);
            model.add()
                    .g("http://cultureUri#ngEl1")
                    .s("http://cultureUri#propertyEl1")
                    .p(RdfSchema.DOMAIN)
                    .o("http://cultureUri#classEl1");
            model.add()
                    .g("http://cultureUri#ngEl1")
                    .s("http://cultureUri#propertyEl1")
                    .p(RdfSchema.RANGE)
                    .o("http://cultureUri#classEl2");
        }
        return model;
    }
}
