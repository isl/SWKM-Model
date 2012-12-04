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

package gr.forth.ics.swkm.model2.importer;

import gr.forth.ics.swkm.model2.importer.RdfStores.Representation;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Arrays;
import java.util.Map;
import java.util.Iterator;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.sql.SQLException;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Transitively;

/**
 *
 * @author egiannak
 */
public class TestImporterHierarchyChange extends TestCase {
        private final String name;
        private final Representation repr;
        String[][] paths = {
            {"http://ngtest.org#", /*"C:/TestData/TestHierarchyChange.trig" */ "test/resources/TestHierarchyChange.trig"},
            {"http://ngtest2.org#", /*"C:/TestData/TestHierarchyChange2.trig" */ "test/resources/TestHierarchyChange2.trig"}
        };
        protected final Jdbc jdbc;
        private final Database db = Database.db1;
        public TestImporterHierarchyChange(String testName, Representation repr,
            String name) {
        super(testName);
        this.repr = repr;
        this.name = name;
        this.jdbc = new Jdbc(db.getDataSource());
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        for (Representation r : Arrays.asList(Representation.HYBRID)) {
            suite.addTest(new TestImporterHierarchyChange("test", r, "test schema"));
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
        // put the initialization og map classes here because we want to keep this mapping in all file tests
        final Map<String, Integer> classes = Maps.newHashMap();
        classes.put("http://www.w3.org/2000/01/rdf-schema#Resource", DbConstants.getIdFor(RdfSchema.RESOURCE));

        for (int i = 0; i < paths.length; i++) {
            System.out.println("Saving file: " + paths[i][1]);
            final Model model = TestUtils.createModel(paths[i][0], paths[i][1]);
            store.store(model);

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

            //check store of classes
            for (RdfNode classNode : model.findNodes(RdfType.CLASS))
            {
                if (((Resource)classNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)classNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
                int[][] map = TestUtils.checkClassInsertion(((Resource)classNode).getUri().getLocalName());
                int classId = map[0][0];
                int tripleId = map[0][1];
                System.out.println("Class " + ((Resource)classNode).getUri().toString(Uri.Delimiter.WITH) + " - id: " + classId
                        + ", associated triple - id: " + tripleId);
                assert classId!=0;
                assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                classes.put(((Resource)classNode).getUri().toString(Uri.Delimiter.WITH), classId);
            }

            // check ancestors (subclass, class_anc)
            for (RdfNode classNode : model.findNodes(RdfType.CLASS))
            {
                   int classId = classes.get(((Resource)classNode).getUri().toString(Uri.Delimiter.WITH));
                   int countAncestors = 0;
                   int countAncestorsDagFound = 0;
                   for (Inheritable parentNode : classNode.asInheritable().ancestors(Transitively.NO))
                   {
                       countAncestors++;
                       System.out.println("Class " + classNode.toString() + " has ancestor the: " + parentNode.toString());
                       int superClassId = classes.get(((Resource)parentNode).getUri().toString(Uri.Delimiter.WITH));
                       int tripleId = TestUtils.checkExistenceRelationshipDB("subclass", classId, superClassId);
                       assert tripleId!=0;
                       assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                       //check if DAG
                       tripleId = TestUtils.checkExistenceRelationshipDB("class_anc", classId, superClassId);
                       if (tripleId!=0)
                       {
                           System.out.println("Class " + classNode.toString() + " has DAG ancestor the: " + parentNode.toString());
                           countAncestorsDagFound++;
                           assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                           for (Inheritable childNode : classNode.asInheritable().descendants(Transitively.NO))
                           {
                               System.out.println("Class " + classNode.toString() + " has descendant the: " + childNode.toString());
                               int childClassId = classes.get(((Resource)childNode).getUri().toString(Uri.Delimiter.WITH));
                               int tripleChildId = TestUtils.checkExistenceRelationshipDB("class_anc", childClassId, superClassId);
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
