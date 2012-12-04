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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gr.forth.ics.graph.Direction;
import gr.forth.ics.graph.Edge;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.InspectableGraph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.algo.Orders;
import gr.forth.ics.graph.Filters;
import gr.forth.ics.swkm.model2.Model;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A main-memory implementation of {@link Hierarchy}.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class MainMemoryHierarchy extends AbstractHierarchy {
    private final Object existingLabelKey = new Object();
    private final Node root;

    private int nextIndex;

    public MainMemoryHierarchy(Graph graph) {
        super(graph);
        List<Node> roots = findRoots(graph);
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("No root node found for graph: " + graph);
        } else if (roots.size() > 1) {
            root = null; //multiple roots means no single root
        } else {
            root = roots.get(0);
            root.putWeakly(existingLabelKey, new Label(new Interval(0, Integer.MAX_VALUE)));
        }
        recalculateIndexForNewHierarchy();
    }

    @Override
    protected Collection<Node> exploreAdjacent(Node node, Direction direction) {
        Collection<Node> nodes = Lists.newArrayListWithExpectedSize(exploredGraph().degree(node, direction));
        for (Edge e : exploredGraph().edges(node, direction)) {
            nodes.add(e.opposite(node));
        }
        return nodes;
    }

    @Override
    public Label getExistingLabelOf(Node node) {
        return (Label)node.get(existingLabelKey);
    }

    public Node getRoot() {
        return root;
    }

    public Collection<Node> exploreNodesIncludedIn(Collection<Interval> ranges) {
        List<Node> nodes = Lists.newArrayList();
        for (Node n : exploredGraph().nodes()) {
            Label label = getLabelOf(n);
            for (Interval range : ranges) {
                if (label.getTreeLabel().contains(range)) {
                    nodes.add(n);
                }
            }
        }
        return nodes;
    }

    public Collection<Node> getNewRoots() {
        final Set<Node> newRoots = Sets.newHashSet();
        InspectableGraph g = exploredGraph();
        for (Node n : Orders.reverseTopological(g)) {
            if (!isNew(n)) continue; //ignore old nodes
            
            boolean hasNewRootParent = false;
            for (Node parent : g.adjacentNodes(n, Direction.OUT)) {
                if (isNew(parent)) {
                    hasNewRootParent = true;
                }
            }
            if (!hasNewRootParent) {
                newRoots.add(n);
            }
        }
        return newRoots;
    }

    public int getIndexForNewHierarchy() {
        return nextIndex;
    }

    public final void recalculateIndexForNewHierarchy() {
        nextIndex = 0;
        Iterable<Node> nodes = root != null ? exploredGraph().adjacentNodes(root, Direction.IN)
                : exploredGraph().nodes().filter(Filters.outDegreeEqual(exploredGraph(), 0));
        for (Node n : nodes) {
            Label label = getLabelOf(n);
            nextIndex = Math.max(nextIndex, label.getTreeLabel().getPost() + 1);
        }
    }

    public void exploreEverythingAsNew() {
        for (Node n : exploredGraph().nodes()) {
            clearLabel(n);
        }
    }

    /**
     * Creates a metaclass hierarchy out of a model, or returns {@code null} if the model contains no metaclasses.
     *
     * @param model the model of which to create a metaclass hierarchy
     * @return a metaclass hierarchy, or {@code null} if the model contains no metaclasses
     */
    public static MainMemoryHierarchy newMetaclassHierarchy(Model model) {
        Graph g = newMetaclassHierarchyGraph(model);
        if (g == null) return null;
        return new MainMemoryHierarchy(g);
    }

   /**
     * Creates a metaproperty hierarchy out of a model, or returns {@code null} if the model contains no metaproperties.
     *
     * @param model the model of which to create a metaproperty hierarchy
     * @return a metaproperty hierarchy, or {@code null} if the model contains no metaproperties
     */
    public static MainMemoryHierarchy newMetapropertyHierarchy(Model model) {
        Graph g = newMetapropertyHierarchyGraph(model);
        if (g == null) return null;
        return new MainMemoryHierarchy(g);
    }

    /**
     * Creates a class hierarchy out of a model, or returns {@code null} if the model contains no classes.
     *
     * @param model the model of which to create a class hierarchy
     * @return a class hierarchy, or {@code null} if the model contains no classes
     */
    public static MainMemoryHierarchy newClassHierarchy(Model model) {
        Graph g = newClassHierarchyGraph(model);
        if (g == null) return null;
        return new MainMemoryHierarchy(g);
    }

    /**
     * Creates a property hierarchy out of a model, or returns {@code null} if the model contains no properties.
     *
     * @param model the model of which to create a property hierarchy
     * @return a property hierarchy, or {@code null} if the model contains no properties
     */
    public static MainMemoryHierarchy newPropertyHierarchy(Model model) {
        Graph g = newPropertyHierarchyGraph(model);
        if (g == null) return null;
        return new MainMemoryHierarchy(g);
    }
}
