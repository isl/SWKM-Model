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

/**
 *
 * @author egiannak
 */

import gr.forth.ics.swkm.model2.importer.RdfStores.Representation;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.util.NamespaceDependenciesIndex;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Arrays;
import java.util.Map;
import java.util.Iterator;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.RdfNode;
import com.google.common.collect.Iterables;
import gr.forth.ics.swkm.model2.RdfType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.rdfsuite.services.db.JdbcMonitor;
import gr.forth.ics.taskmonitor.Monitor;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.io.RdfIO;
/**
 *
 * @author egiannak
 */
public class TestImporterGeneralSchemaAndData extends TestCase {
        private final String name;
        private final Representation repr;
        String[][] paths = {
            {"http://biologyexample.org#", "test/resources/TestBiologyExample.trig"},
            {"http://www.kp-lab.org/ontologies/CSM/test/DKM3#", "test/resources/DKM3.rdf"}
        };
        String [][] culturePath = {
            {"http://metaschemaUri#", "test/resources/metaschema.rdf"},
            {"http://adminUri#", "test/resources/admin.rdf"},
            {"http://cultureUri#", "test/resources/culture.rdf"},
            {"http://cultureDataUri#", "test/resources/culture_data.rdf"}
        };
        private static final boolean ENABLE_JDBC_MONITORING = true;
        protected final Jdbc jdbc;
        private final Database db = Database.db1;
        public TestImporterGeneralSchemaAndData(String testName, Representation repr,
            String name) {
        super(testName);
        this.repr = repr;
        this.name = name;
        this.jdbc = new Jdbc(db.getDataSource());
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        for (Representation r : Arrays.asList(Representation.HYBRID)) {
            suite.addTest(new TestImporterGeneralSchemaAndData("test", r, "test schema + data"));
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
            RdfIO.write(model, gr.forth.ics.swkm.model2.io.Format.RDFXML).toStream(System.out);
            RdfIO.write(model, gr.forth.ics.swkm.model2.io.Format.TRIG).toStream(System.out);
            store.store(model);
            checkStore(model);
        }

        final Model model = TestUtils.createModelMultipleFiles(culturePath);
        System.out.println("MODEL BEFORE IMPORT: " + model);
        store.store(model);
        System.out.println("MODEL AFTER IMPORT: " + model);
        checkStore(model);
    }

