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

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.importer.DbSynchronizer.UpdatedLabels;
import gr.forth.ics.swkm.model2.labels.Interval;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class VerticalPartitioningStore extends AbstractStore {

    private final SchemaSpecificTables schemaSpecificTables;

    public VerticalPartitioningStore(DataSource dataSource, Configurer conf) {
        super(RdfStores.Representation.VERTICAL_PARTITIONING, dataSource, conf);
        this.schemaSpecificTables = new SchemaSpecificTables(conf);
    }

    private class SchemaSpecificTables {

        private final Classes classes;
        private final Property property;
        private final DataProperty dataProperty;
        private final Resource resource;

        SchemaSpecificTables(Configurer conf) {
            this.classes = new Classes(conf);
            this.property = new Property(conf);
            this.dataProperty = new DataProperty(conf);
            this.resource = new Resource(conf);
        }

        private void createTablePerClassField() throws SQLException {
            Table.createAllTablesDeclaredAsFields(this);
        }

        private void createRepresentationTables() throws SQLException {
            classes.insert(classes.att0, classes.att1, classes.att2).values(DbConstants.getIdFor(RdfSchema.RESOURCE), 2, "Resource");
        }

        private void createRepresentationIndexes() throws SQLException {
            if (conf.createIndexes()) {
                resource.indexOn("idx" + DbConstants.getIdFor(RdfSchema.RESOURCE) + "_uri", resource.att0);
                property.indexOn("p_uri_idx", property.localName);
                dataProperty.indexOn("dp_uri_idx", dataProperty.localName);
            }
        }
    }

    @Override
    protected void initializeRepresentation() throws SQLException {
        schemaSpecificTables.createTablePerClassField();
        schemaSpecificTables.createRepresentationTables();
        schemaSpecificTables.createRepresentationIndexes();
    }

    private static class Classes extends Table {

        final Attribute att0; //TODO: clarify attribute
        final Attribute att1; //TODO: clarify attribute
        final Attribute att2; //TODO: clarify attribute
        final Attribute att3; //TODO: clarify attribute

        Classes(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSchema.CLASS));
            att0 = newAttribute("att0", "INTEGER PRIMARY KEY");
            att1 = newAttribute("att1", "INTEGER NOT NULL");
            att2 = newAttribute("att2", "VARCHAR(%d) NOT NULL", conf.getMaxLocalPartLength());
            att3 = newAttribute("att3", "INTEGER");
        }
    }

    private static class Property extends Table {

        final Attribute postId;
        final Attribute namespaceId;
        final Attribute localName;
        final Attribute domainId;
        final Attribute rangeId;
        final Attribute domainKind;
        final Attribute rangeKind;
        final Attribute att7;
        final Attribute att8;
        final Attribute att9;

        Property(Configurer conf) {
            super("t" + DbConstants.getIdFor(Rdf.PROPERTY));
            postId = newAttribute("att0", "INTEGER PRIMARY KEY");
            namespaceId = newAttribute("att1", "INTEGER NOT NULL");
            localName = newAttribute("att2", "VARCHAR(%d) NOT NULL", conf.getMaxLocalPartLength());
            domainId = newAttribute("att3", "INTEGER NOT NULL");
            rangeId = newAttribute("att4", "INTEGER NOT NULL");
            domainKind = newAttribute("att5", "INTEGER NOT NULL");
            rangeKind = newAttribute("att6", "INTEGER NOT NULL");
            att7 = newAttribute("att7", "INTEGER");
            att8 = newAttribute("att8", "INTEGER");
            att9 = newAttribute("att9", "INTEGER");
        }
    }

    private static class DataProperty extends Table {

        final Attribute postId;
        final Attribute namespaceId;
        final Attribute localName;
        final Attribute domainId;
        final Attribute rangeId;
        final Attribute domainKind;
        final Attribute rangeKind;
        final Attribute att7;
        final Attribute att8;
        final Attribute att9;

        DataProperty(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSuite.DATA_PROPERTY));
            postId = newAttribute("att0", "INTEGER PRIMARY KEY");
            namespaceId = newAttribute("att1", "INTEGER NOT NULL");
            localName = newAttribute("att2", "VARCHAR(%d) NOT NULL", conf.getMaxLiteralLength());
            domainId = newAttribute("att3", "INTEGER NOT NULL");
            rangeId = newAttribute("att4", "INTEGER NOT NULL");
            domainKind = newAttribute("att5", "INTEGER NOT NULL");
            rangeKind = newAttribute("att6", "INTEGER NOT NULL");
            att7 = newAttribute("att7", "INTEGER");
            att8 = newAttribute("att8", "INTEGER");
            att9 = newAttribute("att9", "INTEGER");
        }
    }

    private static class Resource extends Table {

        private final Attribute att0; //TODO: clarify attribute
        private final Attribute att1; //TODO: clarify attribute

        Resource(Configurer conf) {
            super("tc" + DbConstants.getIdFor(RdfSchema.RESOURCE));
            att0 = newAttribute("att0", "VARCHAR(%d)", conf.getMaxUriLength());
            att1 = newAttribute("att1", "INTEGER");
        }
    }

    protected void updateInstanceTable(Map<Interval, Interval> oldIntervalToNew, RdfType type) throws SQLException {
        String tab=null;
        if (type == RdfType.CLASS) {
            tab = "tc";
        } else {
            tab = "tp";
        }

        StringBuilder sb = new StringBuilder();
        for (Entry<Interval, Interval> e : oldIntervalToNew.entrySet()) {
            Interval oldInterval = e.getKey();
            Interval newInterval = e.getValue();
            sb.append("alter table ");
            sb.append(tab);
            sb.append(oldInterval.getPost());
            sb.append(" rename to ");
            sb.append(tab);
            sb.append(newInterval.getPost());
            sb.append(";");
        }
        jdbc.execute(sb.toString());
    }

//    @Override protected void storeClasses(ImportContext context) throws SQLException {
//        throw new UnsupportedOperationException();
//    }
}