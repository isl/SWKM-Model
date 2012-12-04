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


package gr.forth.ics.swkm.model2;

import com.google.common.collect.ImmutableSet;
import gr.forth.ics.swkm.model2.util.RandomTripleGenerator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.event.EmptyRdfNodeListener;
import gr.forth.ics.swkm.model2.event.TripleListener;
import gr.forth.ics.swkm.model2.event.TypeChange;
import gr.forth.ics.swkm.model2.index.ModelIndexer;
import gr.forth.ics.swkm.model2.index.ModelIndexers;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class ModelTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public ModelTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(RefImplIndexerTest.class);
        suite.addTestSuite(NodeListIndexerTest.class);
        suite.addTestSuite(MultimapsIndexerTest.class);
        suite.addTestSuite(TreeMapIndexerTest.class);
        return suite;
    }

    private Model model;
    private Model referenceModel;
    protected abstract ModelIndexer createIndex();
    private Iterator<Triple> tripleGenerator;

    @Override
    protected void setUp() {
        model = new ModelImpl(createIndex());
        referenceModel = ModelBuilder.newHorizontal().build();
        tripleGenerator = RandomTripleGenerator.newDefault().triplesFor(referenceModel);
    }

    private void importTriple(Triple triple) {
        final ObjectNode s = triple.subject().mappedTo(model);
        final Resource p = triple.predicate().mappedTo(model);
        final RdfNode o = triple.object().mappedTo(model);
        for (Resource g : triple.graphs()) {
            model.add(
                g.mappedTo(model),
                s, p, o);
        }
    }

    private static Comparator<Resource> comp = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private void assertEquals(Triples expected, Triples actual) {
        Set<String> expectedStrings = new TreeSet<String>();
        for (Triple t : expected) {
            Set<Resource> orderedGraphs = Sets.newTreeSet(comp);
            Iterables.addAll(orderedGraphs, t.graphs());
            expectedStrings.add(t.toSimpleString() + orderedGraphs);
        }

        Set<String> actualStrings = new TreeSet<String>();
        for (Triple t : actual) {
            Set<Resource> orderedGraphs = Sets.newTreeSet(comp);
            Iterables.addAll(orderedGraphs, t.graphs());
            actualStrings.add(t.toSimpleString() + orderedGraphs);
        }
        assertEquals(expectedStrings, actualStrings);
        assertEquals(Iterables.size(expected), Iterables.size(actual));
    }

    private static final int TRIPLES = 30;

    private List<WeakReference<Triple>> performQueriesAndDeletesTest() {
        Set<RdfNode> nodes = Sets.newHashSetWithExpectedSize(TRIPLES * 2);
        for (int i = 0; i < TRIPLES; i++) {
            Triple triple = tripleGenerator.next();
            importTriple(triple);
            Iterables.addAll(nodes, triple.nodes());
        }
        //{s}
        for (RdfNode subject : nodes) {
            if (!subject.isObjectNode()) {
                continue;
            }
            Triples expected = referenceModel.triples()
                    .s((ObjectNode)subject)
                    .fetch();
            Triples actual = model.triples()
                    .s((ObjectNode)subject.mappedTo(model))
                    .fetch();
            assertEquals(expected, actual);
        }

        //{s, p}
        for (RdfNode subject : nodes) {
            if (!subject.isObjectNode()) {
                continue;
            }
            for (RdfNode predicate : nodes) {
                if (!predicate.isResource()) {
                    continue;
                }
                Triples expected = referenceModel.triples()
                        .s((ObjectNode)subject)
                        .p((Resource)predicate)
                        .fetch();
                Triples actual = model.triples()
                        .s((ObjectNode)subject.mappedTo(model))
                        .p((Resource)predicate.mappedTo(model))
                        .fetch();
                assertEquals(expected, actual);
            }
        }
        
        //{s, o}
        for (RdfNode subject : nodes) {
            if (!subject.isObjectNode()) {
                continue;
            }
            for (RdfNode object : nodes) {
                Triples expected = referenceModel.triples()
                        .s((ObjectNode)subject)
                        .o(object)
                        .fetch();
                Triples actual = model.triples()
                        .s((ObjectNode)subject.mappedTo(model))
                        .o(object.mappedTo(model))
                        .fetch();
                assertEquals(expected, actual);
            }
        }

        //{s, p, o}
        for (RdfNode subject : nodes) {
            if (!subject.isObjectNode()) {
                continue;
            }
            for (RdfNode predicate : nodes) {
                if (!predicate.isResource()) {
                    continue;
                }
                for (RdfNode object : nodes) {
                    Triples expected = referenceModel.triples()
                            .s((ObjectNode)subject)
                            .p((Resource)predicate)
                            .o(object)
                            .fetch();
                    Triples actual = model.triples()
                            .s((ObjectNode)subject.mappedTo(model))
                            .p((Resource)predicate.mappedTo(model))
                            .o(object.mappedTo(model))
                            .fetch();
                    assertEquals(expected, actual);
                }
            }
        }

        //{p}
        for (RdfNode predicate : nodes) {
            if (!predicate.isResource()) {
                continue;
            }
            Triples expected = referenceModel.triples()
                    .s((Resource)predicate)
                    .fetch();
            Triples actual = model.triples()
                    .s((Resource)predicate.mappedTo(model))
                    .fetch();
            assertEquals(expected, actual);
        }

        //{p, o}
        for (RdfNode predicate : nodes) {
            if (!predicate.isResource()) {
                continue;
            }
            for (RdfNode object : nodes) {
                Triples expected = referenceModel.triples()
                        .p((Resource)predicate)
                        .o(object)
                        .fetch();
                Triples actual = model.triples()
                        .p((Resource)predicate.mappedTo(model))
                        .o(object.mappedTo(model))
                        .fetch();
                assertEquals(expected, actual);
            }
        }

        //{o}
        for (RdfNode object : nodes) {
            Triples expected = referenceModel.triples()
                    .o(object)
                    .fetch();
            Triples actual = model.triples()
                    .o(object.mappedTo(model))
                    .fetch();
            assertEquals(expected, actual);
        }

        //{}
        {
            Triples expected = referenceModel.triples().fetch();
            Triples actual = model.triples().fetch();
            assertEquals(expected, actual);
        }

        List<WeakReference<Triple>> weakRefs = Lists.newArrayList();
        for (Triple t : Lists.newArrayList(model.triples().fetch())) {
            weakRefs.add(new WeakReference<Triple>(t));
            boolean changed = model.triples().s(t.subject()).p(t.predicate()).o(t.object()).delete();
            assertTrue(changed);
        }
        model.retypeNodes();
        return weakRefs;
    }

    public void testQueriesAndMemoryLeaks() throws Throwable {
        List<WeakReference<Triple>> weakRefs = performQueriesAndDeletesTest();

        //test deletions leak no memory
        for (int i = 0; i < 100; i++) {
            System.gc();
        }
        for (WeakReference<Triple> ref : weakRefs) {
            try {
                assertNull(String.valueOf(ref.get()), ref.get());
            } catch (Throwable t) {
//                System.err.println("TAKE HEAP DUMP: " + ref.get());
//                Thread.sleep(60000);
                throw t;
            }
        }
    }
    
    //Mapped nodes must preserve identity even before they are added
    //as part of a triple in a model. That is because if two separate nodes
    //were created for, say, the same URI, and subsequently both
    //nodes were used to add two triples referring to that URI,
    //the model would have two nodes with the same URI, an
    //inconsistency.
    public void testNodeIdentity() {
        assert model.map(ns + "test") == model.map(ns + "test");
        assert model.mapBlankNode(ns + "id") == model.mapBlankNode(ns + "id");
        assert model.mapLiteral(Literal.create("5")) == model.mapLiteral(Literal.create("5"));
        assert model.mapObjectNode(ns + "uri") == model.mapObjectNode(ns + "uri");
        assert model.mapResource(Uri.parse(ns + "uri")) == model.mapResource(Uri.parse(ns + "uri"));
    }
    
    public void testDeletions() {
        for (int i = 0; i < 100; i++) {
            Triple triple = tripleGenerator.next();
            importTriple(triple);
        }
        assert model.triples().fetch().iterator().hasNext() == true;
        model.triples().delete();
        assert model.triples().fetch().iterator().hasNext() == false;
    }

    public void testDeleteNonExistingTriples() {
        model.triples().s(ns + "s").p(ns + "p").o(ns + "o").delete();
    }

    public void testDoesNotReportTriplesTwice() {
        ObjectNode s = model.mapResource(ns + "s");
        Resource p = model.mapResource(ns + "p");
        RdfNode o = model.mapResource(ns + "o");

        Resource g1 = model.mapResource(ns + "g1");
        Resource g2 = model.mapResource(ns + "g2");

        model.add().g(g1).s(s).p(p).o(o);
        model.add().g(g2).s(s).p(p).o(o);

        assertEquals(1, count(model.triples().s(s).fetch(), s, p, o));
        assertEquals(1, count(model.triples().p(p).fetch(), s, p, o));
        assertEquals(1, count(model.triples().o(o).fetch(), s, p, o));
        assertEquals(1, count(model.triples().s(s).p(p).fetch(), s, p, o));
        assertEquals(1, count(model.triples().s(s).o(o).fetch(), s, p, o));
        assertEquals(1, count(model.triples().p(p).o(o).fetch(), s, p, o));
        assertEquals(1, count(model.triples().s(s).p(p).o(o).fetch(), s, p, o));
    }

    private static int count(Triples triples, ObjectNode s, Resource p, RdfNode o) {
        int count = 0;
        for (Triple t : triples) {
            if (t.subject() == s && t.predicate() == p && t.object() == o) {
                count++;
            }
        }
        return count;
    }

    public void testFindNodesDoesNotReportOrphanNodes() {
        Resource r1 = model.add().newClass(ns + "myClass");

        assert Sets.newHashSet(model.findNodes(RdfType.CLASS)).contains(r1);

        r1.subjectTriples().delete();

        assert !Sets.newHashSet(model.findNodes(RdfType.CLASS)).contains(r1);
    }
    
    public void testNamespaceQueries() {
        Resource class1 = model.add().newClass("http://ns1#class1");
        Resource class2 = model.add().newClass("http://ns2#class2");
        Resource property1 = model.add().newProperty("http://ns1#property1");
        Resource property2 = model.add().newProperty("http://ns2#property2");
        Resource alt = model.add().newAlt("http://ns3#alt");

        assert Sets.newHashSet(model.findSchemaNodes(new Uri("http://ns1#", ""), RdfType.CLASS))
                .equals(ImmutableSet.of(class1));
        assert Sets.newHashSet(model.findSchemaNodes(new Uri("http://ns2#", ""), RdfType.CLASS))
                .equals(ImmutableSet.of(class2));
        assert Sets.newHashSet(model.findSchemaNodes(Uri.parse("http://ns1#"), RdfType.PROPERTY))
                .equals(ImmutableSet.of(property1));
        assert Sets.newHashSet(model.findSchemaNodes(Uri.parse("http://ns2#"), RdfType.PROPERTY))
                .equals(ImmutableSet.of(property2));

        assert Sets.newHashSet(model.findSchemaNodes(Uri.parse("http://ns1#"), RdfType.CLASS, RdfType.PROPERTY))
                .equals(ImmutableSet.of(class1, property1));
        
        try {
            model.findSchemaNodes(Uri.parse("http://ns2#"), RdfType.INDIVIDUAL, RdfType.PROPERTY);
            fail();
        } catch (IllegalArgumentException ok) { }

        try {
            model.findSchemaNodes(Uri.parse("http://ns3#"), RdfType.ALT);
            fail();
        } catch (IllegalArgumentException ok) { }

        model.triples().s(class1).delete();
        assert Sets.newHashSet(model
                .findSchemaNodes(Uri.parse("http://ns1#"), RdfType.CLASS, RdfType.PROPERTY))
                .equals(ImmutableSet.of(property1));
        
        model.triples().s(property1).delete();
        assert Sets.newHashSet(model
                .findSchemaNodes(Uri.parse("http://ns1#"), RdfType.CLASS, RdfType.PROPERTY))
                .isEmpty();
    }

    public void testNamespaces() {
        Resource class1 = model.add().newClass("http://ns1#class1");
        assert model.namespaces().equals(
                Sets.newHashSet(Uri.parse("http://ns1#"), RdfSchema.NAMESPACE));

        Resource class2 = model.add().newClass("http://ns2#class2");
        assert model.namespaces().equals(
                Sets.newHashSet(Uri.parse("http://ns1#"), Uri.parse("http://ns2#"), RdfSchema.NAMESPACE));

        Resource alt = model.add().newAlt("http://ns3#alt");
        assert model.namespaces().equals(
                Sets.newHashSet(Uri.parse("http://ns1#"), Uri.parse("http://ns2#"),
                RdfSchema.NAMESPACE, Rdf.NAMESPACE));

        class1.subjectTriples().delete();

        assert model.namespaces().equals(Sets.newHashSet(
                Uri.parse("http://ns2#"), RdfSchema.NAMESPACE, Rdf.NAMESPACE));
    }

    public void testTypeEvents() {
        class MyListener extends EmptyRdfNodeListener {
            Map<RdfNode, RdfType> nodesToOldTypes = Maps.newHashMap();
            @Override
            public void onTypeChange(TypeChange change) {
                nodesToOldTypes.put(change.node(), change.oldType());
            }
        }
        MyListener listener = new MyListener();
        model.addRdfNodeListener(listener);
        ObjectNode myClass = model.add().s(ns + "myClass").p(Rdf.TYPE).o(RdfSchema.CLASS).subject();
        assert listener.nodesToOldTypes.get(myClass) == RdfType.UNKNOWN;
        
        //turning the class to a metaclass
        model.add().s(myClass).p(RdfSchema.SUBCLASSOF).o(RdfSchema.CLASS);
        assert listener.nodesToOldTypes.get(myClass) == RdfType.CLASS;

        //now verify that the listener stops listening
        listener.nodesToOldTypes.clear();
        model.removeRdfNodeListener(listener);
        model.add().newClass(ns + "anotherClass");
        assert listener.nodesToOldTypes.isEmpty();
    }

    public void testNodeAdditionEvents() {
        class MyListener extends EmptyRdfNodeListener {
            Set<RdfNode> added = Sets.newHashSet();
            @Override
            public void onNodeAddition(RdfNode node) {
                added.add(node);
            }

            @Override
            public void onNodeDeletion(RdfNode node) {
                added.remove(node);
            }
        }
        MyListener listener = new MyListener();
        model.addRdfNodeListener(listener);
        Resource r = model.mapResource(ns + "r");
        assert listener.added.size() == 0;

        model.add().g(ns + "g").s(r).p(ns + "p").o(ns + "o");
        assert listener.added.size() == 4;

        model.add().g(ns + "g1").s(r).p(ns + "p1").o(ns + "o1");
        assert listener.added.size() == 4 + 3;

        model.triples().g(ns + "g1").s(r).p(ns + "p1").o(ns + "o1").delete();
        assert listener.added.size() == 4;

        Triple t = model.add().g(ns + "g1").s(r).p(ns + "p1").o(ns + "o1");
        assert listener.added.size() == 4 + 3;

        t.delete();
        assert listener.added.size() == 4;
        
        model.triples().g(ns + "g").s(r).p(ns + "p").o(ns + "o").delete();
        assert listener.added.size() == 0;
    }

    public void testNodeDeletionEvents() {
        class MyListener extends EmptyRdfNodeListener {
            Set<RdfNode> deleted = Sets.newHashSet();
            @Override
            public void onNodeDeletion(RdfNode node) {
                deleted.add(node);
            }
        }
        MyListener listener = new MyListener();
        model.addRdfNodeListener(listener);

        model.add().g(ns + "g").s(ns + "s").p(ns + "p").o(ns + "o");
        model.triples().g(ns + "g").s(ns + "s").p(ns + "p").o(ns + "o").delete();

        assert listener.deleted.size() == 4;
    }

    public void testTripleReturnedOnlyOnceWhenMatchedInMultipleGraphs() {
        model.add().g(ns + "g1").s(ns + "s").p(ns + "p").o(ns + "o");
        model.add().g(ns + "g2").s(ns + "s").p(ns + "p").o(ns + "o");

        int count = 0;
        for (Triple t : model.triples().s(ns + "s").p(ns + "p").o(ns + "o").fetch()) {
            count++;
        }
        assert count == 1;
    }

    public void testSimpleTripleDeletion() {
        model.add().s(ns + "s").p(ns + "p").o(ns + "o");

        assert true == model.triples().s(ns + "s").p(ns + "p").o(ns + "o").delete();
    }

    public void testQuadTripleDeletion() {
        model.add().g(ns + "g").s(ns + "s").p(ns + "p").o(ns + "o");

        assert true == model.triples().g(ns + "g").s(ns + "s").p(ns + "p").o(ns + "o").delete();
    }

    public void testTripleEvents() {
        final Resource[] namedGraphHolder = new Resource[1];
        final Triple[] tripleHolder = new Triple[1];
        final boolean[] addedHolder = new boolean[1];

        model.addTripleListener(new TripleListener() {
            public void onTripleAddition(Resource namedGraph, Triple triple) {
                addedHolder[0] = true;
                namedGraphHolder[0] = namedGraph;
                tripleHolder[0] = triple;
            }

            public void onTripleDeletion(Resource namedGraph, Triple triple) {
                addedHolder[0] = false;
                namedGraphHolder[0] = namedGraph;
                tripleHolder[0] = triple;
            }
        });

        Triple t = model.add().s(ns + "s").p(ns + "p").o(ns + "o");
        assert namedGraphHolder[0] == Iterables.getOnlyElement(t.graphs());
        assert tripleHolder[0] == t;
        assert addedHolder[0] == true;

        namedGraphHolder[0] = null;
        tripleHolder[0] = null;

        Resource g = model.mapResource(ns + "g");
        model.add().g(g).s(ns + "s").p(ns + "p").o(ns + "o");
        
        assert namedGraphHolder[0] == g;
        assert tripleHolder[0] == t;

        assert true == model.triples().g(ns + "g").s(ns + "s").p(ns + "p").o(ns + "o").delete();

        assert namedGraphHolder[0] == g;
        assert tripleHolder[0] == t;
        assert addedHolder[0] == false;

        g = Iterables.getOnlyElement(t.graphs());
        assert true == model.triples().g(g).s(ns + "s").p(ns + "p").o(ns + "o").delete();
        //but not it is deleted
        assert namedGraphHolder[0] == g;
        assert tripleHolder[0] == t;
        assert addedHolder[0] == false;
    }

    public void testInferable1() {
        model.add().s(ns + "s").p(ns + "p").o(ns + "o");
        assert model.isInferable(
                model.mapObjectNode(ns + "s"),
                model.mapResource(ns + "p"),
                model.map(ns + "o"));
    }

    public void testInferable2a() {
        model.add().s(ns + "s").p(RdfSchema.SUBCLASSOF).o(ns + "o1");
        model.add().s(ns + "o1").p(RdfSchema.SUBCLASSOF).o(ns + "o2");
        assert model.isInferable(
                model.mapObjectNode(ns + "s"),
                model.mapResource(RdfSchema.SUBCLASSOF),
                model.map(ns + "o2"));
    }

    public void testInferable2b() {
        model.add().s(ns + "s").p(RdfSchema.SUBPROPERTYOF).o(ns + "o1");
        model.add().s(ns + "o1").p(RdfSchema.SUBPROPERTYOF).o(ns + "o2");
        assert model.isInferable(
                model.mapObjectNode(ns + "s"),
                model.mapResource(RdfSchema.SUBPROPERTYOF),
                model.map(ns + "o2"));
    }

    public void testInferable3() {
        model.add().s(ns + "s").p(Rdf.TYPE).o(ns + "o1");
        model.add().s(ns + "o1").p(RdfSchema.SUBCLASSOF).o(ns + "o2");
        assert model.isInferable(
                model.mapObjectNode(ns + "s"),
                model.mapResource(Rdf.TYPE),
                model.map(ns + "o2"));
    }

    public void testInferable4() {
        model.add().s(ns + "s").p(ns + "p2").o(ns + "o");
        model.add().s(ns + "p2").p(RdfSchema.SUBPROPERTYOF).o(ns + "p1");
        assert model.isInferable(
                model.mapObjectNode(ns + "s"),
                model.mapResource(ns + "p1"),
                model.map(ns + "o"));
    }
    
    public void testTripleCount() {
        model.add().s(ns + "s1").p(ns + "p1").o(ns + "o1");
        model.add().s(ns + "s2").p(ns + "p2").o(ns + "o2");
        assert model.tripleCount() == 2;
    }
    
    public void testTripleCountInNamedGraph() {
        model.add().g(ns + "g1").s(ns + "s").p(ns + "p").o(ns + "o");
        model.add().g(ns + "g2").s(ns + "s").p(ns + "p").o(ns + "o");
        assert model.tripleCount() == 1;
    }

    public void testBehaviorWhenDeletingFromNonExistingGraph() {
        model.triples().g(ns + "iDontExist").delete();
    }

    public void testTripleResultsAreLive() {
        model.add().s(ns + "test").p(RdfSchema.SUBCLASSOF).o(ns + "foufoutos");

        Triples t = model.triples().s(ns + "test").o(ns + "tost").fetch();

        assert t.iterator().hasNext() == false;

        Triple triple = model.add().s(ns + "test").p(Rdf.TYPE).o(ns + "tost");

        try {
            assert Iterables.size(t) == 1;
        } catch (AssertionError e) {
            System.err.println(getClass());
            throw e;
        }
        assert Iterables.getOnlyElement(t) == triple;
    }

    public static class RefImplIndexerTest extends ModelTest {
        public RefImplIndexerTest(String testName) {
            super(testName);
        }

        @Override
        protected ModelIndexer createIndex() {
            return ModelIndexers.createHorizontalModelIndexer();
        }
    }

    public static class NodeListIndexerTest extends ModelTest {
        public NodeListIndexerTest(String testName) {
            super(testName);
        }

        @Override
        protected ModelIndexer createIndex() {
            return ModelIndexers.createNodeListsModelIndexer();
        }
    }
    
    public static class MultimapsIndexerTest extends ModelTest {
        public MultimapsIndexerTest(String testName) {
            super(testName);
        }

        @Override
        protected ModelIndexer createIndex() {
            return ModelIndexers.createMultimapsModelIndexer();
        }
    }
    
    public static class TreeMapIndexerTest extends ModelTest {
        public TreeMapIndexerTest(String testName) {
            super(testName);
        }

        @Override
        protected ModelIndexer createIndex() {
            return ModelIndexers.createTreeMapModelIndexer();
        }
    }
}