    private void checkStore(final Model model) throws Exception {
        jdbc.doInConnection(new ConnectionTask<Void>() {
            @Override
            public Void execute() throws SQLException {
                // check store of namespaces and namespace dependencies
                NamespaceDependenciesIndex index = new NamespaceDependenciesIndex(model);
                Iterator nsIterator = model.namespaces().iterator();
                while (nsIterator.hasNext())
                {
                    Uri ns = (Uri)nsIterator.next();
                    int namespaceId = TestUtils.checkExistenceNamespaceDB(ns.toString(Uri.Delimiter.WITH));
                    assert namespaceId!=0;

                    Iterator nsDependencies = index.getDependencies(ns, gr.forth.ics.swkm.model2.Transitively.NO).iterator();
                    while (nsDependencies.hasNext())
                    {
                        Uri nsDependency = (Uri)nsDependencies.next();
                        assert TestUtils.checkExistenceNamespaceDependencyDB(namespaceId, nsDependency.toString(Uri.Delimiter.WITH));
                    }
                }

                // check store of named graphs and graphsets
                for (gr.forth.ics.swkm.model2.Resource resource : model.namedGraphs())
                    if (!resource.is(RdfSuite.IMPORTER_SIDE_EFFECTS))
                    {
                        int namedGraphId = TestUtils.checkExistenceNamedGraphDB(resource.getUri().toString(Uri.Delimiter.WITH));
                        assert namedGraphId != 0;
                        assert TestUtils.checkExistenceGraphsetDB(namedGraphId);
                    }

                Map<String, Integer> classes = Maps.newHashMap();
                classes.put("http://www.w3.org/2000/01/rdf-schema#Resource", DbConstants.getIdFor(RdfSchema.RESOURCE));
                Map<String, Integer> properties = Maps.newHashMap();
                Map<String, Integer> metaclasses = Maps.newHashMap();
                metaclasses.put("http://www.w3.org/2000/01/rdf-schema#Resource", DbConstants.getIdFor(RdfSchema.RESOURCE));
                metaclasses.put("Class", DbConstants.getIdFor(RdfSchema.CLASS));
                metaclasses.put("DProperty", DbConstants.getIdFor(RdfSuite.DATA_PROPERTY));
                metaclasses.put("Property", DbConstants.getIdFor(Rdf.PROPERTY));
                metaclasses.put("LiteralType", DbConstants.getIdFor(RdfSchema.LITERAL));
                metaclasses.put("Thesaurus", DbConstants.getIdFor(RdfSuite.THESAURUS));
                metaclasses.put("Enumeration", DbConstants.getIdFor(RdfSuite.ENUMERATION));
                // check store of metaclasses, metaproperties
                for (RdfNode clazz : model.findNodes(RdfType.METACLASS, RdfType.METAPROPERTY)) {
                    Uri uri = ((Resource)clazz).getUri();
                    int[][] map = TestUtils.checkMetaClassInsertion(uri.getLocalName());
                    int classId = map[0][0];
                    int tripleId = map[0][1];
                    System.out.println("MetaClass " + uri.toString(Uri.Delimiter.WITH) + " - id: " + classId
                            + ", associated triple - id: " + tripleId);
                    assert classId!=0;
                    assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                    metaclasses.put(uri.toString(Uri.Delimiter.WITH), classId);
               }
                //check store of classes and resources
                for (RdfNode classNode : model.findNodes(RdfType.CLASS))
                {
                    if (!metaclasses.containsKey(((Resource)classNode).getUri().toString(Uri.Delimiter.WITH)))
                    {
                        int[][] map = TestUtils.checkClassInsertion(((Resource)classNode).getUri().getLocalName());
                        int classId = map[0][0];
                        int tripleId = map[0][1];
                        System.out.println("Class " + ((Resource)classNode).getUri().toString(Uri.Delimiter.WITH) + " - id: " + classId
                                + ", associated triple - id: " + tripleId);
                        assert classId!=0;
                        assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                        classes.put(((Resource)classNode).getUri().toString(Uri.Delimiter.WITH), classId);

                        for (gr.forth.ics.swkm.model2.views.IndividualView resource : classNode.asClass().instances())
                        {
                            int resourceTripleId = TestUtils.checkExistenceResourceDB(classId, ((Resource)resource).getUri().toString(Uri.Delimiter.WITH));
                            System.out.println("Resource " + ((Resource)resource).getUri().toString(Uri.Delimiter.WITH) + " is class instance of class: " + ((Resource)classNode).getUri().toString(Uri.Delimiter.WITH)
                                + ", associated triple - id: " + resourceTripleId);
                            assert resourceTripleId!=0;
                            assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(resourceTripleId);
                        }
                    }
                }

               for (RdfNode propertyNode : model.findNodes(RdfType.PROPERTY))
               {            
                    if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;

                    Resource property = model.mapResource(((Resource)propertyNode).getUri());
                    int[][] map = TestUtils.checkPropertyInsertion(((Resource)propertyNode).getUri().getLocalName());
                    int propertyId = map[0][0];
                    int tripleId = map[0][1];
                    System.out.println("Property " + ((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH) + " - id: " + propertyId
                            + ", associated triple - id: " + tripleId);
                    assert propertyId!=0;
                    assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                    properties.put(((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH), propertyId);
               }

               for (RdfNode propertyNode : model.findNodes(RdfType.PROPERTY))
               {
                   if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
                    Resource property = model.mapResource(((Resource)propertyNode).getUri());
                    int propertyId = properties.get(property.getUri().toString(Uri.Delimiter.WITH));

                    Triple domainTriple = Iterables.getOnlyElement(model.triples()
                                .s(property).p(RdfSchema.DOMAIN).fetch());
                    Triple rangeTriple = Iterables.getOnlyElement(model.triples()
                                .s(property).p(RdfSchema.RANGE).fetch());
                    Resource domain = (Resource)domainTriple.object();
                    Resource range = (Resource)rangeTriple.object();
                    
                    for (Triple propTriple : property.asProperty().propertyInstances())
                    {
                        //att0 subject
                        //att1 object
                        //att2 property id
                        //att3 triple id
                        if (!propTriple.subject().isResource())
                            continue;

                        int subjectId = DbConstants.getTripleSubjectRQLKindId(propTriple.subject().type());
                        int objectId;
                        int propTripleId = 0;

                        if (propTriple.object().type().equals(RdfType.INDIVIDUAL))
                            objectId = DbConstants.getRqlKindFor(RdfSchema.RESOURCE);
                        else
                            objectId = DbConstants.getTripleRangeRQLKindId(range);

                        PreparedStatement pstmt = Jdbc.prepared(String.format("SELECT att3 FROM tp%dk%d WHERE att0 = ? and att2 = %d", subjectId, objectId, propertyId));
                        System.out.println("Property instance of  property - id: " + propertyId
                            + ", subject: " + propTriple.subject() + ", object: "+ propTriple.object());
                        if (propTriple.subject().type().equals(RdfType.INDIVIDUAL))
                            pstmt.setString(1, ((Resource)propTriple.subject()).getUri().toString());
                        else if (propTriple.subject().type().equals(RdfType.CLASS))
                            pstmt.setInt(1, classes.get(((Resource)propTriple.subject()).getUri().toString(Uri.Delimiter.WITH)));
                        else if (propTriple.subject().type().equals(RdfType.PROPERTY))
                            pstmt.setInt(1, properties.get(((Resource)propTriple.subject()).getUri().toString(Uri.Delimiter.WITH)));
                        System.out.println(pstmt.toString());
                        ResultSet rs = pstmt.executeQuery();
                        try {
                            if (rs.next())
                            {
                                propTripleId = rs.getInt(1);
                            }
                            else
                                propTripleId = 0;
                        }
                        catch (SQLException e)
                        {
                            throw e;
                        }
                        finally
                        {
                            rs.close();
                            pstmt.close();
                        }
                        System.out.println("Property instance of  property - id: " + propertyId
                            + ", subject: " + propTriple.subject() + ", triple -id: "+ propTripleId);
                        assert propTripleId!=0;
                        assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(propTripleId);
                    }
               }

               for (Triple triple : model.triples().p(RdfSchema.COMMENT).fetch())
               {
                   int tripleId = 0;
                   if (triple.subject().type().equals(RdfType.CLASS))
                       tripleId = TestUtils.checkExistenceClassCommentDB(classes.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), ((LiteralNode)triple.object()).getLiteral().getValue());
                   else if (triple.subject().type().equals(RdfType.PROPERTY))
                       tripleId = TestUtils.checkExistencePropertyCommentDB(properties.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), ((LiteralNode)triple.object()).getLiteral().getValue());
                   else if (triple.subject().type().equals(RdfType.INDIVIDUAL))
                       tripleId = TestUtils.checkExistenceResourceCommentDB(((Resource)triple.subject()).getUri().toString(), ((LiteralNode)triple.object()).getLiteral().getValue());

                   System.out.println("Comment - Subject: " + triple.subject()
                            + ", comment: " + triple.object() + ", triple -id: "+ tripleId);
                   assert tripleId!=0;
                   assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
               }

               for (Triple triple : model.triples().p(RdfSchema.ISDEFINEDBY).fetch())
               {
                   int tripleId = 0;
                   if (triple.subject().type().equals(RdfType.CLASS))
                       tripleId = TestUtils.checkExistenceClassIsDefinedByDB(classes.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), classes.get(((Resource)triple.object()).getUri().toString(Uri.Delimiter.WITH)));
                   else if (triple.subject().type().equals(RdfType.PROPERTY))
                       tripleId = TestUtils.checkExistencePropertyIsDefinedByDB(properties.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), properties.get(((Resource)triple.object()).getUri().toString(Uri.Delimiter.WITH)));
                   else if (triple.subject().type().equals(RdfType.INDIVIDUAL))
                       tripleId = TestUtils.checkExistenceResourceIsDefinedByDB(((Resource)triple.subject()).getUri().toString(), ((Resource)triple.object()).getUri().toString());

                   System.out.println("Is Defined By - Subject: " + triple.subject()
                            + ", is defined by: " + triple.object() + ", triple -id: "+ tripleId);
                   assert tripleId!=0;
                   assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
               }

               for (Triple triple : model.triples().p(RdfSchema.LABEL).fetch())
               {
                   int tripleId = 0;
                   if (triple.subject().type().equals(RdfType.CLASS))
                       tripleId = TestUtils.checkExistenceClassLabelDB(classes.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), ((LiteralNode)triple.object()).getLiteral().getValue());
                   else if (triple.subject().type().equals(RdfType.PROPERTY))
                       tripleId = TestUtils.checkExistencePropertyLabelDB(properties.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), ((LiteralNode)triple.object()).getLiteral().getValue());
                   else if (triple.subject().type().equals(RdfType.INDIVIDUAL))
                       tripleId = TestUtils.checkExistenceResourceLabelDB(((Resource)triple.subject()).getUri().toString(), ((LiteralNode)triple.object()).getLiteral().getValue());

                   System.out.println("Label - Subject: " + triple.subject()
                            + ", label: " + triple.object() + ", triple -id: "+ tripleId);
                   assert tripleId!=0;
                   assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
               }

