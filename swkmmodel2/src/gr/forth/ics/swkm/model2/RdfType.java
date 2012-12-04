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

import gr.forth.ics.swkm.model2.TypeInference.InternalType;

/**
 * Represents the various types an {@linkplain RdfNode} can have.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public enum RdfType {
    /**
     * A class. If a {@linkplain RdfNode node} {@code X} is a class, the existence of this triple is implied:
     * {@code <X rdfs:subClassOf rdfs:Resource>}
     */
    CLASS(InternalType.CLASS) ,
    /**
     * A property. If a {@linkplain RdfNode node} {@code X} is a property, the existence of this triple is implied:
     * {@code <X rdf:type rdf:Property>}
     */
    PROPERTY(InternalType.PROPERTY),
    /**
     * A metaclass. If a {@linkplain RdfNode node} {@code X} is a metaclass, the existence of this triple is implied:
     * {@code <X rdfs:subClassOf rdfs:Class>}
     */
    METACLASS(InternalType.METACLASS),
    /**
     * A metaproperty. If a {@linkplain RdfNode node} {@code X} is a metaproperty, the existence of this triple is implied:
     * {@code <X rdfs:subClassOf rdf:Property>}
     */
    METAPROPERTY(InternalType.METAPROPERTY),
    /**
     * An individual. If a {@linkplain RdfNode node} {@code X} is an individual, the existence of this triple is implied:
     * {@code <X rdf:type rdfs:Resource>}
     */
    INDIVIDUAL(InternalType.INDIVIDUAL),
    /**
     * An alternation. If a {@linkplain RdfNode node} {@code X} is an alternation, the existence of this triple is implied:
     * {@code <X rdf:type rdf:Alt>}
     */
    ALT(InternalType.ALT),
    /**
     * A bag. If a {@linkplain RdfNode node} {@code X} is a bag, the existence of this triple is implied:
     * {@code <X rdf:type rdf:Bag>}
     */
    BAG(InternalType.BAG),
    /**
     * A sequence. If a {@linkplain RdfNode node} {@code X} is a sequence, the existence of this triple is implied:
     * {@code <X rdf:type rdf:Seq>}
     */
    SEQ(InternalType.SEQ),
    /**
     * Unknown.
     */
    UNKNOWN(InternalType.UNKNOWN),
    /**
     * A literal. 
     */
    LITERAL(InternalType.LITERAL),
    /**
     * A sequence. If a {@linkplain RdfNode node} {@code X} is a sequence, the existence of this triple is implied:
     * {@code <X rdf:type http://139.91.183.30:9090/RDF/rdfsuite.rdfs#Graph>}
     */
    NAMED_GRAPH(InternalType.NAMED_GRAPH),
    /**
     * An {@code XML Schema} type.
     */
    XML_TYPE(InternalType.XML_TYPE)
    ;


    private final InternalType internal;

    RdfType(InternalType internal) {
        this.internal = internal;
    }
    /**
     * Returns true if this is one of CLASS, METACLASS, PROPERTY, METAPROPERTY.
     * 
     * @return true if this is one of CLASS, METACLASS, PROPERTY, METAPROPERTY
     */
    public boolean isSchema() {
        switch (this) {
            case CLASS: case PROPERTY: case METACLASS: case METAPROPERTY:
                return true;
        }
        return false;
    }

    /**
     * Returns true if this is CLASS.
     * 
     * @return true if this is CLASS
     */
    public boolean isClass() {
        return this == CLASS;
    }

    /**
     * Returns true if this is PROPERTY.
     * 
     * @return true if this is PROPERTY
     */
    public boolean isProperty() {
        return this == PROPERTY;
    }

    /**
     * Returns true if this is METACLASS.
     * 
     * @return true if this is METACLASS
     */
    public boolean isMetaclass() {
        return this == METACLASS;
    }

    /**
     * Returns true if this is METAPROPERTY.
     * 
     * @return true if this is METAPROPERTY
     */
    public boolean isMetaproperty() {
        return this == METAPROPERTY;
    }

    /**
     * Returns true if this is INDIVIDUAL.
     * 
     * @return true if this is INDIVIDUAL
     */
    public boolean isIndividual() {
        return this == INDIVIDUAL;
    }

    /**
     * Returns true if this is UNKNOWN.
     * 
     * @return true if this is UNKNOWN
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Returns true if this is LITERAL.
     * 
     * @return true if this is LITERAL
     */
    public boolean isLiteral() {
        return this == LITERAL;
    }
    
    /**
     * Returns true if this is ALT or BAG or SEQ.
     * 
     * @return true if this is ALT or BAG or SEQ
     */
    public boolean isContainer() {
        switch (this) {
            case ALT: case BAG: case SEQ:
                return true;
        }
        return false;
    }
        
    /**
     * Returns true if this is ALT.
     * 
     * @return true if this is ALT
     */
    public boolean isAlt() {
        return this == ALT;
    }
    
    /**
     * Returns true if this is BAG.
     * 
     * @return true if this is BAG
     */
    public boolean isBag() {
        return this == BAG;
    }
    
    /**
     * Returns true if this is SEQ.
     * 
     * @return true if this is SEQ
     */
    public boolean isSeq() {
        return this == SEQ;
    }
    
    /**
     * Returns true if this is NAMED_GRAPH.
     * 
     * @return true if this is NAMED_GRAPH
     */
    public boolean isNamedGraph() {
        return this == NAMED_GRAPH;
    }

    /**
     * Returns true if this is XML_TYPE.
     *
     * @return true if this is XML_TYPE
     */
    public boolean isXmlType() {
        return this == XML_TYPE;
    }

    InternalType toInternal() {
        return internal;
    };
}
    
