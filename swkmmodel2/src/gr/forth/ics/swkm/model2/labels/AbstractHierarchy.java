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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import gr.forth.ics.graph.Direction;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.InspectableGraph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.swkm.model2.GraphUtils;
import gr.forth.ics.swkm.model2.GraphUtils.RdfGraph;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * Template implementation of {@link Hierarchy}.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class AbstractHierarchy implements Hierarchy {
    private final Graph graph;
    private Object LABEL_KEY = new Object();

    public AbstractHierarchy(Graph graph) {
        Assert.notNull(graph);
        this.graph = graph;
    }

    private Collection<Node> explore(Node node, Direction direction) {
		if (node == null) throw new IllegalArgumentException("null node (perhaps you used the root of the properties hierarchy, which doesn't exist and is null?)");
        final Object visited = new Object();
        Collection<Node> explored = new HashSet<Node>();
        LinkedList<Node> stack = new LinkedList<Node>();
        stack.add(node);
        while (!stack.isEmpty()) {
            Node currentNode = stack.remove();
            if (currentNode.has(visited)) {
                continue;
            }
            currentNode.putWeakly(visited, null);
            Collection<Node> nextNodes = exploreAdjacent(currentNode, direction);
            stack.addAll(nextNodes);
            explored.addAll(nextNodes);
        }
        return explored;
    }

    public Collection<Node> exploreAncestors(Node node) {
        return explore(node, Direction.OUT);
    }

    public Collection<Node> exploreDescendants(Node node) {
        return explore(node, Direction.IN);
    }

    public Collection<Node> exploreDirectAncestors(Node node) {
        return exploreAdjacent(node, Direction.OUT);
    }

    public Collection<Node> exploreDirectDescendants(Node node) {
        return exploreAdjacent(node, Direction.IN);
    }

    protected abstract Collection<Node> exploreAdjacent(Node node, Direction direction);

    public abstract Label getExistingLabelOf(Node node);

    public Label getLabelOf(Node node) {
        if (node == null) {
            return getExistingLabelOf(node);
        }
        checkOwned(node);
        if (node.has(LABEL_KEY)) {
            return (Label) node.get(LABEL_KEY);
        }
        Label label = getExistingLabelOf(node);
        if (label == null) {
            label = Label.newEmpty();
        }
        Label copy = label.copy();
        node.putWeakly(LABEL_KEY, copy);
        return copy;
    }

    /**
     * Clears the label of a node and henceforth treats it as new.
     *
     * @param node the node of which to clear the label
     */
    protected void clearLabel(Node node) {
        node.remove(LABEL_KEY);
    }

    public InspectableGraph exploredGraph() {
        return graph;
    }

    public boolean isNew(Node node) {
        return getExistingLabelOf(node) == null;
    }

    public void propagateInterval(Node node, Interval interval) {
        checkOwned(node);
        LinkedList<Node> stack = new LinkedList<Node>();
        stack.addLast(node);
        boolean direct = true;
        while (!stack.isEmpty()) {
            Node current = stack.removeLast();
            boolean changed = getLabelOf(current).addPropagatedLabel(interval, direct);
            direct = false;
            if (!changed) {
                continue;
            }
            for (Node ancestor : graph.adjacentNodes(current, Direction.OUT)) {
                stack.addLast(ancestor);
            }
        }
    }

    private void checkOwned(Node node) {
        if (!isOwned(node)) {
            if (node == null) {
                throw new IllegalArgumentException("null");
            }
            throw new IllegalArgumentException("Node: " + node +
                    " is not in the graph of this hierarchy: [" + graph + "]");
        }
    }

    protected boolean isOwned(Node node) {
        return graph.containsNode(node);
    }

    @Override
    public String toString() {
        return toString(graph);
    }

    protected String toString(InspectableGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nodes (").append(graph.nodeCount()).append(") :\n");
        for (Node node : graph.nodes()) {
            sb.append("    ").append(node).append(" (Label: ").
                    append(getLabelOf(node)).append(")\n");
        }
        return sb.toString();
    }

    public boolean hasUpdatedLabel(Node node) {
        Label existing = getExistingLabelOf(node);
        Label label = getLabelOf(node);
        return !(label.getTreeLabel().isEmpty() || label.equals(existing));
    }

    protected static Graph newClassHierarchyGraph(Model model) {
        RdfGraph g = GraphUtils.toGraph(model,
                new Predicate<Triple>() {
                    public boolean apply(Triple triple) {
                        return triple.subject().type().isClass();
                    }
                }, RdfSchema.SUBCLASSOF);
        ensureAllNodesIncluded(model, g, RdfType.CLASS);
        if (g.graph().nodeCount() == 0) {
            return null;
        }
        //class hierarchies *must* have rdfs:Resource as root
        g = createRootedGraph(g, model.mapResource(RdfSchema.RESOURCE));
        return g.graph();
    }

    protected static Graph newPropertyHierarchyGraph(Model model) {
        RdfGraph g = GraphUtils.toGraph(model, RdfSchema.SUBPROPERTYOF);
        ensureAllNodesIncluded(model, g, RdfType.PROPERTY);
        if (g.graph().nodeCount() == 0) {
            return null;
        }
        //property hierarchies do not have a predefined root
        return g.graph();
    }

    protected static Graph newMetaclassHierarchyGraph(Model model) {
        RdfGraph g = GraphUtils.toGraph(model,
                new Predicate<Triple>() {

                    public boolean apply(Triple triple) {
                        return triple.subject().type().isMetaclass();
                    }
                }, RdfSchema.SUBCLASSOF);
        ensureAllNodesIncluded(model, g, RdfType.METACLASS);
        if (g.graph().nodeCount() == 0) {
            return null;
        }
        //metaclass hierarchies *must* have rdfs:Class as root
        g = createRootedGraph(g, model.mapResource(RdfSchema.CLASS));
        return g.graph();
    }

    protected static Graph newMetapropertyHierarchyGraph(Model model) {
        RdfGraph g = GraphUtils.toGraph(model,
                new Predicate<Triple>() {

                    public boolean apply(Triple triple) {
                        return triple.subject().type().isMetaproperty();
                    }
                }, RdfSchema.SUBCLASSOF);
        ensureAllNodesIncluded(model, g, RdfType.METAPROPERTY);
        if (g.graph().nodeCount() == 0) {
            return null;
        }
        //metaproperties hierarchies *must* have rdf:Property as root
        g = createRootedGraph(g, model.mapResource(Rdf.PROPERTY));
        return g.graph();
    }

    /**
     * This makes sure that all nodes of a specific type are mapped into the graph. This for example is useful
     * when the model defines classes which do not participate in *any* rdf:subclassOf relationship (but are inferred
     * as classes for other reasons), which will thus
     * be ignored by the algorithm that translates such relationships to edges.
     */
    private static void ensureAllNodesIncluded(Model model, RdfGraph g, RdfType type) {
        final Map<RdfNode, Node> nodeMap = g.nodeMap();
        final Graph graph = g.graph();
        for (RdfNode node : model.findNodes(type)) {
            if (nodeMap.containsKey(node)) {
                continue;
            }
            nodeMap.put(node, graph.newNode(node));
        }
    }

   /**
     * Transforms a graph to a rooted graph, by making sure that all nodes are (directly
     * or indirectly) connected to a root node that corresponds to the provided URI.
     * If the URI already corresponds to a node in the graph, that node is reused.
     */
    static RdfGraph createRootedGraph(RdfGraph graph, RdfNode rootRdfNode) {
        Map<RdfNode, Node> map = graph.nodeMap();
        Graph g = graph.graph();
        List<Node> subRoots = findRoots(g);
        Node root = map.get(rootRdfNode);
        if (root == null) {
            root = g.newNode(rootRdfNode);
            map.put(rootRdfNode, root);
        }
        for (Node subRoot : subRoots) {
            if (subRoot == root) {
                continue;
            }
            g.newEdge(subRoot, root);
        }
        return new RdfGraph(g, map);
    }

    protected static List<Node> findRoots(InspectableGraph graph) {
        List<Node> roots = Lists.newArrayList();
        for (Node n : graph.nodes()) {
            if (graph.outDegree(n) == 0) {
                roots.add(n);
            }
        }
        return roots;
    }
}