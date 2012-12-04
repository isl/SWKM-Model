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
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.Filters;
import gr.forth.ics.graph.Graphs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author theohari
 */
class BenderLabeler implements Labeler {

    private final double T;
    private final long universeLength;
//    public int maxNumOfHierarchies = 2 << 6;
    public static final int maxNumOfHierarchies = (2000000000 - 300) / 1000;
    private TreeBuilder treeBuilder;

    /**
     * Stores the nodes of a new hierarchy that got a new label (tree- or non tree-).
     * For every new hierarchy we consider, we clear the cache
     */
    public BenderLabeler(double T) {
        this(T, 2L << 30);
    }

    public BenderLabeler(double T, long universeLength) {
        if ((T < 1.0) || (T > 2.0)) {
            throw new RuntimeException("T should lie in [1, 2]. It is found to be: " + T);
        }
        this.T = T;
        this.universeLength = universeLength;
    }

    public void assignLabels(Hierarchy hierarchy, TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
        try {
            assignLabels(hierarchy, true);
        } finally {
            this.treeBuilder = null;
        }
        checkForNewEdgesWithOldNodes(hierarchy);
    }

    private void assignLabels(Hierarchy hierarchy, boolean sparse) {
        Collection<Node> rootsOfNewHierarchies = hierarchy.getNewRoots();
        fetchSiblings(hierarchy, rootsOfNewHierarchies);

        if (hierarchy.getRoot() != null && treeBuilder != null) {
            for (Node root : rootsOfNewHierarchies) {
                treeBuilder.treeEdge(root, hierarchy.getRoot());
            }
        }

        //TODO: rootsOfNewHierarchies should not contain "http://139.91.183.30:9090/RDF/rdfsuite.rdfs#Graph"
        for (Node curRootOfNewHierarchy : rootsOfNewHierarchies) {
            ProperAncInfo properAncInfo = findProperAnc(curRootOfNewHierarchy, hierarchy);
            Node properAnc = properAncInfo.getProperAnc();
            Interval curAncMaxInterval = properAncInfo.getMaxInterval();

            int estimatedIntervalSize = fits(curRootOfNewHierarchy, curAncMaxInterval, T, hierarchy);
            if (properAnc == hierarchy.getRoot()) {
                estimatedIntervalSize = estimateIntervalSize(curRootOfNewHierarchy, T, hierarchy, true);
                Interval intervalOfNextHierarchy = getIntervalOfNextHierarchy(hierarchy, estimatedIntervalSize, false);
                overflowCheck(intervalOfNextHierarchy, hierarchy);
                labelHierarchy(curRootOfNewHierarchy, intervalOfNextHierarchy, hierarchy);
            } else if (estimatedIntervalSize >= 0) {
                Interval label = getProperSubInterval(curAncMaxInterval, estimatedIntervalSize);
                labelHierarchy(curRootOfNewHierarchy, label, hierarchy);
            } else {
//                Not sure if the two following lines are needed:
//                Collection<Interval> parentTreeLabels = findParentTreeLabels(curRootOfNewHierarchy, hierarchy);
//                hierarchy.exploreNodesIncludedIn(parentTreeLabels);

                hierarchy.exploreAncestors(curRootOfNewHierarchy);
                HashSet<Node> relabeledClasses = relabelClasses(curRootOfNewHierarchy, properAnc, hierarchy);
                modifyPropagatedLabels(relabeledClasses, hierarchy);
                Interval freeRange = findMaxAvailableInterval(properAnc, hierarchy);
                estimatedIntervalSize = estimateIntervalSize(curRootOfNewHierarchy, T, hierarchy, true);
                Interval label = getProperSubInterval(freeRange, estimatedIntervalSize);
                labelHierarchy(curRootOfNewHierarchy, label, hierarchy);
            }
        }
    }

    private void checkForNewEdgesWithOldNodes(Hierarchy hierarchy) {
        for (Node n : hierarchy.exploredGraph().nodes()) {
            if ( !hierarchy.isNew(n)) {
                propagateLabel(n, hierarchy);
            }
        }
    }

    private Interval getProperSubInterval(Interval curAncMaxInterval, int estimatedIntervalSize) {
        return new Interval(curAncMaxInterval.getIndex(), curAncMaxInterval.getIndex() + estimatedIntervalSize);
    }

