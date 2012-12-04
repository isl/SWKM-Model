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

import gr.forth.ics.swkm.model2.Literal;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ValidatorTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public ValidatorTest(String testName) {
        super(testName);
    }
    
    private final Model model = ModelBuilder.newSparse().build();

    public void testPropertiesHaveOneDomainAndRange1() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert model.triples().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.RESOURCE).fetch().iterator().hasNext();
        assert model.triples().s(ns + "p").p(RdfSchema.RANGE).o(RdfSchema.RESOURCE).fetch().iterator().hasNext();
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().size() == 2;
        assert pc.getWarnings().get(0).getErrorCode() == ErrorCode.NO_DOMAIN;
        assert pc.getWarnings().get(1).getErrorCode() == ErrorCode.NO_RANGE;
    }
    
    public void testPropertiesHaveOneDomainAndRange2() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert model.triples().s(ns + "p").p(RdfSchema.RANGE).o(RdfSchema.RESOURCE).fetch().iterator().hasNext();
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().size() == 1;
        assert pc.getWarnings().get(0).getErrorCode() == ErrorCode.NO_RANGE;
    }

    public void testPropertiesHaveOneDomainAndRange3() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().isEmpty();
    }
    
    public void testPropertiesHaveOneDomainAndRange4() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain1");
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain2");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().isEmpty();
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.NON_UNIQUE_DOMAIN;
    }
    
    public void testPropertiesHaveOneDomainAndRange5() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range1");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range2");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().isEmpty();
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.NON_UNIQUE_RANGE;
    }

    public void testNoIllegalDomains1() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(XmlSchema.BOOLEAN);
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 1;
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.ILLEGAL_DOMAIN;
        assert pc.getWarnings().get(0).getErrorCode() == ErrorCode.NO_RANGE;
    }
    
    public void testNoIllegalDomains2() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.LITERAL);
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 1;
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.ILLEGAL_DOMAIN;
        assert pc.getWarnings().get(0).getErrorCode() == ErrorCode.NO_RANGE;
    }
    
    public void testNoIllegalDomains3() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.CLASS);
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().size() == 1;
        assert pc.getWarnings().get(0).getErrorCode() == ErrorCode.NO_RANGE;
    }
    
    public void testNoSubClassOfCycle() {
        model.add().s(ns + "c1").p(RdfSchema.SUBCLASSOF).o(ns + "c2");
        model.add().s(ns + "c2").p(RdfSchema.SUBCLASSOF).o(ns + "c1");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().isEmpty();
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.CYCLE_IN_SUBCLASSOF;
    }
    
    public void testNoSubPropertyOfCycle() {
        model.add().s(ns + "p1").p(RdfSchema.SUBPROPERTYOF).o(ns + "p2");
        model.add().s(ns + "p2").p(RdfSchema.SUBPROPERTYOF).o(ns + "p1");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.CYCLE_IN_SUBPROPERTYOF;
        
        //ignoring warnings, because validation for them is unreliable when cycles exist
    }
    
    public void testSubPropertiesHaveCompatibleDomainAndRanges1() {
        model.add().s(ns + "super_p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "super_p").p(RdfSchema.DOMAIN).o(ns + "super_domain");
        model.add().s(ns + "super_p").p(RdfSchema.RANGE).o(ns + "super_range");
        
        model.add().s(ns + "sub_p").p(RdfSchema.SUBPROPERTYOF).o(ns + "super_p");
        model.add().s(ns + "sub_p").p(RdfSchema.DOMAIN).o(ns + "sub_domain");
        model.add().s(ns + "sub_p").p(RdfSchema.RANGE).o(ns + "sub_range");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 2;
        assert pc.getWarnings().isEmpty();
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.INCOMPATIBLE_DOMAIN;
        assert pc.getErrors().get(1).getErrorCode() == ErrorCode.INCOMPATIBLE_RANGE;
    }
    
    public void testSubPropertiesHaveCompatibleDomainAndRanges2() {
        model.add().s(ns + "super_p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "super_p").p(RdfSchema.DOMAIN).o(ns + "super_domain");
        model.add().s(ns + "super_p").p(RdfSchema.RANGE).o(ns + "super_range");
        
        model.add().s(ns + "sub_p").p(RdfSchema.SUBPROPERTYOF).o(ns + "super_p");
        model.add().s(ns + "sub_p").p(RdfSchema.DOMAIN).o(ns + "sub_domain");
        model.add().s(ns + "sub_p").p(RdfSchema.RANGE).o(ns + "sub_range");
        
        model.add().s(ns + "sub_domain").p(RdfSchema.SUBCLASSOF).o(ns + "super_domain");
        model.add().s(ns + "sub_range").p(RdfSchema.SUBCLASSOF).o(ns + "super_range");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().isEmpty();
    }
    
    public void testPropertyInstancesHaveCompatibleTypes1() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range");
        
        model.add().s(ns + "subject").p(ns + "p").o(ns + "object");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 2;
        assert pc.getWarnings().isEmpty();
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.WRONGLY_TYPED_NODE;
        assert pc.getErrors().get(1).getErrorCode() == ErrorCode.WRONGLY_TYPED_NODE;
    }
    
    public void testPropertyInstancesHaveCompatibleTypes2() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "range");
        
        model.add().s(ns + "subject").p(ns + "p").o(ns + "object");
        model.add().s(ns + "subject").p(Rdf.TYPE).o(ns + "domain");
        model.add().s(ns + "object").p(Rdf.TYPE).o(ns + "range");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().isEmpty();
    }
    
    public void testSimpleValidLiteral() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(RdfSchema.LITERAL);
        
        model.add().s(ns + "x").p(Rdf.TYPE).o(ns + "domain");
        model.add().s(ns + "x").p(ns + "p").o(Literal.create("literal"));
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().isEmpty();
    }
    
    public void testSimpleInvalidLiteral() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(RdfSchema.RESOURCE);
        
        model.add().s(ns + "x").p(Rdf.TYPE).o(ns + "domain");
        model.add().s(ns + "x").p(ns + "p").o(Literal.create("literal"));
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().isEmpty();
        
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.WRONGLY_TYPED_LITERAL;
    }
    
    public void testTypedValidLiteral() {
        Uri myType = Uri.parse(ns + "myType");
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(myType);
        
        model.add().s(ns + "x").p(Rdf.TYPE).o(ns + "domain");
        model.add().s(ns + "x").p(ns + "p").o(Literal.createWithType("literal", myType));
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().isEmpty();
        assert pc.getWarnings().isEmpty();
    }
    
    public void testTypedInvalidLiteral() {
        Uri myType = Uri.parse(ns + "myType");
        Uri notMyType = Uri.parse("notMyType");
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(ns + "domain");
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(myType);
        
        model.add().s(ns + "x").p(Rdf.TYPE).o(ns + "domain");
        model.add().s(ns + "x").p(ns + "p").o(Literal.createWithType("literal", notMyType));
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().isEmpty();
        
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.WRONGLY_TYPED_LITERAL;
    }
    
    public void testRdfSuiteClassAllowsMetaclassesAndMetaproperties() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSuite.CLASS);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(RdfSuite.CLASS);
        
        model.add().s(ns + "metaclass").p(RdfSchema.SUBCLASSOF).o(RdfSchema.CLASS);
        model.add().s(ns + "metaproperty").p(RdfSchema.SUBCLASSOF).o(Rdf.PROPERTY);
        
        model.add().s(ns + "metaclass").p(ns + "p").o(ns + "metaproperty");
        model.add().s(ns + "metaproperty").p(ns + "p").o(ns + "metaclass");
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 0;
        assert pc.getWarnings().size() == 0;
    }
    
    public void testRdfSuiteClassAllowsMetaclassesAndMetaproperties2() {
        model.add().s(ns + "p").p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.CLASS);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(RdfSuite.CLASS);
        
        model.add().s(ns + "metaclass").p(RdfSchema.SUBCLASSOF).o(RdfSchema.CLASS);
        model.add().s(ns + "metaproperty").p(RdfSchema.SUBCLASSOF).o(Rdf.PROPERTY);
        
        //this is invalid, subject is declared as rdfs:Class, not rdfsuite:Class that accepts metaproperties
        model.add().s(ns + "metaproperty").p(ns + "p").o(ns + "metaclass"); 
        
        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 0;
        
        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.WRONGLY_TYPED_NODE;
    }

    public void testMetaclassesCannotBeTypesOfMetaclasses() {
        Resource mc1 = model.add().newMetaclass(ns + "mc1");
        Resource mc2 = model.add().newMetaclass(ns + "mc2");
        model.add().s(mc1).p(Rdf.TYPE).o(mc2);

        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 0;

        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.ILLEGAL_TRIPLE;
    }

    public void testLiterals() {
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.RESOURCE);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(XmlSchema.INT);
        model.add().s(ns + "s").p(Rdf.TYPE).o(RdfSchema.RESOURCE);
        model.add().s(ns + "s").p(ns + "p").o(Literal.createWithType("1", XmlSchema.INT));

        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 0;
        assert pc.getWarnings().size() == 0;

        model.add().s(ns + "s").p(ns + "p").o(Literal.createWithType("wrong", XmlSchema.INT));
        pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 0;

        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.ILLEGAL_LITERAL;
    }

    public void testUntypedLiteralsWorksWithXmlTypes() {
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.RESOURCE);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(XmlSchema.INT);
        model.add().s(ns + "s").p(Rdf.TYPE).o(RdfSchema.RESOURCE);
        model.add().s(ns + "s").p(ns + "p").o(Literal.create("1"));

        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        pc.checkNoErrorOrWarning();
    }

    public void testUntypedLiteralsWorksWithRdfLiteral() {
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.RESOURCE);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(RdfSchema.LITERAL);
        model.add().s(ns + "s").p(Rdf.TYPE).o(RdfSchema.RESOURCE);
        model.add().s(ns + "s").p(ns + "p").o(Literal.create("1"));

        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        pc.checkNoErrorOrWarning();
    }

    public void testUntypedLiteralsDontWorkForArbitraryTypes() {
        model.add().s(ns + "p").p(RdfSchema.DOMAIN).o(RdfSchema.RESOURCE);
        model.add().s(ns + "p").p(RdfSchema.RANGE).o(ns + "someArbitraryClass");
        model.add().s(ns + "s").p(Rdf.TYPE).o(RdfSchema.RESOURCE);
        model.add().s(ns + "s").p(ns + "p").o(Literal.create("1"));

        ProblemCollector pc = new ProblemCollector();
        Validator.defaultValidator().validate(model, pc);
        assert pc.getErrors().size() == 1;
        assert pc.getWarnings().size() == 0;

        assert pc.getErrors().get(0).getErrorCode() == ErrorCode.WRONGLY_TYPED_LITERAL;
    }
}
