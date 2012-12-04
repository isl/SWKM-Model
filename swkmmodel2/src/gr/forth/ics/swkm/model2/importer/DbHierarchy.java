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


package gr.forth.ics.swkm.model2.importer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gr.forth.ics.graph.Direction;
import gr.forth.ics.swkm.model2.labels.*;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import gr.forth.ics.util.IntervalSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * A hierarchy implementation (for labeling) which communicates with a database.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
abstract class DbHierarchy extends MainMemoryHierarchy {
    private final Graph graph;

    //not final because it can change by merging with another PredefinedLabels instance
    private Map<Uri, Label> predefinedLabels;

    private static final ThreadLocal<Map<Uri, Label>> local
            = new ThreadLocal<Map<Uri, Label>>();

    //this must be maintained by any exploration of existing (in the database) node.
    private final Map<Integer, Node> nodesByPost = Maps.newHashMap();

    private DbHierarchy(Graph g,
            Map<Uri, Label> predefinedLabels) {
        super(init(g, predefinedLabels));
        this.graph = g;
        this.predefinedLabels = predefinedLabels;
        local.remove();

        for (Node n : g.nodes()) {
            if (!isNew(n)) {
                nodesByPost.put(getLabelOf(n).getTreeLabel().getPost(), n);
            }
        }
    }

    private Node getOrCreateNode(Interval interval) {
        Node n = nodesByPost.get(interval.getPost());
        if (n == null) {
            //create node only if there was not already found
            n = graph.newNode(interval);
            nodesByPost.put(interval.getPost(), n);
        }
        return n;
    }

    //only works if hasKnownUri(node) == true
    private Uri uriOf(Node node) {
        return ((Resource)node.getValue()).getUri();
    }

    //only works if hasKnownUri(node) == false
    private Interval intervalOf(Node node) {
        return (Interval)node.getValue();
    }

    //this is a wonderfully ugly hack to avoid circular initialization problem. Sorry.
    private static Graph init(Graph g, Map<Uri, Label> labels) {
        local.set(labels);
        return g;
    }

    @Override
    public Label getExistingLabelOf(Node node) {
        if (node == null) { //(non-existent) root of properties, sorry for hard-coding
            return new Label(new Interval(301, 2000000000));
        }
        Map<Uri, Label> labels = this.predefinedLabels;
        if (labels == null) {
            labels = local.get();
        }
        if (!hasKnownUri(node)) {
            //its an explored node of which we don't have its URI
            return new Label(intervalOf(node));
        }
        Label label = labels.get(uriOf(node));
        if (label != null) {
            return label;
        }
        return super.getExistingLabelOf(node);
    }

    boolean hasKnownUri(Node node) {
        return !(node.getValue() instanceof Interval);
    }

    @Override
    public boolean isNew(Node node) {
        Object value = node.getValue();
        if (value instanceof Interval) {
            return false;
        } else if (value instanceof Resource) {
            Uri uri = ((Resource)value).getUri();
            if (uri.hasEqualNamespace(RdfSchema.NAMESPACE) ||
                    uri.hasEqualNamespace(Rdf.NAMESPACE) ||
                    uri.hasEqualNamespace(XmlSchema.NAMESPACE) ||
                    uri.hasEqualNamespace(RdfSuite.NAMESPACE)) {
                return false;
            }
            return super.isNew(node);
        } else {
            throw new IllegalArgumentException("Unexpected node value: " + value);
        }
    }

