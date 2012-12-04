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
import gr.forth.ics.graph.InspectableGraph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.path.Path;
import gr.forth.ics.graph.path.Traverser;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class LabelVerifier {
    private LabelVerifier() {
    }

    public static void verify(final Hierarchy hierarchy, InspectableGraph wholeGraph) {
        //test reachability <==> label inclusion
        new Reachability(hierarchy.exploredGraph()) {
            @Override
            protected void visitPair(Node n1, Node n2, boolean reachable) {
                Label label1 = hierarchy.getLabelOf(n1);
                Label label2 = hierarchy.getLabelOf(n2);
                if (label1 == null) {
                    throw new AssertionError("Node " + n1 + " has null label!");
                }
                if (label2 == null) {
                    throw new AssertionError("Node " + n2 + " has null label!");
                }
                if (reachable) {
                    boolean contained = label2.contains(label1);
                    if (!contained) {
                        throw new AssertionError(n2 + " is reachable from " + n1 + " but its label: " +
                                label2 + " does not contain the label of the other node: " + label1);
                    }
                } else {
                    boolean contained = label2.contains(label1);
                    if (contained) {
                        throw new AssertionError(n2 + " is not reachable from " + n1 + " but its label: " +
                                label2 + " contain the label of the other node: " + label1);
                    }
                }
            }
        };
    }

    private static abstract class Reachability {
        Reachability(InspectableGraph g) {
            Traverser traverser = Traverser.newDfs().build();
            for (Node n : g.nodes()) {
                final Set<Node> reachableNodes = new LinkedHashSet<Node>();
                for (Path path : traverser.traverse(g, n, Direction.OUT)) {
                    reachableNodes.add(path.tailNode());
                }
                reachableNodes.remove(n);
                for (Node other : g.nodes()) {
                    if (other == n) {
                        continue;
                    }
                    boolean isReachable = reachableNodes.contains(other);
                    visitPair(n, other, isReachable);
                }
            }
        }

        protected abstract void visitPair(Node n1, Node n2, boolean reachable);
    }
}
