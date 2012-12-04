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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import gr.forth.ics.swkm.model2.event.TypeChange;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Engine for inferring RDF node types by examining the triples they participate into.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class TypeInference {
    private TypeInference() { }

    private interface TypeChanger {
        /**
         * Forcibly changes the type of node, which may throw RdfTypeException if there is a conflict.
         */
        void changeType(RdfNodeImpl node, RdfType type);


        /**
         * Tries to change the type of a node, or silently ignores it if not possible (in a localized sense, that is
         * by judging only if the transition from the old type to the new type is valid).
         */
        void tryChangeType(RdfNodeImpl node, RdfType type);
    }

    static void applyTypingRulesForNamedGraph(ResourceImpl namedGraph) {
        if (namedGraph.type() == RdfType.NAMED_GRAPH) {
            //early exit: nothing to do
            return;
        }
        new CollectingTypeChanger().executeAll(null, new PendingChange(null, namedGraph,
                namedGraph.type(), RdfType.NAMED_GRAPH));
    }

    static void applyTypingRules(TripleImpl t) {
        new CollectingTypeChanger().executeAll(new PendingTriple(t, null), null);
    }

    static TypeChange initialChange(RdfNodeImpl node, RdfType oldType, RdfType newType) {
        return new PendingChange(null, node, oldType, newType);
    }

    private static class PendingChange implements TypeChange  {
        final PendingTriple cause;
        final RdfNodeImpl node;
        final RdfType newType;
        final RdfType oldType;

        PendingChange(PendingTriple cause, RdfNodeImpl node, RdfType oldType, RdfType newType) {
            this.cause = cause;
            this.node = node;
            this.newType = newType;
            this.oldType = oldType;
        }

        public RdfNode node() {
            return node;
        }

        public RdfType oldType() {
            return oldType;
        }

        public Triple cause() {
            return cause == null ? null : cause.triple;
        }

        public TypeChange parentChange() {
            return cause == null ? null : cause.cause;
        }

        public TypeChange rootChange() {
            TypeChange root = this;
            while (root.parentChange() != null) {
                root = root.parentChange();
            }
            return root;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            PendingChange change = this;
            while (change != null) {
                sb.append(change.node).append(" changing from ").append(change.oldType).append(" to ")
                        .append(change.newType);
                PendingTriple triple = change.cause;
                if (triple == null) {
                    break;
                }
                sb.append("\n  due to ").append(triple.triple.toStringWithTypes());
                change = triple.cause;
                if (change != null) {
                    sb.append("\n which was visited due to ");
                }
            }
            return sb.toString();
        }
    }

    private static class PendingTriple {
        final TripleImpl triple;
        final PendingChange cause;

        PendingTriple(TripleImpl triple, PendingChange cause) { this.triple = triple; this.cause = cause; }
    }

    private static class CollectingTypeChanger implements TypeChanger {
        private final LinkedList<PendingChange> pendingChanges = Lists.newLinkedList();
        private PendingTriple pendingTriple = null;

        public void changeType(RdfNodeImpl node, RdfType type) {
            if (node.type() == type) {
                return;
            }
            pendingChanges.addFirst(new PendingChange(pendingTriple, node, node.type(), type));
        }

        public void tryChangeType(RdfNodeImpl node, RdfType type) {
            if (node.internalType().canBecome(type)) {
                changeType(node, type);
            }
        }

        @SuppressWarnings("unchecked") //depends on all triples of a model being TripleImpl
        void executeAll(PendingTriple initialTriple, PendingChange initialChange) {
            List<NodeAndTypePair> oldTypes = Lists.newArrayList();
            LinkedList<Iterator<PendingTriple>> pendingTriples = Lists.newLinkedList();
            if (initialTriple != null) {
                pendingTriples.add(Iterators.singletonIterator(initialTriple));
            }
            if (initialChange != null) {
                pendingChanges.addFirst(initialChange);
            }
            PendingChange change = null;
            try {
                //we want to exhaust both pending triples and changes, concentrating on one or the other each time
                while (!pendingTriples.isEmpty() || !pendingChanges.isEmpty()) {
                    //first apply all currently pending changes
                    while (!pendingChanges.isEmpty()) {
                        change = pendingChanges.removeFirst();
                        RdfNodeImpl node = change.node;
                        InternalType oldType = node.internalType();
                        InternalType newType = oldType.transformTo(change.newType);
                        if (!node.isTypePossible(newType.getType())) {
                            throw new RdfTypeException("Attempted to change " + node + " to " + newType.getType() + ", which is illegal");
                        }
                        oldTypes.add(new NodeAndTypePair((ObjectNodeImpl)node, oldType)); //typing rules don't affect literals
                        node.setInternalType(newType, change);
                        ObjectNode objectNode = (ObjectNode)node; //if it changed type, it cannot be literal
                        MakeTriplePending makeTriplePending = new MakeTriplePending(change);
                        pendingTriples.add(Iterators.transform(
                            (Iterator)node.owner.triples().s(objectNode).fetch().iterator(),
                            makeTriplePending));
                        pendingTriples.add(Iterators.transform(
                            (Iterator)node.owner.triples().o(objectNode).fetch().iterator(),
                            makeTriplePending));
                    }
                    //then pop a triple and visit it
                    while (!pendingTriples.isEmpty()) {
                        Iterator<PendingTriple> iterator = pendingTriples.peek();
                        if (!iterator.hasNext()) {
                            pendingTriples.pop();
                            continue;
                        }
                        pendingTriple = iterator.next();
                        TripleImpl triple = pendingTriple.triple;
                        triple.predicate().internalType().applyRules(triple, this);
                        break;
                    }
                }
            } catch (RdfTypeException e) {
                if (change != null) {
                    e.setTypeChange(change, change.toString()); //capture the string now, before reverting the types!
                }
                for (NodeAndTypePair pair : oldTypes) {
                    ObjectNodeImpl node = pair.node;
                    InternalType oldType = pair.type;
                    node.setInternalType(oldType, TypeInference.initialChange(node, node.type(), oldType.getType()));
                }
                throw e;
            }
        }

        private static class NodeAndTypePair {
            final ObjectNodeImpl node;
            final InternalType type;
            NodeAndTypePair(ObjectNodeImpl node, InternalType type) {
                this.node = node;
                this.type = type;
            }
        }

        private static class MakeTriplePending implements Function<TripleImpl, PendingTriple> {
            private final PendingChange cause;
            
            MakeTriplePending(PendingChange cause) {
                this.cause = cause;
            }

            public PendingTriple apply(TripleImpl triple) {
                return new PendingTriple(triple, cause);
            }
        }
    }

    enum InternalType {
        RDFS_SUBCLASSOF() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                RdfType sType = triple.subject().type();
                RdfType oType = triple.object().type();

                if (sType == RdfType.METAPROPERTY || oType == RdfType.METAPROPERTY) {
                    typeChanger.changeType(triple.subject(), RdfType.METAPROPERTY);
                    typeChanger.changeType(triple.object(), RdfType.METAPROPERTY);
                } else if (sType == RdfType.METACLASS || oType == RdfType.METACLASS) {
                    typeChanger.changeType(triple.subject(), RdfType.METACLASS);
                    typeChanger.changeType(triple.object(), RdfType.METACLASS);
                } else {
                    typeChanger.changeType(triple.subject(), RdfType.CLASS);
                    typeChanger.changeType(triple.object(), RdfType.CLASS);
                }
            }
            @Override RdfType getType() { return RdfType.PROPERTY; }
        },

        RDFS_SUBPROPERTYOF() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                typeChanger.changeType(triple.subject(), RdfType.PROPERTY);
                typeChanger.changeType(triple.object(), RdfType.PROPERTY);
            }
            @Override RdfType getType() { return RdfType.PROPERTY; }
        },

        RDFS_DOMAIN_OR_RANGE() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                typeChanger.changeType(triple.subject(), RdfType.PROPERTY);
                switch (triple.object().type()) {
                    case CLASS: case METACLASS: case METAPROPERTY: case XML_TYPE:
                        break;
                    default:
                        typeChanger.changeType(triple.object(), RdfType.CLASS);
                }
            }
            @Override RdfType getType() { return RdfType.PROPERTY; }
        },

        RDF_TYPE() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                final ObjectNodeImpl s = triple.subject();
                final RdfNodeImpl o = triple.object();

                //check final states of object
                switch (o.internalType()) {
                    case ALT:
                        typeChanger.changeType(s, RdfType.ALT); return;
                    case BAG:
                        typeChanger.changeType(s, RdfType.BAG); return;
                    case SEQ:
                        typeChanger.changeType(s, RdfType.SEQ); return;
                    case CONSTANT_METACLASS:
                        if (!(s.type().isMetaclass() || s.type().isMetaproperty())) {
                            typeChanger.changeType(s, RdfType.CLASS);
                        }
                        return;
                    case METACLASS:
                        if (!s.type().isMetaclass()) {
                            typeChanger.changeType(s, RdfType.CLASS);
                        } else if (s.internalType() == CONSTANT_METACLASS) {
                            throw new RdfTypeException("rdfs:Class cannot be a type of anything: " + triple);
                        }
                        return;
                    case METAPROPERTY:
                        typeChanger.changeType(s, RdfType.PROPERTY); return;
                    case RDFSUITE_GRAPH:
                        typeChanger.changeType(s, RdfType.NAMED_GRAPH); return;
                }

                switch (s.type()) {
                    case CLASS: case METACLASS:
                        typeChanger.changeType(o, RdfType.METACLASS); return;
                    case PROPERTY:
                        typeChanger.changeType(o, RdfType.METAPROPERTY); return;
                }

                //check remaining cases
                switch (o.type()) {
                    case UNKNOWN: case INDIVIDUAL:
                        typeChanger.changeType(o, RdfType.CLASS); //fall-through
                    case CLASS:
                        typeChanger.changeType(s, RdfType.INDIVIDUAL); return;
                    case METACLASS:
                        typeChanger.changeType(s, RdfType.CLASS); return;
                }
                throw new RdfTypeException("Unexpected (invalid) types for rdf:type triple: " + triple.toStringWithTypes());
            }
            @Override RdfType getType() { return RdfType.PROPERTY; }
        },

        CONSTANT_METACLASS() {
            @Override RdfType getType() { return RdfType.METACLASS; }
        },

        CONSTANT_CLASS() {
            @Override RdfType getType() { return RdfType.CLASS; }
        },

        RDFSUITE_GRAPH() {
            @Override RdfType getType() { return RdfType.CLASS; }
        },

        CLASS() {
            @Override RdfType getType() { return RdfType.CLASS; }
            @Override boolean canBecome(RdfType type) {
                return type.isSchema();
            }
        },

        PROPERTY() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                typeChanger.tryChangeType(triple.subject(), RdfType.INDIVIDUAL);
                typeChanger.tryChangeType(triple.object(), RdfType.INDIVIDUAL);
            }
            @Override RdfType getType() { return RdfType.PROPERTY; }
            @Override boolean canBecome(RdfType type) {
                return type == RdfType.METAPROPERTY;
            }
        },

        METACLASS() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a metaclass: " + triple);
            }
            @Override RdfType getType() { return RdfType.METACLASS; }
            @Override boolean canBecome(RdfType type) {
                return type == RdfType.METAPROPERTY;
            }
        },

        METAPROPERTY() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a metaproperty: " + triple);
            }
            @Override RdfType getType() { return RdfType.METAPROPERTY; }
        },

        INDIVIDUAL() {
            @Override RdfType getType() { return RdfType.INDIVIDUAL; }
            @Override boolean canBecome(RdfType type) {
                return !(type == RdfType.LITERAL || type == RdfType.XML_TYPE); //all others are allowed
            }
        },

        ALT() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be an alternation: " + triple);
            }
            @Override RdfType getType() { return RdfType.ALT; }
        },

        BAG() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a bag: " + triple);
            }
            @Override RdfType getType() { return RdfType.BAG; }
        },

        SEQ() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a sequence: " + triple);
            }
            @Override RdfType getType() { return RdfType.SEQ; }
        },

        XML_TYPE() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be an XML type: " + triple);
            }
            @Override RdfType getType() { return RdfType.XML_TYPE; }
        },

        NAMED_GRAPH() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a named graph: " + triple);
            }
            @Override RdfType getType() { return RdfType.NAMED_GRAPH; }
        },

        UNKNOWN() {
            @Override RdfType getType() { return RdfType.UNKNOWN; }
            @Override boolean canBecome(RdfType type) {
                return true;
            }
        },

        LITERAL() {
            @Override void applyRules(TripleImpl triple, TypeChanger typeChanger) {
                throw new RdfTypeException("The predicate cannot be a literal: " + triple);
            }
            @Override RdfType getType() { return RdfType.LITERAL; }
        },
        ;

        void applyRules(TripleImpl triple, TypeChanger typeChanger) {
            typeChanger.changeType(triple.predicate(), RdfType.PROPERTY); //all predicates are properties
            PROPERTY.applyRules(triple, typeChanger);
        }
        
        abstract RdfType getType();

        /**
         * Never called with an equal type as this; that is always allowed.
         */
        boolean canBecome(RdfType type) {
            return false;
        }

        final InternalType transformTo(RdfType newType) {
            if (newType == getType()) {
                return this;
            }
            if (!canBecome(newType)) {
                throw new RdfTypeException("Cannot change " + getType() + " to " + newType);
            }
            return newType.toInternal();
        }

        @Override public String toString() { return getType().toString(); }
    }

    private static final Map<Uri, InternalType> defaultTypes = ImmutableMap.<Uri, InternalType>builder()
            .put(RdfSchema.CLASS, InternalType.CONSTANT_METACLASS)
            .put(RdfSchema.RESOURCE, InternalType.CONSTANT_CLASS)
            .put(RdfSchema.SUBCLASSOF, InternalType.RDFS_SUBCLASSOF)
            .put(RdfSchema.SUBPROPERTYOF, InternalType.RDFS_SUBPROPERTYOF)
            .put(RdfSchema.DOMAIN, InternalType.RDFS_DOMAIN_OR_RANGE)
            .put(RdfSchema.RANGE, InternalType.RDFS_DOMAIN_OR_RANGE)
            .put(Rdf.TYPE, InternalType.RDF_TYPE)
            .put(Rdf.STATEMENT, InternalType.CONSTANT_CLASS)
            .put(Rdf.PROPERTY, InternalType.METAPROPERTY)
            .put(Rdf.ALT, InternalType.ALT)
            .put(Rdf.BAG, InternalType.BAG)
            .put(Rdf.SEQ, InternalType.SEQ)
            .put(RdfSuite.GRAPH, InternalType.RDFSUITE_GRAPH)
            .build();

     static InternalType defaultTypeFor(RdfNode node) {
         if (node instanceof Resource) {
             Uri uri = ((Resource)node).getUri();
             InternalType type = defaultTypes.get(uri);
             if (type != null) {
                 return type;
             }
            if (uri.hasEqualNamespace(RdfSchema.NAMESPACE) || uri.hasEqualNamespace(Rdf.NAMESPACE)) {
                return inferTypeFromNamingConvention(uri);
            } else if (uri.hasEqualNamespace(XmlSchema.NAMESPACE)) {
                return InternalType.XML_TYPE;
            }
         }
         return InternalType.UNKNOWN;
    }

    private static InternalType inferTypeFromNamingConvention(Uri uri) {
        boolean isCapital = Character.isUpperCase(uri.getLocalName().charAt(0));
        //exploit the fact that all classes in rdf(s) begin with a capital letter
        return isCapital ? InternalType.METACLASS : InternalType.PROPERTY;
    }
}