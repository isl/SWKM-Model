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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ReifierTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public ReifierTest(String testName) {
        super(testName);
    }

    public void testIncludeReifiedTriples() {
        Model m = ModelBuilder.newSparse().build();
        Resource s = m.mapResource(Uri.parse(ns + "A"));
        Resource p = m.mapResource(Rdf.TYPE);
        Resource o = m.mapResource(RdfSchema.RESOURCE);
        Uri uri = Uri.parse(ns + "statement");
        m.add(null, m.mapResource(uri), m.mapResource(Rdf.TYPE), m.mapResource(Rdf.STATEMENT));
        m.add(null, m.mapResource(uri), m.mapResource(Rdf.SUBJECT), s);
        m.add(null, m.mapResource(uri), m.mapResource(Rdf.PREDICATE), p);
        m.add(null, m.mapResource(uri), m.mapResource(Rdf.OBJECT), o);

        Collection<Triple> c = new ArrayList<Triple>();
        Reifier.includeReifiedTriples(m, c);
        
        assert c.size() == 1;
        Triple t = c.iterator().next();
        assert t.subject() == s;
        assert t.predicate() == p;
        assert t.object() == o;
        
        Iterator<Triple> i = m.triples()
            .s(s)
            .p(p)
            .o(o).fetch().iterator();
        
        assert i.hasNext();
        assert i.next() == t;
        assert !i.hasNext();
    }
}
