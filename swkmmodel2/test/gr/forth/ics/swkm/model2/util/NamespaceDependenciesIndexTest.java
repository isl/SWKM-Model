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


package gr.forth.ics.swkm.model2.util;

import com.google.common.collect.ImmutableSet;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.Collections;
import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class NamespaceDependenciesIndexTest extends TestCase {
    
    public NamespaceDependenciesIndexTest(String testName) {
        super(testName);
    }

    private final Model model = ModelBuilder.newSparse().build();
    private final NamespaceDependenciesIndex index = new NamespaceDependenciesIndex(model);

    public void testGetDependenciesAndDependants() {
        Uri ns1 = Uri.parse("http://ns1#");
        Uri ns2 = Uri.parse("http://ns2#");
        Uri ns3 = Uri.parse("http://ns3#");
        Uri class1 = ns1.withLocalName("class1");
        Uri class2 = ns2.withLocalName("class2");
        Uri class3 = ns3.withLocalName("class3");

        Triple t1 = model.add().s(class1).p(RdfSchema.SUBCLASSOF).o(class2);

        assert index.getDependencies(ns1, Transitively.NO).equals(ImmutableSet.of(ns2));
        assert index.getDependencies(ns1, Transitively.YES).equals(ImmutableSet.of(ns2));
        assert index.getDependants(ns1, Transitively.NO).isEmpty();
        assert index.getDependants(ns1, Transitively.YES).isEmpty();

        assert index.getDependencies(ns2, Transitively.NO).isEmpty();
        assert index.getDependencies(ns2, Transitively.YES).isEmpty();
        assert index.getDependants(ns2, Transitively.NO).equals(ImmutableSet.of(ns1));
        assert index.getDependants(ns2, Transitively.YES).equals(ImmutableSet.of(ns1));

        Triple t2 = model.add().s(class2).p(RdfSchema.SUBCLASSOF).o(class3);

        assert index.getDependencies(ns1, Transitively.NO).equals(ImmutableSet.of(ns2));
        assert index.getDependencies(ns1, Transitively.YES).equals(ImmutableSet.of(ns2, ns3));
        assert index.getDependants(ns1, Transitively.NO).isEmpty();
        assert index.getDependants(ns1, Transitively.YES).isEmpty();

        assert index.getDependencies(ns2, Transitively.NO).equals(ImmutableSet.of(ns3));
        assert index.getDependencies(ns2, Transitively.YES).equals(ImmutableSet.of(ns3));
        assert index.getDependants(ns2, Transitively.NO).equals(ImmutableSet.of(ns1));
        assert index.getDependants(ns2, Transitively.YES).equals(ImmutableSet.of(ns1));

        assert index.getDependencies(ns3, Transitively.NO).isEmpty();
        assert index.getDependencies(ns3, Transitively.YES).isEmpty();
        assert index.getDependants(ns3, Transitively.NO).equals(ImmutableSet.of(ns2));
        assert index.getDependants(ns3, Transitively.YES).equals(ImmutableSet.of(ns1, ns2));

        t1.delete();
        
        assert index.getDependencies(ns1, Transitively.NO).isEmpty();
        assert index.getDependencies(ns1, Transitively.YES).isEmpty();
        assert index.getDependants(ns1, Transitively.NO).isEmpty();
        assert index.getDependants(ns1, Transitively.YES).isEmpty();

        assert index.getDependencies(ns2, Transitively.NO).equals(ImmutableSet.of(ns3));
        assert index.getDependencies(ns2, Transitively.YES).equals(ImmutableSet.of(ns3));
        assert index.getDependants(ns2, Transitively.NO).isEmpty();
        assert index.getDependants(ns2, Transitively.YES).isEmpty();

        assert index.getDependencies(ns3, Transitively.NO).isEmpty();
        assert index.getDependencies(ns3, Transitively.YES).isEmpty();
        assert index.getDependants(ns3, Transitively.NO).equals(ImmutableSet.of(ns2));
        assert index.getDependants(ns3, Transitively.YES).equals(ImmutableSet.of(ns2));

        t2.delete();

        for (Uri ns : new Uri[] { ns1, ns2, ns3 }) {
            assert index.getDependencies(ns, Transitively.NO).isEmpty();
            assert index.getDependencies(ns, Transitively.YES).isEmpty();
            assert index.getDependants(ns, Transitively.NO).isEmpty();
            assert index.getDependants(ns, Transitively.YES).isEmpty();
        }
    }

}
