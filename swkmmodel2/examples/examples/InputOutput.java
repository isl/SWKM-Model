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
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import java.io.IOException;

import gr.forth.ics.rdfsuite.services.RdfDocument;
import java.io.File;
import gr.forth.ics.rdfsuite.services.util.IOUtils;

/**
 * Simple example of reading and writing RDF data. For simplicity, we read from plain strings, although
 * reading from files, URLs and generally from InputStreams is supported too.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class InputOutput {
    static final String input =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
        "<rdf:Description rdf:about=\"http://namespace/class1\">" +
        "        <subClassOf xmlns=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"http://www.w3.org/2000/01/rdf-schema#Resource\"/>" +
        "</rdf:Description>" +
        "<rdf:Description rdf:about=\"http://namespace/class2\">" +
        "        <subClassOf xmlns=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"http://www.w3.org/2000/01/rdf-schema#Resource\"/>" +
        "</rdf:Description>" +
        "<rdf:Description rdf:about=\"http://namespace/class1\">" +
        "<subClassOf xmlns=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"http://namespace/class2\"/>" +
        "</rdf:Description>" +
        "</rdf:RDF>";


    public static void main(String[] args) throws IOException {
        /*Model model = ModelBuilder.newSparse().build();

        RdfIO.read(input, Format.RDFXML).withBase("http://baseURItoResolveRelativeURIs").
                withDefaultNamedGraph("http://target/named/graph#").into(model);
        //when reading from a File or a URL, specifying an explicit base URI is optional, but in other cases
        //it is obligatory

        //also: a default target named graph from triples is always optional, if none given,
        //the following is implied:
        Uri defaultNamedGraph = RdfSuite.DEFAULT_GRAPH_URI ;

        System.out.println("The whole model: ");
        System.out.println(model); //prints the model's triples

        //observe that every triple went into the named graph we specified
        System.out.println("The contents of our specified target named graph: ");
        System.out.println(model.triples().g("http://target/named/graph#").fetch());

        //in a format like RDF/XML, we can't specify in the input the target named graph of a triple,
        //so every triple goes to the default named graph

        //in TriG or TriX though, the syntax allows specifying named graphs, so the default named graph
        //is only used as a target for those triples that do not define any named graph to contain them

        //this merely prints the model as TriG to the standard output
        System.out.println("The model as TriG");
        RdfIO.write(model, Format.TRIG).withBase("http://some/base/URI").toStream(System.out);

        */
        RdfDocument thirdVersion = new RdfDocument("http://example.org#",
                IOUtils.readFileAsString(new File("C:/TestData/testManyNGs.trig")),
                gr.forth.ics.rdfsuite.services.Format.TRIG);

        Model myModel = ModelBuilder.newSparse().build();
        try {
            RdfIO.read(thirdVersion.getContent(), Format.TRIG).withBase("http://example.org").into(myModel);
        } catch (Exception readException)
        {
            System.out.println(readException);
        }
        System.out.println("after creating model...");


         try {
            System.out.println("inside try to write the new contents of the model in the file..");
            RdfIO.write(myModel, Format.TRIG).toFile(new File("C:/TestData/testeleni_12.txt"));
        } catch (IOException e)
        {
            System.out.println(e);
        }

    }

}