    @Override
    public Collection<Node> exploreNodesIncludedIn(Collection<Interval> ranges) {
        //merging overlapping intervals
        IntervalSet intervalSet = new IntervalSet();
        for (Interval interval : ranges) {
            intervalSet.add(interval.getIndex(), interval.getPost());
        }

        try {
            //building a single query and placing the burden on the database to return *unique*
            //results (by using UNION and not UNION ALL) seems faster than either
            //having a prepared statement invoked per each interval, or by using UNION ALL.
            //The latter alternative is only a bit slower, practically the same. In my microbenchmark,
            //there were two intervals and roughly 30% duplicate results.
            StringBuilder sb = new StringBuilder(512);
            sb.append("SELECT s.att2, s.att0, s.att1 FROM ") //index, post, superPost
                    .append(getTableName())
                    .append(" s WHERE FALSE "); // :-)
            for (gr.forth.ics.util.Interval interval : intervalSet.intervals()) {
                        sb.append("OR (s.att2 >= ").append(interval.start())
                           .append(" AND s.att0 <= ").append(interval.end())
                           .append(") ");
            }
            ResultSet rs = Jdbc.statement().executeQuery(sb.toString());

            final Collection<Node> loadedNodes = Lists.newArrayList();
            final Object parentKey = new Object();
            while (rs.next()) {
                final int index = rs.getInt(1);
                final int post = rs.getInt(2);
                final Interval interval = new Interval(index, post);
                Node n = getOrCreateNode(interval);

                final int parentPost = rs.getInt(3);
                n.putWeakly(parentKey, parentPost);
                loadedNodes.add(n);
                getLabelOf(n).setTreeLabel(interval);
            }
            rs.close();

            for (Node n : loadedNodes) {
                Node parentNode = nodesByPost.get(n.getInt(parentKey));
                if (parentNode != null && !graph.areAdjacent(n, parentNode, Direction.OUT)) {
                    graph.newEdge(n, parentNode);
                }
            }
            return loadedNodes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Collection<Node> exploreAdjacent(Node node, Direction direction) {
        if (node != null && isNew(node)) { //node may be null if we are talking about the root of the properties hierarchy
            return graph.adjacentNodes(node, direction).drainToList();
        }
        Collection<Node> nodes = Lists.newArrayList();
        try {
            final ResultSet rs;
            if (node != null) {
                final String whereClause;
                switch (direction) {
                    case IN: whereClause = "s1.att0 = ? AND s2.att1 = s1.att0"; break;
                    case OUT: whereClause = "s1.att0 = ? AND s1.att1 = s2.att0"; break;
                    default: throw new IllegalArgumentException("This direction is not allowed: " + direction);
                }
                rs = Jdbc.prepared(
                        "SELECT s2.att2, s2.att0 " + //index, post
                        "FROM " + getTableName() + " s1, " + getTableName() + " s2 WHERE " +
                        whereClause,
                        getExistingLabelOf(node).getTreeLabel().getPost()).executeQuery();
            } else {
                if (direction == Direction.IN) {
                    rs = Jdbc.prepared(
                            "SELECT s.att2, s.att0 " + //index, post
                            "FROM " + getTableName() + " s WHERE " +
                            "s.att1 IS NULL").executeQuery();
                } else {
                    rs = Jdbc.prepared("SELECT * FROM " + getTableName() + " WHERE FALSE")
                            .executeQuery(); //no ancestors!
                }
            }
            while (rs.next()) {
                final int index = rs.getInt(1);
                final int post = rs.getInt(2);
                Node adjacent = getOrCreateNode(new Interval(index, post));
                nodes.add(adjacent);
                if (node != null && !graph.areAdjacent(node, adjacent, direction)) {
                    if (direction == Direction.OUT) {
                        graph.newEdge(node, adjacent);
                    } else {
                        graph.newEdge(adjacent, node);
                    }
                }
            }
            rs.close();
            return nodes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getIndexForNewHierarchy() {
        if (super.getIndexForNewHierarchy() != 0) {
            return super.getIndexForNewHierarchy();
        }
        try {
            //We *must* lock this table so concurrent imports will not try to store
            //overlapping labels with the current import.
            //This obviously kills the concurrency of importing, since the lock cannot
            //even be released earlier than transaction commit/rollback, and we really
            //want to have only *one* transaction for the import.

            //This is the strictest kind of lock is PostgreSQL
            Jdbc.execute("LOCK MAX_POSTORDER IN ACCESS EXCLUSIVE MODE");

            ResultSet rs = Jdbc.query("SELECT " + getFieldNameOfMaxPostOrderTable() + " FROM MAX_POSTORDER");
            rs.next();
            int value = rs.getInt(1);
            rs.close();
            return value;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exploreEverythingAsNew() {
        predefinedLabels.clear();
        nodesByPost.clear();
        exploreNodesIncludedIn(Arrays.asList(new Interval(0, Integer.MAX_VALUE)));
        predefinedLabels = PredefinedLabels.swkmPredefinedLabels().toMap();
        for (Node n : exploredGraph().nodes()) {
            clearLabel(n);
        }
    }

    abstract RdfType getType();
    abstract String getTableName();
    abstract String getFieldNameOfMaxPostOrderTable();

    Map<Uri, Label> findUpdatedResourcesWithKnownUri() {
        Map<Uri, Label> resourcesToLabels = Maps.newHashMap();
        for (Node n : exploredGraph().nodes()) {
            if (hasKnownUri(n) && hasUpdatedLabel(n)) {
                resourcesToLabels.put(uriOf(n), getLabelOf(n));
            }
        }
        return resourcesToLabels;
    }

    Map<Interval, Label> findUpdatedResourcesWithUnknownUri() {
        Map<Interval, Label> resourcesToLabels = Maps.newHashMap();
        for (Node n : exploredGraph().nodes()) {
            if (!hasKnownUri(n) && hasUpdatedLabel(n)) {
                resourcesToLabels.put(intervalOf(n), getLabelOf(n));
            }
        }
        return resourcesToLabels;
    }

    static DbHierarchy newClassHierarchy(Model model, PredefinedLabels predefinedLabels) {
        Graph g = newClassHierarchyGraph(model);
        if (g == null) {
            return null;
        }
        return new DbClassHierarchy(g, predefinedLabels);
    }

    static DbHierarchy newPropertyHierarchy(Model model, PredefinedLabels predefinedLabels) {
        Graph g = newPropertyHierarchyGraph(model);
        if (g == null) {
            return null;
        }
        return new DbPropertyHierarchy(g, predefinedLabels);
    }

    static DbHierarchy newMetaclassHierarchy(Model model, PredefinedLabels predefinedLabels) {
        Graph g = newMetaclassHierarchyGraph(model);
        if (g == null) {
            return null;
        }
        return new DbMetaclassHierarchy(g, predefinedLabels);
    }

    static DbHierarchy newMetapropertyHierarchy(Model model, PredefinedLabels predefinedLabels) {
        Graph g = newMetapropertyHierarchyGraph(model);
        if (g == null) {
            return null;
        }
        return new DbMetapropertyHierarchy(g, predefinedLabels);
    }

    private static class DbClassHierarchy extends DbHierarchy {
        public DbClassHierarchy(Graph g,
                PredefinedLabels predefinedLabels) {
            super(g, predefinedLabels.toMap());
        }

        @Override
        RdfType getType() {
            return RdfType.CLASS;
        }

        @Override
        String getTableName() {
            return "SUBCLASS";
        }

        @Override String getFieldNameOfMaxPostOrderTable() {
            return "att2";
        }
    }

    private static class DbPropertyHierarchy extends DbHierarchy {
        public DbPropertyHierarchy(Graph g,
                PredefinedLabels predefinedLabels) {
            super(g, predefinedLabels.toMap());
        }

        @Override
        RdfType getType() {
            return RdfType.PROPERTY;
        }

        @Override
        String getTableName() {
            return "SUBPROPERTY";
        }

        @Override String getFieldNameOfMaxPostOrderTable() {
            return "att3";
        }
    }

    private static class DbMetaclassHierarchy extends DbHierarchy {
        public DbMetaclassHierarchy(Graph g,
                PredefinedLabels predefinedLabels) {
            super(g, predefinedLabels.toMap());
        }

        @Override
        RdfType getType() {
            return RdfType.METACLASS;
        }

        @Override
        String getTableName() {
            return "SUBMETACLASS";
        }

        @Override String getFieldNameOfMaxPostOrderTable() {
            return "att0";
        }
    }

    private static class DbMetapropertyHierarchy extends DbHierarchy {
        public DbMetapropertyHierarchy(Graph g,
                PredefinedLabels predefinedLabels) {
            super(g, predefinedLabels.toMap());
        }

        @Override
        RdfType getType() {
            return RdfType.METAPROPERTY;
        }

        @Override
        String getTableName() {
            return "SUBMETAPROPERTY";
        }

        @Override String getFieldNameOfMaxPostOrderTable() {
            return "att1";
        }
    }
}
