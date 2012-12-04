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

import gr.forth.ics.swkm.model2.diff.TextualTriple;
import gr.forth.ics.swkm.model2.diff.DiffFunctions;
import gr.forth.ics.swkm.model2.diff.Delta;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.validation.Validator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Papavasileiou Vicky
 */
public class SameFiles extends TestCase {
    private String version1, version2,ns1,ns2;
    private Delta explicitExpected, closureExpected;
    private String testCase;

    public SameFiles(String test, String testCase, String version1, String version2,
            String ns1,String ns2,Delta explicitDelta, Delta closureDelta) {
        super(test);
        this.version1 = version1;
        this.version2 = version2;
        this.ns1 = ns1;
        this.ns2 = ns2;
        this.explicitExpected = explicitDelta;
        this.closureExpected = closureDelta;
        this.testCase = testCase;
    }

    public static TestSuite suite() throws FileNotFoundException, IOException {
        TestSuite suite = new TestSuite();

        for (DiffCase diffCase : DiffCases.fromFile()) {
            suite.addTest(new SameFiles("testSameFiles", diffCase.getName(),
                    diffCase.getVersion1(), diffCase.getVersion1(),
                    diffCase.getNs1(),diffCase.getNs1(),
                    null,null));
            suite.addTest(new SameFiles("testSameFiles", diffCase.getName(),
                    diffCase.getVersion2(), diffCase.getVersion2(),
                    diffCase.getNs2(),diffCase.getNs2(),
                    null,null));
        }       

        return suite;
    }

    public void testSameFiles() throws FileNotFoundException, IOException {
        setName("testSameFiles"+testCase);
        Model model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File(this.version1),Format.RDFXML).
                withBase(this.ns1).into(model1);
        Validator.defaultValidator().validateAndFailOnFirstError(model1);

        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File(this.version2),Format.RDFXML).
                withBase(this.ns2).into(model2);
        Validator.defaultValidator().validateAndFailOnFirstError(model2);

        Delta explicitActual = DiffFunctions.explicit().diff(model1, model2);
        DiffComparisons.assertEqual("added",explicitActual.getAddedSet(), new HashSet<TextualTriple>());
        DiffComparisons.assertEqual("deleted",explicitActual.getDeletedSet(), new HashSet<TextualTriple>());

        Delta closureActual = DiffFunctions.closure().diff(model1, model2);
        DiffComparisons.assertEqual("addedClosure", closureActual.getAddedSet(), new HashSet<TextualTriple>());
        DiffComparisons.assertEqual("deletedClosure", closureActual.getDeletedSet(), new HashSet<TextualTriple>());

    }
}
