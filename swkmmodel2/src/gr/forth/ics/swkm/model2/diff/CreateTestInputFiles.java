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



package gr.forth.ics.swkm.model2.diff;

import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.Inference;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri.UriFormatException;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.validation.ValidationException;
import gr.forth.ics.swkm.model2.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Papavasileiou Vicky
 */
class CreateTestInputFiles {

    public static void main(String[] args) throws IOException{

        String outputPath = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/diff/results/";
//        createGoOuputFiles(outputPath);
        createCidocOuputFiles(outputPath);
    }

    /*
     * Creates the expected output of explicit and closure diff.
     * In order to create the output of the explicit diff the triples of the
     * two models are intersected.
     * In order to create the output of the closure diff the triples of the closed models
     * are intersected.
     * @param inputFile1 The file of the first ontology
     * @param inputFile2 The file of the second ontology
     * @param ns1 The base uri of the first ontology
     * @param ns2 The base uri of the second ontology
     * @param explicit The name prefix of the files that contain the triples of explicit diff
     * @param closure The name prefix of the files that contain the triples of closure diff
     */
    static void createExpectedExplicitDelta(Model model1,Model model2,
            String ns1, String ns2,String path) throws IOException{

        //Compute added and deleted triples using set comparison
//        Set<Triple> triples1 = Sets.newHashSet(model1.triples().fetch());
//        Set<Triple> triples2 = Sets.newHashSet(model2.triples().fetch());

        Set<TextualTriple> triples1 = new HashSet<TextualTriple>();
        for(Triple triple: model1.triples().fetch()){
            triples1.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }
        Set<TextualTriple> triples2 = new HashSet<TextualTriple>();
        for(Triple triple: model2.triples().fetch()){
            triples2.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }

        //Deleted Triples
        Set<TextualTriple> tmp = new HashSet<TextualTriple>(triples1);
        triples1.removeAll(triples2);
        Set<TextualTriple> deletedT = new HashSet<TextualTriple>(triples1);
        //Added Triples
        triples2.removeAll(tmp);
        Set<TextualTriple> addedT = new HashSet<TextualTriple>(triples2);

        //Create explicit Delta
        Model deleted = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it1 = deletedT.iterator();
        while(it1.hasNext()){
            TextualTriple next = it1.next();
            deleted.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }

        Model added = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it2 = addedT.iterator();
        while(it2.hasNext()){
            TextualTriple next = it2.next();
            added.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }
        RdfIO.write(added, Format.TRIG).withBase(ns2).toFile(
                new File(path+"_explicit_AddedTriples.trig"));
        RdfIO.write(deleted, Format.TRIG).withBase(ns1).toFile(
                new File(path +"_explicit_DeletedTriples.trig"));
    }

    static void createExpectedClosureDelta(Model model1, Model model2,
            String ns1, String ns2,String path) throws IOException{

        //Create closure Delta
        Inference.closure(model1);
        Inference.closure(model2);
        Set<TextualTriple> triples1 = new HashSet<TextualTriple>();
        for(Triple triple: model1.triples().fetch()){
            triples1.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }
        Set<TextualTriple> triples2 = new HashSet<TextualTriple>();
        for(Triple triple: model2.triples().fetch()){
            triples2.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }

        //Deleted Triples
        Set<TextualTriple> tmp = new HashSet<TextualTriple>(triples1);
        triples1.removeAll(triples2);
        Set<TextualTriple> deletedT = new HashSet<TextualTriple>(triples1);
        //Added Triples
        triples2.removeAll(tmp);
        Set<TextualTriple> addedT = new HashSet<TextualTriple>(triples2);

        //Create explicit Delta
        Model deleted = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it1 = deletedT.iterator();
        while(it1.hasNext()){
            TextualTriple next = it1.next();
            deleted.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }

        Model added = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it2 = addedT.iterator();
        while(it2.hasNext()){
            TextualTriple next = it2.next();
            added.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }
        RdfIO.write(added, Format.TRIG).withBase(ns1).toFile(
                new File(path+"_closure_AddedTriples.trig"));
        RdfIO.write(deleted, Format.TRIG).withBase(ns2).toFile(
                new File(path+"_closure_DeletedTriples.trig"));
    }

    static void createExpectedDenseDelta(Model model1, Model model2,
            String ns1, String ns2,String path) throws IOException{

        //Create dense Delta
        Model deleted = createDeletedDense(model1, model2);
        Model added = createAddedDense(model1, model2);

        RdfIO.write(added, Format.TRIG).withBase(ns2).toFile(
                new File(path+"_dense_AddedTriples.trig"));
        RdfIO.write(deleted, Format.TRIG).withBase(ns1).toFile(
                new File(path +"_dense_DeletedTriples.trig"));
    }

