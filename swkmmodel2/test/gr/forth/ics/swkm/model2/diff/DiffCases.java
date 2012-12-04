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

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiffCases{

    private static List<Data> dataFiles;

    public static List<DiffCase> fromFile() throws FileNotFoundException, IOException{
        List<DiffCase> diffCases = new ArrayList<DiffCase>();

        //Add small testing files
        for(int i=0; i<dataFiles.size(); i++){
            System.out.println(dataFiles.get(i).getVersion1() + " -- " +
                    dataFiles.get(i).getVersion2());
            Model added = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getAddedExplicit()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs2()).into(added);
            Model deleted = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getDeletedExplicit()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs1()).into(deleted);
            Delta explicit =DeltaUtils.createfromModels(added, deleted);
            /* ---------------------------- */
            added = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getAddedClosure()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs2()).into(added);
            deleted = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getDeletedClosure()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs1()).into(deleted);
            Delta closure = DeltaUtils.createfromModels(added, deleted);
            /* ---------------------------- */
            added = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getAddedDense()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs2()).into(added);
            deleted = ModelBuilder.newSparse().build();
            RdfIO.read(new File(dataFiles.get(i).getDeletedDense()),Format.TRIG).
                    withBase(dataFiles.get(i).getNs1()).into(deleted);
            Delta dense = DeltaUtils.createfromModels(added, deleted);

            Delta denseClosure = new TripleDelta((Set<Triple>)dense.getAddedSet(), (Set<Triple>)closure.getDeletedSet());
            Delta explicitDense = new TripleDelta((Set<Triple>)explicit.getAddedSet(), (Set<Triple>)dense.getDeletedSet());
            diffCases.add(new DiffCase(dataFiles.get(i).getName(),
                    dataFiles.get(i).getVersion1(),dataFiles.get(i).getVersion2(),
                    dataFiles.get(i).getNs1(),dataFiles.get(i).getNs2(),explicit, closure, explicitDense,denseClosure));

        }

       return diffCases;
    }


    static{
        dataFiles = new ArrayList<Data>();
        String pathFiles = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/diff/";
        String pathResults = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/diff/results/";

        /*dataFiles.add(new Data("AddClass1",
                pathFiles+"AddClass1_a.rdf",pathFiles+"AddClass1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"AddClass1_Delta_explicit_added.trig",
                pathResults+"AddClass1_Delta_explicit_deleted.trig",
                pathResults+"AddClass1_Delta_closure_added.trig",
                pathResults+"AddClass1_Delta_closure_deleted.trig",
                pathResults+"AddClass1_Delta_dense_added.trig",
                pathResults+"AddClass1_Delta_dense_deleted.trig"
                ));

        dataFiles.add(new Data("AddClass2",
                pathFiles+"AddClass2_a.rdf",pathFiles+"AddClass2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"AddClass2_Delta_explicit_added.trig",
                pathResults+"AddClass2_Delta_explicit_deleted.trig",
                pathResults+"AddClass2_Delta_closure_added.trig",
                pathResults+"AddClass2_Delta_closure_deleted.trig",
                pathResults+"AddClass2_Delta_dense_added.trig",
                pathResults+"AddClass2_Delta_dense_deleted.trig"
               ));

        dataFiles.add(new Data("ClassChangeID1",
                pathFiles+"ClassChangeID1_a.rdf",pathFiles+"ClassChangeID1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"ClassChangeID1_Delta_explicit_added.trig",
                pathResults+"ClassChangeID1_Delta_explicit_deleted.trig",
                pathResults+"ClassChangeID1_Delta_closure_added.trig",
                pathResults+"ClassChangeID1_Delta_closure_deleted.trig",
                pathResults+"ClassChangeID1_Delta_dense_added.trig",
                pathResults+"ClassChangeID1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Class_typeOf1",
                pathFiles+"Class_typeOf1_a.rdf",pathFiles+"Class_typeOf1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Class_typeOf1_Delta_explicit_added.trig",
                pathResults+"Class_typeOf1_Delta_explicit_deleted.trig",
                pathResults+"Class_typeOf1_Delta_closure_added.trig",
                pathResults+"Class_typeOf1_Delta_closure_deleted.trig",
                pathResults+"Class_typeOf1_Delta_dense_added.trig",
                pathResults+"Class_typeOf1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Class_typeOf2",
                pathFiles+"Class_typeOf2_a.rdf",pathFiles+"Class_typeOf2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Class_typeOf2_Delta_explicit_added.trig",
                pathResults+"Class_typeOf2_Delta_explicit_deleted.trig",
                pathResults+"Class_typeOf2_Delta_closure_added.trig",
                pathResults+"Class_typeOf2_Delta_closure_deleted.trig",
                pathResults+"Class_typeOf2_Delta_dense_added.trig",
                pathResults+"Class_typeOf2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Class_typeOf3",
                pathFiles+"Class_typeOf3_a.rdf",pathFiles+"Class_typeOf3_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Class_typeOf3_Delta_explicit_added.trig",
                pathResults+"Class_typeOf3_Delta_explicit_deleted.trig",
                pathResults+"Class_typeOf3_Delta_closure_added.trig",
                pathResults+"Class_typeOf3_Delta_closure_deleted.trig",
                pathResults+"Class_typeOf3_Delta_dense_added.trig",
                pathResults+"Class_typeOf3_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Class_typeOf4",
                pathFiles+"Class_typeOf4_a.rdf",pathFiles+"Class_typeOf4_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Class_typeOf4_Delta_explicit_added.trig",
                pathResults+"Class_typeOf4_Delta_explicit_deleted.trig",
                pathResults+"Class_typeOf4_Delta_closure_added.trig",
                pathResults+"Class_typeOf4_Delta_closure_deleted.trig",
                pathResults+"Class_typeOf4_Delta_dense_added.trig",
                pathResults+"Class_typeOf4_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample1",
                pathFiles+"fexample1_a.rdf",pathFiles+"fexample1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample1_Delta_explicit_added.trig",
                pathResults+"fexample1_Delta_explicit_deleted.trig",
                pathResults+"fexample1_Delta_closure_added.trig",
                pathResults+"fexample1_Delta_closure_deleted.trig",
                pathResults+"fexample1_Delta_dense_added.trig",
                pathResults+"fexample1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample2",
                pathFiles+"fexample2_a.rdf",pathFiles+"fexample2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample2_Delta_explicit_added.trig",
                pathResults+"fexample2_Delta_explicit_deleted.trig",
                pathResults+"fexample2_Delta_closure_added.trig",
                pathResults+"fexample2_Delta_closure_deleted.trig",
                pathResults+"fexample2_Delta_dense_added.trig",
                pathResults+"fexample2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample3",
                pathFiles+"fexample3_a.rdf",pathFiles+"fexample3_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample3_Delta_explicit_added.trig",
                pathResults+"fexample3_Delta_explicit_deleted.trig",
                pathResults+"fexample3_Delta_closure_added.trig",
                pathResults+"fexample3_Delta_closure_deleted.trig",
                pathResults+"fexample3_Delta_dense_added.trig",
                pathResults+"fexample3_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample4",
                pathFiles+"fexample4_a.rdf",pathFiles+"fexample4_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample4_Delta_explicit_added.trig",
                pathResults+"fexample4_Delta_explicit_deleted.trig",
                pathResults+"fexample4_Delta_closure_added.trig",
                pathResults+"fexample4_Delta_closure_deleted.trig",
                pathResults+"fexample4_Delta_dense_added.trig",
                pathResults+"fexample4_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample5",
                pathFiles+"fexample5_a.rdf",pathFiles+"fexample5_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample5_Delta_explicit_added.trig",
                pathResults+"fexample5_Delta_explicit_deleted.trig",
                pathResults+"fexample5_Delta_closure_added.trig",
                pathResults+"fexample5_Delta_closure_deleted.trig",
                pathResults+"fexample5_Delta_dense_added.trig",
                pathResults+"fexample5_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample6",
                pathFiles+"fexample6_a.rdf",pathFiles+"fexample6_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample6_Delta_explicit_added.trig",
                pathResults+"fexample6_Delta_explicit_deleted.trig",
                pathResults+"fexample6_Delta_closure_added.trig",
                pathResults+"fexample6_Delta_closure_deleted.trig",
                pathResults+"fexample6_Delta_dense_added.trig",
                pathResults+"fexample6_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample7",
                pathFiles+"fexample7_a.rdf",pathFiles+"fexample7_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample7_Delta_explicit_added.trig",
                pathResults+"fexample7_Delta_explicit_deleted.trig",
                pathResults+"fexample7_Delta_closure_added.trig",
                pathResults+"fexample7_Delta_closure_deleted.trig",
                pathResults+"fexample7_Delta_dense_added.trig",
                pathResults+"fexample7_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("fexample8",
                pathFiles+"fexample8_a.rdf",pathFiles+"fexample8_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"fexample8_Delta_explicit_added.trig",
                pathResults+"fexample8_Delta_explicit_deleted.trig",
                pathResults+"fexample8_Delta_closure_added.trig",
                pathResults+"fexample8_Delta_closure_deleted.trig",
                pathResults+"fexample8_Delta_dense_added.trig",
                pathResults+"fexample8_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain1",
                pathFiles+"Property_domain1_a.rdf",pathFiles+"Property_domain1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain1_Delta_explicit_added.trig",
                pathResults+"Property_domain1_Delta_explicit_deleted.trig",
                pathResults+"Property_domain1_Delta_closure_added.trig",
                pathResults+"Property_domain1_Delta_closure_deleted.trig",
                pathResults+"Property_domain1_Delta_dense_added.trig",
                pathResults+"Property_domain1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain2",
                pathFiles+"Property_domain2_a.rdf",pathFiles+"Property_domain2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain2_Delta_explicit_added.trig",
                pathResults+"Property_domain2_Delta_explicit_deleted.trig",
                pathResults+"Property_domain2_Delta_closure_added.trig",
                pathResults+"Property_domain2_Delta_closure_deleted.trig",
                pathResults+"Property_domain2_Delta_dense_added.trig",
                pathResults+"Property_domain2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain3",
                pathFiles+"Property_domain3_a.rdf",pathFiles+"Property_domain3_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain3_Delta_explicit_added.trig",
                pathResults+"Property_domain3_Delta_explicit_deleted.trig",
                pathResults+"Property_domain3_Delta_closure_added.trig",
                pathResults+"Property_domain3_Delta_closure_deleted.trig",
                pathResults+"Property_domain3_Delta_dense_added.trig",
                pathResults+"Property_domain3_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain4",
                pathFiles+"Property_domain4_a.rdf",pathFiles+"Property_domain4_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain4_Delta_explicit_added.trig",
                pathResults+"Property_domain4_Delta_explicit_deleted.trig",
                pathResults+"Property_domain4_Delta_closure_added.trig",
                pathResults+"Property_domain4_Delta_closure_deleted.trig",
                pathResults+"Property_domain4_Delta_dense_added.trig",
                pathResults+"Property_domain4_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain5",
                pathFiles+"Property_domain5_a.rdf",pathFiles+"Property_domain5_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain5_Delta_explicit_added.trig",
                pathResults+"Property_domain5_Delta_explicit_deleted.trig",
                pathResults+"Property_domain5_Delta_closure_added.trig",
                pathResults+"Property_domain5_Delta_closure_deleted.trig",
                pathResults+"Property_domain5_Delta_dense_added.trig",
                pathResults+"Property_domain5_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_domain6",
                pathFiles+"Property_domain6_a.rdf",pathFiles+"Property_domain6_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_domain6_Delta_explicit_added.trig",
                pathResults+"Property_domain6_Delta_explicit_deleted.trig",
                pathResults+"Property_domain6_Delta_closure_added.trig",
                pathResults+"Property_domain6_Delta_closure_deleted.trig",
                pathResults+"Property_domain6_Delta_dense_added.trig",
                pathResults+"Property_domain6_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range1",
                pathFiles+"Property_range1_a.rdf",pathFiles+"Property_range1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range1_Delta_explicit_added.trig",
                pathResults+"Property_range1_Delta_explicit_deleted.trig",
                pathResults+"Property_range1_Delta_closure_added.trig",
                pathResults+"Property_range1_Delta_closure_deleted.trig",
                pathResults+"Property_range1_Delta_dense_added.trig",
                pathResults+"Property_range1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range2",
                pathFiles+"Property_range2_a.rdf",pathFiles+"Property_range2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range2_Delta_explicit_added.trig",
                pathResults+"Property_range2_Delta_explicit_deleted.trig",
                pathResults+"Property_range2_Delta_closure_added.trig",
                pathResults+"Property_range2_Delta_closure_deleted.trig",
                pathResults+"Property_range2_Delta_dense_added.trig",
                pathResults+"Property_range2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range3",
                pathFiles+"Property_range3_a.rdf",pathFiles+"Property_range3_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range3_Delta_explicit_added.trig",
                pathResults+"Property_range3_Delta_explicit_deleted.trig",
                pathResults+"Property_range3_Delta_closure_added.trig",
                pathResults+"Property_range3_Delta_closure_deleted.trig",
                pathResults+"Property_range3_Delta_dense_added.trig",
                pathResults+"Property_range3_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range4",
                pathFiles+"Property_range4_a.rdf",pathFiles+"Property_range4_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range4_Delta_explicit_added.trig",
                pathResults+"Property_range4_Delta_explicit_deleted.trig",
                pathResults+"Property_range4_Delta_closure_added.trig",
                pathResults+"Property_range4_Delta_closure_deleted.trig",
                pathResults+"Property_range4_Delta_dense_added.trig",
                pathResults+"Property_range4_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range5",
                pathFiles+"Property_range5_a.rdf",pathFiles+"Property_range5_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range5_Delta_explicit_added.trig",
                pathResults+"Property_range5_Delta_explicit_deleted.trig",
                pathResults+"Property_range5_Delta_closure_added.trig",
                pathResults+"Property_range5_Delta_closure_deleted.trig",
                pathResults+"Property_range5_Delta_dense_added.trig",
                pathResults+"Property_range5_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("Property_range6",
                pathFiles+"Property_range6_a.rdf",pathFiles+"Property_range6_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"Property_range6_Delta_explicit_added.trig",
                pathResults+"Property_range6_Delta_explicit_deleted.trig",
                pathResults+"Property_range6_Delta_closure_added.trig",
                pathResults+"Property_range6_Delta_closure_deleted.trig",
                pathResults+"Property_range6_Delta_dense_added.trig",
                pathResults+"Property_range6_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("RemoveClass1",
                pathFiles+"RemoveClass1_a.rdf",pathFiles+"RemoveClass1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"RemoveClass1_Delta_explicit_added.trig",
                pathResults+"RemoveClass1_Delta_explicit_deleted.trig",
                pathResults+"RemoveClass1_Delta_closure_added.trig",
                pathResults+"RemoveClass1_Delta_closure_deleted.trig",
                pathResults+"RemoveClass1_Delta_dense_added.trig",
                pathResults+"RemoveClass1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("RemoveClass2",
                pathFiles+"RemoveClass2_a.rdf",pathFiles+"RemoveClass2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"RemoveClass2_Delta_explicit_added.trig",
                pathResults+"RemoveClass2_Delta_explicit_deleted.trig",
                pathResults+"RemoveClass2_Delta_closure_added.trig",
                pathResults+"RemoveClass2_Delta_closure_deleted.trig",
                pathResults+"RemoveClass2_Delta_dense_added.trig",
                pathResults+"RemoveClass2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SameGraph1",
                pathFiles+"SameGraph1_a.rdf",pathFiles+"SameGraph1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SameGraph1_Delta_explicit_added.trig",
                pathResults+"SameGraph1_Delta_explicit_deleted.trig",
                pathResults+"SameGraph1_Delta_closure_added.trig",
                pathResults+"SameGraph1_Delta_closure_deleted.trig",
                pathResults+"SameGraph1_Delta_dense_added.trig",
                pathResults+"SameGraph1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SameGraph2",
                pathFiles+"SameGraph2_a.rdf",pathFiles+"SameGraph2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SameGraph2_Delta_explicit_added.trig",
                pathResults+"SameGraph2_Delta_explicit_deleted.trig",
                pathResults+"SameGraph2_Delta_closure_added.trig",
                pathResults+"SameGraph2_Delta_closure_deleted.trig",
                pathResults+"SameGraph2_Delta_dense_added.trig",
                pathResults+"SameGraph2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("simpleExample1",
                pathFiles+"simpleExample1_a.rdf",pathFiles+"simpleExample1_b.rdf",
                "http://Mymodel#","http://Mymodel#",
                pathResults+"simpleExample1_Delta_explicit_added.trig",
                pathResults+"simpleExample1_Delta_explicit_deleted.trig",
                pathResults+"simpleExample1_Delta_closure_added.trig",
                pathResults+"simpleExample1_Delta_closure_deleted.trig",
                pathResults+"simpleExample1_Delta_dense_added.trig",
                pathResults+"simpleExample1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SuperClasses1",
                pathFiles+"SuperClasses1_a.rdf",pathFiles+"SuperClasses1_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SuperClasses1_Delta_explicit_added.trig",
                pathResults+"SuperClasses1_Delta_explicit_deleted.trig",
                pathResults+"SuperClasses1_Delta_closure_added.trig",
                pathResults+"SuperClasses1_Delta_closure_deleted.trig",
                pathResults+"SuperClasses1_Delta_dense_added.trig",
                pathResults+"SuperClasses1_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SuperClasses2",
                pathFiles+"SuperClasses2_a.rdf",pathFiles+"SuperClasses2_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SuperClasses2_Delta_explicit_added.trig",
                pathResults+"SuperClasses2_Delta_explicit_deleted.trig",
                pathResults+"SuperClasses2_Delta_closure_added.trig",
                pathResults+"SuperClasses2_Delta_closure_deleted.trig",
                pathResults+"SuperClasses2_Delta_dense_added.trig",
                pathResults+"SuperClasses2_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SuperClasses3",
                pathFiles+"SuperClasses3_a.rdf",pathFiles+"SuperClasses3_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SuperClasses3_Delta_explicit_added.trig",
                pathResults+"SuperClasses3_Delta_explicit_deleted.trig",
                pathResults+"SuperClasses3_Delta_closure_added.trig",
                pathResults+"SuperClasses3_Delta_closure_deleted.trig",
                pathResults+"SuperClasses3_Delta_dense_added.trig",
                pathResults+"SuperClasses3_Delta_dense_deleted.trig"));

        dataFiles.add(new Data("SuperClasses4",
                pathFiles+"SuperClasses4_a.rdf",pathFiles+"SuperClasses4_b.rdf",
                "http://Mymodel~_~v1#","http://Mymodel~_~v2#",
                pathResults+"SuperClasses4_Delta_explicit_added.trig",
                pathResults+"SuperClasses4_Delta_explicit_deleted.trig",
                pathResults+"SuperClasses4_Delta_closure_added.trig",
                pathResults+"SuperClasses4_Delta_closure_deleted.trig",
                pathResults+"SuperClasses4_Delta_dense_added.trig",
                pathResults+"SuperClasses4_Delta_dense_deleted.trig"));*/
        //CIDOC
        pathFiles = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/cidoc/new/";
        pathResults = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/diff/results/";
        
        dataFiles.add(new Data("cidoc_v3.2.1-cidoc_v3.3.2",
                pathFiles+"cidoc_crm_v3.2.1.rdf",pathFiles+"cidoc_crm_v3.3.2.rdf",
                "http://cidoc#","http://cidoc#",
                pathResults+"Cidoc_3.2.1-3.3.2_explicit_AddedTriples.trig",
                pathResults+"Cidoc_3.2.1-3.3.2_explicit_DeletedTriples.trig",
                pathResults+"Cidoc_3.2.1-3.3.2_closure_AddedTriples.trig",
                pathResults+"Cidoc_3.2.1-3.3.2_closure_DeletedTriples.trig",
                pathResults+"Cidoc_3.2.1-3.3.2_dense_AddedTriples.trig",
                pathResults+"Cidoc_3.2.1-3.3.2_dense_DeletedTriples.trig"));

        dataFiles.add(new Data("cidoc_v3.3.2-cidoc_v3.4.9",
                pathFiles+"cidoc_crm_v3.3.2.rdf",pathFiles+"cidoc_crm_v3.4.9.rdf",
                "http://cidoc#","http://cidoc#",
                pathResults+"Cidoc_3.3.2-3.4.9_explicit_AddedTriples.trig",
                pathResults+"Cidoc_3.3.2-3.4.9_explicit_DeletedTriples.trig",
                pathResults+"Cidoc_3.3.2-3.4.9_closure_AddedTriples.trig",
                pathResults+"Cidoc_3.3.2-3.4.9_closure_DeletedTriples.trig",
                pathResults+"Cidoc_3.3.2-3.4.9_dense_AddedTriples.trig",
                pathResults+"Cidoc_3.3.2-3.4.9_dense_DeletedTriples.trig"));

        dataFiles.add(new Data("cidoc_v3.4.9-cidoc_v4.2",
                pathFiles+"cidoc_crm_v3.4.9.rdf",pathFiles+"cidoc_crm_v4.2.rdf",
                "http://cidoc#","http://cidoc#",
                pathResults+"Cidoc_3.4.9-4.2_explicit_AddedTriples.trig",
                pathResults+"Cidoc_3.4.9-4.2_explicit_DeletedTriples.trig",
                pathResults+"Cidoc_3.4.9-4.2_closure_AddedTriples.trig",
                pathResults+"Cidoc_3.4.9-4.2_closure_DeletedTriples.trig",
                pathResults+"Cidoc_3.4.9-4.2_dense_AddedTriples.trig",
                pathResults+"Cidoc_3.4.9-4.2_dense_DeletedTriples.trig"));

        //Go
        pathFiles = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/";
        pathResults = "/home/papavas/repository/SwkmTestSuite/trunk/main/data/diff/results/";

        dataFiles.add(new Data("GoData",pathFiles+"25-11-2008/go.rdf",pathFiles+"16-12-2008/go.rdf",
                "http://purl.uniprot.org/go.rdf#","http://purl.uniprot.org/go.rdf#",
                pathResults+"Go_25_11-16_12_explicit_AddedTriples.trig",
                pathResults+"Go_25_11-16_12_explicit_DeletedTriples.trig",
                pathResults+"Go_25_11-16_12_closure_AddedTriples.trig",
                pathResults+"Go_25_11-16_12_closure_DeletedTriples.trig",
                pathResults+"Go_25_11-16_12_dense_AddedTriples.trig",
                pathResults+"Go_25_11-16_12_dense_DeletedTriples.trig"));

       dataFiles.add(new Data("GoData",pathFiles+"16-12-2008/go.rdf",pathFiles+"24-3-2009/go.rdf",
                "http://purl.uniprot.org/go.rdf#","http://purl.uniprot.org/go.rdf#",
                pathResults+"Go_16_12-24_3_explicit_AddedTriples.trig",
                pathResults+"Go_16_12-24_3_explicit_DeletedTriples.trig",
                pathResults+"Go_16_12-24_3_closure_AddedTriples.trig",
                pathResults+"Go_16_12-24_3_closure_DeletedTriples.trig",
                pathResults+"Go_16_12-24_3_dense_AddedTriples.trig",
                pathResults+"Go_16_12-24_3_dense_DeletedTriples.trig"));
       dataFiles.add(new Data("GoData",pathFiles+"24-3-2009/go.rdf",pathFiles+"5-5-2009/go.rdf",
                "http://purl.uniprot.org/go.rdf#","http://purl.uniprot.org/go.rdf#",
                pathResults+"Go_24_3-5_5_explicit_AddedTriples.trig",
                pathResults+"Go_24_3-5_5_explicit_DeletedTriples.trig",
                pathResults+"Go_24_3-5_5_closure_AddedTriples.trig",
                pathResults+"Go_24_3-5_5_closure_DeletedTriples.trig",
                pathResults+"Go_24_3-5_5_dense_AddedTriples.trig",
                pathResults+"Go_24_3-5_5_dense_DeletedTriples.trig"));

       dataFiles.add(new Data("GoData",pathFiles+"5-5-2009/go.rdf",pathFiles+"26-5-2009/go.rdf",
                "http://purl.uniprot.org/go.rdf#","http://purl.uniprot.org/go.rdf#",
                pathResults+"Go_5_5-26_5_explicit_AddedTriples.trig",
                pathResults+"Go_5_5-26_5_explicit_DeletedTriples.trig",
                pathResults+"Go_5_5-26_5_closure_AddedTriples.trig",
                pathResults+"Go_5_5-26_5_closure_DeletedTriples.trig",
                pathResults+"Go_5_5-26_5_dense_AddedTriples.trig",
                pathResults+"Go_5_5-26_5_dense_DeletedTriples.trig"));
    }

}