    private void changeNodeTreeLabel(Hierarchy hierarchy, Node n, int lengthOfNewHier) {
        Label nLabel = hierarchy.getLabelOf(n);
        Interval oldTreeLabel = nLabel.getTreeLabel();
        Interval newTreeLabel = new Interval(oldTreeLabel.getIndex(), oldTreeLabel.getPost() + lengthOfNewHier);
        nLabel.setTreeLabel(newTreeLabel);
    }

    private void modifyPropagatedLabels(
            HashSet<Node> relabeledClasses,
            Hierarchy hierarchy) {
        for (Node n : relabeledClasses) {
            Interval oldTreeLabel = hierarchy.getExistingLabelOf(n).getTreeLabel();
            Interval newTreeLabel = hierarchy.getLabelOf(n).getTreeLabel();
            Collection<Node> directAncs = getDirectAncestorsInMem(hierarchy, n);
//                    hierarchy.exploreDirectAncestors(n);
            for (Node directAnc : directAncs) {
                Interval directAncTreeLabel = hierarchy.getLabelOf(directAnc).getTreeLabel();
                if (!directAncTreeLabel.contains(oldTreeLabel)) {
                    changePropagatedLabel(directAnc, oldTreeLabel, newTreeLabel, hierarchy, true);
                }
            }
        }
    }

    private void changePropagatedLabel(
            Node n,
            Interval oldTreeLabel,
            Interval newTreeLabel,
            Hierarchy hierarchy,
            boolean direct) {
        Label label = hierarchy.getLabelOf(n);
        boolean existed = label.removePropagatedLabel(oldTreeLabel, direct);
        if (existed) {
            label.addPropagatedLabel(newTreeLabel, direct);
        }
        for (Node anc : getAncestorsInMem(hierarchy, n) //            hierarchy.exploreAncestors(n)
                ) {
            changePropagatedLabel(anc, oldTreeLabel, newTreeLabel, hierarchy, false);
        }
    }

    private void overflowCheck(Interval intervalOfNextHierarchy, Hierarchy hierarchy) {
        if (hierarchy.getRoot() == null ||
                intervalOfNextHierarchy.getPost() < hierarchy.getLabelOf(hierarchy.getRoot()).getTreeLabel().getPost()) {
            return;
        } else {
            relabelAll(hierarchy);
        }
    }

    private void relabelAll(Hierarchy hierarchy) {
        hierarchy.exploreEverythingAsNew();
        assignLabels(hierarchy, false);
    }

    /**
     * Labels a new hierarchy under the assumption that it does not fit in the available interval
     *
     * Implements Bender's labels update algorithm.
     * May need to get all old classes from the DB.
     *
     * @param root, the root of the new hierarchy
     * @param parent, the old node under which root is inserted
     */
    private HashSet<Node> relabelClasses(
            Node root,
            Node parent,
            Hierarchy hierarchy) {
        HashSet<Node> relabeledClasses = new HashSet<Node>();
        Node wholeRoot = hierarchy.getRoot();
        Collection<Node> directDescsOfWholeHierarchy = getDirectDescendantsInMem(hierarchy, wholeRoot);
//                hierarchy.exploreDirectDescendants(wholeRoot);

        //Flouris has asked to try to avoid changing the label of a root 
        //(i.e. direct descendant of the wholeRoot) of a hierarchy.
        int freeGapNeededSparse = estimateIntervalSize(root, T, hierarchy, true);
        int freeGapNeededDense = estimateIntervalSize(root, T, hierarchy, false);
        Node ancWithProperFreeGap = findAncestorWithProperFreeGap(root, parent, hierarchy, freeGapNeededSparse, freeGapNeededDense, directDescsOfWholeHierarchy);
        int properGapNeeded = 0;
        if (directDescsOfWholeHierarchy.contains(ancWithProperFreeGap)) {
            properGapNeeded = freeGapNeededDense;
        } else {
            properGapNeeded = freeGapNeededSparse;
        }
        relabelClass(parent, hierarchy, properGapNeeded, relabeledClasses, ancWithProperFreeGap);
        return relabeledClasses;
    }

