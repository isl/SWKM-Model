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


package gr.forth.ics.swkm.model2.importer;

import gr.forth.ics.dbdiff.Column;
import gr.forth.ics.dbdiff.DeltaReporter;
import gr.forth.ics.dbdiff.DeltaReporter.In;
import gr.forth.ics.dbdiff.ForeignKey;
import gr.forth.ics.dbdiff.Index;
import gr.forth.ics.dbdiff.Row;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class DeltaReporterImpl implements DeltaReporter {
    private final Map<In, StringBuilder> errors = new EnumMap<In, StringBuilder>(In.class);

    {
        errors.put(In.FIRST, new StringBuilder());
        errors.put(In.SECOND, new StringBuilder());
    }
    private final StringBuilder sb = new StringBuilder();

    public void startReport() {
        //do nothing
    }
    
    public void startTable(String arg0) {
        //do nothing
    }
    
    public void endTable(String arg0) {
        //do nothing
    }
    
    public void endReport() {
        //do nothing
    }
    
    public void extraColumn(String table, Column column, In where) {
        errors.get(where).append("Extra column: " + column + " in table '" +
                table + "'\n");
    }

    public void extraForeignKey(String table, ForeignKey foreignKey, In where) {
        errors.get(where).append("Extra foreign key: " + foreignKey +
                " in table '" + table + "'\n");
    }

    public void extraIndex(String table, Index index, In where) {
        errors.get(where).append("Extra index: " + index + "\n");
    }

    public void extraRow(String table, Row row, In where) {
        errors.get(where).append("Extra row: " + row + " in table '" + table +
                "'\n");
    }

    public void extraTable(String table, In where) {
        errors.get(where).append("Extra table: " + table + "\n");
    }

    public void validate() {
        if (errors.get(In.FIRST).length() > 0 || errors.get(In.FIRST).length() >
                0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Databases not equal.\nExtra features of first database:\n").
                    append(errors.get(In.FIRST)).append("\n#####################\n" +
                    "Extra features of second database:\n").append(errors.get(In.SECOND));
            throw new AssertionError(sb.toString());
        }
    }
}
