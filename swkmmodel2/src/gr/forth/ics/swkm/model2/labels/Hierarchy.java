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
import gr.forth.ics.graph.InspectableGraph;
import gr.forth.ics.graph.Node;
import java.util.Collection;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
//Kids point to (Direction.OUT) parents. 
public interface Hierarchy {
    /**
     * Returns the root of this hierarchy. It is guaranteed to always have an
     * existing label.
     */
    Node getRoot();
    
    /**
     * Returns the currently explored graph, including the root.
     */
    InspectableGraph exploredGraph();
    
    /**
     * Explores and returns all the direct ancestors of the specified node. The node
     * must currently belong to the explored part of this hierarchy (see {@link #exploredGraph}).
     */
    Collection<Node> exploreDirectAncestors(Node node);
    
    /**
     * Explores and returns all the ancestors (direct or indirect) of the specified node. The node
     * must currently belong to the explored part of this hierarchy (see {@link #exploredGraph}).
     */
    Collection<Node> exploreAncestors(Node node);
    
    /**
     * Explores and returns all the direct descendants of the specified node. The node
     * must currently belong to the explored part of this hierarchy (see {@link #exploredGraph}).
     */
    Collection<Node> exploreDirectDescendants(Node node);
    
    /**
     * Explores and returns all the descendants (direct or indirect) of the specified node. The node
     * must currently belong to the explored part of this hierarchy (see {@link #exploredGraph}).
     */
    Collection<Node> exploreDescendants(Node node);
    
    /**
     * Explores and returns all nodes of which the tree label is included in at least one of the provided ranges.
     */
    Collection<Node> exploreNodesIncludedIn(Collection<Interval> ranges);
    
    /**
     * Tests whether the specified node is new, {@literal i.e.} it does not have an existing label.
     */
    boolean isNew(Node node);
    
    /**
     * Returns the new nodes which have none new direct ancestors. These are
     * always explored.
     */
    Collection<Node> getNewRoots();
    
    /**
     * Returns a copy of the existing label of a node, or null if the node is new. 
     * The specified node must be explored.
     */
    Label getExistingLabelOf(Node node);
    
    /**
     * Returns the most-recent label of the specified node, or an empty label if there
     * is none. The returned label can be modified to actually change the label of the node.
     * The specified node must be explored.
     */
    Label getLabelOf(Node node);
    
    /**
     * Returns whether a node has different existing label ({@link #getExistingLabelOf(Node) })
     * and current label ({@link #getLabelOf(Node) }).
     * The specified node must be explored.
     */
    boolean hasUpdatedLabel(Node node);
    
    /**
     * Returns the next free index of the label of the root of this hierarchy. This could be
     * also calculated by exploring the direct descendants of the root and subtracting their
     * labels from the root label. This method is not guaranteed to explore the direct
     * descendants of the root.
     */
    int getIndexForNewHierarchy();
    
    /**
     * Recalculates the next free index of the label of the root of this hierarchy.
     * @see #getIndexForNewHierarchy() 
     */
    void recalculateIndexForNewHierarchy();
    
    /**
     * Propagates an interval to a node and to all its ancestors.
     */
    void propagateInterval(Node node, Interval interval);
    
    /**
     * Explores everything from the database.
     */
    void exploreEverythingAsNew();
}
