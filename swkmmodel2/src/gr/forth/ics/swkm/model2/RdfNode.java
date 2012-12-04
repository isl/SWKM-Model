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

import gr.forth.ics.swkm.model2.index.Index;
import gr.forth.ics.swkm.model2.views.ClassView;
import gr.forth.ics.swkm.model2.views.ContainerView;
import gr.forth.ics.swkm.model2.views.IndividualView;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.views.MetaclassView;
import gr.forth.ics.swkm.model2.views.MetapropertyView;
import gr.forth.ics.swkm.model2.views.PropertyView;

/**
 * A representation of an RDF entity (a {@link Resource}, a {@link BlankNode}, or a {@link LiteralNode}). 
 * An RdfNode is created by {@linkplain Model} and can at most be owned by a single Model.
 * 
 * <a name="nodeTyping"><!-- Linked by Model --></a>
 * <p>Nodes have an associated {@linkplain RdfType type} (see {@linkplain RdfNode#type()}.
 * This is automatically inferred by the owner model,
 * using RDF inference rules. See <a href="../../../../../docs/ClassPropertyResource.pdf">
 * "Discussion on the semantics of rdfs:Resource, rdfs:Class, rdf:Property"</a> for
 * a reference of these rules. The type of a node may change during its lifetime, due
 * to triple additions/deletions. It should be noted that, for uniformity, literal nodes also have a type,
 * which is always {@linkplain RdfType#LITERAL}. Notice that typing information of a model can only
 * produce contradictions when <em>adding</em> triples. In such a case, an {@link RdfTypeException}
 * will be thrown, and any changed types will be reverted to the last valid values.
 * 
 * <p>An RdfNode can offer more methods based on its type. For example, if
 * {@code node.type().isClass()}, then the node can be <em>viewed</em> as a
 * <em>class</em>. In particular, {@code ClassView classView = node.asClass();}
 * would access the {@linkplain ClassView} interface that offers specialized methods for
 * nodes that are classes. Similar interfaces exist for other types (properties,
 * metaclasses, metaproperties, individuals, named graphs), see the {@code asXXX}
 * methods and the
 * <a href="gr/forth/ics/swkm/model2/views/package-summary.html">gr.forth.ics.swkm.model2.views</a>
 * package.
 * 
 * @see <a href="../../../../../docs/ClassPropertyResource.pdf">"Discussion on the semantics of rdfs:Resource, rdfs:Class, rdf:Property" for a reference of supported RDF typing rules.</a>
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface RdfNode extends Ownable {
    /**
     * Returns whether this node has any triples. (If not, then it is not considered part
     * of the {@link Model model} that created it).
     *
     * @return whether this node has any triples
     */
    boolean hasTriples();

    /**
     * Returns true if this node is a {@linkplain Resource}.
     * 
     * <p>Equivalent, but preferable, to {@code this instanceof Resource}.
     * 
     * @return true if this node is a {@linkplain Resource}
     */
    boolean isResource();
    
    /**
     * Returns true if this node is a {@linkplain LiteralNode}.
     * 
     * <p>Equivalent, but preferable, to {@code this instanceof LiteralNode}.
     * 
     * @return true if this node is a {@linkplain LiteralNode}
     */
    boolean isLiteral();
    
    /**
     * Returns true if this node is a {@linkplain BlankNode}.
     * 
     * <p>Equivalent, but preferable, to {@code this instanceof BlankNode}.
     * 
     * @return true if this node is a {@linkplain BlankNode}
     */
    boolean isBlankNode();
    
    /**
     * Returns true if this node is a {@linkplain ObjectNode}.
     * 
     * <p>Equivalent, but preferable, to {@code this instanceof ObjectNode}.
     * 
     * @return true if this node is a {@linkplain ObjectNode}
     */
    boolean isObjectNode();
    
    /**
     * Returns true if and only if {@code isResource() && ((Resource)this).getUri().equals(uri)}.
     * 
     * @param uri the URI to test whether it is associated with this node
     * @return true if and only if {@code isResource() && ((Resource)this).getUri().equals(uri)}
     */
    boolean is(Uri uri);
    
    /**
     * Returns the current RDF type of this node. Type might change in the
     * future if triples are added to or deleted from the {@linkplain #owner() owning} Model.
     * 
     * @return the current type of this node
     */
    RdfType type();
    
    /**
     * <strong>Users should not call this method; it is only needed for custom
     * {@linkplain gr.forth.ics.swkm.model2.index.ModelIndexer indexing} implementations</strong>
     * 
     * @return this node's index
     */
    Index getIndex();
    
    /**
     * Returns the triples that have this node as subject, in the model that owns this node.
     * If this node is not an ObjectNode (or equivalently {@code isObjectNode()} returns false)
     * then this method is guaranteed to return no results, since only ObjectNodes can ever
     * be the subject of a triple.
     * 
     * @return the triples that have this node as subject, in the model that owns this node
     */
    Triples subjectTriples();
    
    /**
     * Returns the triples that have this node as predicate, in the model that owns this node.
     * If this node is not a Resource (or equivalently {@code isResource()} returns false)
     * then this method is guaranteed to return no results, since only Resources can ever
     * be the predicate of a triple.
     * 
     * @return the triples that have this node as predicate, in the model that owns this node
     */
    Triples predicateTriples();
    
    /**
     * Returns the triples that have this node as object, in the model that owns this node.
     * 
     * @return the triples that have this node as object, in the model that owns this node
     */
    Triples objectTriples();

    /**
     * Maps this node to another model. The returned node will have the same
     * type ({@code Resource} or {@code BlankNode} or {@code LiteralNode}) and the same identifier as this one,
     * but not necessarily the same {@linkplain #type() RdfType}, since the latter
     * depends on the triples of a particular model.
     * 
     * @param model the model into which to map this node
     * @return the mapped node
     */
    RdfNode mappedTo(Model model);
    
    /**
     * Returns a class view of this node. The returned view is only usable as long
     * as this node remains a class, or else its methods will throw an {@linkplain IllegalStateException}.
     * 
     * @return a class view of this node
     * @throws IllegalStateException if {@code type() != RdfType.CLASS}
     */
    ClassView asClass() throws IllegalStateException;
    
    /**
     * Returns a property view of this node. The returned view is only usable as long
     * as this node remains a property, or else its methods will throw an {@linkplain IllegalStateException}.
     * 
     * @return a property view of this node
     * @throws IllegalStateException if {@code type() != RdfType.PROPERTY}
     */
    PropertyView asProperty() throws IllegalStateException;
    
    /**
     * Returns a metaclass view of this node. The returned view is only usable as long
     * as this node remains a metaclass, or else its methods will throw an {@linkplain IllegalStateException}.
     * 
     * @return a metaclass view of this node
     * @throws IllegalStateException if {@code type() != RdfType.METACLASS}
     */
    MetaclassView asMetaclass() throws IllegalStateException;
    
    /**
     * Returns a metaproperty view of this node. The returned view is only usable as long
     * as this node remains a metaproperty, or else its methods will throw an {@linkplain IllegalStateException}.
     * 
     * @return a metaproperty view of this node
     * @throws IllegalStateException if {@code type() != RdfType.METAPROPERTY}
     */
    MetapropertyView asMetaproperty() throws IllegalStateException;
    
    /**
     * Returns a individual view of this node. The returned view is only usable as long
     * as this node remains a individual, or else its methods will throw an {@linkplain IllegalStateException}.
     * 
     * @return a individual view of this node
     * @throws IllegalStateException if {@code type() != RdfType.INDIVIDUAL}
     */
    IndividualView asIndividual() throws IllegalStateException;
    
    /**
     * Returns an inheritable (that is "something that can be part of an inheritance relationship")
     * view of this node. The returned view is only usable as long
     * as this node remains an inheritable, or else its methods will throw an {@linkplain IllegalStateException}.
     * For convenience, this call succeeds even for {@link RdfType#XML_TYPE XML types}, although these
     * do not participate in any inheritance relationship.
     * 
     * @return a named graph view of this node
     * @throws IllegalStateException if {@code type() != RdfType.CLASS &&
     * type() != RdfType.PROPERTY && type() != RdfType.METACLASS &&
     * type() != RdfType.METAPROPERTY && type() != RdfType.XML_TYPE}
     */
    Inheritable asInheritable() throws IllegalStateException;

    /**
     * Returns a container view of this node. The returned view is only usable as long
     * as this node remains a container, or else its methods will throw an {@linkplain IllegalStateException}.
     *
     * @return a container view of this node
     * @throws IllegalStateException if {@code type() != RdfType.BAG && type() != RdfType.SEQ && type() != RdfType.SEQ}
     */
    ContainerView asContainer() throws IllegalStateException;
}
