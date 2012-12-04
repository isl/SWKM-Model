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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.util.Permutator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import junit.framework.TestCase;
import static gr.forth.ics.swkm.model2.RdfType.*;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class TypingTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public TypingTest(String testName) {
        super(testName);
    }

    //this abstraction can be removed and Model be used directly. This was created
    //when these tests ran on both Model implementation (of swkmmodel and of swkmmodel2)
    public interface InterpretationEngine {
        public boolean hasTriple(String subject, String predicate, String object);
        void add(String namedGraph, String subject, String predicate, String object);
        void validate();
        RdfType type(String o);
        
        void delete(String subject, String predicate, String object);
        void retype();
        boolean supportsDeletes();
    }

    protected InterpretationEngine newEngine() {
        return new InterpretationEngine() {
            final Model model = ModelBuilder.newSparse().build();
            final Map<List<String>, Triple> triples = new HashMap<List<String>, Triple>();

            public void add(String namedGraph, String subject, String predicate, String object) {
                Resource mappedNamedGraph = (Resource)resource(namedGraph);
                ObjectNode mappedSubject = (ObjectNode)resource(subject);
                Resource mappedPredicate = (Resource)resource(predicate);
                RdfNode mappedObject = resource(object);

                Triple t = model.add(mappedNamedGraph, mappedSubject, mappedPredicate, mappedObject);
                triples.put(Arrays.asList(subject.toString(), predicate.toString(), object.toString()), t);
            }

            public void retype() {
                model.retypeNodes();
            }

            private RdfNode resource(String o) {
                if (o == null) {
                    return null;
                } else if (o.startsWith("_")) {
                    return model.mapBlankNode(o);
                } else if (o.startsWith("\"")) {
                    return model.mapLiteral(Literal.parse(o));
                } else {
                    return model.mapResource(toUri(o));
                }
            }

            private Uri toUri(String o) {
                return Uri.parse(o);
            }

            public RdfType type(String o) {
                return resource(o).type();
            }

            public void validate() {
            }

            public void delete(String subject, String predicate, String object) {
                List<String> list = Arrays.asList(subject, predicate, object);
                Triple t = triples.get(list);
                if (t == null) {
                    throw new RuntimeException("Triple: " + list + " not found");
                }
                model.delete(t);
            }

            public boolean supportsDeletes() {
                return true;
            }

            public boolean hasTriple(String subject, String predicate, String object) {
                return model.triples().s(subject).p(predicate).o(object)
                        .fetch().iterator().hasNext();
            }

            @Override
            public String toString() {
                return model.toString();
            }
        };
    }
    
    public void testClass() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, RdfSchema.CLASS)
                .assertType(ns + "resource", CLASS)
                .execute();
    }
    
    public void testClassAndProperty() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "resource", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testProperty() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, Rdf.PROPERTY)
                .assertType(ns + "resource", PROPERTY)
                .execute();
    }
    
    public void testPropertyWithDomain() {
        newTest()
                .triple(ns + "resource", RdfSchema.DOMAIN, ns + "domain")
                .assertType(ns + "resource", PROPERTY)
                .execute();
    }
    
    public void testPropertyWithRange() {
        newTest()
                .triple(ns + "resource", RdfSchema.DOMAIN, ns + "range")
                .assertType(ns + "resource", PROPERTY)
                .execute();
    }
    
    public void testDomainIsMadeClass() {
        newTest()
                .triple(ns + "resource", RdfSchema.DOMAIN, ns + "domain")
                .assertType(ns + "domain", CLASS)
                .execute();
    }
    
    public void testRangeIsMadeClass() {
        newTest()
                .triple(ns + "resource", RdfSchema.RANGE, ns + "range")
                .assertType(ns + "range", CLASS)
                .execute();
    }
    
    public void testDomainRemainsMetaClass() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "resource", RdfSchema.DOMAIN, ns + "metaclass")
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testRangeRemainsMetaClass() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "resource", RdfSchema.RANGE, ns + "metaclass")
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testIsolatedTypeTriple() {
        newTest()
                .triple(ns + "resource1", Rdf.TYPE, ns + "resource2")
                .assertType(ns + "resource1", INDIVIDUAL)
                .assertType(ns + "resource2", CLASS)
                .execute();
    }
    
    public void testLongChainOfMetaclasses() {
        PermutativeTest test = newTest();
        test.triple(ns + "A0", RdfSchema.SUBCLASSOF, RdfSchema.CLASS);
        for (int i = 1; i < 6; i++) {
            test.triple(ns + "A" + i, RdfSchema.SUBCLASSOF, ns + "A" + (i - 1));
        }
        test.executeWithDeletes();
    }
    
    public void testPropertyInstance() {
        newTest()
                .triple(ns + "resource1", ns + "resource2", ns + "resource3")
                .assertType(ns + "resource1", INDIVIDUAL, UNKNOWN)
                .assertType(ns + "resource2", PROPERTY)
                .assertType(ns + "resource3", INDIVIDUAL, UNKNOWN)
                .execute()
                .executeWithDeletes();
    }
    
    public void testSubPropertyOf() {
        newTest()
                .triple(ns + "resource1", RdfSchema.SUBPROPERTYOF, ns + "resource2")
                .assertType(ns + "resource1", PROPERTY)
                .assertType(ns + "resource2", PROPERTY)
                .execute();
    }
    
    public void testSubClass() {
        newTest()
                .triple(ns + "class1", RdfSchema.SUBCLASSOF, ns + "class2")
                .assertType(ns + "class1", CLASS)
                .assertType(ns + "class2", CLASS)
                .execute();
    }
    
    public void testMetaClass() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testTypeOfMetaClass() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass")
                .assertType(ns + "class", CLASS)
                .execute();
    }
    
    public void testTypeChain1() {
        newTest()
                .triple(ns + "individual", Rdf.TYPE, ns + "class")
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass")
                .assertType(ns + "individual", INDIVIDUAL)
                .assertType(ns + "class", CLASS)
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testTypeChain2() {
        newTest()
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass")
                .triple(ns + "individual", Rdf.TYPE, ns + "class")
                .assertType(ns + "individual", INDIVIDUAL)
                .assertType(ns + "class", CLASS)
                .execute();
    }
    
    public void testTypeChain3() {
        newTest()
                .triple(ns + "metaclass1", Rdf.TYPE, ns + "metaclass2")
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass1")
                .triple(ns + "individual", Rdf.TYPE, ns + "class")
                .assertType(ns + "metaclass1", METACLASS)
                .assertType(ns + "metaclass2", METACLASS)
                .assertType(ns + "class", CLASS)
                .assertType(ns + "individual", INDIVIDUAL)
                .execute();
    }
    
    public void testTypeChain4() {
        newTest()
                .triple(ns + "metaclass2", Rdf.TYPE, ns + "metaclass3")
                .triple(ns + "metaclass1", Rdf.TYPE, ns + "metaclass2")
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass1")
                .triple(ns + "individual", Rdf.TYPE, ns + "class")
                .assertType(ns + "metaclass1", METACLASS)
                .assertType(ns + "metaclass2", METACLASS)
                .assertType(ns + "metaclass3", METACLASS)
                .assertType(ns + "class", CLASS)
                .assertType(ns + "individual", INDIVIDUAL)
                .execute();
    }

    public void testMetaclassCanBeATypeOfRdfsClass() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "metaclass", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "metaclass", Rdf.TYPE, ns + "X")
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testMetapropertyCannotBeATypeOfAnything() {
        newTest()
                .triple(ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(ns + "metaproperty", Rdf.TYPE, ns + "something")
                .executeInvalid("A metaproperty cannot be a type of anything");
    }
    
    public void testTypeAndSubclass1() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, ns + "B")
                .triple(ns + "X", Rdf.TYPE, ns + "B")
                .triple(ns + "B", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "A", METACLASS)
                .assertType(ns + "X", CLASS)
                .assertType(ns + "B", METACLASS)
                .execute()
                .executeWithDeletes();
    }
    
    public void testTypeAndSubclass2() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, ns + "B")
                .triple(ns + "X", Rdf.TYPE, ns + "A")
                .triple(ns + "B", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "A", METACLASS)
                .assertType(ns + "X", CLASS)
                .assertType(ns + "B", METACLASS)
                .execute()
                .executeWithDeletes();
    }
    
    public void testTypeAndSubclass3() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, ns + "B")
                .triple(ns + "X", Rdf.TYPE, ns + "B")
                .triple(ns + "A", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "A", METACLASS)
                .assertType(ns + "X", CLASS)
                .assertType(ns + "B", METACLASS)
                .execute()
                .executeWithDeletes();
    }
    
    public void testTypeAndSubclass4() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, ns + "B")
                .triple(ns + "X", Rdf.TYPE, ns + "B")
                .triple(ns + "B", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "A", METACLASS)
                .assertType(ns + "X", CLASS)
                .assertType(ns + "B", METACLASS)
                .execute()
                .executeWithDeletes();
    }
    
    public void testTypeAndSubclass5() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, ns + "B")
                .triple(ns + "A", Rdf.TYPE, ns + "C")
                .triple(ns + "A", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "C", METACLASS)
                .execute();
    }
    
    public void testTypeOfType() {
        newTest()
                .triple(ns + "B", Rdf.TYPE, ns + "C")
                .triple(ns + "A", Rdf.TYPE, ns + "B")
                .assertType(ns + "A", INDIVIDUAL)
                .assertType(ns + "B", CLASS)
                .assertType(ns + "C", METACLASS)
                .execute();
    }
    
    public void testSubClassOfMetaClassMakesSubjectMetaClass() {
        newTest()
                .triple(ns + "metaclass1", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "metaclass2", RdfSchema.SUBCLASSOF, ns + "metaclass1")
                .assertType(ns + "metaclass2", METACLASS)
                .execute();
    }
    
    public void testSubClassWithSubjectClassMakesObjectClass() {
        newTest()
                .triple(ns + "class1", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "class1", RdfSchema.SUBCLASSOF, ns + "class2")
                .assertType(ns + "class2", CLASS)
                .execute();
    }
    
    public void testSubClassWithSubjectMetaClassMakesObjectMetaClass() {
        newTest()
                .triple(ns + "metaclass1", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "metaclass1", RdfSchema.SUBCLASSOF, ns + "metaclass2")
                .assertType(ns + "metaclass2", METACLASS)
                .execute();
    }
    
    public void testSuperClassTurnsToMetaClass() {
        newTest()
                .triple(ns + "x", RdfSchema.SUBCLASSOF, ns + "y")
                .triple(ns + "x", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(ns + "x", METACLASS)
                .assertType(ns + "y", METACLASS)
                .execute();
    }
    
    public void testClassTurnsToMetaProperty1() {
        newTest()
                .triple(ns + "metaproperty", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .assertType(ns + "metaproperty", METAPROPERTY)
                .execute();
    }
    
    public void testClassTurnsToMetaProperty2() {
        newTest()
                .triple(ns + "metaproperty1", RdfSchema.SUBCLASSOF, ns + "metaproperty2")
                .triple(ns + "metaproperty1", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .assertType(ns + "metaproperty1", METAPROPERTY)
                .assertType(ns + "metaproperty2", METAPROPERTY)
                .execute();
    }
    
    public void testSubClassWithSubjectProperty() {
        newTest()
                .triple(ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(ns + "property", RdfSchema.SUBCLASSOF, ns + "class")
                .executeInvalid("Allowed a class to be a subclass of a property!");
    }
    
    public void testSubClassWithObjectProperty() {
        newTest()
                .triple(ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(ns + "class", RdfSchema.SUBCLASSOF, ns + "property")
                .executeInvalid("Allowed a property to be a subclass of a class!");
    }
    
    public void testMetaProperty() {
        newTest()
                .triple(ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .assertType(ns + "metaproperty", METAPROPERTY)
                .execute();
    }
    
    public void testTypeOfMetaProperty() {
        newTest()
                .triple(ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(ns + "property", Rdf.TYPE, ns + "metaproperty")
                .assertType(ns + "property", PROPERTY)
                .execute();
    }
    
    public void testSubClassOfMetaProperty() {
        newTest()
                .triple(ns + "metaproperty2", RdfSchema.SUBCLASSOF, ns + "metaproperty1")
                .triple(ns + "metaproperty1", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .assertType(ns + "metaproperty2", METAPROPERTY)
                .execute();
    }
    
    public void testSubPropertyOfMetaProperty1() {
        newTest()
                .triple(ns + "resource1", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(ns + "resource2", RdfSchema.SUBPROPERTYOF, ns + "resource1")
                .executeInvalid("It is wrong to use subPropertyOf of a metaproperty (must use subClassOf)");
    }
    
    public void testSubPropertyOfMetaProperty2() {
        newTest()
                .triple(ns + "resource2", RdfSchema.SUBPROPERTYOF, ns + "resource1")
                .triple(ns + "resource1", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .executeInvalid("It is wrong to use subPropertyOf of a metaproperty (must use subClassOf)");
    }
    
    public void testSubClassOfProperty1() {
        newTest()
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(ns + "resource", RdfSchema.SUBCLASSOF, ns + "property")
                .executeInvalid("It is wrong to use subClassOf with a property as object");
    }
    
    public void testSubClassOfProperty2() {
        newTest()
                .triple(ns + "resource", RdfSchema.SUBCLASSOF, ns + "property")
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid("It is wrong to use subClassOf with a property as object");
    }
    
    public void testStatement() {
        newTest()
                .triple(ns + "statement", Rdf.TYPE, Rdf.STATEMENT)
                .assertType(ns + "statement", INDIVIDUAL)
                .execute();
    }
    
    public void testStatementBySubject() {
        newTest()
                .triple(ns + "statement", Rdf.SUBJECT, ns + "x")
                .assertType(ns + "statement", INDIVIDUAL)
                .execute();
    }
    
    public void testStatementByPredicate() {
        newTest()
                .triple(ns + "statement", Rdf.PREDICATE, ns + "x")
                .assertType(ns + "statement", INDIVIDUAL)
                .execute();
    }
    
    public void testStatementByObject() {
        newTest()
                .triple(ns + "statement", Rdf.OBJECT, ns + "x")
                .assertType(ns + "statement", INDIVIDUAL)
                .execute();
    }
    
    public void testClassTypeOfMetaClass() {
        newTest()
                .triple(ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "class", Rdf.TYPE, ns + "metaclass")
                .assertType(ns + "metaclass", METACLASS)
                .execute();
    }
    
    public void testPropertyTypeOfMetaProperty() {
        newTest()
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(ns + "property", Rdf.TYPE, ns + "metaproperty")
                .assertType(ns + "metaproperty", METAPROPERTY)
                .execute();
    }
    
    public void testAlt() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, Rdf.ALT)
                .assertType(ns + "resource", ALT)
                .execute();
    }
    
    public void testBag() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, Rdf.BAG)
                .assertType(ns + "resource", BAG)
                .execute();
    }
    
    public void testSeq() {
        newTest()
                .triple(ns + "resource", Rdf.TYPE, Rdf.SEQ)
                .assertType(ns + "resource", SEQ)
                .execute();
    }
    
    public void testSubclassingBagDisallowed1() {
        newTest()
                .triple(ns + "bag", Rdf.TYPE, Rdf.BAG)
                .triple(ns + "invalid", RdfSchema.SUBCLASSOF, ns + "bag")
                .executeInvalid("Cannot subclass a bag");
    }
    
    public void testSubclassingBagDisallowed2() {
        newTest()
                .triple(ns + "bag", Rdf.TYPE, Rdf.BAG)
                .triple(ns + "bag", RdfSchema.SUBCLASSOF, ns + "invalid")
                .executeInvalid("Cannot subclass a bag");
    }
    
    public void testSubclassingAltDisallowed1() {
        newTest()
                .triple(ns + "alt", Rdf.TYPE, Rdf.ALT)
                .triple(ns + "invalid", RdfSchema.SUBCLASSOF, ns + "alt")
                .executeInvalid("Cannot subclass an alt");
    }
    
    public void testSubclassingAltDisallowed2() {
        newTest()
                .triple(ns + "alt", Rdf.TYPE, Rdf.ALT)
                .triple(ns + "alt", RdfSchema.SUBCLASSOF, ns + "invalid")
                .executeInvalid("Cannot subclass an alt");
    }
    
    public void testSubclassingSeqDisallowed1() {
        newTest()
                .triple(ns + "seq", Rdf.TYPE, Rdf.SEQ)
                .triple(ns + "invalid", RdfSchema.SUBCLASSOF, ns + "seq")
                .executeInvalid("Cannot subclass a seq");
    }
    
    public void testSubclassingSeqDisallowed2() {
        newTest()
                .triple(ns + "seq", Rdf.TYPE, Rdf.SEQ)
                .triple(ns + "seq", RdfSchema.SUBCLASSOF, ns + "invalid")
                .executeInvalid("Cannot subclass a seq");
    }
    
    public void testRdfClass() {
        newTest()
                .triple(ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .assertType(RdfSchema.CLASS, METACLASS)
                .execute();
    }
    
    public void testRdfProperty() {
        newTest()
                .triple(ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .assertType(Rdf.PROPERTY, METAPROPERTY)
                .execute();
    }
    
    public void testRdfResource1() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .executeInvalid("rdf:Resource cannot be converted to a metaclass");
    }
    
    public void testRdfResource2() {
        newTest()
                .triple(ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(RdfSchema.RESOURCE, RdfSchema.SUBCLASSOF, ns + "metaclass")
                .executeInvalid("rdf:Resource cannot be converted to a metaclass");
    }
    
    public void testUnknownTriple() {
        newTest()
                .triple(ns + "a", ns + "b", ns + "c")
                .assertType(ns + "a", INDIVIDUAL)
                .assertType(ns + "b", PROPERTY)
                .assertType(ns + "c", INDIVIDUAL)
                .execute();
    }
    
    public void testIndividualCanBecomeAStatement() {
        newTest()
                .triple(ns + "statement", ns + "b", ns + "c")
                .triple(ns + "statement", Rdf.TYPE, Rdf.STATEMENT)
                .assertType(ns + "statement", INDIVIDUAL)
                .execute();
    }
    
    public void testIndividualCanBecomeAnAlt() {
        newTest()
                .triple(ns + "alt", ns + "b", ns + "c")
                .triple(ns + "alt", Rdf.TYPE, Rdf.ALT)
                .assertType(ns + "alt", ALT)
                .execute();
    }
    
    public void testIndividualCanBecomeABag() {
        newTest()
                .triple(ns + "bag", ns + "b", ns + "c")
                .triple(ns + "bag", Rdf.TYPE, Rdf.BAG)
                .assertType(ns + "bag", BAG)
                .execute();
    }
    
    public void testIndividualCanBecomeASeq() {
        newTest()
                .triple(ns + "seq", ns + "b", ns + "c")
                .triple(ns + "seq", Rdf.TYPE, Rdf.SEQ)
                .assertType(ns + "seq", SEQ)
                .execute();
    }
    
    public void testStatementCannotBeClass() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.STATEMENT)
                .triple(ns + "a", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }
    
    public void testAltCannotBeClass() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.ALT)
                .triple(ns + "a", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }
    
    public void testBagCannotBeClass() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.BAG)
                .triple(ns + "a", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }
    
    public void testSeqCannotBeClass() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.SEQ)
                .triple(ns + "a", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }
    
    public void testStatementCannotBeProperty() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.STATEMENT)
                .triple(ns + "a", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testAltCannotBeProperty() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.ALT)
                .triple(ns + "a", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testBagCannotBeProperty() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.BAG)
                .triple(ns + "a", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testSeqCannotBeProperty() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, Rdf.SEQ)
                .triple(ns + "a", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testLiteralAsObject() {
        newTest()
                .triple(ns + "a", ns + "b", "\"literal\"")
                .assertType("\"literal\"", LITERAL)
                .execute();
    }
    
    public void testTypeOfLiteral() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, "\"literal\"")
                .executeInvalid("Cannot be a type of a literal");
    }
    
    public void testBlankClassNotAllowed() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }
    
    public void testBlankPropertyNotAllowed() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testTypeOfBlankNotAllowed() {
        newTest()
                .triple(ns + "a", Rdf.TYPE, blank("b"))
                .executeInvalid();
    }
    
    public void testBlankStatement() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, Rdf.STATEMENT)
                .assertType(blank("a"), INDIVIDUAL)
                .execute();
    }
    
    public void testBlankAlt() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, Rdf.ALT)
                .assertType(blank("a"), ALT)
                .execute();
    }
    
    public void testBlankSeq() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, Rdf.SEQ)
                .assertType(blank("a"), SEQ)
                .execute();
    }
    
    public void testBlankBag() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, Rdf.BAG)
                .assertType(blank("a"), BAG)
                .execute();
    }
    
    public void testBlankIndividual() {
        newTest()
                .triple(blank("a"), Rdf.TYPE, ns + "b")
                .assertType(blank("a"), INDIVIDUAL)
                .execute();
    }
    
    public void testTypeOfResource() {
        newTest()
                .triple(ns + "A", Rdf.TYPE, RdfSchema.RESOURCE)
                .assertType(ns + "A", INDIVIDUAL)
                .execute();
    }
    
    public void testSubclassOfResource() {
        newTest()
                .triple(ns + "A", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .assertType(ns + "A", CLASS)
                .execute();
    }
    
    public void testNamedGraph1() {
        newTest()
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .assertType(ns + "namedGraph", NAMED_GRAPH)
                .execute();
    }

    public void testNamedGraph2() {
        newTest()
                .triple(null, ns + "namedGraph", Rdf.TYPE, RdfSuite.GRAPH)
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .assertType(ns + "namedGraph", NAMED_GRAPH)
                .execute();
    }

    public void testNamedGraph3() {
        newTest()
                .triple(null, ns + "namedGraph", Rdf.TYPE, RdfSchema.CLASS)
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testNamedGraph4() {
        newTest()
                .triple(null, ns + "namedGraph", Rdf.TYPE, RdfSuite.GRAPH)
                .assertType(ns + "namedGraph", NAMED_GRAPH)
                .execute();
    }
    
    public void testNamedGraphCannotBeSubclass() {
        newTest()
                .triple(null, ns + "namedGraph", RdfSchema.SUBCLASSOF, ns + "X")
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testNamedGraphCannotBePredicate() {
        newTest()
                .triple(null, ns + "X", ns + "namedGraph", ns + "Y")
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testNamedGraphCannotHaveDomain() {
        newTest()
                .triple(null, ns + "namedGraph", RdfSchema.DOMAIN, ns + "X")
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testNamedGraphCannotHaveRange() {
        newTest()
                .triple(null, ns + "namedGraph", RdfSchema.RANGE, ns + "X")
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testNamedGraphCannotBeSubproperty() {
        newTest()
                .triple(null, ns + "namedGraph", RdfSchema.SUBPROPERTYOF, ns + "X")
                .triple(ns + "namedGraph", ns + "A", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }
    
    public void testTypeOfPropertyNotAllowed() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "X", Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testTypeOfIndividualNotAllowed() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "X", Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfUnknown() {
        newTest()
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "X")
                .executeInvalid();
    }

    public void testRdfClassCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testRdfClassCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "metaclass")
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "metaproperty")
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfClass() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "class")
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testRdfClassCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, RdfSchema.CLASS, Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testRdfPropertyCannotBeTypeOfUnknown() {
        newTest()
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "X")
                .executeInvalid();
    }

    public void testRdfPropertyCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testRdfPropertyCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "metaclass")
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "metaproperty")
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfClass() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "class")
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testRdfPropertyCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }

    public void testRdfResourceCanBeTypeOfUnknown() {
        newTest()
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "X")
                .assertType(ns + "X", METACLASS)
                .execute();
    }

    public void testRdfResourceCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testRdfResourceCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testRdfResourceCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testRdfResourceCanBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "metaclass")
                .execute();
    }

    public void testRdfResourceCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "metaproperty")
                .executeInvalid();
    }

    public void testRdfResourceCannotBeTypeOfClass() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "class")
                .executeInvalid();
    }

    public void testRdfResourceCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testRdfResourceCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testMetaclassCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaclass", Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testMetaclassCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaclass", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testMetaclassCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaclass", Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testMetaclassCanBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaclass2", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaclass", Rdf.TYPE, ns + "metaclass2")
                .assertType(ns + "metaclass", METACLASS)
                .assertType(ns + "metaclass2", METACLASS)
                .execute();
    }

    public void testMetaclassCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaclass", Rdf.TYPE, ns + "metaproperty")
                .executeInvalid();
    }

    public void testMetaclassCannotBeTypeOfClass() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "metaclass", Rdf.TYPE, ns + "class")
                .executeInvalid();
    }

    public void testMetaclassCannotBeTypeOfProperty() {
        newTest()
                .triple(null, "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, "metaclass", Rdf.TYPE, "property")
                .executeInvalid();
    }

    public void testMetaclassCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "metaclass", Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testMetapropertyCannotBeTypeOfUnknown() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", Rdf.TYPE, ns + "X")
                .executeInvalid();
    }

    public void testMetapropertyCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testMetapropertyCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "metaproperty", Rdf.TYPE, ns + "metaclass")
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "metaproperty1", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty2", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty1", Rdf.TYPE, ns + "metaproperty2")
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfClass() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "metaproperty", Rdf.TYPE, ns + "class")
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testMetapropertyCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "metaproperty", Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testClassCanBeTypeOfUnknown() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, ns + "X")
                .assertType(ns + "X", METACLASS)
                .execute();
    }

    public void testClassCanBeTypeOfRdfClass() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .execute();
    }

    public void testClassCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testClassCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testClassCanBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "class", Rdf.TYPE, ns + "metaclass")
                .execute();
    }

    public void testClassCannotBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "x", Rdf.TYPE, ns + "class")
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "class", Rdf.TYPE, ns + "metaproperty")
                .executeInvalid(); //although <class type metaproperty> is allowed (and transforms class to property),
                //eventually an error is raised as a side-effect of the change (particularly x cannot be a type of a property)
    }

    public void testClassCanBeTypeOfClass() {
        newTest()
                .triple(null, ns + "X", Rdf.TYPE, ns + "class1")
                .triple(null, ns + "Y", Rdf.TYPE, ns + "class2")
                .triple(null, ns + "class1", Rdf.TYPE, ns + "class2")
                .assertType(ns + "class2", METACLASS)
                .execute();
    }

    public void testClassCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "class", Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testClassCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "class", RdfSchema.SUBCLASSOF, RdfSchema.RESOURCE)
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testPropertyCanBeTypeOfUnknown() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "property", Rdf.TYPE, ns + "X")
                .assertType(ns + "X", METAPROPERTY)
                .execute();
    }

    public void testPropertyCannotBeTypeOfRdfClass() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "property", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }

    public void testPropertyCanBeTypeOfRdfProperty() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .execute();
    }

    public void testPropertyCannotBeTypeOfRdfResource() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "property", Rdf.TYPE, RdfSchema.RESOURCE)
                .executeInvalid();
    }

    public void testPropertyCannotBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "property", Rdf.TYPE, ns + "metaclass")
                .executeInvalid();
    }

    public void testPropertyCanBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "property", Rdf.TYPE, ns + "metaproperty")
                .execute();
    }

    public void testPropertyCanBeTypeOfClass() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "Y", Rdf.TYPE, ns + "class")
                .triple(null, ns + "property", Rdf.TYPE, ns + "class")
                .assertType(ns + "class", METAPROPERTY)
                .execute();
    }

    public void testPropertyCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "property1", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "property2", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "property1", Rdf.TYPE, ns + "property2")
                .executeInvalid();
    }

    public void testPropertyCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "property", Rdf.TYPE, ns + "individual")
                .executeInvalid();
    }
    
    public void testIndividualCanBeTypeOfUnknown() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "individual", Rdf.TYPE, ns + "X")
                .assertType(ns + "X", CLASS)
                .execute();
    }

    public void testIndividualCannotBeTypeOfRdfClass() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.CLASS)
                .executeInvalid();
    }

    public void testIndividualCannotBeTypeOfRdfProperty() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "individual", Rdf.TYPE, Rdf.PROPERTY)
                .executeInvalid();
    }

    public void testIndividualCanBeTypeOfRdfResource() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .execute();
    }

    public void testIndividualCanBeTypeOfMetaclass() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, ns + "X")
                .triple(null, ns + "metaclass", RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .triple(null, ns + "individual", Rdf.TYPE, ns + "metaclass")
                .assertType(ns + "individual", CLASS)
                .execute();
    }

    public void testIndividualCanBeTypeOfMetaproperty() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, ns + "X")
                .triple(null, ns + "metaproperty", RdfSchema.SUBCLASSOF, Rdf.PROPERTY)
                .triple(null, ns + "individual", Rdf.TYPE, ns + "metaproperty")
                .assertType(ns + "individual", PROPERTY)
                .execute();
    }

    public void testIndividualCanBeTypeOfClass() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "class", Rdf.TYPE, RdfSchema.CLASS)
                .triple(null, ns + "individual", Rdf.TYPE, ns + "class")
                .execute();
    }

    public void testIndividualCannotBeTypeOfProperty() {
        newTest()
                .triple(null, ns + "individual", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "property", Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, ns + "individual", Rdf.TYPE, ns + "property")
                .executeInvalid();
    }

    public void testIndividualCannotBeTypeOfIndividual() {
        newTest()
                .triple(null, ns + "individual1", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "individual2", Rdf.TYPE, RdfSchema.RESOURCE)
                .triple(null, ns + "individual1", Rdf.TYPE, ns + "individual2")
                .executeInvalid();
    }

    public void testDefaultTriples() {
        newTest()
                .triple(null, RdfSchema.RESOURCE, Rdf.TYPE, RdfSchema.CLASS)
                .assertType(RdfSchema.RESOURCE, CLASS)
                .execute();

        newTest()
                .triple(null, RdfSchema.CLASS, RdfSchema.SUBCLASSOF, RdfSchema.CLASS)
                .assertType(RdfSchema.CLASS, METACLASS)
                .execute();

        newTest()
                .triple(null, Rdf.PROPERTY, Rdf.TYPE, RdfSchema.CLASS)
                .triple(null, RdfSchema.DOMAIN, Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, RdfSchema.RANGE, Rdf.TYPE, Rdf.PROPERTY)
                .assertType(Rdf.PROPERTY, METAPROPERTY)
                .assertType(RdfSchema.DOMAIN, PROPERTY)
                .assertType(RdfSchema.RANGE, PROPERTY)
                .execute();

        newTest()
                .triple(null, Rdf.TYPE, Rdf.TYPE, Rdf.PROPERTY)
                .assertType(Rdf.TYPE, PROPERTY)
                .execute();

        newTest()
                .triple(null, Rdf.SUBJECT, Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, Rdf.PREDICATE, Rdf.TYPE, Rdf.PROPERTY)
                .triple(null, Rdf.OBJECT, Rdf.TYPE, Rdf.PROPERTY)
                .assertType(Rdf.SUBJECT, PROPERTY)
                .assertType(Rdf.PREDICATE, PROPERTY)
                .assertType(Rdf.OBJECT, PROPERTY)
                .execute();

        newTest()
                .triple(null, ns + "a", RdfSchema.COMMENT, ns + "b")
                .execute();
    }

    ///////////////////////////////////////////////////
    
    protected static String blank(String id) {
        return "_:" + id;
    }
    
    PermutativeTest newTest() {
        return new PermutativeTest();
    }
    
    class PermutativeTest {
        private final List<List<String>> triples = Lists.newArrayList();
        private final Map<String, List<RdfType>> assertions =
                Maps.newHashMap();

        public PermutativeTest triple(Object subject, Object predicate, Object object) {
            return triple(null, subject, predicate, object);
        }
        
        public PermutativeTest triple(Object namedGraph, Object subject, Object predicate, Object object) {
            triples.add(Arrays.asList(namedGraph == null ? null : namedGraph.toString(),
                    subject.toString(), predicate.toString(), object.toString()));
            return this;
        }

        public PermutativeTest assertType(Object resource, RdfType... expectedType) {
            assertions.put(resource.toString(), Arrays.asList(expectedType));
            return this;
        }
        
        public void executeInvalid() {
            executeInvalid(getName());
        }

        public void executeInvalid(String message) {
            for (List<List<String>> permutation : Permutator.permutations(triples)) {
                InterpretationEngine engine = newEngine();
                try {
                    for (List<String> triple : permutation) {
                        engine.add(triple.get(0), triple.get(1), triple.get(2), triple.get(3));
                    }
                    engine.validate();
                    fail(message + buildPermutationMessage(permutation));
                } catch (Exception ok) {
                }
                validateAssertions(engine, permutation);
            }
        }

        public PermutativeTest execute() {
            for (List<List<String>> permutation : Permutator.permutations(triples)) {
                InterpretationEngine engine = newEngine();
                for (List<String> triple : permutation) {
                    try {
                        engine.add(triple.get(0), triple.get(1), triple.get(2), triple.get(3));
                    } catch (Exception e) {
                        handleTripleAdditionException(e, permutation);
                    }
                }
                try {
                    engine.validate();
                } catch (Exception e) {
                    throw new RuntimeException(
                            buildPermutationMessage(permutation), e);
                }
                validateAssertions(engine, permutation);
            }
            return this;
        }

        public void executeWithDeletes() {
            if (!newEngine().supportsDeletes()) {
                return;
            }
            for (List<List<String>> permutation : Permutator.permutations(triples)) {
                for (int i = 1; i < permutation.size(); i++) {
                    Set<List<String>> correctTriples =
                            new LinkedHashSet<List<String>>(permutation.subList(i,
                            permutation.size()));

                    //only add the correct triples here
                    InterpretationEngine engine1 = newEngine();

                    //add all triples here, and then remove the unwanted ones
                    InterpretationEngine engine2 = newEngine();

                    //both should produce the same results
                    try {
                        for (List<String> triple : permutation) {
                            if (correctTriples.contains(triple)) {
                                engine1.add(triple.get(0), triple.get(1), triple.get(2), triple.get(3));
                            }
                            engine2.add(triple.get(0), triple.get(1), triple.get(2), triple.get(3));
                        }

                        for (List<String> triple : permutation) {
                            if (!correctTriples.contains(triple)) {
                                engine2.delete(triple.get(1), triple.get(2), triple.get(3));
                            }
                        }
                        engine2.retype();
                    } catch (Exception e) {
                        handleTripleAdditionException(e, permutation);
                    }

                    Exception firstException = null, secondException = null;
                    try {
                        engine1.validate();
                    } catch (Exception e) {
                        firstException = e;
                    }
                    try {
                        engine2.validate();
                    } catch (Exception e) {
                        secondException = e;
                    }

                    //either both engines fail, or both succeed, or else there is a problem
                    if ((firstException == null) != (secondException == null)) {
                        if (firstException != null) {
                            throw new RuntimeException(
                                    "The engine that only got the correct triples throw an exception",
                                    firstException);
                        }
                        if (secondException != null) {
                            throw new RuntimeException(
                                    "The engine that got all triples and then remove the unwanted ones throw an exception",
                                    firstException);
                        }
                    }
                    
                    Set<String> allResources = new HashSet<String>();
                    for (List<String> triple : correctTriples) {
                        allResources.addAll(triple);
                    }
                    allResources.remove(null);
                    //all resources must have the same type!
                    for (String resource : allResources) {
                        RdfType type1 = engine1.type(resource);
                        RdfType type2 = engine2.type(resource);
                        if (type1 != type2) {
                            throw new AssertionError("Inconsistent categorization for triples: " + toString(permutation)
                                    + "\nUndeleted triples: " + toString(correctTriples)
                                    + "\nThe resource with the inconsistent categorization: " + resource + "\n" +
                                    "The first engine, which got only the above triples, categorized it as " + type1 + "\n" +
                                    "The second engine, which got more triples and then removed some, categorized it as " + type2);
                        }
                    }
                }
            }
        }
        
        private String toString(Collection<List<String>> triples) {
            StringBuilder sb = new StringBuilder();
            for (Object triple : triples) {
                sb.append("\n").append(triple);
            }
            return sb.toString();
        }
        
        private void handleTripleAdditionException(Exception e, List<List<String>> permutation) {
            throw new RuntimeException(
                    buildPermutationMessage(permutation), e);
        }

        private void validateAssertions(InterpretationEngine engine, List<List<String>> permutation) {
            for (String resource : assertions.keySet()) {
                List<RdfType> allowedTypes = assertions.get(resource);
                boolean found = false;
                RdfType type = engine.type(resource);
                for (RdfType allowedType : allowedTypes) {
                    if (type == allowedType) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Resource '" + resource + "' should be " +
                            allowedTypes + " but was " + type + "." +
                            buildPermutationMessage(permutation));
                }
            }
        }

        private String buildPermutationMessage(List<List<String>> permutation) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nCurrent list of triples:");
            for (List<String> triple : permutation) {
                sb.append("\n    ");
                sb.append("<").append(triple.get(0)).append(">");
                sb.append("  <").append(triple.get(1)).append(">");
                sb.append("  <").append(triple.get(2)).append(">");
                sb.append("  <").append(triple.get(3)).append(">");
            }
            return sb.toString();
        }
    }
}