    private void relabelClass(
            Node n,
            Hierarchy hierarchy,
            int lengthOfNewHier,
            HashSet<Node> relabeledClasses,
            Node ancWithProperFreeGap) {
        relabeledClasses.add(n);
        changeNodeTreeLabel(hierarchy, n, lengthOfNewHier);

        Node directAnc = findSpanTreeDirectAnc(n, hierarchy);
        Collection<Node> siblings = findSiblingsNext(n, directAnc, hierarchy);
        moveSiblings(n, siblings, hierarchy, lengthOfNewHier);
        Node spanTreeDirectAnc = findSpanTreeDirectAnc(n, hierarchy);
        if (
                (spanTreeDirectAnc != hierarchy.getRoot()) &&
                (spanTreeDirectAnc != ancWithProperFreeGap)
                ) {
            relabelClass(spanTreeDirectAnc, hierarchy, lengthOfNewHier, relabeledClasses, ancWithProperFreeGap);
        }
    }

    private void moveSiblings(
            Node n,
            Collection<Node> siblings,
            Hierarchy hierarchy,
            int lengthOfNewHier) {
        for (Node sibling : siblings) {
            if (sibling == n) {
                continue;
            }
            moveLabelRight(sibling, hierarchy, lengthOfNewHier);
            Collection<Node> descs = getDescendantsInMem(hierarchy, sibling);
//                    hierarchy.exploreDescendants(sibling);
            Interval siblingTreeLabel = hierarchy.getExistingLabelOf(sibling).getTreeLabel();
            for (Node desc : descs) {
                Interval descTreeLabel = hierarchy.getExistingLabelOf(desc).getTreeLabel();
                if (siblingTreeLabel.contains(descTreeLabel)) {
                    moveLabelRight(desc, hierarchy, lengthOfNewHier);
                }
            }
        }
    }

    private void moveLabelRight(
            Node n,
            Hierarchy hierarchy,
            int lengthOfNewHier) {
        Label label = hierarchy.getLabelOf(n);
        Interval oldTreeLabel = label.getTreeLabel();
        Interval newTreeLabel = new Interval(
                oldTreeLabel.getIndex() + lengthOfNewHier,
                oldTreeLabel.getPost() + lengthOfNewHier);
        label.setTreeLabel(newTreeLabel);
    }

    private Collection<Node> findSiblingsNext(Node n, Node directAnc, Hierarchy hierarchy) {
        Collection<Node> siblingsNextN = new HashSet<Node>();
        Collection<Node> siblings = getDirectDescendantsInMem(hierarchy, directAnc);
//                hierarchy.exploreDirectDescendants(directAnc);
        Interval nTreeInterval = hierarchy.getExistingLabelOf(n).getTreeLabel();
        Interval directAncTreeInterval = hierarchy.getExistingLabelOf(directAnc).getTreeLabel();
        for (Node sibling : siblings) {
            if (sibling == n) {
                continue;
            }
            Label existingLabelOfSibling = hierarchy.getExistingLabelOf(sibling);
            if (existingLabelOfSibling == null) {
                continue;
            }
            Interval siblingTreeInterval = existingLabelOfSibling.getTreeLabel();
            if ((directAncTreeInterval.contains(siblingTreeInterval)) &&
                    (nTreeInterval.getPost() < siblingTreeInterval.getPost())) {
                siblingsNextN.add(sibling);
            }
        }
        return siblingsNextN;
    }

    private Node findAncestorWithProperFreeGap(
            Node n,
            Node parent,
            Hierarchy hierarchy,
            int freeGapNeededSparse,
            int freeGapNeededDense,
            Collection<Node> directDescsOfWholeHierarchy) {
        Node spanTreeDirectAnc = null;
        if (parent != null) {
            spanTreeDirectAnc = parent;
        } else {
            spanTreeDirectAnc = findSpanTreeDirectAnc(n, hierarchy);
        }
        int freeGap = findMaxAvailableInterval(spanTreeDirectAnc, hierarchy).length();
        int properGapNeeded = 0;

        //Flouris has asked to try to avoid changing the label of a root 
        //(i.e. direct descendant of the wholeRoot) of a hierarchy.
        if (directDescsOfWholeHierarchy.contains(n)) {
            properGapNeeded = freeGapNeededDense;
        } else {
            properGapNeeded = freeGapNeededSparse;
        }
        if (freeGap >= properGapNeeded) {
            return spanTreeDirectAnc;
        }
        return findAncestorWithProperFreeGap(spanTreeDirectAnc, null, hierarchy, freeGapNeededSparse, freeGapNeededDense, directDescsOfWholeHierarchy);
    }

