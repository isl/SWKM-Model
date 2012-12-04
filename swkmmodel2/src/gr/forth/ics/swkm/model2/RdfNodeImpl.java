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

import com.google.common.base.Joiner;
import gr.forth.ics.swkm.model2.TypeInference.InternalType;
import gr.forth.ics.swkm.model2.event.TypeChange;
import gr.forth.ics.swkm.model2.index.Index;
import gr.forth.ics.swkm.model2.labels.LabelManager;
import gr.forth.ics.swkm.model2.views.ClassView;
import gr.forth.ics.swkm.model2.views.ContainerView;
import gr.forth.ics.swkm.model2.views.IndividualView;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.views.MetaclassView;
import gr.forth.ics.swkm.model2.views.MetapropertyView;
import gr.forth.ics.swkm.model2.views.PropertyView;
import org.springframework.util.Assert;

/**
 * An abstract implementation of RdfNode.
 * 
 * <p>Provides default implementation for type-testing methods of {@link RdfNode},
 * and also implements the object-oriented view of a node (based on its RdfType).
 * 
 * <p>All isXXX methods return false - subclasses override only the particular method
 * that they need to make return true.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
abstract class RdfNodeImpl implements RdfNode,
        ClassView, PropertyView, MetaclassView, MetapropertyView,
        IndividualView, ContainerView {
    private Index index;

    final ModelImpl owner;

    /**
     * Counts the triples that reference this node. Maintained by ModelImpl and TripleImpl.
     */
    private int tripleCounter;

    RdfNodeImpl(ModelImpl owner) {
        Assert.notNull(owner, "owner");
        this.owner = owner;
    }
    
    public Model owner() {
        return owner;
    }

    public boolean hasTriples() {
        return tripleCounter > 0;
    }

    abstract InternalType internalType();

    abstract void setInternalType(InternalType internalType, TypeChange change);

    /**
     * Returns whether the node can ever attain the specified type. The rules are: (a) Resources
     * can become anything, (b) LiteralNodes may only have the Literal type, (c) BlankNodes
     * cannot be any schema type.
     */
    abstract boolean isTypePossible(RdfType type);
    
    /**
     * Returns false.
     */
    public boolean isResource() {
        return false;
    }

    /**
     * Returns false.
     */
    public boolean isBlankNode() {
        return false;
    }

    /**
     * Returns false.
     */
    public boolean isLiteral() {
        return false;
    }
    
    public boolean isObjectNode() {
        return this instanceof ObjectNode;
    }

    void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    //only ObjectNode can ever be the subject of a triple, so override in ObjectNodeImpl
    //to perform an actual triple query
    public Triples subjectTriples() {
        return Triples.empty();
    }

    //only Resource can ever be the predicate of a triple, so override in ResourceImpl
    //to perform an actual triple query
    public Triples predicateTriples() {
        return Triples.empty();
    }
    
    public Triples objectTriples() {
        return owner().triples().o(this).fetch();
    }
    
    public ClassView asClass() {
        checkType(RdfType.CLASS);
        return this;
    }

    public PropertyView asProperty() {
        checkType(RdfType.PROPERTY);
        return this;
    }
    
    public MetaclassView asMetaclass() {
        checkType(RdfType.METACLASS);
        return this;
    }

    public MetapropertyView asMetaproperty() {
        checkType(RdfType.METAPROPERTY);
        return this;
    }
    
    public Inheritable asInheritable() {
        checkInheritable();
        return this;
    }

    public IndividualView asIndividual() {
        checkType(RdfType.INDIVIDUAL);
        return this;
    }

    public ContainerView asContainer() {
        checkContainer();
        return this;
    }

    private void checkType(RdfType type) {
        if (type() != type) {
            throw new IllegalStateException("This functionality is only applicable to nodes of type " +
                    type + " but this node: " + this + " is a type of: " + type());
        }
    }
    
    private void checkInheritable() {
        if (!type().isSchema() && !type().isXmlType()) {
            throw new IllegalStateException("This functionality is only applicable to nodes of some inheritable type "
                    + " but this node: " + this + " is a type of: " + type());
        }
    }

    private void checkContainer() {
        if (!type().isContainer()) {
            throw new IllegalStateException("This functionality is only applicable to nodes of container type " +
                    " but this node: " + this + " is a type of: " + type());
        }
    }
    
    //********
    //Inheritable
    //********
    public Inheritable leastCommonAncestorWith(RdfNode inheritable) {
        if (type() != inheritable.type()) {
            return null; //different types cannot participate in the same hierarchy
        }
        throw new UnsupportedOperationException("Implement least common ancestor");
    }

    public boolean isAncestorOf(RdfNode resource) {
        LabelManager labelManager = owner.getLabelManager();
        final Resource first = (Resource)this;
        final Resource second = (Resource)resource;
        if (labelManager.areLabelsAvailable()) {
            return labelManager.isFirstAncestorOfSecond(first, second);
        }
        return owner.objectViewSupport().isFirstAncestorOfSecond(first, second);
    }

    public boolean isDescendantOf(RdfNode resource) {
        return ((Inheritable)resource).isAncestorOf(this);
    }
    
    //********
    //ClassView
    //********
    public Iterable<PropertyView> domainOf() {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually properties
        Iterable<PropertyView> results =
                (Iterable)owner.objectViewSupport().findPropertiesWithDomain((Resource)this);
        return results;
    }

    public Iterable<PropertyView> rangeOf() {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually properties
        Iterable<PropertyView> results =
                (Iterable)owner.objectViewSupport().findPropertiesWithRange((Resource)this);
        return results;
    }

    public Iterable<IndividualView> instances() {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually individuals
        Iterable<IndividualView> results =
                (Iterable)owner.objectViewSupport().findIndividuals((Resource)this);
        return results;
    }

    public Iterable<ClassView> subClasses(Transitively transitively) {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually classes
        Iterable<ClassView> results =
                (Iterable)owner.objectViewSupport().findDescendants((Resource)this, transitively);
        return results;
    }

    public Iterable<ClassView> superClasses(Transitively transitively) {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually classes
        Iterable<ClassView> results =
                (Iterable)owner.objectViewSupport().findAncestors((Resource)this, transitively);
        return results;
    }

    public Iterable<MetaclassView> metaclasses() {
        checkType(RdfType.CLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually metaclasses
        Iterable<MetaclassView> results =
                (Iterable)owner.objectViewSupport().findMetaclassesOfClass((Resource)this);
        return results;
    }
    //********
    //End of ClassView
    //********
    
    //********
    //PropertyView
    //********
    public Iterable<MetapropertyView> metaproperties() {
        checkType(RdfType.PROPERTY);

        @SuppressWarnings("unchecked") //returned nodes are actually metaclasses
        Iterable<MetapropertyView> results =
                (Iterable)owner.objectViewSupport().findMetapropertiesOfProperty((Resource)this);
        return results;
    }

    public Triples propertyInstances() {
        checkType(RdfType.PROPERTY);
        
        return owner.objectViewSupport().findPropertyInstances((Resource)this);
    }

    public Iterable<Inheritable> domains() {
        checkType(RdfType.PROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are some kind of inheritables
        Iterable<Inheritable> results =
                (Iterable)owner.objectViewSupport().findDomainsOfProperty((Resource)this);
        return results;
    }

    public Iterable<Inheritable> ranges() {
        checkType(RdfType.PROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are some kind of inheritables
        Iterable<Inheritable> results =
                (Iterable)owner.objectViewSupport().findRangesOfProperty((Resource)this);
        return results;
    }

    public Iterable<PropertyView> subProperties(Transitively transitively) {
        checkType(RdfType.PROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are actually properties
        Iterable<PropertyView> results =
                (Iterable)owner.objectViewSupport().findDescendants((Resource)this, transitively);
        return results;
    }

    public Iterable<PropertyView> superProperties(Transitively transitively) {
        checkType(RdfType.PROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are actually properties
        Iterable<PropertyView> results =
                (Iterable)owner.objectViewSupport().findAncestors((Resource)this, transitively);
        return results;
    }
    //********
    //End of PropertyView
    //********
    
    
    //********
    //MetaclassView
    //********
    public Iterable<ClassView> classes() {
        checkType(RdfType.METACLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually classes
        Iterable<ClassView> results =
                (Iterable)owner.objectViewSupport().findClassesOfMetaclass((Resource)this);
        return results;
    }

    public Iterable<MetaclassView> subMetaclasses(Transitively transitively) {
        checkType(RdfType.METACLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually metaclasses
        Iterable<MetaclassView> results =
                (Iterable)owner.objectViewSupport().findDescendants((Resource)this, transitively);
        return results;
    }

    public Iterable<MetaclassView> superMetaclasses(Transitively transitively) {
        checkType(RdfType.METACLASS);
        
        @SuppressWarnings("unchecked") //returned nodes are actually metaclasses
        Iterable<MetaclassView> results =
                (Iterable)owner.objectViewSupport().findAncestors((Resource)this, transitively);
        return results;
    }
    //********
    //End of MetaclassView
    //********

    //********
    //MetapropertyView
    //********
    public Iterable<PropertyView> properties() {
        checkType(RdfType.METAPROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are actually properties
        Iterable<PropertyView> results =
                (Iterable)owner.objectViewSupport().findPropertiesOfMetaproperty((Resource)this);
        return results;
    }

    public Iterable<MetapropertyView> subMetaproperties(Transitively transitively) {
        checkType(RdfType.METAPROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are actually metaproperties
        Iterable<MetapropertyView> results =
                (Iterable)owner.objectViewSupport().findDescendants((Resource)this, transitively);
        return results;
    }

    public Iterable<MetapropertyView> superMetaproperties(Transitively transitively) {
        checkType(RdfType.METAPROPERTY);
        
        @SuppressWarnings("unchecked") //returned nodes are actually metaproperties
        Iterable<MetapropertyView> results =
                (Iterable)owner.objectViewSupport().findAncestors((Resource)this, transitively);
        return results;
    }
    //********
    //End of MetapropertyView
    //********
    
    //********
    //IndividualView
    //********
    public Iterable<ClassView> types() {
        checkType(RdfType.INDIVIDUAL);

        @SuppressWarnings("unchecked") //returned nodes are actually classes
        Iterable<ClassView> results =
                (Iterable)owner.objectViewSupport().findClassesOfIndividual((ObjectNode)this);
        return results;
    }
    //********
    //End of IndividualView
    //********
    
    //********
    //ContainerView
    //********
    public Iterable<RdfNode> getMembers() {
        checkContainer();

        @SuppressWarnings("unchecked") //returned nodes are actually classes
        Iterable<RdfNode> results =
                (Iterable)owner.objectViewSupport().findMembersOfContainer((ObjectNode)this);
        return results;
    }
    //********
    //End of ContainerView
    //********

    public Iterable<? extends Inheritable> ancestors(Transitively transitively) {
        switch (type()) {
            case CLASS:
                return superClasses(transitively);
            case METACLASS:
                return superMetaclasses(transitively);
            case PROPERTY:
                return superProperties(transitively);
            case METAPROPERTY:
                return superMetaproperties(transitively);
            default:
        }
        throw new NotMemberOfHierarchyException(this);
    }

    public Iterable<? extends Inheritable> descendants(Transitively transitively) {
        switch (type()) {
            case CLASS:
                return subClasses(transitively);
            case METACLASS:
                return subMetaclasses(transitively);
            case PROPERTY:
                return subProperties(transitively);
            case METAPROPERTY:
                return subMetaproperties(transitively);
            default:
        }
        throw new NotMemberOfHierarchyException(this);
    }

    public boolean isClass() {
        return type().isClass();
    }

    public boolean isMetaclass() {
        return type().isMetaclass();
    }

    public boolean isProperty() {
        return type().isProperty();
    }
    
    public boolean isMetaproperty() {
        return type().isMetaproperty();
    }
    
    private static class NotMemberOfHierarchyException extends IllegalStateException {
        NotMemberOfHierarchyException(RdfNodeImpl node) {
            super("This node: " + node + " is of type: " + node.type() + ", but this operation " +
                    "is applicable only for types: " +
                    "[" + Joiner.on(",").join(RdfType.CLASS, RdfType.PROPERTY,
                    RdfType.METACLASS, RdfType.METAPROPERTY) + "]");
        }
    }

    void incrementCounter() {
        tripleCounter++;
        if (tripleCounter == 1) {
            owner.onAddedNode(this);
        }
    }

    void decrementCounter() {
        tripleCounter--;
        if (tripleCounter == 0) {
            owner.onDeletedNode(this);
        }
    }
}
