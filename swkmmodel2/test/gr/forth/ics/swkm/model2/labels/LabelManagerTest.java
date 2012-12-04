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


package gr.forth.ics.swkm.model2.labels;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.EnumSet;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class LabelManagerTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public LabelManagerTest(String testName) {
        super(testName);
    }

    protected abstract LabelManager newLabelManager(Model model, Labeler labeler);

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(NonIncrementalMainMemoryManagerTest.class);
        return suite;
    }

    private void prepareModel(Model model) {
        model.add().newClass(ns + "A");
        model.add().newClass(ns + "A1");
        model.add().newClass(ns + "A11");
        model.add().newClass(ns + "A2");
        model.add().newClass(ns + "A21");
        model.add().s(ns + "X").p(Rdf.TYPE).o(ns + "B");

        model.add().s(ns + "A1").p(RdfSchema.SUBCLASSOF).o(ns + "A");
        model.add().s(ns + "A11").p(RdfSchema.SUBCLASSOF).o(ns + "A1");
        model.add().s(ns + "A2").p(RdfSchema.SUBCLASSOF).o(ns + "A");
        model.add().s(ns + "A21").p(RdfSchema.SUBCLASSOF).o(ns + "A2");

        model.add().newProperty(ns + "P1");
        model.add().newProperty(ns + "P12");
        model.add().newProperty(ns + "P2");
        model.add().s(ns + "X").p(ns + "P3").o(ns + "Y");
        model.add().s(ns + "P12").p(RdfSchema.SUBPROPERTYOF).o(ns + "P1");

        model.add().newMetaclass(ns + "MC1");
        model.add().newMetaclass(ns + "MC11");
        model.add().s(ns + "B").p(Rdf.TYPE).o(ns + "MC2");
        model.add().s(ns + "MC11").p(RdfSchema.SUBCLASSOF).o(ns + "MC1");
    }

    public void testSimple() {
        /*
         * Load some triples in a model, then create labels, and check their consistency.
         */
        Model model = ModelBuilder.newSparse().build();
        Labeler labeler = Labelers.newDefault();

        prepareModel(model);

        LabelManager labelManager = newLabelManager(model, labeler);
        model.setLabelManager(labelManager);
        labelManager.updateLabels(null);

        for (RdfType type : EnumSet.of(RdfType.CLASS, RdfType.PROPERTY, RdfType.METACLASS, RdfType.METAPROPERTY)) {
            for (RdfNode c1 : model.findNodes(type)) {
                for (RdfNode c2 : model.findNodes(type)) {
                    assertTrue(areConsistent(c1, c2));
                }
            }
        }

        assertTrue(labelManager.areLabelsAvailable());
    }
    
    public void testWithUpdatesAndLabeling() {
        /*
         * Create labels in a model, *then* load some triples to it, update labels, and check the labeling works
         */
        Model model = ModelBuilder.newSparse().build();
        Labeler labeler = Labelers.newDefault();

        LabelManager labelManager = newLabelManager(model, labeler);
        model.setLabelManager(labelManager);
        labelManager.updateLabels(null);

        prepareModel(model);
        model.updateLabels();

        assertTrue(labelManager.areLabelsAvailable());
        
        for (RdfType type : EnumSet.of(RdfType.CLASS, RdfType.PROPERTY, RdfType.METACLASS, RdfType.METAPROPERTY)) {
            for (RdfNode c1 : model.findNodes(type)) {
                for (RdfNode c2 : model.findNodes(type)) {
                    assertTrue(areConsistent(c1, c2));
                }
            }
        }

        assertTrue(labelManager.areLabelsAvailable());
    }

    public void testClassAndPropertyLabelsNotMixed() {
        Model model = ModelBuilder.newSparse().build();

        model.add().newClass(ns + "C");
        model.add().newClass(ns + "C1");
        model.add().newClass(ns + "C2");

        model.add().newProperty(ns + "P");
        model.add().newProperty(ns + "P1");
        model.add().newProperty(ns + "P2");

        model.add().s(ns + "C1").p(RdfSchema.SUBCLASSOF).o(ns + "C");
        model.add().s(ns + "C2").p(RdfSchema.SUBCLASSOF).o(ns + "C");

        model.add().s(ns + "P1").p(RdfSchema.SUBPROPERTYOF).o(ns + "P");
        model.add().s(ns + "P2").p(RdfSchema.SUBPROPERTYOF).o(ns + "P");

        model.updateLabels();

        for (String clazz : new String[] { ns + "C", ns + "C1", ns + "C2" } ) {
            for (String property : new String[] { ns + "P", ns + "P1", ns + "P2" }) {
                Inheritable i1 = model.map(clazz).asInheritable();
                Inheritable i2 = model.map(property).asInheritable();
                assert !i1.isAncestorOf(i2);
                assert !i1.isDescendantOf(i2);
                assert !i2.isAncestorOf(i1); //symmetric and redundant checks, but be on the safe side
                assert !i2.isDescendantOf(i1);
            }
        }
    }

    private static boolean areConsistent(RdfNode c1, RdfNode c2) {
        //c1 is ancestor of c2 if c2 name starts with c1 name, i.e. A1>A11
        boolean isAncestor1 = c2.toString().startsWith(c1.toString())
                || (c1.toString().endsWith("Resource") && c1.type().isClass())
                || (c1.toString().endsWith("Class") && c1.type().isMetaclass());
        boolean isAncestor2 = c1.asInheritable().isAncestorOf(c2.asInheritable());
        return isAncestor1 == isAncestor2;
    }

    public static class NonIncrementalMainMemoryManagerTest extends LabelManagerTest {
        public NonIncrementalMainMemoryManagerTest(String testName) {
            super(testName);
        }

        @Override
        protected LabelManager newLabelManager(Model model, Labeler labeler) {
            return new LabelManagers.NonIncrementalMainMemoryManager(model, labeler);
        }
    }
}
