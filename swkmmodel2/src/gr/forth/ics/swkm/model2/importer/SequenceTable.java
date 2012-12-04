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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class SequenceTable extends Table {
    private final int startIndex;
    
    public SequenceTable(String name, int startIndex) {
        super(name);
        this.startIndex = startIndex;
    }
    
    @Override
    public SequenceTable createTable() throws SQLException {
        Jdbc.execute(String.format("CREATE SEQUENCE %s START %d", getName(), startIndex));
        return this;
    }

    public int nextValue() throws SQLException {
        ResultSet rs = Jdbc.query(String.format("SELECT nextval('%s')", getName()));
        try {
            rs.next();
            return rs.getInt(1);
        } finally {
            rs.close();
        }
    }


    class SequenceHelper {
        private final PreparedStatement ps;

        SequenceHelper() throws SQLException {
            ps = Jdbc.prepared("SELECT nextval('" + getName() + "')");
        }

        int next() throws SQLException {
            ResultSet rs = ps.executeQuery();
            try {
                rs.next();
                return rs.getInt(1);
            } finally {
                rs.close();
            }
        }
    }
}
