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
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import junit.framework.TestCase;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ObjectViewTest extends TestCase {
    private static String ns = "http://myDomain#";
    
    public ObjectViewTest(String arg0) {
        super(arg0);
    }
    
    private Model model;
    
    @Override
    protected void setUp() {
        model = ModelBuilder.newSparse().build();
    }
    
    public void testClass() {
        Resource resource = model.mapResource(RdfSchema.RESOURCE);
        Resource a = model.mapResource(Uri.parse(ns + "a"));
        Resource b = model.mapResource(Uri.parse(ns + "b"));
        Resource c = model.mapResource(Uri.parse(ns + "c"));
        model.add().s(a).p(RdfSchema.SUBCLASSOF).o(resource);
        model.add().s(b).p(RdfSchema.SUBCLASSOF).o(a);
        model.add().s(c).p(RdfSchema.SUBCLASSOF).o(b);
        assert Iterables.getOnlyElement(a.asClass().superClasses(Transitively.NO)) == resource;
        assert Iterables.getOnlyElement(a.asClass().superClasses(Transitively.YES))  == resource;
        
        assert Sets.newHashSet(b.asClass().superClasses(Transitively.YES)).equals(Sets.newHashSet(a, resource));
        assert Sets.newHashSet(b.asClass().superClasses(Transitively.NO)).equals(Sets.newHashSet(a));
        
        assert Sets.newHashSet(c.asClass().superClasses(Transitively.NO)).equals(Sets.newHashSet(b));
        assert Sets.newHashSet(c.asClass().superClasses(Transitively.YES)).equals(Sets.newHashSet(a, b, resource));
        
        assert Iterables.isEmpty(c.asClass().subClasses(Transitively.NO));
        assert Iterables.isEmpty(c.asClass().subClasses(Transitively.YES));
        
        assert Iterables.getOnlyElement(b.asClass().subClasses(Transitively.YES)) == c;
        assert Iterables.getOnlyElement(b.asClass().subClasses(Transitively.NO)) == c;
        
        assert Iterables.getOnlyElement(a.asClass().subClasses(Transitively.NO)) == b;
        assert Sets.newHashSet(a.asClass().subClasses(Transitively.YES)).equals(Sets.newHashSet(c, b));
    }
}
