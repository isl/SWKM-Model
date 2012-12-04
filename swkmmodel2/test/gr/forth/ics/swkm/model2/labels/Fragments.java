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
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.GraphChecker;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.SecondaryGraph;
import gr.forth.ics.graph.algo.Generators;
import gr.forth.ics.graph.path.Path;
import gr.forth.ics.graph.path.Traverser;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Fragments {
    private final Random random;
    
    public Fragments(Random random) {
        this.random = random;
    }
    
    public Fragment randomTree(final String rootName, int size) {
        final SecondaryGraph graph = new SecondaryGraph();
        final Object kidsKey = new Object();
        Node root = Generators.createRandomTree(graph, size, Direction.IN);
        root.setValue(rootName);
        root.putWeakly(kidsKey, 0);
        for (Path path : Traverser.newDfs().build().traverse(graph, root, Direction.IN)) {
            if (path.size() > 0) {
                Node parent = path.getNode(-2);
                int kids = parent.getInt(kidsKey);
                path.tailNode().setValue(parent + "<" + kids);
                parent.putWeakly(kidsKey, kids + 1);
                path.tailNode().putWeakly(kidsKey, 0);
            }
        }
        return new Fragment(graph);
    }

    public Fragment randomDag(String rootName, int nodeCount, int maxCrossEdges) {
        Fragment fragment = randomTree(rootName, nodeCount);
        Graph graph = fragment.graph;
        for (int i = 0; i < maxCrossEdges; i++) {
            Node n1 = fragment.randomNode();
            Node n2 = fragment.randomNode();
            if (n1 == n2 || graph.areAdjacent(n1, n2)) {
                continue;
            }
            
            Edge e = graph.newEdge(n1, n2);
            if (!GraphChecker.isAcyclic(graph)) {
                graph.removeEdge(e);
            }
        }
        return fragment;
    }
    
    public class Fragment {
        private final SecondaryGraph graph;
        private final List<Node> nodes;
        private final Node root;
        
        Fragment(SecondaryGraph graph) {
            this.graph = graph;
            this.nodes = graph.nodes().drainToList();
            this.root = graph.aNode();
        }
        
        public Node randomNode() {
            return randomNode(nodes);
        }
        
        private Node randomNode(List<Node> nodes) {
            return nodes.get(random.nextInt(nodes.size()));
        }
        
        public SecondaryGraph graph() {
            return graph;
        }
        
        public Node root() {
            return root;
        }
        
        public InMemoryHierarchy toHierarchy() {
            return InMemoryHierarchy.newInstance(graph, graph, root);
        }
        
        public void attachToRandomNode(InMemoryHierarchy hierarchy) {
            attachTo(hierarchy, randomNode(hierarchy.wholeGraph().nodes().drainToList()));
        }
        
        public void attachIndependently(InMemoryHierarchy hierarchy) {
            attachTo(hierarchy, hierarchy.getRoot());
        }

        public void attachTo(InMemoryHierarchy hierarchy, Node parent) {
            SecondaryGraph whole = hierarchy.wholeGraph();
            
            whole.adoptGraph(graph);
            hierarchy.exploredGraph().adoptGraph(graph);
                
            whole.newEdge(root, parent);
        }
    }
}
