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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class InferenceTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public InferenceTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(InferenceTest.class);
        suite.addTestSuite(InferenceWithLabelingTest.class);
        return suite;
    }

    protected void maybeCreateLabels(Model model) {
    }

    public static class InferenceWithLabelingTest extends InferenceTest {
        public InferenceWithLabelingTest(String testName) {
            super(testName);
        }

        @Override
        protected void maybeCreateLabels(Model model) {
            model.updateLabels();
        }
    }

    public void testReduceSubClassInference() {
        executeTransitiveReductionTest(RdfSchema.SUBCLASSOF);
    }
    
    public void testReduceSubPropertyInference() {
        executeTransitiveReductionTest(RdfSchema.SUBPROPERTYOF);
    }
    
    public void testReduceTypeInference() {
        Model model = ModelBuilder.newSparse().build();
        Triple nonRedundant1 = model.add().s(ns + "A").p(Rdf.TYPE).o(ns + "B");
        Triple nonRedundant2 = model.add().s(ns + "B").p(RdfSchema.SUBCLASSOF).o(ns + "C");
        
        Triple redundant = model.add().s(ns + "A").p(Rdf.TYPE).o(ns + "C");

        maybeCreateLabels(model);
        
        Inference.reduce(model);
        assert contains(model, nonRedundant1);
        assert contains(model, nonRedundant2);
        assert !contains(model, redundant);
    }

    //Search Inference.java for special case handling for details
    public void testReduceTypeInferenceSpecialCase() {
        Model model = ModelBuilder.newSparse().build();
        Triple nonRedundant1 = model.add().s(ns + "A").p(RdfSchema.SUBCLASSOF).o(ns + "B");
        Triple nonRedundant2 = model.add().s(ns + "B").p(Rdf.TYPE).o(ns + "C");

        Triple redundant = model.add().s(ns + "A").p(Rdf.TYPE).o(ns + "C");

        maybeCreateLabels(model);

        Inference.reduce(model);
        assert contains(model, nonRedundant1);
        assert contains(model, nonRedundant2);
        assert contains(model, redundant); //This is the difference with the previous test
    }

    public void testReducePropertyInstanceInference() {
        Model model = ModelBuilder.newSparse().build();
        Triple nonRedundant1 = model.add().s(ns + "Q").p(RdfSchema.SUBPROPERTYOF).o(ns + "P");
        Triple nonRedundant2 = model.add().s(ns + "X").p(ns + "Q").o(ns + "Y");
        Triple redundant = model.add().s(ns + "X").p(ns + "P").o(ns + "Y");

        maybeCreateLabels(model);

        Inference.reduce(model);
        assert contains(model, nonRedundant1);
        assert contains(model, nonRedundant2);
        assert !contains(model, redundant);
    }

    
    public void testClosureSubClassInference() {
        Model model = ModelBuilder.newSparse().build();
        model.add().s(ns + "A").p(RdfSchema.SUBCLASSOF).o(ns + "B");
        model.add().s(ns + "B").p(RdfSchema.SUBCLASSOF).o(ns + "C");
        Inference.closure(model);
        for(String a : new String[] {ns + "A", ns + "B", ns + "C"}) {
            assert Sets.newHashSet(model.map(a).asInheritable().ancestors(Transitively.NO))
                .equals(Sets.newHashSet(model.map(a).asInheritable().ancestors(Transitively.YES)));
        }
    }

    public void testClosureSubPropertyInference() {
        Model model = ModelBuilder.newSparse().build();
        model.add().s(ns + "A").p(RdfSchema.SUBPROPERTYOF).o(ns + "B");
        model.add().s(ns + "B").p(RdfSchema.SUBPROPERTYOF).o(ns + "C");
        Inference.closure(model);
        for(String a : new String[] {ns + "A", ns + "B", ns + "C"}) {
            assert Sets.newHashSet(model.map(a).asInheritable().ancestors(Transitively.NO))
                .equals(Sets.newHashSet(model.map(a).asInheritable().ancestors(Transitively.YES)));
        }
    }

    public void testClosureTypeInference() {
        Model model = ModelBuilder.newSparse().build();
        model.add().s(ns + "A").p(RdfSchema.SUBCLASSOF).o(ns + "B");
        model.add().s(ns + "a").p(Rdf.TYPE).o(ns + "A");
        Inference.closure(model);
        assert model.triples().s(ns + "a").p(Rdf.TYPE).o(ns + "B").fetch().iterator().hasNext();
    }

    public void testClosurePropertyInstanceInference() {
        Model model = ModelBuilder.newSparse().build();
        model.add().s(ns + "Q").p(RdfSchema.SUBPROPERTYOF).o(ns + "P");
        model.add().s(ns + "a").p(ns + "Q").o(ns + "b");
        Inference.closure(model);
        assert model.triples().s(ns + "a").p(ns + "P").o(ns + "b").fetch().iterator().hasNext();
    }
    
    private static boolean contains(Model model, Triple triple) {
        Triples t = model.triples()
            .s(triple.subject())
            .p(triple.predicate())
            .o(triple.object())
            .fetch();
        if (Iterables.isEmpty(t)) {
            return false;
        }
        return triple == Iterables.getOnlyElement(t);
    }
    
    private void executeTransitiveReductionTest(Uri transitiveProperty) {
        Model model = ModelBuilder.newSparse().build();
        
        Uri[] uris = { 
            Uri.parse(ns + "A"), Uri.parse(ns + "B"),
            Uri.parse(ns + "C"), Uri.parse(ns + "D"),
            Uri.parse(ns + "E") };
        
        Set<Triple> redundantEdges = new HashSet<Triple>();
        Set<Triple> nonRedundantEdges = new HashSet<Triple>();
        
        for (int i = 0; i < uris.length - 1; i++) {
            for (int j = i + 1; j < uris.length; j++) {
                Triple t = model.add().s(uris[i]).p(transitiveProperty).o(uris[j]);
                if (j == i + 1) {
                    nonRedundantEdges.add(t);
                } else {
                    redundantEdges.add(t);
                }
            }
        }
        
        maybeCreateLabels(model);
        Inference.reduce(model);
        for (Triple t : nonRedundantEdges) {
            assert contains(model, t);
        }
        for (Triple t : redundantEdges) {
            assert !contains(model, t);
        }
    }
}