    private Node findSpanTreeDirectAnc(Node n, Hierarchy hierarchy) {
        Collection<Node> directAncs = getDirectAncestorsInMem(hierarchy, n);
//                hierarchy.exploreDirectAncestors(n);
        Interval nTreeInterval = hierarchy.getExistingLabelOf(n).getTreeLabel();
        for (Node directAnc : directAncs) {
            Interval directAncTreeInterval = hierarchy.getExistingLabelOf(directAnc).getTreeLabel();
            if (directAncTreeInterval.contains(nTreeInterval)) {
                return directAnc;
            }
        }
        return null;
    }

    private Collection<Interval> findParentTreeLabels(Node curRootOfNewHierarchy, Hierarchy hierarchy) {
        Collection<Interval> parentTreeLabels = new HashSet<Interval>();

        Collection<Node> parents = hierarchy.exploreDirectAncestors(curRootOfNewHierarchy);
        for (Node parent : parents) {
            Interval parentTreeLabel = hierarchy.getExistingLabelOf(parent).getTreeLabel();
            //if parent is a class of the old namespace
            if (parentTreeLabel != null) {
                parentTreeLabels.add(parentTreeLabel);
            }
        }
        return parentTreeLabels;
    }

    private Collection<Node> orderAccordingToDescNum(Collection<Node> col, Hierarchy hierarchy) {
        TreeMap<Integer, ArrayList<Node>> numToElems = new TreeMap<Integer, ArrayList<Node>>(
                Collections.reverseOrder());
        for (Node c : col) {
            int numOfDescs = hierarchy.exploreDescendants(c).size();
            ArrayList<Node> elements = numToElems.get(numOfDescs);
            if (elements == null) {
                elements = new ArrayList<Node>(numOfDescs);
            }
            elements.add(c);
            numToElems.put(numOfDescs, elements);
        }
        ArrayList<Node> ordered = new ArrayList<Node>(col.size());
        for (ArrayList<Node> elems : numToElems.values()) {
            ordered.addAll(elems);
        }
        return ordered;
    }

    private Collection<Node> notVisitedDescs(Node n, Hierarchy hierarchy) {
        Collection<Node> notVisitedDescs = new ArrayList<Node>();
        Collection<Node> descs = getDescendantsInMem(hierarchy, n);
//                hierarchy.exploreDescendants(n);
        for (Node d : descs) {
            if (hierarchy.getLabelOf(d).getTreeLabel().isEmpty()) {
                notVisitedDescs.add(d);
            }
        }
        return notVisitedDescs;
    }

    private boolean labelClass(
            Node c,
            Stack<Node> encountered,
            Hierarchy hierarchy,
            int step,
            int index) {
        encountered.add(c);

        Collection<Node> directDescs = hierarchy.exploreDirectDescendants(c);
        Collection<Node> orderDirectDescs = orderAccordingToDescNum(directDescs, hierarchy);

        Collection<Node> notVisitedDescs = notVisitedDescs(c, hierarchy);

        Interval interval = new Interval(index, index + (notVisitedDescs.size() + 1) * step - 1);
        boolean oldExisted = assignLabelClass(c, interval, hierarchy);
        if (oldExisted) {
            return true;
        }
        Node prevSibling = null;
        for (Node desc : orderDirectDescs) {
            if (transitiveEdge(desc, c, hierarchy)) {
                continue;
            }
            if (treeBuilder != null) {
                treeBuilder.treeEdge(desc, c);
            }
            int properIndex = 0;
            if (prevSibling == null) {
                properIndex = index;
            } else {
                Interval prevTreeLabel = hierarchy.getLabelOf(prevSibling).getTreeLabel();
                properIndex = prevTreeLabel.getPost() + 1;
            }
            boolean existed = labelClass(desc, encountered, hierarchy, step, properIndex);
            if (!existed) {
                prevSibling = desc;
            }
        }

        return false;
    }

