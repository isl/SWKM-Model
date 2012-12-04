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

import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
public class ModelBuilderTest extends TestCase {
    private static String ns = "http://myDomain#";
    private final Model model;
    private static Uri ngUri = new Uri("http://ng", "");

    public ModelBuilderTest(String testName, Model model) {
        super(testName);
        this.model = model;
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        Model[] modelsWithTyping = {
            ModelBuilder.newSparse().build(),
            ModelBuilder.newFull().build(),
            ModelBuilder.newTrees().build(),
            ModelBuilder.newSparse().withTypeInference().build(),
            ModelBuilder.newFull().withTypeInference().build(),
            ModelBuilder.newTrees().withTypeInference().build(),
        };

        Model[] modelsWithoutTyping = {
            ModelBuilder.newSparse().withoutTypeInference().build(),
            ModelBuilder.newFull().withoutTypeInference().build(),
            ModelBuilder.newTrees().withoutTypeInference().build(),
            ModelBuilder.newHorizontal().build(),
        };

        for (Model model : modelsWithTyping) {
            suite.addTest(new ModelBuilderTest("testHasTyping", model));
        }
        for (Model model : modelsWithoutTyping) {
            suite.addTest(new ModelBuilderTest("testNotHasTyping", model));
            suite.addTest(new ModelBuilderTest("testRetypeAllNodes", model));
        }
        suite.addTest(new ModelBuilderTest("testDefaultBaseUri",
                ModelBuilder.newSparse().withDefaultNamedGraphUri(ngUri).build()));

        suite.addTest(new ModelBuilderTest("testWithUriValidation",
                ModelBuilder.newSparse().withUriValidation().build()));
        suite.addTest(new ModelBuilderTest("testWithUriValidation",
                ModelBuilder.newSparse().build()));
        suite.addTest(new ModelBuilderTest("testWithoutUriValidation",
                ModelBuilder.newSparse().withoutUriValidation().build()));
        return suite;
    }

    public void testHasTyping() {
        model.add().s(ns + "C").p(Rdf.TYPE).o(RdfSchema.CLASS);
        assertEquals(model.mapResource(ns + "C").type(), RdfType.CLASS);
    }

    public void testNotHasTyping() {
        model.add().s(ns + "C").p(Rdf.TYPE).o(RdfSchema.CLASS);
        assertEquals(model.mapResource(ns + "C").type(), RdfType.UNKNOWN);
    }

    public void testRetypeAllNodes() {
        model.add().s(ns + "C").p(Rdf.TYPE).o(RdfSchema.CLASS);
        model.retypeNodes();
        assertEquals(model.mapResource(ns + "C").type(), RdfType.CLASS);
    }

    public void testDefaultBaseUri() {
        model.add().s(ns + "s").p(ns + "p").o(ns + "o");
        assertTrue(model.triples().g(ngUri).fetch().iterator().hasNext());
    }

    public void testWithUriValidation() {
        try {
            model.mapResource("a");
        } catch (IllegalArgumentException e) {
            //ok
            return;
        }
        fail();
    }
    public void testWithoutUriValidation() {
        try {
            model.mapResource("a");
        } catch (IllegalArgumentException e) {
            fail();
            return;
        }
    }
}
