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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import gr.forth.ics.graph.Direction;
import gr.forth.ics.graph.Edge;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.Node;
import gr.forth.ics.graph.algo.Orders;
import gr.forth.ics.graph.algo.transitivity.Closure;
import gr.forth.ics.graph.algo.transitivity.Transitivity;
import gr.forth.ics.graph.event.EdgeListener;
import gr.forth.ics.graph.algo.transitivity.PathFinder;
import gr.forth.ics.graph.algo.transitivity.SuccessorSetFactory;
import gr.forth.ics.graph.event.EmptyGraphListener;
import gr.forth.ics.graph.event.GraphEvent;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A class that can delete redundant triples, based on RDF(S) inference rules.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Inference {
    /**
     * Deletes all redundant triples from the specified model.
     * <p>
     * These are the rules that define which triples are considered redundant:
     * <ul>
     * <li>
     *  if all these triples are found (for any {@code ?A, ?B, ?C}): <BR>
     *{@code <?A rdfs:subClassOf ?B>} <BR>
     *{@code <?B rdfs:subClassOf ?C>} <BR>
     *{@code <?A rdfs:subClassOf ?C>} <BR>
     * then the last triple is redundant.
     * </li>
     * <li>
     *  if all these triples are found (for any {@code ?A, ?B, ?C}): <BR>
     *{@code <?A rdfs:subPropertyOf ?B>} <BR>
     *{@code <?B rdfs:subPropertyOf ?C>} <BR>
     *{@code <?A rdfs:subPropertyOf ?C>} <BR>
     * then the last triple is redundant.
     * </li>
     * <li>
     *  if all these triples are found (for any {@code ?A, ?B, ?C}): <BR>
     *{@code <?A rdfs:subClassOf ?B>} <BR>
     *{@code <?C rdf:type ?A>} <BR>
     *{@code <?C rdf:type ?B>} <BR>
     * then the last triple is redundant.
     * </li>
     * <li>
     *  if all these triples are found (for any {@code ?P, ?Q, ?X, ?Y}): <BR>
     *{@code <?Q rdfs:subPropertyOf ?P>} <BR>
     *{@code <?X ?Q ?Y>} <BR>
     *{@code <?X ?P ?Y>} <BR>
     * then the last triple is redundant.
     * </li>
     * </ul>
     *
     * <p>Implementation note: This method performs significantly better when the specified model
     * has been <em>labeled</em>, which can be forced via {@link Model#updateLabels()}.
     *
     * @param model the model from which to delete the redundant triples
     */
    public static void reduce(Model model) {
        model.delete(findRedundantTriples(model));
    }

    /**
     * Returns, but does not delete, the redundant triples of a model, as defined in {@link #reduce(Model)}.
     *
     * @param model the model for which to find the redundant triples
     * @return the redundant triples of the specified model
     */
    public static Collection<Triple> findRedundantTriples(Model model) {
        Collection<Triple> triplesToRemove = new ArrayList<Triple>();
        {
            Graph subClassGraph = GraphUtils.classesAndInstancesGraph(model);
            PathFinder pathFinder = ((ModelImpl)model).getLabelManager().areLabelsAvailable() ?
                    new LabelingPathFinder(model) : null;
            Iterables.addAll(triplesToRemove, reduce(subClassGraph,
                    pathFinder != null ? pathFinder :
                        Transitivity.acyclicClosure(subClassGraph, SuccessorSetFactory.intervalBased(subClassGraph))
                        , true));
        }

        {
            Graph subPropertyGraph = GraphUtils.propertiesAndInstancesGraph(model);
            PathFinder pathFinder = ((ModelImpl)model).getLabelManager().areLabelsAvailable() ?
                    new LabelingPathFinder(model) : null;
            Iterables.addAll(triplesToRemove, reduce(subPropertyGraph,
                    pathFinder != null ? pathFinder :
                        Transitivity.acyclicClosure(subPropertyGraph, SuccessorSetFactory.intervalBased(subPropertyGraph)),
                        false));
        }
        return triplesToRemove;
    }

    private static final Function<Edge, Triple> edgeToTriple = new Function<Edge, Triple>() {
        public Triple apply(Edge edge) {
            return (Triple)edge.getValue();
        }
    };


    private static Iterable<Triple> reduce(Graph graph, PathFinder pathFinder, boolean specialCase) {
        final Collection<Edge> removedEdges = new ArrayList<Edge>();
        EdgeListener listener = new EmptyGraphListener() {
            @Override
            public void edgeRemoved(GraphEvent e) {
                removedEdges.add(e.getEdge());
            }
        };
        graph.addEdgeListener(listener);
        if (specialCase) {
            acyclicReductionAvoidingSpecialCase(graph, pathFinder);
        } else {
            Transitivity.acyclicReduction(graph, pathFinder);
        }
        graph.removeEdgeListener(listener);
        return Iterables.transform(removedEdges, edgeToTriple);
    }

    /**Copied from gr.forth.ics.graph.algo.transitivity.Transitivity#acyclicReduction
     * in order to add a special case handling.
     * 
     * While <A type C> is redundant when these exist:
     * <A type B>
     * <B subClassOf C>
     * 
     * It is not redundant if only these exist:
     * <A subClassOf B>
     * <B type C>.
     *
     * In the graph, we have an edge for either triple kind, so in both cases the edge A --> C
     * will be considered redundant. We have to special case to avoid treating the second case
     * as redundant.
     */
    private static void acyclicReductionAvoidingSpecialCase(Graph g, PathFinder pathFinder) {
        for (Node n : Orders.topological(g)) {
            for (Edge e1 : g.edges(n, Direction.OUT)) {
                for (Edge e2 : g.edges(n, Direction.OUT)) {
                    if (e1 == e2) continue;
                    if (pathFinder.pathExists(e1.opposite(n), e2.opposite(n))) {
                        //this is the special case to avoid:
                        if (edgeHasUri(e1, RdfSchema.SUBCLASSOF)) {
                            //we remove e2 as redundant only if we find a subClassOf triple connecting
                            //the two neighboring nodes to n
                            boolean foundSubClassOf = false;
                            for (Edge e3 : g.edges(e1.opposite(n), e2.opposite(n))) {
                                if (edgeHasUri(e3, RdfSchema.SUBCLASSOF)) {
                                    foundSubClassOf = true;
                                    break;
                                }
                            }
                            if (!foundSubClassOf) {
                                continue; //didn't find subClassOf, thus e2 is not really redundant
                            }
                        }
                        g.removeEdge(e2);
                    }
                }
            }
        }
    }

    private static boolean edgeHasUri(Edge e, Uri uri) {
        return ((Triple)e.getValue()).predicate().is(uri);
    }

    private static class LabelingPathFinder implements PathFinder {
        private final Model model;

        LabelingPathFinder(Model model) {
            this.model = model;
        }

        public boolean pathExists(Node n1, Node n2) {
            RdfNode node1 = (RdfNode)n1.getValue();
            RdfNode node2 = (RdfNode)n2.getValue();
            return node1.asInheritable().isDescendantOf(node2.asInheritable());
        }
    }

    /**
     * Adds all the inferred triples to the specified model
     * <p>
     * These are the rules that define which triples are considered inferred:
     * <ul>
     * <li>
     *  if all these triples are found (for any {@code ?A, ?B, ?C}): <BR>
     *{@code <?A rdfs:subClassOf ?B>} <BR>
     *{@code <?B rdfs:subClassOf ?C>} <BR>
     * then this triple is added: {@code <?A rdfs:subClassOf ?C>}.
     * </li>
     * <li>
     *  if all these triples are found (for any {@code ?A, ?B, ?C}): <BR>
     *{@code <?A rdfs:subPropertyOf ?B>} <BR>
     *{@code <?B rdfs:subPropertyOf ?C>} <BR>
     * then this triple is added: {@code <?A rdfs:subPropertyOf ?C>}.
     * </li>
     *<li>
     *  if all these triples are found (for any {@code ?A, ?B, ?a}): <BR>
     *{@code <?A rdfs:subClassOf ?B>} <BR>
     *{@code <?a rdf:type ?A>} <BR>
     * then this triple is added: {@code <?a rdf:type ?B>}.
     * </li>
     * <li>
     *  if all these triples are found (for any {@code ?P, ?Q, ?X, ?Y}): <BR>
     *{@code <?Q rdfs:subPropertyOf ?P>} <BR>
     *{@code <?X ?Q ?Y>} <BR>
     * then this triple is added: {@code <?X ?P ?Y>}.
     * </li>
     * </ul>
     *
     * @param model the model in which to add the inferred triples
     */
    public static void closure(Model model) {
        Graph g = GraphUtils.toGraph(model, RdfSchema.SUBCLASSOF).graph();
        Closure closure = Transitivity.acyclicClosure(g, SuccessorSetFactory.intervalBased(g));
        materializeSubsumptionInference(g, closure, model, RdfSchema.SUBCLASSOF);
        materializeTypeInference(g, model, closure);
        
        g = GraphUtils.toGraph(model, RdfSchema.SUBPROPERTYOF).graph();
        closure = Transitivity.acyclicClosure(g, SuccessorSetFactory.intervalBased(g));
        materializeSubsumptionInference(g, closure, model, RdfSchema.SUBPROPERTYOF);
        materializePropertyInstanceInference(g, model, closure);
        
    }

    private static void materializePropertyInstanceInference(Graph g, Model model, Closure closure) {
        for (Node n1 : g.nodes()) {
            Resource property = (Resource) n1.getValue();
            for (Triple triple : model.triples().p(property).fetch()) {
                for (Node propAnc : closure.successorsOf(n1)) {
                    ObjectNode subj = triple.subject();
                    RdfNode obj = triple.object();
                    model.add().s(subj).p((Resource) propAnc.getValue()).o(obj);
                }
            }
        }
    }

    private static void materializeSubsumptionInference(Graph g, Closure closure, Model model, Uri property) {
        Resource p = model.mapResource(property);
        for (Node n1 : g.nodes()) {
            Resource rdfNodeSub = (Resource) n1.getValue();
            for (Node n2 : closure.successorsOf(n1)) {
                Resource rdfNodeObj = (Resource) n2.getValue();
                model.add().s(rdfNodeSub).p(p).o(rdfNodeObj);
            }
        }
    }

    private static void materializeTypeInference(Graph g, Model model, Closure closure) {
        Resource p = model.mapResource(Rdf.TYPE);
        for (Node n : g.nodes()) {
            Resource node = (Resource) n.getValue();
            for (ObjectNode instance : model.triples().p(p).o(node).fetch().subjects()) {
                for (Node anc : closure.successorsOf(n)) {
                    model.add().s(instance).p(p).o((RdfNode) anc.getValue());
                }
            }
        }
    }
}