    /**
     * Labels a new Hierarchy.
     *
     * <p>Precondition: The hierarchy fits in the available interval</p>
     *
     * @c the root of the new hierarchy
     * @properAnc the ancestor whose treelabel will contain the treelabel of c.
     * @availableInterval the interval available for the new hierarchy
     */
    private void labelHierarchy(
            Node c,
            Interval availableInterval,
            Hierarchy hierarchy) {
        int step = availableInterval.length() / (getDescendantsInMem(hierarchy, c).size() + 1);
//        int step = availableInterval.length() / (hierarchy.exploreDescendants(c).size() + 1);

        labelClass(c, new Stack<Node>(), hierarchy, step, availableInterval.getIndex());
        hierarchy.recalculateIndexForNewHierarchy();
    }

    private boolean assignLabelClass(
            Node c,
            Interval interval,
            Hierarchy hierarchy) {
        if (interval.isEmpty()) {
            throw new LabelException("Hierarchy too deep to handle; attempted to assign " +
                    "an empty label");
        }
        boolean oldExisted = false;
        Label cLabel = hierarchy.getLabelOf(c);
        if (!cLabel.getTreeLabel().isEmpty()) {
            propagateLabel(c, hierarchy);
            oldExisted = true;
        } else {
            for (Node parent : hierarchy.exploreDirectAncestors(c)) {
                if (interval.equals(hierarchy.getLabelOf(parent).getTreeLabel())) {
                    throw new LabelException("Hierarchy too deep to handle; attempted to assign " +
                            "the same label to a parent and to its kid");
                }
            }

            cLabel.setTreeLabel(interval);

            Collection<Node> directAncs = hierarchy.exploreDirectAncestors(c);
            int count = 0;
            for (Node directAnc : directAncs) {
                if (!hierarchy.getLabelOf(directAnc).getTreeLabel().isEmpty()) {
                    count++;
                }
            }
            if (count >= 1) {
                propagateLabel(c, hierarchy);
            }
        }
        return oldExisted;
    }

    /**
     * @param c the node whose treelabel should be propagated
     */
    private void propagateLabel(Node c, Hierarchy hierarchy) {
        Interval interval = hierarchy.getLabelOf(c).getTreeLabel();
        Collection<Node> directAnc = hierarchy.exploreDirectAncestors(c);
        Collection<Node> anc = hierarchy.exploreAncestors(c);

        for (Node curAnc : anc) {
            Label curAncLabel = hierarchy.getLabelOf(curAnc);

            if ((hierarchy.isNew(curAnc)) ||
                    (!curAncLabel.getTreeLabel().contains(interval))) {

                if (directAnc.contains(curAnc)) {
                    curAncLabel.addPropagatedLabel(interval, true);
                } else {
                    curAncLabel.addPropagatedLabel(interval, false);
                }
            }
        }
    }

    private boolean transitiveEdge(
            Node desc,
            Node anc,
            Hierarchy hierarchy) {
        Collection<Node> directDescs = hierarchy.exploreDirectDescendants(anc);
        for (Node n : directDescs) {
            Collection<Node> nDescs = hierarchy.exploreDescendants(n);
            if (nDescs.contains(desc)) {
                return true;
            }
        }
        return false;
    }

    private Interval getIntervalOfNextHierarchy(
            Hierarchy hierarchy,
            int estimatedIntervalSize,
            boolean sparse) {
        int intervalLengthPerHier = (int) (universeLength / maxNumOfHierarchies);
        int index = hierarchy.getIndexForNewHierarchy();
//        if (intervalLengthPerHier > estimatedIntervalSize) {
        if (sparse) {
            return new Interval(index, index + intervalLengthPerHier);
        } else {
            return new Interval(index, index + estimatedIntervalSize);
        }
    }

