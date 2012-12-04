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

import gr.forth.ics.graph.Edge;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.GraphChecker;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.io.GraphIO;
import gr.forth.ics.swkm.model2.labels.Fragments.Fragment;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class LabelerTest extends TestCase {
    private static final int RESOURCE_MIN = 300;
    private static final int RESOURCE_MAX = 2000000000;
    private static final int NORMAL_SIZE = 10;
    private static final int BIG_SIZE = NORMAL_SIZE * 10;
    private static final int SMALL_SIZE = NORMAL_SIZE / 2;
    
    public LabelerTest(String testName) {
        super(testName);
    }            
    
    public static TestSuite suite() {
//        return new TestSuite();
        TestSuite suite = new TestSuite();
//        suite.addTest(new BenderLabelerTest("testSmallDependentTreeInsertion"));
        suite.addTestSuite(BenderLabelerTest.class);
        return suite;
    }
    
    protected abstract Labeler createLabeler();
    private Labeler labeler;
    private Fragments fragmentFactory;
    private InMemoryHierarchy hierarchy;
    
    @Override
    protected void setUp() {
        labeler = createLabeler();
        fragmentFactory = new Fragments(new Random(0));
        hierarchy = initialHierarchy();
    }

    @Override
    protected void tearDown() throws Exception {
        createGmlFile();
        labeler = null;
        fragmentFactory = null;
        hierarchy = null;
    }
    
    InMemoryHierarchy initialHierarchy() {
        Fragment resource = fragmentFactory.randomTree("RESOURCE", 1);
        InMemoryHierarchy hierarchy = resource.toHierarchy();
        hierarchy.setExistingLabel(resource.root(), new Interval(RESOURCE_MIN, RESOURCE_MAX));
        hierarchy.setIndexForNewHierarchy(301);
        return hierarchy;
    }
    
    public void testSimpleTree() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testSmallIndependentTreeInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        hierarchy.transformToLegacy();
        fragmentFactory.randomTree("TREE_SMALL", SMALL_SIZE)
                .attachIndependently(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testSmallDependentTreeInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        hierarchy.transformToLegacy();
        fragmentFactory.randomTree("TREE_SMALL", SMALL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testBigIndependentTreeInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomTree("TREE_BIG", BIG_SIZE)
                .attachIndependently(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testBigDependentTreeInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomTree("TREE_BIG", BIG_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testSmallIndependentDagInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomDag("SMALL_DAG", SMALL_SIZE, SMALL_SIZE / 2)
                .attachIndependently(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testSmallDependentDagInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomDag("SMALL_DAG", BIG_SIZE, BIG_SIZE / 2)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testBigIndependentDagInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomDag("BIG_DAG", BIG_SIZE, BIG_SIZE / 2)
                .attachIndependently(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testBigDependentDagInsertion() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomDag("BIG_DAG", BIG_SIZE, BIG_SIZE / 2)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testNewEdgesBetweenOldNodes() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        makeDag(hierarchy, NORMAL_SIZE / 2);
        
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testMixed() {
        fragmentFactory.randomTree("ROOT", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        fragmentFactory.randomDag("BIG_DAG", NORMAL_SIZE, NORMAL_SIZE / 2)
                .attachToRandomNode(hierarchy);
        fragmentFactory.randomTree("BIG_TREE", BIG_SIZE)
                .attachIndependently(hierarchy);
        fragmentFactory.randomTree("SMALL_TREE", SMALL_SIZE)
                .attachIndependently(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        Random random = new Random(0);
        for (Node node : hierarchy.wholeGraph().nodes()) {
            if (node == hierarchy.getRoot()) {
                continue;
            }
            if (random.nextBoolean()) {
                hierarchy.clearExistingLabel(node);
            }
        }
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    public void testHierarchyShift() {
        //create a very simple hierarchy
        Fragment first = fragmentFactory.randomTree("FIRST", 1);
        first.attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        //create a second hierarcy
        fragmentFactory.randomTree("SECOND", NORMAL_SIZE)
                .attachToRandomNode(hierarchy);
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
        
        Fragment first_first = fragmentFactory.randomTree("FIRST_FIRST", NORMAL_SIZE);
        first_first.attachTo(hierarchy, first.root());
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();

        fragmentFactory.randomDag("FIRST_FIRST_FIRST", BIG_SIZE, BIG_SIZE / 2)
                .attachTo(hierarchy, first_first.randomNode());
        hierarchy.assignLabelsAndUpdateIndexForNewHierarchy(labeler);
        hierarchy.verifyAndMarkAsOld();
    }
    
    private Collection<Edge> makeDag(InMemoryHierarchy hierarchy, int maxCrossEdges) {
        Random random = new Random(0);
        Graph graph = hierarchy.wholeGraph();
        List<Node> nodes = graph.nodes().drainToList();
        Collection<Edge> edges = new ArrayList<Edge>();
        for (int i = 0; i < maxCrossEdges; i++) {
            Node n1 = nodes.get(random.nextInt(nodes.size()));
            Node n2 = nodes.get(random.nextInt(nodes.size()));
            if (n1 == n2 || graph.areAdjacent(n1, n2)) {
                continue;
            }
            
            Edge e = graph.newEdge(n1, n2);
            if (!GraphChecker.isAcyclic(graph)) {
                graph.removeEdge(e);
            }
            edges.add(e);
        }
        return edges;
    }
    
    private void createGmlFile() throws Exception {
        Graph g = hierarchy.wholeGraph();
//        for (Node n : g.nodes()) {
//            n.setValue(n.getValue().toString() + " [" + hierarchy.getLabelOf(n) + "]=" +
//                    hierarchy.getLabelOf(n).getTreeLabel().length());
//        }
        GraphIO.write(gr.forth.ics.graph.io.Format.GML, g, new File("build/test/" + getName() + ".gml"));
    }

    public static class BenderLabelerTest extends LabelerTest {
        public BenderLabelerTest(String testName) {
            super(testName);
        }

        @Override
        protected Labeler createLabeler() {
            return Labelers.newDefault();
        }
    }
}
