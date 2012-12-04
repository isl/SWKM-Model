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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import gr.forth.ics.graph.Edge;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.PrimaryGraph;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Map;

/**
 * Graph-related utilities.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class GraphUtils {
    /**
     * Creates a graph representation which includes all instances of the specified properties.
     *
     * <p>Each graph edge's {@link Edge#getValue() value} is the respective triple, as well as
     * each graph node's {@link Node#getValue() value} is the respective RdfNode.
     * 
     * @param model the model of which to create a graph representation
     * @param properties the properties instances of which to include in the graph
     * @return a graph with an edge for every property matching the specified URIs
     */
    public static RdfGraph toGraph(Model model, Uri ... properties) {
        return toGraph(model, Predicates.<Triple>alwaysTrue(), properties);
    }

    /**
     * Creates a graph representation which all instances of the specified properties that
     * are accepted by the provided triple filter. Specifically, only those triples which
     * have a predicate equal to one of the properties <em>and</em> the {@code tripleFilter}
     * returns {@code true} for them are inserted to the graph.
     *
     * <p>Each graph edge's {@link Edge#getValue() value} is the respective triple, as well as
     * each graph node's {@link Node#getValue() value} is the respective RdfNode.
     *
     * @param model the model of which to create a graph representation
     * @param tripleFilter the filter predicate for which the triples must satisfy 
     * @param properties the properties instances of which to include in the graph
     * @return a graph with an edge for every property matching the specified URIs
     */
    public static RdfGraph toGraph(Model model, Predicate<Triple> tripleFilter, Uri... properties) {
        Map<RdfNode, Node> map = Maps.newHashMap();
        Graph graph = new PrimaryGraph();
        for (Uri property : properties) {
            for (Triple t : model.triples().p(property).fetch()) {
                if (!tripleFilter.apply(t)) continue;
                Node subject = getOrCreateMapped(t.subject(), graph, map);
                Node object = getOrCreateMapped(t.object(), graph, map);
                graph.newEdge(subject, object, t);
            }
        }
        return new RdfGraph(graph, map);
    }

    /**
     * Creates a graph representation which includes triples that
     * are accepted by the provided triple filter. Specifically, only those triples which
     * have a predicate which the {@code tripleFilter}
     * returns {@code true} for them are inserted to the graph.
     *
     * <p>Each graph edge's {@link Edge#getValue() value} is the respective triple, as well as
     * each graph node's {@link Node#getValue() value} is the respective RdfNode.
     *
     * @param model the model of which to create a graph representation
     * @param tripleFilter the filter predicate for which the triples must satisfy
     * @return a graph with an edge for every property matching the specified URIs
     */
    public static RdfGraph toGraph(Model model, Predicate<Triple> tripleFilter) {
        Map<RdfNode, Node> map = Maps.newHashMap();
        Graph graph = new PrimaryGraph();
            for (Triple t : model.triples().fetch()) {
                if (!tripleFilter.apply(t)) continue;
                Node subject = getOrCreateMapped(t.subject(), graph, map);
                Node object = getOrCreateMapped(t.object(), graph, map);
                graph.newEdge(subject, object, t);
            }
        return new RdfGraph(graph, map);
    }


    /**
     * Creates a graph with an edge for every {@code rdfs:subClassOf} and
     * {@code rdf:type} triple. Each edge's value is the respective triple, as well as
     * each node's value is the respective RdfNode.
     * 
     * @param model the model of which to create a graph
     * @return a graph with an edge for every
     * {@code rdfs:subClassOf} and {@code rdf:type} triple.
     */
    static Graph classesAndInstancesGraph(Model model) {
        return toGraph(model, RdfSchema.SUBCLASSOF, Rdf.TYPE).graph();
    }

    /**
     * Creates a graph with an edge for every {@code rdfs:subPropertyOf}, with nodes
     * the respective properties, and a node for each
     * {@code <X someProperty Y>} triple, which has exactly one edge to the node
     * representing {@code someProperty}.. Each edge's value is the respective triple, as well as
     * each node's value is the respective RdfNode.
     *
     * @param model the model of which to create a graph
     * @return a graph with an edge for every
     * {@code rdfs:subClassOf} and {@code rdf:type} triple.
     */
    static Graph propertiesAndInstancesGraph(Model model) {
        Map<RdfNode, Node> propertiesMap = Maps.newHashMap();
        Map<Pair<RdfNode>, Node> instancesMap = Maps.newHashMap();
        Graph graph = new PrimaryGraph();
        Resource subPropertyOf = model.mapResource(RdfSchema.SUBPROPERTYOF);
        for (RdfNode node : model.findNodes(RdfType.PROPERTY)) {
            Resource property = (Resource)node;
            Node propertyNode = getOrCreateMapped(node, graph, propertiesMap);
            for (Triple triple : model.triples().p(property).fetch()) {
                Pair<RdfNode> instance = new Pair<RdfNode>(triple.subject(), triple.object());

                Node instanceNode = getOrCreateMapped(instance, graph, instancesMap);
                graph.newEdge(instanceNode, propertyNode, triple);
            }
            for (Triple triple : model.triples().s(property).p(subPropertyOf).fetch()) {
                Node superPropertyNode = getOrCreateMapped(triple.object(), graph, propertiesMap);
                graph.newEdge(propertyNode, superPropertyNode, triple);
            }
        }
        return graph;
    }
    
    private static <T> Node getOrCreateMapped(T key, Graph graph, Map<T, Node> map) {
        Node node = map.get(key);
        if (node == null) {
            node = graph.newNode(key);
            map.put(key, node);
        }
        return node;
    }

    private static class Pair<E> {
        private final E one;
        private final E two;

        Pair(E one, E two) {
            this.one = one;
            this.two = two;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pair<?> other = (Pair<?>) obj;
            if (this.one != other.one) {
                return false;
            }
            if (this.two != other.two) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + one.hashCode();
            hash = 37 * hash + two.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "[" + one + ", " + two + "]";
        }
    }

    /**
     * A graph constructed by an {@link Model RDF model}, along with a mapping
     * from {@link RdfNode RDF nodes} to graph nodes.
     */
    public static class RdfGraph {
        private final Graph graph;
        private final Map<RdfNode, Node> map;

        /**
         * Constructs an RdfGraph with a Graph and a mapping between RDF nodes to
         * corresponding nodes of the graph. The map is not copied, but stored directly.
         *
         * @param graph the graph to return from {@link #graph()}
         * @param map the map to return from {@link #nodeMap()}
         */
        public RdfGraph(Graph graph, Map<RdfNode, Node> map) {
            this.graph = graph;
            this.map = map;
        }

        /**
         * Returns the graph constructed from an {@link Model RDF model}.
         *
         * @return the graph constructed from an {@link Model RDF model}
         */
        public Graph graph() {
            return graph;
        }

        /**
         * Returns the (mutable) mapping from RDF nodes to nodes to the
         * contructed {@link #graph() graph}.
         *
         * @return the (mutable) mapping from RDF nodes to nodes to the
         * contructed {@link #graph() graph}
         */
        public Map<RdfNode, Node> nodeMap() {
            return map;
        }
    }
}