    /**
     * Bender's algorithm allows T to be a parameter.
     * Therefore, the interval needed for a new hierarchy may be bigger than its size (in case T>1)
     *
     * @return the length of the interval that should be assigned to the new node, if the length of interval, is enough (it depends on Bender's T variable)
     *  to fit the estimated interval size, -1 otherwise.
     */
    private int fits(Node c, Interval interval, double T, Hierarchy hierarchy) {
        int estimatedIntervalSize = this.estimateIntervalSize(c, T, hierarchy, true);


        //2*descNum is a pesimistic value (big). I choose it to be sure.
        if (interval.length() >= estimatedIntervalSize) {
            return estimatedIntervalSize;
        } else {
            return -1;
        }
    }

    /**
     * Estimates the intervalsize that should be assigned to the given class
     */
    private int estimateIntervalSize(
            Node c,
            double T,
            Hierarchy hierarchy,
            boolean sparse) {
        int size = getDescendantsInMem(hierarchy, c).size();
//        int size = hierarchy.exploreDescendants(c).size();
        if (sparse) {
            return (int) (T * 20 * (size + 2)) + 1;
        } else {
            return 2 * (size + 2) + 1;
        }
    }

    /**
     * A root of a new hierarchy may have many ancestors contained in old hierarchies.
     * This method chooses one of them, whose available interval will be given to the root.
     * Several policies are possible.
     * Now I implement the following: Choose the ancestor with the biggest available interval
     *
     * If curRootOfNewHierarchy has no ancestor, then the maximum available interval returned is [0,-2].
     * Probably not the best choice. I should revise it.
     *
     * Returns null if th
     */
    private ProperAncInfo findProperAnc(Node curRootOfNewHierarchy, Hierarchy hierarchy) {
        Collection<Node> ancestorsOfCurRoot = hierarchy.exploreDirectAncestors(curRootOfNewHierarchy);

        Node properAnc = null;
        Interval maxInterval = new Interval(0, -2);


        /**
         * TODO: if mode==HierarchyLabeler.META_CLASSES, then "http://www.w3.org/2000/01/rdf-schema#Class" should not be in ancestorsOfCurRoot
         * TODO: if mode==HierarchyLabeler.META_PROPS, then http://www.w3.org/1999/02/22-rdf-syntax-ns#Property" should not be in ancestorsOfCurRoot
         */
        for (Node curAnc : ancestorsOfCurRoot) {
            Interval curAncMaxInterval = findMaxAvailableInterval(curAnc, hierarchy);
            properAnc = curAnc;

            if (curAncMaxInterval.length() > maxInterval.length()) {
                maxInterval = curAncMaxInterval;
            }
        }
        ProperAncInfo properAncInfo = new ProperAncInfo(properAnc, maxInterval);
        return properAncInfo;
    }

    /**
     * Returns the maximum free subinterval of curAnc interval .
     */
    private Interval findMaxAvailableInterval(Node curAnc, Hierarchy hierarchy) {
        Interval properInterval = null;
        int max = 0;

        //I get the tree label of curAnc
        Interval curAncTreeInterval = hierarchy.getLabelOf(curAnc).getTreeLabel();

        Collection<Node> descs = hierarchy.exploreDirectDescendants(curAnc);
        TreeSet<Interval> orderedDescTreeIntervals = new TreeSet<Interval>(intervalComparator);

        for (Node desc : descs) {
            Interval curDescTreeInterval = hierarchy.getLabelOf(desc).getTreeLabel();
            if (!curDescTreeInterval.isEmpty()) {
                if (curAncTreeInterval.contains(curDescTreeInterval)) {
                    orderedDescTreeIntervals.add(curDescTreeInterval);
                }
            }
        }

        Interval prev = null;
        for (Interval cur : orderedDescTreeIntervals) {
            //If cur is the left most child of curAnc
            if (prev == null) {
                prev = cur;
                continue;
            }
            int freeIntervalLength = (cur.getIndex() - 1) - (prev.getPost() + 1);

            if (freeIntervalLength > max) {
                properInterval = new Interval(prev.getPost() + 1, cur.getIndex() - 1);
                max = freeIntervalLength;
            }
            prev = cur;
        }
        //check the gap between the right-most child of curAnc and the curAnc post
        if (prev != null) {
            int freeIntervalLength = (curAncTreeInterval.getPost() - 1) - (prev.getPost() + 1);

            if (freeIntervalLength > max) {
                properInterval = new Interval(prev.getPost() + 1, curAncTreeInterval.getPost() - 1);
                max = freeIntervalLength;
            }
        }


        //If curAnc was a leaf before adding the new namespace, consider its own interval
        if (properInterval == null) {

            //if curAnc has no other descedent, except for the one added in the new namespace
            if (orderedDescTreeIntervals.size() == 0) {
                properInterval = new Interval(curAncTreeInterval.getIndex(), curAncTreeInterval.getPost() - 1);
            } else {
                //O kwdikas aytos hthele na piasei thn periptwsh toy kenoy metaksy toy right-most child kai toy 
                //curAnc post. Omws exei piastei parapanw. Kanonika h ektelesh den prepei na ftanei pote edw.
                //Na valw assertion.
                properInterval = new Interval(orderedDescTreeIntervals.last().getPost() + 1, curAncTreeInterval.getPost() - 1);
            }
        }
        return properInterval;
    }

