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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.forth.ics.swkm.model2.importer;

import gr.forth.ics.rdfsuite.services.RdfDocument;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import gr.forth.ics.rdfsuite.services.util.IOUtils;
import java.io.File;
/**
 *
 * @author elena
 */
public class TestUtils {

    public static Model createModel(String docUri, String path) throws IOException {

        Model model = ModelBuilder.newSparse().build();
        String ext = path.substring(path.lastIndexOf('.') + 1, path.length());

        RdfDocument doc = null;
        if (ext.equalsIgnoreCase("trig"))
        {
            doc = new RdfDocument(docUri,
                IOUtils.readFileAsString(new File(path)),
                gr.forth.ics.rdfsuite.services.Format.TRIG);
            RdfIO.read(doc.getContent(), Format.TRIG).withBase(doc.getURI()).into(model);
        }
        else if (ext.equalsIgnoreCase("rdf"))
        {
            doc = new RdfDocument(docUri,
                IOUtils.readFileAsString(new File(path)),
                gr.forth.ics.rdfsuite.services.Format.RDF_XML);
            RdfIO.read(doc.getContent(), Format.RDFXML).withBase(doc.getURI()).into(model);
        }
        
        return model;
    }

    public static Model createModelMultipleFiles(String[][] docs) throws IOException {

        Model model = ModelBuilder.newSparse().build();
        for (int i = 0; i < docs.length; i++) {
            String path = docs[i][1];
            String docUri = docs[i][0];
            System.out.println(docs[i][0]);
            String ext = path.substring(path.lastIndexOf('.') + 1, path.length());

            RdfDocument doc = null;
            if (ext.equalsIgnoreCase("trig"))
            {
                doc = new RdfDocument(docUri,
                    IOUtils.readFileAsString(new File(path)),
                    gr.forth.ics.rdfsuite.services.Format.TRIG);
                RdfIO.read(doc.getContent(), Format.TRIG).withBase(doc.getURI()).into(model);
            }
            else if (ext.equalsIgnoreCase("rdf"))
            {
                doc = new RdfDocument(docUri,
                    IOUtils.readFileAsString(new File(path)),
                    gr.forth.ics.rdfsuite.services.Format.RDF_XML);
                RdfIO.read(doc.getContent(), Format.RDFXML).withBase(doc.getURI()).into(model);
            }
        }

        return model;
    }

    public static int[][] checkClassInsertion(String node1) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0, att4 FROM t1000000000 WHERE att2 = '%s'", node1));
        int[][] map = new int[1][2];
        try {
            if (rs.next())
            {
                map[0][0] = rs.getInt(1);
                map[0][1] = rs.getInt(2);
            }
            else
            {
                map[0][0] = 0;
                map[0][1] = 0;
            }
        }
        finally
        {
            rs.close();
        }
        return map;
    }

    public static int[][] checkMetaClassInsertion(String node1) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0, att3 FROM metaclass WHERE att2 = '%s'", node1));
        int[][] map = new int[1][2];
        try {
            if (rs.next())
            {
                map[0][0] = rs.getInt(1);
                map[0][1] = rs.getInt(2);
            }
            else
            {
                map[0][0] = 0;
                map[0][1] = 0;
            }
        }
        finally
        {
            rs.close();
        }
        return map;
    }

    public static int[][] checkPropertyInsertion(String node1) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0, att9 FROM t2000000000 WHERE att2 = '%s'", node1));
        int[][] map = new int[1][2];
        try {
            if (rs.next())
            {
                map[0][0] = rs.getInt(1);
                map[0][1] = rs.getInt(2);
            }
            else
            {
                map[0][0] = 0;
                map[0][1] = 0;
            }
        }
        finally
        {
            rs.close();
        }
        return map;
    }

    public static int checkExistenceRelationshipDB(String tablename, int id, int superId) throws SQLException
    {
        String requestedTripleColumn = "att3";
        if (tablename.equals("subclass") || tablename.equals("subproperty") || tablename.equals("submetaclass") || tablename.equals("submetaproperty"))
            requestedTripleColumn = "att3";
        else if (tablename.equals("class_anc") || tablename.equals("property_anc"))
            requestedTripleColumn = "att4";
        ResultSet rs = Jdbc.query(String.format("SELECT %s FROM %s WHERE att0 = %d and att1 = %d", requestedTripleColumn, tablename, id, superId));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceNamespaceDB(String namespace) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0 FROM namespace WHERE att1 = '%s'", namespace));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceNamedGraphDB(String namedgraph) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0 FROM namedgraph WHERE att1 = '%s'", namedgraph));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static boolean checkExistenceGraphsetDB(int namedGraphId) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM graphset WHERE att1 = ARRAY[%d]", namedGraphId));
        try {
            return rs.next();
        }
        finally
        {
            rs.close();
        }
    }

    public static boolean checkExistenceAssociationTripleNamedGraphDB(int tripleId) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att0 FROM graphtriples WHERE att1 = %d", tripleId));
        try {
            return rs.next();
        }
        finally
        {
            rs.close();
        }
    }

    public static boolean checkExistenceNamespaceDependencyDB(int namespaceId, String dependencyUri) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM namespace_dependencies nsd WHERE nsd.att0 = %d AND nsd.att1 = (SELECT att0 FROM namespace WHERE att1 = '%s')", namespaceId, dependencyUri));
        try {
            return rs.next();
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceResourceDB(int classId, String resource) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM tc2000000000 WHERE att0 = '%s' AND att1 = %d", resource, classId));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceClassCommentDB(int classId, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM classcomment WHERE att0 = %d AND att1 = '%s'", classId, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistencePropertyCommentDB(int propertyId, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM propertycomment WHERE att0 = %d AND att1 = '%s'", propertyId, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceResourceCommentDB(String resource, String comment) throws SQLException
    {
       ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM resourcecomment WHERE att0 = '%s' AND att1 = '%s'", resource, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceClassIsDefinedByDB(int classId, int classId2) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM classisdefinedby WHERE att0 = %d AND att1 = %d", classId, classId2));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistencePropertyIsDefinedByDB(int propertyId, int propertyId2) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM propertyisdefinedby WHERE att0 = %d AND att1 = %d", propertyId, propertyId2));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceResourceIsDefinedByDB(String resource, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM resourceisdefinedby WHERE att0 = '%s' AND att1 = '%s'", resource, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceClassLabelDB(int classId, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM classlabel WHERE att0 = %d AND att1 = '%s'", classId, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistencePropertyLabelDB(int propertyId, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM propertylabel WHERE att0 = %d AND att1 = '%s'", propertyId, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceResourceLabelDB(String resource, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM resourcelabel WHERE att0 = '%s' AND att1 = '%s'", resource, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceClassSeeAlsoDB(int classId, int classId2) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM classseealso WHERE att0 = %d AND att1 = %d", classId, classId2));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistencePropertySeeAlsoDB(int propertyId, int propertyId2) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM propertyseealso WHERE att0 = %d AND att1 = %d", propertyId, propertyId2));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }

    public static int checkExistenceResourceSeeAlsoDB(String resource, String comment) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT att2 FROM resourceseealso WHERE att0 = '%s' AND att1 = '%s'", resource, comment));
        try {
            if (rs.next())
                return rs.getInt(1);
            else
                return 0;
        }
        finally
        {
            rs.close();
        }
    }
}
