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

import gr.forth.ics.swkm.model2.Literal.LiteralFormatException;
import gr.forth.ics.swkm.model2.Uri.UriFormatException;
import gr.forth.ics.swkm.model2.event.RdfNodeListener;
import gr.forth.ics.swkm.model2.event.TripleListener;
import gr.forth.ics.swkm.model2.labels.LabelManager;
import gr.forth.ics.swkm.model2.labels.PredefinedLabels;
import gr.forth.ics.swkm.model2.views.Inheritable;
import java.util.Iterator;
import java.util.Set;

/**
 * An RDF model. A model consists of a set of triples, which in turn contain three
 * {@link RdfNode} RDF nodes (a <em>subject</em>, a <em>predicate</em>,
 * and an <em>object</em>). Also, triples are grouped into <em>named graphs</em>.
 * A named graph contains a set of triples, and a single triple can be contained in multiple
 * named graphs.
 *
 * <p>Three kinds of RDF nodes are supported:
 * <ul>
 * <li>{@link Resource Resources}, which are nodes identified by a {@link Uri URI}</li>
 * <li>{@link LiteralNode Literals}, which have a simple {@link Literal literal} value</li>
 * <li>{@link BlankNode Blank nodes}, which have a locally (per RDF document) scoped identifier (which
 * is not universally unique, contrary to a resource.</li>
 * </ul>
 *
 * <a name="valuesAndNodes"><!-- Linked by map methods--></a>
 * <h4>Distinction between values and nodes</h4>
 * <p>It is important to understand the distinction between <em>values</em>
 * (URIs, literals, blank node identifiers) and the <em>nodes</em> of a model that correspond to them.
 * Multiple value objects can be created with the same value, but in a model only a <em>single</em> node
 * can ever be created with the same value. This implies a <em>mapping</em> between values and nodes,
 * i.e. all equal values map to the same node. The model manages this mapping. To obtain a node for
 * a value, the various mapping methods should be used (see {@linkplain #map(String)}, {@linkplain #mapResource(Uri)},
 * {@linkplain #mapResource(String)}, {@linkplain #mapLiteral(Literal)}, {@linkplain #mapBlankNode(String)},
 * {@linkplain #mapObjectNode(String)}). The mapping methods return nodes that can be used to
 * create triples.
 *
 * <p>Triples are formed with nodes. Triples can be added through {@linkplain #add(Resource, ObjectNode, Resource, RdfNode)}
 * or {@linkplain #add() }. The first takes all the required nodes directly, while the second offer a more convenient
 * way to add triples without caring so much about explicit mappings. It is illegal to try to add a triple
 * using nodes of another model. The foreign nodes must be mapped to this model instead, simply
 * using {@linkplain RdfNode#mappedTo(Model)}.
 *
 * <p>Nodes have an associated {@linkplain RdfType type}, see for details the
 * <a href="../../../../../gr/forth/ics/swkm/model2/RdfNode.html#nodeTyping">Node typing section in RdfNode</a>.
 *
 * <p>The model offers ways to find the nodes of a type, see
 * {@linkplain #findNodes(RdfType, RdfType[])} and
 * {@linkplain #namedGraphs() }.
 *
 * <p>Triples must be contained at least in one named graph. If a triple is removed from the last graph
 * that contained it, it is removed from the model as well. For convenience,
 * when adding a triple, {@code null} may be specified as the named graph of the triple, which is
 * equivalent to the {@linkplain #defaultNamedGraph() default named graph}.
 *
 * <p>A model can answer simple triple queries, of the form {@code (?namedGraph, ?subject, ?predicate, ?object)},
 * where any part can be fixed or can be left as a "wildcard" (i.e. to match anything). See
 * {@linkplain #triples() } for details.
 *
 * <p>Apart from the triple-based access, a more object oriented access is offered. See
 * {@link RdfNode} and particularly the {@code asXXX} methods.
 *
 * <a name="deleting"><!-- Linked internally --></a>
 * <h4>Deleting triples</h4>
 * <p>Triples may be deleted by several ways. See {@linkplain #delete(Triple)}, {@linkplain #delete(Iterable)},
 * and also {@linkplain Triple#delete() }. Deleting multiple triples in a single call can be faster that deleting
 * triples one by one, but it is not guarranteed. Another way to delete triples is through
 * {@linkplain #triples()}, which is also used to answer triple queries. When the triple query is specified,
 * if, instead of {@linkplain QueryBuilder#fetch()}, {@linkplain QueryBuilder#delete()} is called,
 * all the triples that would normally be returned with the specified query are deleted. See {@linkplain #triples()}
 * for usage examples.
 *
 * <p>Redundant triples can be deleted from a model with the {@linkplain Inference#reduce(Model) } method; see its specification
 * for details on what consistutes a redundant triple.
 *
 * <p>All methods that take instances of {@code RdfNode} may throw {@linkplain ClassCastException}
 * when the RdfNode instances are created using a different Model implementation.
 *
 * <p>All methods of this type throw {@linkplain NullPointerException} for null arguments, unless otherwise specified.
 *
 * @see <a href="../../../../../docs/ClassPropertyResource.pdf">"Discussion on the semantics of rdfs:Resource, rdfs:Class, rdf:Property" for a reference of supported RDF typing rules.</a>
 * @see <a href="../../../../../gr/forth/ics/swkm/model2/RdfNode.html#nodeTyping">Node typing section in RdfNode</a>
 * @see RdfNode
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface Model {
    /**
     * Adds a triple with the specified named graph, subject, predicate and object
     * into this model, if not already there.
     *
     * <p>A distinct Triple instance is created per different combination of subject-predicate-object
     * added in this model, not per different named graph. For example, calling this method
     * with arguments:
     * <ul>
     * <li>{@code <G1, S, P, O>}, and
     * <li>{@code <G2, S, P, O>},
     * </ul>
     * A single triple {@code (S, P, O)} instance would be created, that would be contained in both graphs
     * {@code G1} and {@code G2}.
     *
     * @param namedGraph the named graph that the added triple belongs. If null, {@linkplain #defaultNamedGraph()} is assumed
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @return the added Triple, which will have the same subject, predicate and object as the parameters
     * of this invocation, and will be also contained in the specified named graph
     * @throws IllegalArgumentException if any node is given that is not owned by this model (i.e.
     * it is a node of another model).
     * @throws ValidationException if the added triple creates a node typing error
     * @see #add()
     */
    Triple add(Resource namedGraph, ObjectNode subject, Resource predicate, RdfNode object);

    /**
     * Adds a triple in a step-by-step fashion. The procedure of adding a triple is:
     * <ul>
     * <li>(optionally) define a named graph for the triple
     * <li>define the subject
     * <li>define the predicate
     * <li>define the object
     * </ul>
     * When the workflow is completed, the designated triple is added. This way of adding a triple
     * is in some cases more convenient than {@link #add(Resource, ObjectNode, Resource, RdfNode)},
     * because instead of needing, for each argument, to have the respective {@code RdfNode}
     * (which for example may be obtained by mapping a {@linkplain Uri} to a {@code Resource}),
     * convenient overloadings are offered, which accept simple values (and automatically map them to nodes).
     *
     * <p>Example of adding a triple in the {@linkplain #defaultNamedGraph() default} graph:
     * <pre>{@code
     *Model model = ...;
     *String subjectURI = ...;
     *String predicateURI =...;
     *Literal literal = ...;
     *Triple triple = model.add().s(subjectURI).p(predicateURI).o(literal);
     * }</pre>
     *
     * <p>Another example of adding a triple in the {@linkplain #defaultNamedGraph() default} graph:
     * <pre>{@code
     *Model model = ...;
     *String graphURI = ...;
     *BlankNode subject = ...;
     *Uri predicateURI =...;
     *String literal = ...;
     *Triple triple = model.add().g(graphURI).s(subject).p(predicateURI).o(literal);
     * }</pre>
     *
     * @return an object to be used to add a triple in a step-by-step fashion
     */
    AddContext add();

    /**
     * Deletes a single triple from this model.
     *
     * <p>Note: It should be more efficient to delete a bunch of triples at once with {@linkplain #delete(Iterable)}
     * rather than one by one with this method.
     *
     * <p><strong>Important:</strong> After triple deletions,
     * the typing of the nodes is guarranteed to remain valid and consistent,
     * but it is no longer guarranteed to be <em>minimal</em>. Strongly consider
     * invoking {@link #retypeNodes()} after triple deletions (see the method specification
     * for further details).
     *
     * @param triple the triple to delete (must be non-null)
     * @return whether the triple was actually deleted. If the triple did not exist in this model {@code false} is returned
     */
    boolean delete(Triple triple);

    /**
     * Deletes triples from this model.
     *
     * <p><strong>Important:</strong> After triple deletions,
     * the typing of the nodes is guarranteed to remain valid and consistent,
     * but it is no longer guarranteed to be <em>minimal</em>. Strongly consider
     * invoking {@link #retypeNodes()} after triple deletions (see the method specification
     * for further details).
     *
     * @param triples the triples to delete (both the Iterable and the triples themselves must be non-null)
     * @return whether any triple was actually deleted
     */
    boolean delete(Iterable<Triple> triples);

    /**
     * Recalculates the types of every {@linkplain RdfNode}, by reconsidering
     * all triples of this model.
     *
     * <p>This may only be useful after triple deletions. After triple deletions,
     * the typing of the nodes is guarranteed to remain valid and consistent,
     * but it is no longer guarranteed to be <em>minimal</em>. For example
     * if, after some deletions, this triple remains in the model:
     * {@code <A rdfs:subClassOf B>}, with no other triples containing {@code A}
     * or {@code B}, they might have the type of {@linkplain RdfType#METACLASS},
     * which is an acceptable interpretation, though if the triple was to be added
     * for the first time, the types of {@code A, B} would be {@linkplain RdfType#CLASS}
     * (a type that allows further transition to {@code METACLASS} in some cases, while
     * the opposite is not true).
     *
     * <p>Note that having a non-minimal typing may cause erroneous validation
     * errors, if more triples are added to the model. Continuing the above example,
     * the triple {@code <A rdfs:subClassOf rdfs:Resource>} would cause an error
     * if {@code A} was left as a {@code METACLASS} (which will be the case, if {@code A}
     * was indeed a {@code METACLASS}, and merely some triples that inferred this
     * type were deleted), since this would only be allowed if {@code A} was interpreted
     * as a {@code CLASS}.
     *
     * <p>In summary, after triple deletions, if the minimal typing is required
     * (which is what is produced when triples are only added to a model),
     * invoke this method. If more triples are going to be added, consider whether
     * they might cause erroneous typing errors, or take the safe route and invoke this method.
     */
    void retypeNodes();

    /**
     * Maps a literal to a unique node of this model, creating such a node if necessary.
     *
     * @param literal the literal to uniquely map to a node
     * @return the node that the specified literal uniquely (in this model) maps to
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    LiteralNode mapLiteral(Literal literal);

    /**
     * Maps a literal to a unique node of this model, creating such a node if necessary.
     *
     * @param literal the literal to uniquely map to an existing node
     * @return the node that the specified literal uniquely (in this model) maps to
     * @throws LiteralFormatException if {@linkplain Literal#parse(String)} cannot parse the literal
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    LiteralNode mapLiteral(String literal) throws LiteralFormatException;

    /**
     * Maps a resource to a unique node of this model, creating such a node if necessary.
     *
     * @param uri the URI to uniquely map to a node
     * @return the node that the specified resource uniquely (in this model) maps to
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    Resource mapResource(Resource resource);

    /**
     * Maps a URI to a unique node of this model, creating such a node if necessary.
     *
     * @param uri the URI to uniquely map to a node
     * @return the node that the specified URI uniquely (in this model) maps to
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    Resource mapResource(Uri uri);

    /**
     * Maps a URI (as a string) to a unique node of this model, creating such a node if necessary.
     *
     * <p>Equivalent to {@code mapResource(Uri.parse(uri))}.
     *
     * @param uri the URI (as a string) to uniquely map to a node
     * @return the node that the specified URI uniquely (in this model) maps to
     * @throws UriFormatException if uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    Resource mapResource(String uri) throws UriFormatException;

    /**
     * Maps a blank node identifier to a unique node of this model, creating such a node if necessary.
     *
     * @param id the blank node identifier to uniquely map to a node
     * @return the node that the specified blank node identifier uniquely (in this model) maps to
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    BlankNode mapBlankNode(String id);

    /**
     * Maps a URI or blank node identifier to a unique node of this model, creating such a node if necessary.

     * <p>To determine whether a Uri or a BlankNode will be returned, the following steps
     * will take place:
     * <ul>
     * <li>If {@code uriOrId} cannot be parsed to a Uri (with {@linkplain Uri#tryParse(String)},
     * then the only option is to treat it as a blank node identifier.
     * <li>If there is already a {@code Resource} in this model with the same URI, then it
     * is returned.
     * <li>If there is already a {@code BlankNode} in this model with the same blank node identifier,
     * then it is returned.
     * <li>If still nothing has been returned, delegate to {@linkplain #mapResource(String)}.
     * </ul>
     *
     * @param uriOrId the URI or the blank node identifier to uniquely map to a node
     * @return the node that the specified URI or blank node identifier (according to the above rules) uniquely (in this model) maps to
     * @throws  UriFormatException if no existing {@code Resource} or {@code BlankNode}
     * was found with the same URI or blank node identifier, respectively, and uriOrId
     * cannot be parsed to a Uri (via {@linkplain Uri#parse(String)}
     * @see  <a href="../../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    ObjectNode mapObjectNode(String uriOrId) throws UriFormatException;

    /**
     * Maps a URI or blank node identifier or a literal to a unique node of this model, creating such a node if necessary.
     *
     * <p>To determine whether a Uri or a BlankNode will be returned, the following steps
     * will take place:
     * <ul>
     * <li>If {@code uriOrIdOrLiteral} can be parsed to a Literal (via {@linkplain Literal#tryParse(String)}),
     * delegate to {@linkplain  #mapLiteral(String)}.
     * <li>Otherwise, delegate to {@linkplain #mapObjectNode(String)}
     * </ul>
     *
     * @param uriOrIdOrLiteral the URI or the blank node identifier or literal to uniquely map to a node
     * @return the node that the specified argument (according to the above rules) uniquely (in this model) maps to
     * @throws  UriFormatException if uriOrIdOrLiteral cannot be parsed to a Literal (via {@linkplain Literal#tryParse(String)})
     * and {@linkplain #mapObjectNode(String)} throws this exception
     * @see  <a href="../../../../gr/forth/ics/swkm/model2/Model.html#valuesAndNodes">Distinction between values and nodes</a>
     */
    RdfNode map(String uriOrIdOrLiteral) throws UriFormatException;

    /**
     * Builds a triple query in a step-by-step fashion. The procedure of adding a triple is:
     * <ul>
     * <li>(Optionally) Define a specific named graph.
     * <li>(Optionally) Define a specific subject.
     * <li>(Optionally) Define a specific predicate.
     * <li>(Optionally) Define a specific object.
     * </ul>
     * Each parameter specification allows only triples with the same value at that position. For example,
     * creating a query with subject {@code "A"}, only selects triples that do have a subject {@code "A"}.
     * If multiple options are specified, only triples matching all options are selected. For example, creating
     * a query with subject {@code "A"} and object {@code "B"} will select triples like {@code <A p1 B>, <A p2 B>}
     * but will not select triples like {@code <A p B1>, <A1 p B>} etc.
     *
     * <p>Specifying no option at all designates the query that selects all existing triples.
     *
     * <p>A triple is allowed to be reported more than once in the query results, if it belongs to more than one
     * named graph, and the query does not request a specific named graph.
     *
     * <p>Example of selecting all {@code rdfs:subClassOf} triples:
     * <pre>{@code
     *Model model = ...;
     *Triples triples = model.triples()
     *      .p(RdfSchema.SUBCLASSOF)
     *      .fetch();
     * }</pre>
     *
     * <p>Example of selecting all triples with a specific subject, belonging to a specific graph:
     * <pre>{@code
     *Model model = ...;
     *String graph = ...; //could be a Uri or a Resource too
     *String subject = ...; //could be a Uri or a Resource or a BlankNode too
     *Triples triples = model.triples()
     *      .g(graph)
     *      .s(subject)
     *      .fetch();
     * }</pre>
     *
     * <p>Returned Triples can be iterated simply:
     * <pre>{@code
     *for (Triple triple : triples) {
     *   //...
     *}
     *}</pre>
     *
     * <p>The returned Triples object can also be used to subselect just a specific component
     * of the triples, for example it is easy to select just the subject nodes, like this:
     * <pre>{@code
     *for (ObjectNode subject : triples.subjects()) {
     *   //...
     *}
     *}</pre>
     * See {@link Triples} for more details.
     *
     * <p>A triple query, apart from reporting triples, can also be used to <em>delete</em>
     * selected triples. Just substitute the final {@linkplain QueryBuilder#fetch() fetch()} call
     * with a {@linkplain QueryBuilder#delete() delete()} call. For example, this deletes all
     * the triples of a specific named graph:
     * <pre>{@code
     *Model model = ...;
     *Resource graph = ...;
     *Triples triples = model.triples()
     *      .g(graph)
     *      .delete();
     * }</pre>
     *
     * @return an object to be used to create a triple query in a step-by-step fashion, in
     * order to select and report or delete triples
     */
    QueryBuilder triples();


    /**
     * Returns all nodes of this model that their {@link RdfNode#type() type} is equal
     * to one of the specified types.
     *
     * @param firstType one of the types of the returned nodes
     * @param restTypes the rest of the types of the returned nodes
     * @return all nodes of this model with type included in the specified types
     */
    Iterable<RdfNode> findNodes(RdfType firstType, RdfType... restTypes);

    /**
     * Returns all resources of this model that their {@link RdfNode#type() type} is equal
     * to one of the specified types and the namespace part of their URI matches the specified
     * namespace.
     *
     * @param namespace the namespace part of the URI of the returned resources
     * @param firstType one of the types of the returned nodes. They must be schema,
     * i.e. one of the following: {@code [CLASS, PROPERTY, METACLASS, METAPROPERTY]}
     * @param restTypes the rest of the types of the returned nodes, which should also
     * be schema.
     * @return all resources of this model with type included in the specified types
     * and have the specified namespace part
     * @throws IllegalArgumentException if the specified namespace Uri has a non-empty
     * local part
     */
    Iterable<Resource> findSchemaNodes(Uri namespace, RdfType firstType, RdfType... restTypes);

    /**
     * Returns all resources of this model.
     *
     * @return all resources of this model
     */
    Iterable<Resource> resources();

    /**
     * Returns all literals of this model.
     *
     * @return all literals of this model
     */
    Iterable<LiteralNode> literals();

    /**
     * Returns all blank nodes of this model.
     *
     * @return all blank nodes of this model
     */
    Iterable<BlankNode> blankNodes();

    /**
     * Returns the named graphs of this model.
     *
     * <p>Equivalent to calling {@code nodesOfType(RdfType.NAMED_GRAPH)}
     *
     * @return the named graphs of this model
     */
    Iterable<Resource> namedGraphs();

    /**
     * Returns all namespaces of this model. A namespace is defined if there exists a {@link RdfType#isSchema() schema
     * resource} in a triple of this model, whereas {@link Uri#getNamespaceUri() its namespace part} is the namespace.
     * In other words, if there is schema resource {@code res}, then {@code res.getUri().getNamespaceUri()} will
     * be returned from this method.
     *
     * @return all namespaces of this model
     */
    Set<Uri> namespaces();

    /**
     * Returns the default named graph of this model. Triples that are added to a {@code null}
     * named graph (i.e. using null as a named graph in {@linkplain #add(Resource, ObjectNode, Resource, RdfNode) } method)
     * are added to this named graph.
     * @return the default named graph of this model
     */
    Resource defaultNamedGraph();

    /**
     * Returns whether the specified triple can be inferred by the present model. To invoke
     * this operation, subject, predicate and object must have been specified, but no
     * graph component.
     *
     * <p>A triple {@code <S P O>} is considered inferable if any of the following conditions hold:
     * <ul>
     * <li>The triple {@code <S P O>} exists verbatim in the model
     * <li>If {@code P} is {@code rdfs:subClassOf} or {@code rdfs:subPropertyOf}, and
     * {@code S} is a (direct or indirect) descendant of {@code O}
     * <li>If {@code P} is {@code rdf:type} and there is a triple {@code <S rdf:type X>} where
     * {@code X} is a (direct or indirect) descendant of {@code O}
     * <li>If there is a triple {@code <S P2 O>} where {@code P2} is a subproperty of {@code P}
     * </ul>
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @return whether the triple can be inferred by the present model
     */
    boolean isInferable(ObjectNode subject, Resource predicate, RdfNode object);

    /**
     * Builder of a triple query, which can be used to either select and report triples or delete them.
     */
    interface QueryBuilder {
        /**
         * Specifies that only triples in the specified named graph are to be selected.
         *
         * <p>Only one {@code g(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code g(model.mapResource(namedGraph))}
         *
         * @param namedGraph only triples contained in this are selected
         * @return this
         * @throws UriFormatException if {@linkplain Model#mapResource(String)} throws this exception
         */
        QueryBuilder g(String namedGraph);

        /**
         * Specifies that only triples in the specified named graph are to be selected.
         *
         * <p>Only one {@code g(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code g(model.mapResource(namedGraph))}
         *
         * @param namedGraph only triples contained in this are selected
         * @return this
         */
        QueryBuilder g(Uri namedGraph);

        /**
         * Specifies that only triples in the specified named graph are to be selected.
         *
         * <p>Only one {@code g(...)} overloaded method can be called in each query.
         *
         * @param namedGraph only triples contained in this are selected
         * @return this
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * which this query builder targets
         */
        QueryBuilder g(Resource namedGraph);

        /**
         * Specifies that only triples having the specified subject are to be selected.
         *
         * <p>Only one {@code s(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code s(model.mapObjectNode(uriOrId))}
         *
         * @param uriOrId only triples having this subject are selected
         * @return this
         * @throws UriFormatException if {@linkplain Model#mapObjectNode(String)} throws this exception
         */
        QueryBuilder s(String uriOrId);

        /**
         * Specifies that only triples having the specified subject are to be selected.
         *
         * <p>Only one {@code s(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code s(model.mapResource(uriOrId))}
         *
         * @param uri only triples having this subject are selected
         * @return this
         */
        QueryBuilder s(Uri uri);

        /**
         * Specifies that only triples having the specified subject are to be selected.
         *
         * <p>Only one {@code s(...)} overloaded method can be called in each query.
         *
         * @param node only triples having this subject are selected
         * @return this
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * which this query builder targets
         */
        QueryBuilder s(ObjectNode node);

        /**
         * Specifies that only triples having the specified predicate are to be selected.
         *
         * <p>Only one {@code p(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code p(model.mapResource(uri))}
         *
         * @param uri only triples having this predicate are selected
         * @return this
         * @throws UriFormatException if {@linkplain Model#mapResource(String)} throws this exception
         */
        QueryBuilder p(String uri);

        /**
         * Specifies that only triples having the specified predicate are to be selected.
         *
         * <p>Only one {@code p(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code p(model.mapResource(uri))}
         *
         * @param uri only triples having this predicate are selected
         * @return this
         */
        QueryBuilder p(Uri uri);

        /**
         * Specifies that only triples having the specified predicate are to be selected.
         *
         * <p>Only one {@code p(...)} overloaded method can be called in each query.
         *
         * @param node only triples having this predicate are selected
         * @return this
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * which this query builder targets
         */
        QueryBuilder p(Resource node);

        /**
         * Specifies that only triples having the specified object are to be selected.
         *
         * <p>Only one {@code o(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code p(model.map(uriOrIdOrLiteral))}
         *
         * @param uriOrIdOrLiteral only triples having this object are selected
         * @return this
         * @throws UriFormatException if {@linkplain Model#map(String)} throws this exception
         */
        QueryBuilder o(String uriOrIdOrLiteral);

        /**
         * Specifies that only triples having the specified object are to be selected.
         *
         * <p>Only one {@code o(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code p(model.mapResource(uri))}
         *
         * @param uri only triples having this object are selected
         * @return this
         */
        QueryBuilder o(Uri uri);

        /**
         * Specifies that only triples having the specified object are to be selected.
         *
         * <p>Only one {@code o(...)} overloaded method can be called in each query.
         *
         * <p>This method is equivalent to {@code p(model.mapLiteral(literal))}
         *
         * @param literal only triples having this object are selected
         * @return this
         */
        QueryBuilder o(Literal literal);

        /**
         * Specifies that only triples having the specified object are to be selected.
         *
         * <p>Only one {@code o(...)} overloaded method can be called in each query.
         *
         * @param node only triples having this object are selected
         * @return this
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * which this query builder targets
         */
        QueryBuilder o(RdfNode node);

        /**
         * Returns the triples that match all (if any) the specified criteria (on named graph,
         * subject, predicate, object). Triple objects are
         * allowed to be reported multiple times, if they are contained in multiple named
         * graphs and no specific namedGraph is required by the query, but is not guarranteed.
         *
         * <p><strong>Note:</strong> The returned iterable creates iterators that do <em>not</em>
         * support {@linkplain Iterator#remove()} method. See <a href="Model.html#deleting">other ways</a>
         * to delete triples from a model.
         *
         * @return Triples the triples that match all (if any) the specified criteria
         */
        Triples fetch();

        /**
         * Deletes the triples that match all (if any) the specified criteria (on named graph,
         * subject, predicate, object).
         *
         * <p>If a specific named graph is required, then selected triples are not completely
         * deleted from the owner model, but they are simply removed from that specific named
         * graph.
         *
         * <p><strong>Important:</strong> After triple deletions,
         * the typing of the nodes is guarranteed to remain valid and consistent,
         * but it is no longer guarranteed to be <em>minimal</em>. Strongly consider
         * invoking {@link #retypeNodes()} after triple deletions (see the method specification
         * for further details).
         *
         * @return true if any triple was actually deleted
         */
        boolean delete();
    }

    /**
     * The initial builder of a triple addition, which allows the user to define either the named graph
     * or the subject (implying the use of the {@linkplain Model#defaultNamedGraph()
     * default named graph}) of the to-be-added triple.
     *
     * @see Model#add() for examples
     */
    interface AddContext extends AddSubject {
        /**
         * Declares that the to-be-added triple should be in the specified named graph.
         *
         * @param namedGraph the named graph which will include the to-be-added triple
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * into which the triple is to be added. See {@linkplain RdfNode#mappedTo(Model)} for a way
         * to translate a node of a model to a node of another
         */
        AddSubject g(Resource namedGraph);

        /**
         * Declares that the to-be-added triple should be in the specified named graph.
         *
         * <p>This method is equivalent to {@code g(model.mapResource(uri))}
         *
         * @param uri the named graph which will include the to-be-added triple
         * @return an object to handle the rest of the procedure of the triple addition
         * @throws UriFormatException if {@linkplain Model#mapResource(String)} throws this exception
         */
        AddSubject g(String uri);

        /**
         * Declares that the to-be-added triple should be in the specified named graph.
         *
         * <p>This method is equivalent to {@code g(model.mapResource(uri))}
         *
         * @param uri the named graph which will include the to-be-added triple
         * @return an object to handle the rest of the procedure of the triple addition
         */
        AddSubject g(Uri uri);
    }

    /**
     * A builder of a triple addition, which allows the user to define
     * the subject of the to-be-added triple.
     *
     * @see Model#add() for examples
     */
    interface AddSubject {
        /**
         * Declares that the to-be-added triple should have the specified subject.
         *
         * @param subject the subject that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * into which the triple is to be added. See {@linkplain RdfNode#mappedTo(Model)} for a way
         * to translate a node of a model to a node of another
         */
        AddPredicate s(ObjectNode subject);

        /**
         * Declares that the to-be-added triple should have the specified subject.
         *
         * <p>This method is equivalent to {@code s(model.mapResource(uri))}
         *
         * @param uri the subject that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         */
        AddPredicate s(Uri uri);

        /**
         * Declares that the to-be-added triple should have the specified subject.
         *
         * <p>This method is equivalent to {@code s(model.mapResource(uriOrId))}
         *
         * @param uriOrId the subject that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         * @throws UriFormatException if {@linkplain Model#mapObjectNode(String)} throws this exception
         */
        AddPredicate s(String uriOrId);

        /**
         * Creates a new class resource with the specified URI.
         *
         * @param classUri the URI of the class to create
         * @return a new class resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newClass(String classUri) throws UriFormatException;

        /**
         * Creates a new class resource with the specified URI.
         *
         * @param classUri the URI of the class to create
         * @return a new class resource with the specified URI
         */
        Resource newClass(Uri classUri);

        /**
         * Creates a new property resource with the specified URI.
         *
         * @param propertyUri the URI of the property to create
         * @return a new property resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newProperty(String propertyUri) throws UriFormatException;

        /**
         * Creates a new property resource with the specified URI.
         *
         * @param propertyUri the URI of the property to create
         * @return a new property resource with the specified URI
         */
        Resource newProperty(Uri propertyUri);

        /**
         * Creates a new metaclass resource with the specified URI.
         *
         * @param metaclassUri the URI of the metaclass to create
         * @return a new metaclass resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newMetaclass(String metaclassUri) throws UriFormatException;

        /**
         * Creates a new metaclass resource with the specified URI.
         *
         * @param metaclassUri the URI of the metaclass to create
         * @return a new metaclass resource with the specified URI
         */
        Resource newMetaclass(Uri metaclassUri);

        /**
         * Creates a new metaproperty resource with the specified URI.
         *
         * @param metapropertyUri the URI of the metaproperty to create
         * @return a new metaproperty resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newMetaproperty(String metapropertyUri) throws UriFormatException;

        /**
         * Creates a new metaproperty resource with the specified URI.
         *
         * @param metapropertyUri the URI of the metaproperty to create
         * @return a new metaproperty resource with the specified URI
         */
        Resource newMetaproperty(Uri metapropertyUri);

        /**
         * Creates a new individual resource with the specified URI.
         *
         * @param individualUri the URI of the individual to create
         * @return a new individual resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newIndividual(String individualUri) throws UriFormatException;

        /**
         * Creates a new individual resource with the specified URI.
         *
         * @param individualUri the URI of the individual to create
         * @return a new individual resource with the specified URI
         */
        Resource newIndividual(Uri individualUri);

        /**
         * Creates a new Alt resource with the specified URI.
         *
         * @param altUri the URI of the Alt to create
         * @return a new Alt resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newAlt(String altUri) throws UriFormatException;

        /**
         * Creates a new Alt resource with the specified URI.
         *
         * @param altUri the URI of the Alt to create
         * @return a new Alt resource with the specified URI
         */
        Resource newAlt(Uri altUri);

        /**
         * Creates a new Bag resource with the specified URI.
         *
         * @param bagUri the URI of the Bag to create
         * @return a new Bag resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newBag(String bagUri) throws UriFormatException;

        /**
         * Creates a new Bag resource with the specified URI.
         *
         * @param bagUri the URI of the Bag to create
         * @return a new Bag resource with the specified URI
         */
        Resource newBag(Uri bagUri);

        /**
         * Creates a new Seq resource with the specified URI.
         *
         * @param seqUri the URI of the Seq to create
         * @return a new Seq resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newSeq(String seqUri) throws UriFormatException;

        /**
         * Creates a new Seq resource with the specified URI.
         *
         * @param seqUri the URI of the Seq to create
         * @return a new Seq resource with the specified URI
         */
        Resource newSeq(Uri seqUri);

        /**
         * Creates a new named graph resource with the specified URI.
         *
         * @param namedGraphUri the URI of the named graph to create
         * @return a new named graph resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newNamedGraph(String namedGraphUri) throws UriFormatException;

        /**
         * Creates a new named graph resource with the specified URI.
         *
         * @param namedGraphUri the URI of the named graph to create
         * @return a new named graph resource with the specified URI
         */
        Resource newNamedGraph(Uri namedGraphUri);

        /**
         * Creates a new statement (rdf:Statement) resource with the specified URI.
         *
         * @param statementUri the URI of the statement to create
         * @return a new statement resource with the specified URI
         * @throws UriFormatException if the specified uri cannot be parsed to a Uri (via {@code Uri.parse(uri)})
         */
        Resource newStatement(String statementUri) throws UriFormatException;

        /**
         * Creates a new statement (rdf:Statement) resource with the specified URI.
         *
         * @param statementUri the URI of the statement to create
         * @return a new statement resource with the specified URI
         */
        Resource newStatement(Uri statementUri);
    }

    /**
     * A builder of a triple addition, which allows the user to define
     * the predicate of the to-be-added triple.
     *
     * @see Model#add() for examples
     */
    interface AddPredicate {
        /**
         * Declares that the to-be-added triple should have the specified predicate.
         *
         * @param predicate the predicate that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * into which the triple is to be added. See {@linkplain RdfNode#mappedTo(Model)} for a way
         * to translate a node of a model to a node of another
         */
        AddObject p(Resource predicate);

        /**
         * Declares that the to-be-added triple should have the specified predicate.
         *
         * <p>This method is equivalent to {@code p(model.mapResource(uri))}
         *
         * @param uri the predicate that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         */
        AddObject p(Uri uri);

        /**
         * Declares that the to-be-added triple should have the specified predicate.
         *
         * <p>This method is equivalent to {@code p(model.mapResource(uri))}
         *
         * @param uri the predicate that the to-be-added triple will have
         * @return an object to handle the rest of the procedure of the triple addition
         * @see Model#add() for examples
         * @throws UriFormatException if {@linkplain Model#mapResource(String)} throws this exception
         */
        AddObject p(String uri);
    }

    /**
     * A builder of a triple addition, which allows the user to define
     * the object of the to-be-added triple, and adds the triple.
     *
     * @see Model#add() for examples
     */
    interface AddObject {
        /**
         * Declares that the to-be-added triple should have the specified object,
         * and adds the triple.
         *
         * @param object the object  that the to-be-added triple will have
         * @return the added triple
         * @throws IllegalArgumentException if the specified node does not belong to the model
         * into which the triple is to be added. See {@linkplain RdfNode#mappedTo(Model)} for a way
         * to translate a node of a model to a node of another
         * @throws ValidationException if the added triple creates a node typing error
         * @see Model#add() for examples
         */
        Triple o(RdfNode object);

        /**
         * Declares that the to-be-added triple should have the specified object,
         * and adds the triple.
         *
         * <p>This method is equivalent to {@code o(model.mapResource(uri))}
         *
         * @param uri the object that the to-be-added triple will have
         * @return the added triple
         * @throws ValidationException if the added triple creates a node typing error
         * @see Model#add() for examples
         */
        Triple o(Uri uri);

        /**
         * Declares that the to-be-added triple should have the specified object,
         * and adds the triple.
         *
         * <p>This method is equivalent to {@code o(model.mapLiteral(literal))}
         *
         * @param literal the object that the to-be-added triple will have
         * @return the added triple
         * @throws ValidationException if the added triple creates a node typing error
         * @see Model#add() for examples
         */
        Triple o(Literal literal);

        /**
         * Declares that the to-be-added triple should have the specified object,
         * and adds the triple.
         *
         * <p>This method is equivalent to {@code o(model.map(uriOrIdOrLiteral))}
         *
         * @param uriOrIdOrLiteral the object that the to-be-added triple will have
         * @return the added triple
         * @see Model#add() for examples
         * @throws ValidationException if the added triple creates a node typing error
         * @throws UriFormatException if {@linkplain Model#map(String)} throws this exception
         */
        Triple o(String uriOrIdOrLiteral);
    }

    /**
     * Sets the label manager to be used by this model. The label manager provides optimizations
     * for hierarchy ({@code is-a}) queries based on labeling schemes.
     *
     * <p>The provided label manager must be preconfigured to have as a target this model,
     * i.e. this condition must hold: {@code labelManager.getTargetModel() == this}
     *
     * @param labelManager the labelManager to be used by this model
     * @throws IllegalArgumentException if {@code labelManager.getTargetModel() != this}
     */
    void setLabelManager(LabelManager labelManager);

    /**
     * Provides a hint to the underlying {@linkplain LabelManager} that it needs to make
     * sure that the labeling of the various hierarchies is updated and ready to be used, so
     * to optimize {@linkplain Inheritable#isAncestorOf(RdfNode)}/{@linkplain Inheritable#isDescendantOf(RdfNode)}
     * queries.
     *
     * @see #setLabelManager(LabelManager)
     */
    void updateLabels();

    /**
     * Provides a hint to the underlying {@linkplain LabelManager} that it needs to make
     * sure that the labeling of the various hierarchies is updated and ready to be used, as
     * {@link #updateLabels()} does, while it also respects the provided predefined labels.
     *
     * @see #setLabelManager(LabelManager)
     */
    void updateLabels(PredefinedLabels predefinedLabels);

    /**
     * Adds a RdfNodeListener to this model, which will be notified on subsequent node events
     * of any node of this model.
     *
     * @param rdfNodeListener the listener to add to this model
     */
    void addRdfNodeListener(RdfNodeListener rdfNodeListener);

    /**
     * Removes a RdfNodeListener from this model, meaning it will not be notified on
     * subsequent node events of any node of this model.
     *
     * <p>If {@code rdfNodeListener} is {@code null}, nothing happens (i.e. no NullPointerException
     * is thrown).
     *
     * @param rdfNodeListener the listener to remove from this model
     */
    void removeRdfNodeListener(RdfNodeListener rdfNodeListener);

    /**
     * Adds a TripleListener to this model, which will be notified on subsequent triple events
     * of this model.
     *
     * @param tripleListener the listener to add to this model
     */
    void addTripleListener(TripleListener tripleListener);

    /**
     * Removes a TripleListener from this model, meaning it will not be notified on
     * subsequent triple events of this model.
     *
     * <p>If {@code tripleListener} is {@code null}, nothing happens (i.e. no NullPointerException
     * is thrown).
     *
     * @param tripleListener the listener to remove from this model
     */
    void removeTripleListener(TripleListener tripleListener);

    /**
     * Returns the number of {@link Triple} instances contained in the model. Even if a triple is contained
     * in multiple named graphs, it is only counted once.
     *
     * @return the number of {@code Triple} instances contained in the indexer
     */
    int tripleCount();
}