    private void fetchSiblings(Hierarchy hierarchy, Collection<Node> rootsOfNewHierarchies) {
        for (Node r : rootsOfNewHierarchies) {
            for (Node anc : hierarchy.exploreDirectAncestors(r)) {
                hierarchy.exploreDirectDescendants(anc);
            }
        }
    }
    
    private static final Comparator<Interval> intervalComparator = new Comparator<Interval>() {
        public int compare(Interval o1, Interval o2) {
            return o1.getPost() - o2.getPost();
        }
    };

    Collection<Node> getDirectDescendantsInMem(Hierarchy hierarchy, Node n) {
        return getNodesInMem(hierarchy, n, true, false);
    }

    Collection<Node> getDirectAncestorsInMem(Hierarchy hierarchy, Node n) {
        return getNodesInMem(hierarchy, n, true, true);
    }

    Collection<Node> getDescendantsInMem(Hierarchy hierarchy, Node n) {
        return getNodesInMem(hierarchy, n, false, false);
    }

    Collection<Node> getAncestorsInMem(Hierarchy hierarchy, Node n) {
        return getNodesInMem(hierarchy, n, false, true);
    }

    Collection<Node> getNodesInMem(Hierarchy hierarchy, Node n, boolean direct, boolean anc) {
        Direction d = null;
        if (n == null && hierarchy.getRoot() == null) {
            if (anc) {
                return Collections.<Node>emptySet();
            }
            if (direct) {
                return hierarchy.exploredGraph().nodes().filter(Filters.outDegreeEqual(hierarchy.exploredGraph(), 0)).drainToSet();
            } else {
                return hierarchy.exploredGraph().nodes().drainToSet();
            }
        }
        if (anc) {
            d = Direction.OUT;
        } else {
            d = Direction.IN;
        }
        if (direct) {
            return hierarchy.exploredGraph().adjacentNodes(n, d).drainToSet();
        } else {
            return Graphs.collectNodes(hierarchy.exploredGraph(), n, d);
        }
    }
}

class RelabelClassesInfo {

    HashMap<Interval, Interval> oldToNewLabels = null;
    TreeMap<Interval, Node> inervalToNodesThatNeedRelabeling = null;
    Interval freeRange = null;

    RelabelClassesInfo(
            HashMap<Interval, Interval> oldToNewLabels,
            TreeMap<Interval, Node> inervalToNodesThatNeedRelabeling,
            Interval freeRange) {
        this.oldToNewLabels = oldToNewLabels;
        this.inervalToNodesThatNeedRelabeling = inervalToNodesThatNeedRelabeling;
        this.freeRange = freeRange;
    }

    Interval getFreeRange() {
        return freeRange;
    }

    TreeMap<Interval, Node> getInervalToNodesThatNeedRelabeling() {
        return inervalToNodesThatNeedRelabeling;
    }

    HashMap<Interval, Interval> getOldToNewLabels() {
        return oldToNewLabels;
    }
}

class ProperAncInfo {

    private final Node properAnc;
    private final Interval maxInterval;

    ProperAncInfo(
            Node properAnc,
            Interval maxInterval) {
        this.properAnc = properAnc;
        this.maxInterval = maxInterval;
    }

    Interval getMaxInterval() {
        return maxInterval;
    }

    Node getProperAnc() {
        return properAnc;
    }
}
