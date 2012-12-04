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

import gr.forth.ics.graph.Direction;
import gr.forth.ics.graph.Edge;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.SecondaryGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.util.Assert;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class InMemoryHierarchy extends AbstractHierarchy {
    private final SecondaryGraph wholeGraph;
    private final SecondaryGraph exploredGraph;
    private final Node root;
    private final Object EXISTING_LABEL = new Object();
    private Object IS_OLD = new Object();
    
    private int indexForNewHierarchy = 0;
    
    private InMemoryHierarchy(SecondaryGraph wholeGraph, SecondaryGraph exploredGraph,
            Node root) {
        super(exploredGraph);
        Assert.isTrue(wholeGraph.containsNode(root));
        this.wholeGraph = wholeGraph;
        this.exploredGraph = exploredGraph;
        this.root = root;
        exploredGraph.adoptNode(root);
    }
    
    public void verifyAndMarkAsOld() {
//        try {
            LabelVerifier.verify(this, wholeGraph);
            transformToLegacy();
//        } catch (AssertionError e) {
//            System.err.println("Explored graph, just before assertion error:\n" + exploredGraph);
//            throw e;
//        }
    }

    public Node getRoot() {
        return root;
    }
    
    public Collection<Node> getNewRoots() {
        Collection<Node> roots = new ArrayList<Node>();
        out:
        for (Node node : exploredGraph.nodes()) {
            if (getExistingLabelOf(node) != null) {
                continue;
            }
            for (Node parent : wholeGraph.adjacentNodes(node, Direction.OUT)) {
                if (getExistingLabelOf(parent) == null) {
                    continue out;
                }
            }
            roots.add(node);
        }
        return roots;
    }

    @Override
    public boolean isNew(Node node) {
        return !node.has(IS_OLD);
    }
    
    @Override
    public Label getExistingLabelOf(Node node) {
        Label existingLabel = (Label)node.get(EXISTING_LABEL);
        if (existingLabel == null) {
            return null;
        }
        return existingLabel.copy();
    }

    @Override
    protected Collection<Node> exploreAdjacent(Node node, Direction direction) {
        Collection<Node> nodes = new ArrayList<Node>();
        for (Edge e : wholeGraph.edges(node, direction)) {
            exploredGraph.adoptEdge(e);
            nodes.add(e.opposite(node));
        }
        return nodes;
    }
    
    public SecondaryGraph wholeGraph() {
        return wholeGraph;
    }
    
    @Override
    public SecondaryGraph exploredGraph() {
        return exploredGraph;
    }
    
    /**
     * Makes everything unexplored, and regards all nodes "not new".
     */
    public void transformToLegacy() {
        exploredGraph.removeAllNodes();
        for (Node node : wholeGraph.nodes()) {
            setExistingLabel(node, getLabelOf(node));
            assert getExistingLabelOf(node).equals(getLabelOf(node));
        }
    }
    
    public void setExistingLabel(Node node, Interval interval) {
        setExistingLabel(node, new Label(interval));
    } 

    public void setExistingLabel(Node node, Label label) {
        Assert.isTrue(wholeGraph.containsNode(node));
        node.putWeakly(EXISTING_LABEL, label.copy());
        node.putWeakly(IS_OLD, Boolean.TRUE);
    }
    
    public void clearExistingLabel(Node node) {
        node.putWeakly(EXISTING_LABEL, null);
        node.putWeakly(IS_OLD, Boolean.FALSE);
    }

    public Collection<Node> exploreNodesIncludedIn(Collection<Interval> ranges) {
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : wholeGraph.nodes()) {
            for (Interval range : ranges) {
                Label label = getLabelOf(node);
                if (range.contains(label.getTreeLabel())) {
                    exploredGraph.adoptNode(node);
                    nodes.add(node);
                    break;
                }
            }
        }
        return nodes;
    }
   
    public static InMemoryHierarchy newInstance(SecondaryGraph wholeGraph, SecondaryGraph exploredGraph, Node root) {
        Assert.notNull(wholeGraph);
        Assert.notNull(exploredGraph);
        for (Node n : exploredGraph.nodes()) {
            Assert.isTrue(wholeGraph.containsNode(n), "Explored graph must be completely contained in whole graph");
        }
        if (exploredGraph == wholeGraph) {
            exploredGraph = new SecondaryGraph(exploredGraph);
        }
        return new InMemoryHierarchy(wholeGraph, exploredGraph, root);
    }

    public int getIndexForNewHierarchy() {
        return indexForNewHierarchy;
    }
    
    public void setIndexForNewHierarchy(int indexForNewHierarchy) {
        this.indexForNewHierarchy = indexForNewHierarchy;
    }
    
    public void assignLabelsAndUpdateIndexForNewHierarchy(Labeler labeler) {
        labeler.assignLabels(this, null);
        recalculateIndexForNewHierarchy();
    }
    
    @Override
    protected boolean isOwned(Node node) {
        return wholeGraph.containsNode(node);
    }
    
    @Override
    public String toString() {
        return toString(wholeGraph);
    }

    public void recalculateIndexForNewHierarchy() {
        //update max used post, i.e. indexForNewHierarchy
        Iterable<Node> roots = wholeGraph.adjacentNodes(root, Direction.IN);
        int max = indexForNewHierarchy;
        for (Node kid : roots) {
            boolean wasContained = exploredGraph.containsNode(kid);
            exploredGraph.adoptNode(kid);
            max = Math.max(getLabelOf(kid).getTreeLabel().getPost() + 1, max);
            if (!wasContained) {
                exploredGraph.removeNode(kid);
            }
        }
        indexForNewHierarchy = max;
    }

    public void exploreEverythingAsNew() {
        exploredGraph.adoptGraph(wholeGraph);
        IS_OLD = new Object();
    }
}
