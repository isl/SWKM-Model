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


package gr.forth.ics.swkm.model2.validation;

import com.google.common.base.Joiner;
import gr.forth.ics.graph.path.Path;
import gr.forth.ics.swkm.model2.Literal;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;

/**
 * Validation error codes.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public enum ErrorCode {
    /**
     * A property has no domain.
     */
    NO_DOMAIN,
    
    /**
     * A property has no range.
     */
    NO_RANGE,
    
    /**
     * A property has more than one domain.
     */
    NON_UNIQUE_DOMAIN,
    
    /**
     * A property has more than one range.
     */
    NON_UNIQUE_RANGE,
    
    /**
     * A property has an illegal domain.
     */
    ILLEGAL_DOMAIN,
    
    /**
     * There is a cycle in the rdfs:subClassOf relation.
     */
    CYCLE_IN_SUBCLASSOF,
    
    /**
     * There is a cycle in the rdfs:subPropertyOf relation.
     */
    CYCLE_IN_SUBPROPERTYOF,
    
    /**
     * A subproperty declares a domain which is not a descendant of the super property's domain.
     */
    INCOMPATIBLE_DOMAIN,
    
    /**
     * A subproperty declares a range which is not a descendant of the super property's range.
     */
    INCOMPATIBLE_RANGE,
    
    /**
     * A property instance declares an object or a subject that is not compatible with
     * the property's declared domain or range respectively.
     */
    WRONGLY_TYPED_NODE,
    
    /**
     * A property instance uses a literal object which is not compatible with the
     * declared range of the property.
     */
    WRONGLY_TYPED_LITERAL,

    /**
     * A triple is illegal.
     */
    ILLEGAL_TRIPLE,

    /**
     * A literal value is illegal, according to the validation rules of its type.
     */
    ILLEGAL_LITERAL
    ;

    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#NON_UNIQUE_DOMAIN}.
     * 
     * @param property the problematic property
     * @param domains the domains of the property
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#NON_UNIQUE_DOMAIN}.
     */
    public static ValidationProblem nonUniquePropertyDomain(Resource property, Iterable<?> domains) {
        return ValidationProblem.error("Property: " + property + " has more than one domains: " 
                + Joiner.on(",").join(domains), NON_UNIQUE_DOMAIN);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#NON_UNIQUE_RANGE}.
     * 
     * @param property the problematic property
     * @param ranges the ranges of the property
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#NON_UNIQUE_RANGE}.
     */
    public static ValidationProblem nonUniquePropertyRange(Resource property, Iterable<?> ranges) {
        return ValidationProblem.error("Property: " + property + " has more than one ranges: " 
                + Joiner.on(",").join(ranges), NON_UNIQUE_RANGE);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_DOMAIN}.
     * 
     * @param property the problematic property
     * @param domain the illegal domain
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_DOMAIN}.
     */
    public static ValidationProblem illegalDomain(Resource property, Resource domain) {
        return ValidationProblem.error("Property " + property + " defines a domain that is not allowed: " + domain, ILLEGAL_DOMAIN);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#CYCLE_IN_SUBCLASSOF}.
     * 
     * @param cycle the cycle found in the {@code rdfs:subClassOf} relation
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#CYCLE_IN_SUBCLASSOF}.
     */
    public static ValidationProblem cycleInSubclassOf(Path cycle) {
        return ValidationProblem.error("Cycle in rdfs:subClassOf relation: " + cycle, ErrorCode.CYCLE_IN_SUBCLASSOF);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#CYCLE_IN_SUBPROPERTYOF}.
     * 
     * @param cycle the cycle found in the {@code rdfs:subPropertyOf} relation
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#CYCLE_IN_SUBPROPERTYOF}.
     */
    public static ValidationProblem cycleInSubpropertyOf(Path cycle) {
        return ValidationProblem.error("Cycle in rdfs:subPropertyOf relation: " + cycle, ErrorCode.CYCLE_IN_SUBPROPERTYOF);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#INCOMPATIBLE_DOMAIN}.
     * 
     * @param subProperty the subProperty with the incompatible domain
     * @param subDomain the domain of the subProperty, which is compatible with the superDomain
     * @param superProperty the superProperty
     * @param superDomain the domain of the superProperty
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#INCOMPATIBLE_DOMAIN}.
     */
    public static ValidationProblem incompatibleDomain(Resource subProperty, Resource subDomain,
            Resource superProperty, Resource superDomain) {
        return ValidationProblem.error("Subproperty: " + subProperty + " has domain: " + subDomain
                + " which is not a descendant of the domain: " + superDomain + " of the superproperty: " + superProperty,
                INCOMPATIBLE_DOMAIN);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#INCOMPATIBLE_RANGE}.
     * 
     * @param subProperty the subProperty with the incompatible range
     * @param subRange the range of the subProperty, which is compatible with the superRange
     * @param superProperty the superProperty
     * @param superRange the range of the superProperty
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#INCOMPATIBLE_RANGE}.
     */
    public static ValidationProblem incompatibleRange(Resource subProperty, Resource subRange,
            Resource superProperty, Resource superRange) {
        return ValidationProblem.error("Subproperty: " + subProperty + " has range: " + subRange
                + "which is not a descendant of the range: " + superRange + " of the superproperty: " + superProperty,
                INCOMPATIBLE_RANGE);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_NODE}.
     * 
     * @param propertyInstance the property instance with the wrongly typed subject
     * @param domain the domain that the subject of the property instance should be a type of
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_NODE}.
     */
    public static ValidationProblem wronglyTypedSubject(Triple propertyInstance, Resource domain) {
        return ValidationProblem.error("Property instance: " + propertyInstance.toSimpleString() + " has subject which is not " +
                "a type of: " + domain + ", which is the declared domain of the property", ErrorCode.WRONGLY_TYPED_NODE);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_NODE}.
     * 
     * @param propertyInstance the property instance with the wrongly typed object
     * @param range the range that the object of the property instance should be a type of
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_NODE}.
     */
    public static ValidationProblem wronglyTypedObject(Triple propertyInstance, Resource range) {
        return ValidationProblem.error("Property instance: " + propertyInstance + " has object which is not " +
                "a type of: " + range + ", which is the declared range of the property", ErrorCode.WRONGLY_TYPED_NODE);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_LITERAL}.
     * 
     * @param propertyInstance the property instance with the wrongly typed object literal
     * @param range the range that the object of the property instance should be a type of
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#WRONGLY_TYPED_LITERAL}.
     */
    public static ValidationProblem wronglyTypedLiteral(Triple propertyInstance, Resource range) {
        return ValidationProblem.error("Property instance: " + propertyInstance.toSimpleString() + " has a literal object which does not " +
                "match with the type of: " + range + ", which is the declared range of the property. Either the literal " +
                "is untyped and the range is not rdfs:Literal, or it is typed and is different with the range", ErrorCode.WRONGLY_TYPED_LITERAL);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#NO_DOMAIN}.
     * 
     * @param property the property that has no domain
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#NO_DOMAIN}.
     */
    public static ValidationProblem noDomain(Resource property) {
        return ValidationProblem.warning("No domain defined for " + property, NO_DOMAIN);
    }
    
    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#NO_RANGE}.
     * 
     * @param property the property that has no range
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#NO_RANGE}.
     */
    public static ValidationProblem noRange(Resource property) {
        return ValidationProblem.warning("No range defined for " + property, NO_RANGE);
    }

    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_TRIPLE}.
     *
     * @param triple the illegal triple
     * @param explanation an explanation about why the triple is illegal
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_TRIPLE}.
     */
    public static ValidationProblem illegalTriple(Triple triple, String explanation) {
        return ValidationProblem.error(explanation, ILLEGAL_TRIPLE);
    }

    /**
     * Creates a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_LITERAL}.
     *
     * @param literal the illegal literal
     * @return a ValidationProblem with error code of {@linkplain ErrorCode#ILLEGAL_LITERAL}.
     */
    public static ValidationProblem illegalLiteral(Literal literal) {
        return ValidationProblem.error("Literal: " + literal + " is not legal according to its type", ILLEGAL_LITERAL);
    }
}