               for (Triple triple : model.triples().p(RdfSchema.SEEALSO).fetch())
               {
                   int tripleId = 0;
                   if (triple.subject().type().equals(RdfType.CLASS))
                       tripleId = TestUtils.checkExistenceClassSeeAlsoDB(classes.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), classes.get(((Resource)triple.object()).getUri().toString(Uri.Delimiter.WITH)));
                   else if (triple.subject().type().equals(RdfType.PROPERTY))
                       tripleId = TestUtils.checkExistencePropertySeeAlsoDB(properties.get(((Resource)triple.subject()).getUri().toString(Uri.Delimiter.WITH)), properties.get(((Resource)triple.object()).getUri().toString(Uri.Delimiter.WITH)));
                   else if (triple.subject().type().equals(RdfType.INDIVIDUAL))
                       tripleId = TestUtils.checkExistenceResourceSeeAlsoDB(((Resource)triple.subject()).getUri().toString(), ((Resource)triple.object()).getUri().toString());

                   System.out.println("See also - Subject: " + triple.subject()
                            + ", see also: " + triple.object() + ", triple -id: "+ tripleId);
                   assert tripleId!=0;
                   assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
               }

                // check ancestors (subclass, class_anc)
                for (RdfNode classNode : model.findNodes(RdfType.CLASS))
                {
                   System.out.println("Checking ancestors for Class: " + classNode.toString());
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

                // check ancestors (submetaclass, metaclass_anc)
                for (RdfNode classNode : model.findNodes(RdfType.METACLASS))
                {
                   System.out.println("Checking ancestors for MetaClass: " + classNode.toString());

                   if (((Resource)classNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)classNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;

                   int classId = metaclasses.get(((Resource)classNode).getUri().toString(Uri.Delimiter.WITH));
                   int countAncestors = 0;
                   int countAncestorsDagFound = 0;
                   for (Inheritable parentNode : classNode.asInheritable().ancestors(Transitively.NO))
                   {
                       countAncestors++;
                       System.out.println("MetaClass " + classNode.toString() + " has ancestor the: " + parentNode.toString());
                       int superClassId = metaclasses.get(((Resource)parentNode).getUri().toString(Uri.Delimiter.WITH));
                       int tripleId = TestUtils.checkExistenceRelationshipDB("submetaclass", classId, superClassId);
                       assert tripleId!=0;
                       assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                       //check if DAG
                       tripleId = TestUtils.checkExistenceRelationshipDB("metaclass_anc", classId, superClassId);
                       if (tripleId!=0)
                       {
                           System.out.println("MetaClass " + classNode.toString() + " has DAG ancestor the: " + parentNode.toString());
                           countAncestorsDagFound++;
                           assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                           for (Inheritable childNode : classNode.asInheritable().descendants(Transitively.NO))
                           {
                               System.out.println("MetaClass " + classNode.toString() + " has descendant the: " + childNode.toString());
                               int childClassId = metaclasses.get(((Resource)childNode).getUri().toString(Uri.Delimiter.WITH));
                               int tripleChildId = TestUtils.checkExistenceRelationshipDB("metaclass_anc", childClassId, superClassId);
                               assert tripleChildId!=0;
                               assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleChildId);
                           }
                       }
                   }
                   if (countAncestors>0) assert (countAncestors==(countAncestorsDagFound + 1));
                }

                // check ancestors (subproperty, property_anc)
                for (RdfNode propertyNode : model.findNodes(RdfType.PROPERTY))
                {
                    System.out.println("Checking ancestors for Property: " + propertyNode.toString());
                    if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
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

                // check ancestors (submetaproperty, metaproperty_anc)
                for (RdfNode propertyNode : model.findNodes(RdfType.METAPROPERTY))
                {
                    System.out.println("Checking ancestors for MetaProperty: " + propertyNode.toString());
                    if (((Resource)propertyNode).getUri().getNamespaceUri().equals(Rdf.NAMESPACE) ||
                            ((Resource)propertyNode).getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
                        continue;
                
                   int propertyId = metaclasses.get(((Resource)propertyNode).getUri().toString(Uri.Delimiter.WITH));
                   int countAncestors = 0;
                   int countAncestorsDagFound = 0;
                   for (Inheritable parentNode : propertyNode.asInheritable().ancestors(Transitively.NO))
                   {
                       countAncestors++;
                       System.out.println("MetaProperty " + propertyNode.toString() + " has ancestor the: " + parentNode.toString());
                       int superPropertyId = metaclasses.get(((Resource)parentNode).getUri().toString(Uri.Delimiter.WITH));
                       int tripleId = TestUtils.checkExistenceRelationshipDB("submetaproperty", propertyId, superPropertyId);
                       assert tripleId!=0;
                       assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                       //check if DAG
                       tripleId = TestUtils.checkExistenceRelationshipDB("metaproperty_anc", propertyId, superPropertyId);
                       if (tripleId!=0)
                       {
                           System.out.println("MetaProperty " + propertyNode.toString() + " has DAG ancestor the: " + parentNode.toString());
                           countAncestorsDagFound++;
                           assert TestUtils.checkExistenceAssociationTripleNamedGraphDB(tripleId);
                           for (Inheritable childNode : propertyNode.asInheritable().descendants(Transitively.NO))
                           {
                               System.out.println("MetaProperty " + propertyNode.toString() + " has descendant the: " + childNode.toString());
                               int childClassId = metaclasses.get(((Resource)childNode).getUri().toString(Uri.Delimiter.WITH));
                               int tripleChildId = TestUtils.checkExistenceRelationshipDB("metaproperty_anc", childClassId, superPropertyId);
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
