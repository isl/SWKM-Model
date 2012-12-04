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
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.validation.Validator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author papavas
 */
public class CorrectDelta extends TestCase {
    private String version1, version2,ns1,ns2;
    private Delta explicitExpected, closureExpected;
    private Delta explicitDenseExpected, closureDenseExpected;
    private String testCase;

    public CorrectDelta(String test, String testCase, String version1, String version2,
            String ns1,String ns2,Delta explicitDelta, Delta closureDelta,Delta explicitDenseDelta, Delta closureDenseDelta) {
        super(test);
        this.version1 = version1;
        this.version2 = version2;
        this.ns1 = ns1;
        this.ns2 = ns2;
        this.explicitExpected = explicitDelta;
        this.closureExpected = closureDelta;
        this.explicitDenseExpected = explicitDenseDelta;
        this.closureDenseExpected = closureDenseDelta;
        this.testCase = testCase;
    }

    public static TestSuite suite() throws FileNotFoundException, IOException {
        TestSuite suite = new TestSuite();

        for (DiffCase diffCase : DiffCases.fromFile()) {
            suite.addTest(new CorrectDelta("testCorrectDelta", diffCase.getName(),
                diffCase.getVersion1(), diffCase.getVersion2(),
                diffCase.getNs1(),diffCase.getNs2(),
                diffCase.getExplicitDelta(),diffCase.getClosureDelta(),
                diffCase.getExplicitDenseDelta(),diffCase.getClosureDenseDelta()));
        }

        return suite;
    }

    public void testCorrectDelta() throws FileNotFoundException, IOException {

        setName("testCorrectDelta"+testCase);
        Model model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File(this.version1),Format.RDFXML).
                withBase(this.ns1).into(model1);
        if(testCase.contains("Go")){
                RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                    Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model1);
        }
        Validator.defaultValidator().validateAndFailOnFirstError(model1);
        System.out.println("Triples of first model: " +model1.tripleCount());

        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File(this.version2),Format.RDFXML).
                withBase(this.ns2).into(model2);
        if(testCase.contains("Go")){
                RdfIO.read(new File("/home/papavas/repository/SwkmTestSuite/trunk/main/data/go/25-11-2008/core.rdfs"),
                    Format.RDFXML).withBase("http://purl.uniprot.org/core.rdfs#").into(model2);
        }
        Validator.defaultValidator().validateAndFailOnFirstError(model2);
        System.out.println("Triples of second model: " +model2.tripleCount());

        Delta explicitActual = DiffFunctions.explicit().diff(model1, model2);
        Delta diff = DeltaUtils.difference(explicitActual, this.explicitExpected);
        assertTrue(diff.getAddedSet().isEmpty());
        assertTrue(diff.getDeletedSet().isEmpty());

        Delta closureActual = DiffFunctions.closure().diff(model1, model2);
        diff = DeltaUtils.difference(closureActual, this.closureExpected);
        assertTrue(diff.getAddedSet().isEmpty());
        assertTrue(diff.getDeletedSet().isEmpty());

        Delta explicitDenseActual = DiffFunctions.explicit_dense().diff(model1, model2);
        diff = DeltaUtils.difference(explicitDenseActual, this.explicitDenseExpected);
        assertTrue(diff.getAddedSet().isEmpty());
        assertTrue(diff.getDeletedSet().isEmpty());

        Delta closureDenseActual = DiffFunctions.dense_closure().diff(model1, model2);
        diff = DeltaUtils.difference(closureDenseActual, this.closureDenseExpected);
        assertTrue(diff.getAddedSet().isEmpty());
        assertTrue(diff.getDeletedSet().isEmpty());
    }


}