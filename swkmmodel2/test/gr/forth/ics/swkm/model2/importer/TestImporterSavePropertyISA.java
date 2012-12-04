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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.forth.ics.swkm.model2.importer;

/**
 *
 * @author egiannak
 */

import gr.forth.ics.swkm.model2.importer.RdfStores.Representation;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import java.util.Arrays;
import java.util.Map;
import java.util.Iterator;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.sql.SQLException;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;

public class TestImporterSavePropertyISA extends TestCase {
        private final String name;
        private final Representation repr;
        String[][] paths = {
            {"http://ngtest.org#", "test/resources/TestPropertySubAnc.trig"}
        };
        protected final Jdbc jdbc;
        private final Database db = Database.db1;

        public TestImporterSavePropertyISA(String testName, Representation repr,
            String name) {
        super(testName);
        this.repr = repr;
        this.name = name;
        this.jdbc = new Jdbc(db.getDataSource());
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
//        for (ModelStub model : Models.allCultureModels()) {
//            suite.addTest(new ImporterTest("test", model));
//        }
//        for (Representation r : Representation.values()) {
        for (Representation r : Arrays.asList(Representation.HYBRID)) {
            suite.addTest(new TestImporterSavePropertyISA("test", r, "test schema"));
        }
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        db.erase();
        RdfStore store = RdfStores.get(repr,
                    db.getDataSource(), Configurers.newDefault());
        store.initializeSchemaIfNeeded();
    }

    public void test() throws Exception {
        setName(getName() + "_" + name + " with " + repr);
        storeNew(Database.db1, repr);
    }

    private void storeNew(Database db, Representation repr) throws Exception {
        RdfStore store = RdfStores.get(repr, db.getDataSource(), Configurers.newDefault());

        for (int i = 0; i < paths.length; i++) {
            System.out.println(paths[i][0]);
            final Model model = TestUtils.createModel(paths[i][0], paths[i][1]);
            System.out.println("Model before importer: " + model);
            store.store(model);
            System.out.println("Model after importer: " + model);
            jdbc.doInConnection(new ConnectionTask<Void>() {
            @Override
            public Void execute() throws SQLException {
                Iterator nsIterator = model.namespaces().iterator();
                while (nsIterator.hasNext())
                {
                    Uri ns = (Uri)nsIterator.next();
                    assert TestUtils.checkExistenceNamespaceDB(ns.toString(Uri.Delimiter.WITH))!=0;
                }

                for (gr.forth.ics.swkm.model2.Resource resource : model.namedGraphs())
                    if (!resource.is(RdfSuite.IMPORTER_SIDE_EFFECTS))
                        assert TestUtils.checkExistenceNamedGraphDB(resource.getUri().toString(Uri.Delimiter.WITH))!=0;

                Map<String, Integer> properties = Maps.newHashMap();

                //check store of properties
                for (RdfNode propertyNode : model.findNodes(RdfType.PROPERTY))
                {
                    if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
                    int[][] map = TestUtils.checkPropertyInsertion(((Resource)propertyNode).getUri().getLocalName());
                    int propertyId = map[0][0];
                    int tripleId = map[0][1];
                    System.out.println("Property " + ((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH) + " - id: " + propertyId
                            + ", associated triple - id: " + tripleId);
                    assert propertyId!=0;
                    assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                    properties.put(((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH), propertyId);
                }

                // check ancestors (subproperty, property_anc)
                for (RdfNode propertyNode : model.findNodes(RdfType.PROPERTY))
                {
                    if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
                   System.out.println("Checking ancestors for: " + propertyNode.toString());
                   int propertyId = properties.get(((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH));
                   int countAncestors = 0;
                   int countAncestorsDagFound = 0;
                   for (Inheritable parentNode : propertyNode.asInheritable().ancestors(Transitively.NO))
                   {
                       countAncestors++;
                       System.out.println("Property " + propertyNode.toString() + " has ancestor the: " + parentNode.toString());
                       int superPropertyId = properties.get(((Resource)parentNode).getUri().toString(Uri.Delimiter.WITH));
                       int tripleId = TestUtils.checkExistenceRelationshipDB("subproperty", propertyId, superPropertyId);
                       assert tripleId!=0;
                       assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                       //check if DAG
                       tripleId = TestUtils.checkExistenceRelationshipDB("property_anc", propertyId, superPropertyId);
                       if (tripleId!=0)
                       {
                           System.out.println("Property " + propertyNode.toString() + " has DAG ancestor the: " + parentNode.toString());
                           countAncestorsDagFound++;
                           assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                           for (Inheritable childNode : propertyNode.asInheritable().descendants(Transitively.NO))
                           {
                               System.out.println("Property " + propertyNode.toString() + " has descendant the: " + childNode.toString());
                               int childClassId = properties.get(((Resource)childNode).getUri().toString(Uri.Delimiter.WITH));
                               int tripleChildId = TestUtils.checkExistenceRelationshipDB("property_anc", childClassId, superPropertyId);
                               assert tripleChildId!=0;
                               assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleChildId);
                           }
                       }
                   }
                   if (countAncestors>0) assert (countAncestors==(countAncestorsDagFound + 1));
                }
            return null;
            }
            });
        }
    }
}
