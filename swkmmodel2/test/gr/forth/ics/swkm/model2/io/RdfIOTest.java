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


package gr.forth.ics.swkm.model2.io;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.ModelDiff;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.util.RandomTripleGenerator;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Uri;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class RdfIOTest extends TestCase {
    private final Format format;

    public RdfIOTest(String testName, Format format) {
        super(testName);
        this.format = format;
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        for (Format format : Format.values()) {
            suite.addTest(new RdfIOTest("testWriteReadRoundtrip", format));
            suite.addTest(new RdfIOTest("testUsesBaseUriAsDefaultNamedGraph", format));
        }
        return suite;
    }

    public void testWriteReadRoundtrip() throws IOException {
        setName(getName() + "_" + format);
        Model model1 = ModelBuilder.newSparse().build();
        RandomTripleGenerator generator = RandomTripleGenerator.newDefault();
        if (!format.supportsNamedGraphs()) {
            generator.setProb(RandomTripleGenerator.Event.NEW_NAMED_GRAPH, 0.0);
        }
        disableBlankNodes(generator);
        
        Iterator<Triple> triplesGenerator = generator.triplesFor(model1);
        for (int i = 0; i < 1000; i++) {
            triplesGenerator.next();
        }
        
        String text = RdfIO.write(model1, format)
                .withBase(model1.defaultNamedGraph().getUri()).toString();
        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(text, format).withBase(model2.defaultNamedGraph().getUri()).into(model2);
        
        ModelDiff.checkEqual(model1, model2);
    }
    
    public void testUsesBaseUriAsDefaultNamedGraph() throws IOException {
        setName(getName() + "_" + format);
        Model model1 = ModelBuilder.newSparse().build();
        RandomTripleGenerator generator = RandomTripleGenerator.newDefault();
        generator.setProb(RandomTripleGenerator.Event.NEW_NAMED_GRAPH, 0.0);
        disableBlankNodes(generator);
        
        Set<Triple> triples = Sets.newLinkedHashSet();
        Iterator<Triple> triplesGenerator = generator.triplesFor(model1);
        for (int i = 0; i < 1000; i++) {
            triples.add(triplesGenerator.next());
        }
        
        final Uri baseUri = Uri.parse("http://baseUri");
        String text = RdfIO.write(model1, format).withBase(baseUri).toString();
        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(text, format).withBase(baseUri).withDefaultNamedGraph(baseUri).into(model2);
        
        Resource defaultNamedGraph = model2.mapResource(baseUri);
        for (Triple t : triples) {
            Triple t2 = Iterables.getOnlyElement(model2.triples()
                .s(t.subject().mappedTo(model2))
                .p(t.predicate().mappedTo(model2))
                .o(t.object().mappedTo(model2))
                .fetch());
            assertTrue(t2.graphs().contains(defaultNamedGraph));
        }
    }

    private static void disableBlankNodes(RandomTripleGenerator generator) {
        //disable blank nodes, because at parsing they get arbitrary identifiers
        generator.setProb(RandomTripleGenerator.Event.NEW_BLANK_ALT, 0.0);
        generator.setProb(RandomTripleGenerator.Event.NEW_BLANK_BAG, 0.0);
        generator.setProb(RandomTripleGenerator.Event.NEW_BLANK_SEQ, 0.0);
        generator.setProb(RandomTripleGenerator.Event.NEW_BLANK_STATEMENT, 0.0);
        generator.setProb(RandomTripleGenerator.Event.NEW_BLANK_INDIVIDUAL, 0.0);
    }
}
