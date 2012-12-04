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


package examples;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;

/**
 * Manually creates a simple schema, some data instances and a property instance. Then
 * it executes a simple triple query.
 *
 * <p>All schema is created using triple additions, not convenience methods.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class SimpleSchemaAndInstances_TripleBased {
    public static void main(String[] args) {
        Model model = ModelBuilder.newSparse().build();

        Uri person = Uri.parse("http://example.com#Person");
        Uri party = Uri.parse("http://example.com#Party");
        Uri democrats = Uri.parse("http://example.com#Democrats");
        Uri republicans = Uri.parse("http://example.com#Republicans");
        Uri jim = Uri.parse("http://example.com#Jim");
        Uri votes = Uri.parse("http://example.com#votes");

        //Defining class Person
        model.add().s(person).p(RdfSchema.SUBCLASSOF).o(RdfSchema.RESOURCE);

        //Defining class Party, and two subclasses: Democrats and Republicans
        model.add().s(party).p(RdfSchema.SUBCLASSOF).o(RdfSchema.RESOURCE);
        model.add().s(democrats).p(RdfSchema.SUBCLASSOF).o(party);
        model.add().s(republicans).p(RdfSchema.SUBCLASSOF).o(party);

        //Defining property "votes", which has a domain of Person and a range of Party
        model.add().s(votes).p(Rdf.TYPE).o(Rdf.PROPERTY);
        model.add().s(votes).p(RdfSchema.DOMAIN).o(person);
        model.add().s(votes).p(RdfSchema.RANGE).o(party);

        //Defining a Person instance
        model.add().s(jim).p(Rdf.TYPE).o(person);

        //Defining a property instance of "votes"
        model.add().s(jim).p(votes).o(democrats);

        /*System.out.println("All triples with 'votes' as predicate:");
        for (Triple t : model.triples().p(votes).fetch()) {
            System.out.println(t);
        } */
        System.out.println("All triples with 'jim' as subject:");
        for (Triple t : model.triples().s(jim).fetch()) {
            System.out.println(t);
        }
    }
}
