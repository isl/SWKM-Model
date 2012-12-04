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


import gr.forth.ics.swkm.model2.importer.*;
import gr.forth.ics.dbdiff.DbDiff;
import gr.forth.ics.rdfsuite.rssdb.repr.DBRepresentation;
import gr.forth.ics.rdfsuite.rssdb.repr.HybridRepresentation;
import gr.forth.ics.rdfsuite.rssdb.repr.SSRepresentation;
import gr.forth.ics.rdfsuite.rssdb.repr.SSWSRepresentation;
import gr.forth.ics.rdfsuite.services.db.PostgreSqlUtils;
import gr.forth.ics.rdfsuite.swkm.model.db.impl.RDFDB_Model;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Database {
    public static final Database db1 = new Database("andreoudb1");
    public static final Database db2 = new Database("swkmdb");
    
    private static final String user = "postgres";
    private static final String pass = "swt3ch";
    
    private final String dbName;
    
    private Database(String dbName) {
        this.dbName = dbName;
    }
    
    @Override
    public String toString() {
        return dbName;
    }
    
    static {
        PostgreSqlUtils.loadDriver();
    }
    
    public void erase() {
        try {
            PostgreSqlUtils.eraseDatabase(getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:postgresql://127.0.0.1/" + dbName, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public DataSource getDataSource() {
        return new DriverManagerDataSource(
                "jdbc:postgresql://127.0.0.1/" + dbName, user, pass);
    }
    
    public RDFDB_Model newModel(RdfStores.Representation repr) {
        try {
            RDFDB_Model model = new RDFDB_Model("127.0.0.1/" + dbName,
                    user, pass, repr.getCode(), true);
            model.getStorage_metadata().initResourceIDs();
            model.import_default_namespaces();
            model.import_rdfsuite_namespace();
            model.import_xmlschema_namespace();
            return model;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public DBRepresentation newDbRepresentation(RdfStores.Representation repr) {
        RDFDB_Model model = newModel(repr);
        model.createLabels();
        switch (repr) {
            case VERTICAL_PARTITIONING:
                return new SSRepresentation(model);
            case HYBRID:
                return new HybridRepresentation(model);
            default: throw new AssertionError();
        }
    }
    
    public static void releaseResources(DBRepresentation repr) {
        try {
            Field field = DBRepresentation.class.getDeclaredField("model");
            field.setAccessible(true);
            RDFDB_Model model = (RDFDB_Model)field.get(repr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void assertEqual(Database database1, Database database2) {
        Connection c1 = database1.getConnection();
        Connection c2 = database2.getConnection();
        DbDiff diff = DbDiff.forPostgres();
        DeltaReporterImpl reporter = new DeltaReporterImpl();
        try {
            diff.compare(c1, c2, reporter);
            reporter.validate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                c1.close();
                c2.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