    private static Model createDeletedDense(Model model1, Model model2){
        //Compute added and deleted triples using set comparison
        Set<TextualTriple> triples1 = new HashSet<TextualTriple>();
        for(Triple triple: model1.triples().fetch()){
            triples1.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }
        Inference.closure(model2);
        Set<TextualTriple> triples2 = new HashSet<TextualTriple>();
        for(Triple triple: model2.triples().fetch()){
            triples2.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }

        //Deleted Triples
        triples1.removeAll(triples2);
        Set<TextualTriple> deletedT = new HashSet<TextualTriple>(triples1);

        //Create deleted dense Delta
        Model deleted = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it1 = deletedT.iterator();
        while(it1.hasNext()){
            TextualTriple next = it1.next();
            deleted.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }
        return deleted;
    }

    private static Model createAddedDense(Model model1, Model model2){
        //Compute added and deleted triples using set comparison
        Inference.closure(model1);
        Set<TextualTriple> triples1 = new HashSet<TextualTriple>();
        for(Triple triple: model1.triples().fetch()){
            triples1.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }
        Set<TextualTriple> triples2 = new HashSet<TextualTriple>();
        for(Triple triple: model2.triples().fetch()){
            triples2.add(new TextualTriple(
                    triple.subject().toString(),
                    triple.predicate().toString(),
                    triple.object().toString()));
        }

        //Added Triples
        triples2.removeAll(triples1);
        Set<TextualTriple> addedT = new HashSet<TextualTriple>(triples2);

        //Create added dense Delta
        Model added = ModelBuilder.newSparse().build();
        Iterator<TextualTriple> it1 = addedT.iterator();
        while(it1.hasNext()){
            TextualTriple next = it1.next();
            added.add().
                    s(next.subjectText()).
                    p(next.predicateText()).
                    o(next.objectText());
        }
        return added;
    }

    private static void createGoOuputFiles(String outputPath)
            throws IOException, UriFormatException, ValidationException {
        //Read files into models and validate
        Model model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model1);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/16-12-2008/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model2);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_25_11-16_12");
        System.out.println("Done explicit");
        createExpectedClosureDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_25_11-16_12");
        System.out.println("Done closure");
        createExpectedDenseDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_25_11-16_12");
        //--------------------------------------------
        model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/16-12-2008/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model1);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/24-3-2009/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model2);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_16_12-24_3");
        createExpectedClosureDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_16_12-24_3");
        createExpectedDenseDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_16_12-24_3");
        //--------------------------------------------
        model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/24-3-2009/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model1);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/5-5-2009/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model2);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_24_3-5_5");
        createExpectedClosureDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_24_3-5_5");
        createExpectedDenseDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_24_3-5_5");
        //--------------------------------------------
        model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/5-5-2009/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model1);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/26-5-2009/go.rdf"),
                Format.RDFXML).withBase("http://purl.uniprot.org/go.rdf#").into(model2);
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_5_5-26_5");
        createExpectedClosureDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_5_5-26_5");
        createExpectedDenseDelta(model1, model2,
                "http://purl.uniprot.org/go.rdf#", "http://purl.uniprot.org/go.rdf#", outputPath + "Go_5_5-26_5");
    }

private static void createCidocOuputFiles(String outputPath)
        throws IOException, UriFormatException, ValidationException {
        //Read files into models and validate
        Model model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v3.2.1.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v3.3.2.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.2.1-3.3.2");
        createExpectedClosureDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.2.1-3.3.2");
        createExpectedDenseDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.2.1-3.3.2");
        //--------------------------------------------
        model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v3.3.2.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v3.4.9.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.3.2-3.4.9");
        createExpectedClosureDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.3.2-3.4.9");
        createExpectedDenseDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.3.2-3.4.9");
        //--------------------------------------------
        model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v3.4.9.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/cidoc_crm_v4.2.rdf"),
                Format.RDFXML).withBase("http://cidoc#").into(model2);

        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        createExpectedExplicitDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.4.9-4.2");
        createExpectedClosureDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.4.9-4.2");
        createExpectedDenseDelta(model1, model2,
                "http://cidoc#", "http://cidoc#", outputPath + "Cidoc_3.4.9-4.2");
        //--------------------------------------------
    }
}
