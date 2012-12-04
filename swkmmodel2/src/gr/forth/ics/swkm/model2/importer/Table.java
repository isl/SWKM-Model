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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
abstract class Table {
    private final String name;
    private final List<Attribute> attributes = new ArrayList<Attribute>(4);
    private final List<String> constraints = new ArrayList<String>(1);

    private final String subTableName;
    
    public Table(String name) {
        this(name, null);
    }
    
    public Table(String name, String subTableName) {
        this.name = name;
        this.subTableName = subTableName;
    }

    public String getName() {
        return name;
    }

    //can return null
    public String getSuperTableName() {
        return subTableName;
    }
    
    protected Attribute newAttribute(String name, String sqlDefinition) {
        Attribute attr = new Attribute(name, sqlDefinition);
        attributes.add(attr);
        return attr;
    }
    
    protected Attribute newAttribute(String name, String sqlDefinitionWithParams, Object... params) {
        Attribute attr = new Attribute(name, String.format(sqlDefinitionWithParams, params));
        attributes.add(attr);
        return attr;
    }
    
    protected void newConstraint(String constraint) {
        Assert.notNull(constraint);
        constraints.add(constraint);
    }
    
    public Table createTable() throws SQLException {
        Jdbc.execute(getCreateTableSql());
        return this;
    }
    
    protected void insert(Attribute[] attrs, Object[] values) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getName());
        if (attrs.length > 0) {
            sb.append(" (").
            append(StringUtils.arrayToCommaDelimitedString(attrs)).
            append(")");
        }
        sb.append(" VALUES (");
        {
            boolean hasPrevious = false;
            for (Object value : values) {
                if (hasPrevious) {
                    sb.append(", ");
                }
                //Avoid writing "NULL" as a valid string value, when NULL literal is meant
                if (value == null) {
                    sb.append("NULL");
                } else {
                    sb.append("'").append(String.valueOf(value)).append("'");
                }
                hasPrevious = true;
            }
        }
        sb.append(")");
        Jdbc.execute(sb.toString());
    }
    
    public Table createIndex(String indexName, boolean clustered, Attribute... attrs) throws SQLException {
        Jdbc.execute(String.format("CREATE INDEX %s on %s (%s)",
                indexName, getName(), Joiner.on(",").join(attrs)));
        if (clustered) {
            Jdbc.execute(String.format("CLUSTER %s on %s", indexName, getName()));
        }
        return this;
    }
    
    protected String getCreateTableSql() {
        if (attributes.isEmpty()) {
            throw new IllegalStateException("Cannot define CREATE TABLE sql with zero attributes");
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("CREATE TABLE ").append(getName())
        .append(" (");
        boolean hasPrevious = false;
        for (Attribute attr : attributes) {
            if (hasPrevious) {
                sb.append(", ");
            }
            attr.renderToSql(sb);
            hasPrevious = true;
        }
        for (String constraint : constraints) {
            sb.append(", CONSTRAINT ").append(constraint);
        }
        sb.append(")");
        if (subTableName != null) {
            sb.append(" INHERITS (").append(subTableName).append(")");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public Values insert(Attribute ... attributes) {
        return new Values(attributes);
    }

    public Table indexOn(String indexName, Attribute... attrs) throws SQLException {
        createIndex(indexName, false, attrs);
        return this;
    }

    public Table clusteredIndexOn(String indexName, Attribute... attrs) throws SQLException {
        createIndex(indexName, true, attrs);
        return this;
    }
    
    public class Values {
        private final Attribute[] attrs;
        private Values(Attribute ... attrs) {
            this.attrs = attrs;
        }
        
        public Table values(Object ... values) throws SQLException {
            Table.this.insert(attrs, values);
            return Table.this;
        }
    }
    
    protected static void createAllTablesDeclaredAsFields(Object instanceWithTablesAsFields)
            throws SQLException {
        //Since there is at least a case of table inheritance, this imposes ordering problem in
        //table creation, and I don't want to depend on the order of getDeclaredFilds() (not portable across JVMs)
        Set<Table> tables = Sets.newLinkedHashSet();
        for (Field f : instanceWithTablesAsFields.getClass().getDeclaredFields()) {
            if (Table.class.isAssignableFrom(f.getType())) {
                try {
                    f.setAccessible(true);
                    tables.add((Table)f.get(instanceWithTablesAsFields));
                } catch (IllegalAccessException e) {
                    throw (AssertionError)new AssertionError().initCause(e);
                } finally {
                    f.setAccessible(false);
                }
            }
        }

        Set<String> alreadyLoaded = Sets.newHashSet(); //note: all lowercase
        while (!tables.isEmpty()) { //the following loop is quite fast due to the hash set being linked
            Table table = null;
            for (Table next : tables) {
                if (next.subTableName == null || alreadyLoaded.contains(next.getSuperTableName().toLowerCase())) {
                    table = next;
                    break;
                }
            }
            if (table == null) {
                throw new AssertionError("Cyclic inheritance in tables!");
            }

            try {
                table.createTable();
                alreadyLoaded.add(table.getName().toLowerCase());
                tables.remove(table);
            } catch (SQLException e) {
                throw (SQLException)new SQLException("Error while creating table: '" + table.getName() + "'").initCause(e);
            }
        }
    }
}
