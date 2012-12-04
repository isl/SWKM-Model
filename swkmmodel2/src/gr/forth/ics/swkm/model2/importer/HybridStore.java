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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import gr.forth.ics.swkm.model2.util.XMLDateTime;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.ResultSet;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triples;
import gr.forth.ics.swkm.model2.importer.SequenceTable.SequenceHelper;
import gr.forth.ics.swkm.model2.labels.Interval;
import gr.forth.ics.swkm.model2.labels.Label;
import gr.forth.ics.swkm.model2.ObjectNode;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class HybridStore extends AbstractStore {
    private static final String ALL_PROPERTIES_TABLE = "t" + DbConstants.getIdFor(Rdf.PROPERTY);
    private static final String DATA_PROPERTIES_TABLE = "t" + DbConstants.getIdFor(RdfSuite.DATA_PROPERTY);

    private final HybridTables hybridTables;
    
    public HybridStore(DataSource dataSource, Configurer conf) {
        super(RdfStores.Representation.HYBRID, dataSource, conf);
        this.hybridTables = new HybridTables(conf);
    }

    private class HybridTables {
        private final Classes classes;
        private final AllProperties property;
        private final DataProperties dataProperty;
        private final ResourceTable resource;
        
        HybridTables(Configurer conf) {
            this.classes = new Classes(conf);
            this.property = new AllProperties(conf);
            this.dataProperty = new DataProperties(conf);
            this.resource = new ResourceTable(conf);
        }
        
        private void createHybridTables() throws SQLException {
            Table.createAllTablesDeclaredAsFields(this);
            classes
                    .insert(classes.classPostId, classes.namespaceId, classes.localName, classes.metaclassPostId)
                    .values(DbConstants.getIdFor(RdfSchema.RESOURCE), 2, "Resource", DbConstants.getIdFor(RdfSchema.CLASS));
        }
        
        private void createRepresentationTables() throws SQLException {
            createAllPropertyTables();
        }

        private void createAllPropertyTables() throws SQLException{
            {
                String sqlType = "VARCHAR(" + conf.getMaxUriLength() + ")";
                int[] domains = {
                    DbConstants.getRqlKindFor(RdfSchema.RESOURCE),
                    /*DbConstants.getRqlIdFor(RdfSuite.GRAPH),*/
                };
                for (int domain : domains) {
                    createTables(domain, sqlType);
                }
            }
            {
                String sqlType = "INTEGER";
                int[] domains = {
                    DbConstants.getRqlKindFor(RdfSchema.CLASS),
                    DbConstants.getRqlKindFor(Rdf.PROPERTY)
                };
                for (int domain : domains) {
                    createTables(domain, sqlType);
                }
            }
        }

        private void createTables(final int domain, final String domainType) throws SQLException {
            class PropertyGroup {
                PropertyGroup(String rangeType, int ... rangeKinds) throws SQLException {
                    createTables(domain, domainType, rangeKinds, rangeType);
                }
            }
            new PropertyGroup("VARCHAR(" + conf.getMaxUriLength() + ")",
                    DbConstants.getRqlKindFor(RdfSchema.RESOURCE),
                    DbConstants.getRqlKindFor(RdfSuite.ENUMERATION));

            new PropertyGroup("VARCHAR(" + conf.getMaxUriLength() + ")",
                    DbConstants.getRqlKindFor(RdfSuite.GRAPH));

            new PropertyGroup("INTEGER",
                    DbConstants.getRqlKindFor(RdfSchema.CLASS),
                    DbConstants.getRqlKindFor(Rdf.PROPERTY),
                    DbConstants.getRqlKindFor(RdfSuite.THESAURUS),
                    DbConstants.getRqlKindFor(XmlSchema.INTEGER),
                    DbConstants.getRqlKindFor(XmlSchema.NON_POSITIVE_INTEGER),
                    DbConstants.getRqlKindFor(XmlSchema.NEGATIVE_INTEGER),
                    DbConstants.getRqlKindFor(XmlSchema.INT),
                    DbConstants.getRqlKindFor(XmlSchema.NON_NEGATIVE_INTEGER),
                    DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_INT),
                    DbConstants.getRqlKindFor(XmlSchema.POSITIVE_INTEGER));

            new PropertyGroup("VARCHAR(" + conf.getMaxLiteralLength() + ")",
                    DbConstants.getRqlKindFor(XmlSchema.STRING),
                    DbConstants.getRqlKindFor(XmlSchema.HEX_BINARY),
                    DbConstants.getRqlKindFor(XmlSchema.ANY_URI),
                    DbConstants.getRqlKindFor(XmlSchema.QNAME),
                    DbConstants.getRqlKindFor(XmlSchema.NOTATION),
                    DbConstants.getRqlKindFor(XmlSchema.NORMALIZED_STRING),
                    DbConstants.getRqlKindFor(XmlSchema.TOKEN),
                    DbConstants.getRqlKindFor(XmlSchema.LANGUAGE),
                    DbConstants.getRqlKindFor(XmlSchema.NMTOKEN),
                    DbConstants.getRqlKindFor(XmlSchema.NMTOKENS),
                    DbConstants.getRqlKindFor(XmlSchema.NAME),
                    DbConstants.getRqlKindFor(XmlSchema.NCNAME),
                    DbConstants.getRqlKindFor(XmlSchema.ID),
                    DbConstants.getRqlKindFor(XmlSchema.IDREF),
                    DbConstants.getRqlKindFor(XmlSchema.IDREFS),
                    DbConstants.getRqlKindFor(XmlSchema.ENTITY),
                    DbConstants.getRqlKindFor(XmlSchema.ENTITIES));

            new PropertyGroup("date",
                    DbConstants.getRqlKindFor(XmlSchema.DATE),
                    DbConstants.getRqlKindFor(XmlSchema.GYEAR_MONTH),
                    DbConstants.getRqlKindFor(XmlSchema.GYEAR),
                    DbConstants.getRqlKindFor(XmlSchema.GMONTH_DAY),
                    DbConstants.getRqlKindFor(XmlSchema.GDAY),
                    DbConstants.getRqlKindFor(XmlSchema.GMONTH));

            new PropertyGroup("boolean",
                    DbConstants.getRqlKindFor(XmlSchema.BOOLEAN));

            new PropertyGroup("numeric",
                    DbConstants.getRqlKindFor(XmlSchema.DECIMAL));

            new PropertyGroup("real",
                    DbConstants.getRqlKindFor(XmlSchema.FLOAT));

            new PropertyGroup("float8",
                    DbConstants.getRqlKindFor(XmlSchema.DOUBLE));

            new PropertyGroup("interval",
                    DbConstants.getRqlKindFor(XmlSchema.DURATION));

            new PropertyGroup("timestamp with time zone",
                    DbConstants.getRqlKindFor(XmlSchema.DATETIME));

            new PropertyGroup("time with time zone",
                    DbConstants.getRqlKindFor(XmlSchema.TIME));

            new PropertyGroup("bytea",
                    DbConstants.getRqlKindFor(XmlSchema.BASE64_BINARY));

            new PropertyGroup("bigint",
                    DbConstants.getRqlKindFor(XmlSchema.LONG),
                    DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_LONG));

            new PropertyGroup("smallint",
                    DbConstants.getRqlKindFor(XmlSchema.SHORT),
                    DbConstants.getRqlKindFor(XmlSchema.BYTE),
                    DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_SHORT),
                    DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_BYTE));
        }

        private void createTables(int domain, String domainType,
                int[] rangeKinds, String rangeType) throws SQLException {
            for (int range : rangeKinds) {
                Jdbc.execute("CREATE TABLE tp%dk%d" +
                        "(att0 %s, att1 %s, att2 INTEGER, att3 INTEGER)",
                        domain, range, domainType, rangeType);
                if (conf.createIndexes()) {
                    Jdbc.execute("CREATE INDEX idx%1$dk%2$d_from on tp%1$dk%2$d (att0)", domain, range);
                    if (!rangeType.equalsIgnoreCase("bytea")) {
                        Jdbc.execute("CREATE INDEX idx%1$dk%2$d_to on tp%1$dk%2$d (att1)", domain, range);
                    }
                    Jdbc.execute("CREATE INDEX idx%1$dk%2$d_pid on tp%1$dk%2$d (att2)", domain, range);
                    Jdbc.execute("CLUSTER idx%1$dk%2$d_pid on tp%1$dk%2$d", domain, range);
                }
            }
        }

        private void createRepresentationIndexes() throws SQLException {
            if (conf.createIndexes()) {
                classes
                        .indexOn("cls_type_idx", classes.metaclassPostId)
                        .indexOn("cls_type_idx_on_local_name", classes.localName);
                
                resource
                        .clusteredIndexOn("Inst_uri_idx", resource.uri)
                        .indexOn("Inst_id_idx", resource.classId);
                
                property
                        .indexOn("p_id_idx", property.post)
                        .clusteredIndexOn("p_uri_idx", property.localPart)
                        .indexOn("p_type_idx", property.metapropertyPostId)
                        .indexOn("p_table_idx", property.propertyTableId);
                
                dataProperty
                        .indexOn("dp_id_idx", property.post)
                        .clusteredIndexOn("dp_uri_idx", property.localPart)
                        .indexOn("dp_type_idx", property.metapropertyPostId)
                        .indexOn("dp_table_idx", property.propertyTableId);
            }
        }
    }
    
    @Override
    protected void initializeRepresentation() throws SQLException {
        hybridTables.createHybridTables();
        hybridTables.createRepresentationTables();
        hybridTables.createRepresentationIndexes();
    }
    
    private static class Classes extends Table {
        final Attribute classPostId; //TODO: clarify attribute
        final Attribute namespaceId; //TODO: clarify attribute
        final Attribute localName; //TODO: clarify attribute
        final Attribute metaclassPostId; //TODO: clarify attribute
        final Attribute tripleId; //TODO: clarify attribute
        
        Classes(Configurer conf) {
            super("t" + DbConstants.getIdFor(RdfSchema.CLASS));
            classPostId = newAttribute("att0", "INTEGER");
            namespaceId = newAttribute("att1", "INTEGER NOT NULL");
            localName = newAttribute("att2", "VARCHAR(%d) NOT NULL", conf.getMaxLocalPartLength());
            metaclassPostId = newAttribute("att3", "INTEGER NOT NULL");
            tripleId = newAttribute("att4", "INTEGER");
            newConstraint("post_type_post PRIMARY KEY(att0, att3)");
        }

        void storeClasses(ImportContext context) throws SQLException {
             InsertHelper insertHelper = new InsertHelper(
                    "INSERT INTO " + getName() + " VALUES (?, ?, ?, ?, nextval('tripleid_generator'))",
                    "tripleid_generator");
            Map<Uri, Label> newLabels = context.updatedLabels.getNewLabels(RdfType.CLASS);
            for (Entry<Uri, Integer> newNamespaceEntry : context.namespaceIds.entrySet()) {
                Uri namespace = newNamespaceEntry.getKey();
                Integer nsId = newNamespaceEntry.getValue();
                for (Resource clazz : context.model.findSchemaNodes(namespace, RdfType.CLASS)) {
                    Uri uri = clazz.getUri();

                    Triples metaclassTriples = context.model.triples().s(clazz).p(Rdf.TYPE).fetch();
                    if (metaclassTriples.iterator().hasNext() == false) {
                        //if there is no metaclass above it, assume rdfs:Class
                        //adding a metaclass on the fly!
                        context.model.add().g(RdfSuite.IMPORTER_SIDE_EFFECTS)
                                .s(clazz).p(Rdf.TYPE).o(RdfSchema.CLASS);
                        context.model.add().g(RdfSuite.DEFAULT_GRAPH_URI)
                                .s(clazz).p(Rdf.TYPE).o(RdfSchema.CLASS);
                    }

                    for (Triple t : metaclassTriples) {
                        Resource metaclass = (Resource)t.object();
                        Uri metaclassUri = metaclass.getUri();
                        Integer classId = newLabels.get(uri).getTreeLabel().getPost();
                        int tripleId = insertHelper.executeAndGetId(classId, nsId, uri.getLocalName(),
                                context.updatedLabels.getLabel(RdfType.METACLASS, metaclassUri).getTreeLabel().getPost());
                        context.tripleIds.put(tripleId, t);
                    }
                }
            }
        }
    }

    private class PropertiesTable extends Table {
        final Attribute post;
        final Attribute nsId;
        final Attribute localPart;
        final Attribute domainId;
        final Attribute rangeId;
        final Attribute domainKind;
        final Attribute rangeKind;
        final Attribute metapropertyPostId;
        final Attribute propertyTableId;
        final Attribute propertyTripleId;
        final Attribute domainTripleId;
        final Attribute rangeTripleId;

        PropertiesTable(Configurer conf, String name, String superTableName) {
            super(name, superTableName);
            post = newAttribute("att0", "INTEGER PRIMARY KEY");
            nsId = newAttribute("att1", "INTEGER NOT NULL");
            localPart = newAttribute("att2", "VARCHAR(%d) NOT NULL", conf.getMaxLocalPartLength());
            domainId = newAttribute("att3", "INTEGER NOT NULL");
            rangeId = newAttribute("att4", "INTEGER NOT NULL");
            domainKind = newAttribute("att5", "INTEGER NOT NULL");
            rangeKind = newAttribute("att6", "INTEGER NOT NULL");
            metapropertyPostId = newAttribute("att7", "INTEGER NOT NULL");
            propertyTableId = newAttribute("att8", "VARCHAR(10) NOT NULL");
            propertyTripleId = newAttribute("att9", "INTEGER");
            domainTripleId = newAttribute("att10", "INTEGER");
            rangeTripleId = newAttribute("att11", "INTEGER");
        }

        void storeProperties(ImportContext context) throws SQLException {
            Map<Uri, Label> propertyLabels = context.updatedLabels.getNewLabels(RdfType.PROPERTY);
            SequenceHelper tripleIdHelper = commonTables.tripleIdGenerator.new SequenceHelper();
            for (Uri ns : context.namespaceIds.keySet()) {
                for (RdfNode propertyNode : context.model.findSchemaNodes(ns, RdfType.PROPERTY)) {
                    Resource property = (Resource)propertyNode;
                    Triples propertyTriples = context.model.triples().s(property).p(Rdf.TYPE).fetch();
                    if (propertyTriples.iterator().hasNext() == false) {
                        for (Uri namedGraph : new Uri[] { RdfSuite.IMPORTER_SIDE_EFFECTS, RdfSuite.DEFAULT_GRAPH_URI} ) {
                            context.model.add().g(namedGraph).s(property).p(Rdf.TYPE).o(Rdf.PROPERTY);
                        }
                    }

                    Triple domainTriple = Iterables.getOnlyElement(context.model.triples()
                            .s(property).p(RdfSchema.DOMAIN).fetch());
                    Triple rangeTriple = Iterables.getOnlyElement(context.model.triples()
                            .s(property).p(RdfSchema.RANGE).fetch());
                    Resource domain = (Resource)domainTriple.object();
                    Resource range = (Resource)rangeTriple.object();

                    //store data properties in t35 (i.e. with domain a class and range a class or literal))
                   //and all other properties in t2000000000.
                    final boolean isDataProperty = domain.type().isClass() &&
                            (range.type().isClass() || range.type().isXmlType() || range.is(RdfSchema.LITERAL));

                    String tableName = isDataProperty ? HybridStore.this.hybridTables.dataProperty.getName() :
                        HybridStore.this.hybridTables.property.getName();

                    PreparedStatement ps = Jdbc.prepared("INSERT INTO " + tableName
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    final Uri uri = property.getUri();

                    final int propertyPostId = propertyLabels.get(uri).getTreeLabel().getPost();
                    final int nsId = context.namespaceIds.get(uri.getNamespaceUri());
                    final String localName = uri.getLocalName();
                    final int domainPostId = context.updatedLabels.getLabel(domain.type(), domain.getUri())
                            .getTreeLabel().getPost();
                    final int rangePostId = context.updatedLabels.getLabel(range.type(), range.getUri())
                            .getTreeLabel().getPost();
                    final int domainKind = DbConstants.domainKind(domain);
                    final int rangeKind = DbConstants.rangeKind(range);

                    final int domainTripleId = tripleIdHelper.next();
                    context.tripleIds.put(domainTripleId, domainTriple);

                    final int rangeTripleId = tripleIdHelper.next();
                    context.tripleIds.put(rangeTripleId, rangeTriple);

                    for (Triple propertyTriple : propertyTriples) {
                        Resource metaproperty = (Resource)propertyTriple.object();
                        final int metapropertyPost = context.updatedLabels.getLabel(RdfType.METAPROPERTY,
                                metaproperty.getUri()).getTreeLabel().getPost();
                        final int propertyTripleId = tripleIdHelper.next();
                        context.tripleIds.put(propertyTripleId, propertyTriple);
                        ps.setInt(1, propertyPostId);
                        ps.setInt(2, nsId);
                        ps.setString(3, localName);
                        ps.setInt(4, domainPostId);
                        ps.setInt(5, rangePostId);
                        ps.setInt(6, domainKind);
                        ps.setInt(7, rangeKind);
                        ps.setInt(8, metapropertyPost);
                        ps.setString(9, DbConstants.getMagicDomain(domain) + "k" + DbConstants.getMagicRange(range));
                        ps.setInt(10, propertyTripleId);
                        ps.setInt(11, domainTripleId);
                        ps.setInt(12, rangeTripleId);

                        ps.execute();
                    }
                }
            }
        }
    }
    
    private class AllProperties extends PropertiesTable {
        AllProperties(Configurer conf) {
            super(conf, ALL_PROPERTIES_TABLE, null);
        }
    }
    
    private class DataProperties extends PropertiesTable {
        DataProperties(Configurer conf) {
            super(conf, DATA_PROPERTIES_TABLE, ALL_PROPERTIES_TABLE);
        }
    }
    
    private class ResourceTable extends Table {
        final Attribute uri;
        final Attribute classId;
        final Attribute tripleId;
        
        ResourceTable(Configurer conf) {
            super("tc" + DbConstants.getIdFor(RdfSchema.RESOURCE));
            uri = newAttribute("att0", "VARCHAR(%d) NOT NULL", conf.getMaxUriLength());
            classId = newAttribute("att1", "INTEGER NOT NULL");
            tripleId = newAttribute("att2", "INTEGER");
        }
    }

    @Override
    protected void storeClasses(ImportContext context) throws SQLException {
        hybridTables.classes.storeClasses(context);
    }
    @Override
    protected void storeProperties(ImportContext context) throws SQLException {
        //stores in both t2000000000 and t35 here (data properties, i.e. with domain a class))
        hybridTables.dataProperty.storeProperties(context);
    }
    @Override
    protected void storePropertyInstances(ImportContext context) throws SQLException {

        //depending on the domain and range of each property instance i have to insert this in the appropriate table.      
      for (Triple triple : context.model.triples().fetch())
      {
            if (!triple.predicate().getUri().getNamespaceUri().equals(Rdf.NAMESPACE) &&
                !triple.predicate().getUri().getNamespaceUri().equals(RdfSchema.NAMESPACE))
            {
                if (triple.subject().type().equals(RdfType.INDIVIDUAL)
                        || triple.subject().type().equals(RdfType.CLASS)
                        || triple.subject().type().equals(RdfType.PROPERTY))
                {
                    boolean boolSubjectIsIndividual = false;
                    if (triple.subject().type().equals(RdfType.INDIVIDUAL))
                        boolSubjectIsIndividual = true;
                    int subjectId = DbConstants.getTripleSubjectRQLKindId(triple.subject().type());
                    int objectId = 0;
                    
                    //i have to insert this triple in one of the tp7k* or tp2k* or tp3k*
                    // 7 for Individual, 2 for Class, 3 for Property
                    // according to the kind of range

                    Iterable rangeIterable = triple.predicate().asProperty().ranges();
                    RdfNode range = (RdfNode)rangeIterable.iterator().next();

                    System.out.println("storePropertyInstances: triple.subject = " + triple.subject().getIdentifier() +
                                        ", triple.subject.type = " + triple.subject().type().name() +
                                        ", triple.predicate = " + triple.predicate().getIdentifier() +
                                        ", triple.range = " + range.toString() +
                                        ", triple.range.type = " + range.type().name() +
                                        ", triple.object = " + triple.object().toString() +
                                        ", triple.object.class = " + triple.object().getClass().getSimpleName() +
                                        ", triple.object.type = " + triple.object().type().name());

                    if (triple.object().type().equals(RdfType.INDIVIDUAL))
                        objectId = DbConstants.getRqlKindFor(RdfSchema.RESOURCE);
                    else
                        if (range.type().equals(RdfType.METACLASS) && triple.object().type().equals(RdfType.LITERAL))
                            objectId = DbConstants.getRqlKindFor(XmlSchema.STRING);
                        else
                            objectId = DbConstants.getTripleRangeRQLKindId(range);
                    //objectId = getTripleRangeRQLKindId(triple.object());
                    // if this property instance exists in database (check the post id) we do not store this triple..
                    // the update if this property instance has changed is job of the update process
                    if (checkExistencePropertyInstanceDB(subjectId, objectId,
                            context.updatedLabels.getLabel(RdfType.PROPERTY, triple.predicate().getUri()).getTreeLabel().getPost()))
                        continue;

                    PreparedStatement pstmt = Jdbc.prepared(String.format("INSERT INTO tp%dk%d (att0, att1, att2, att3) VALUES (?, ?, ?, ?)", subjectId, objectId));
                    int newTripleId = this.commonTables.tripleIdGenerator.nextValue();
                    
                    if (boolSubjectIsIndividual)
                        pstmt.setString(1, triple.subject().isResource() ? ((Resource)triple.subject()).getUri().toString() : triple.subject().toString());
                    else
                        pstmt.setInt(1, context.updatedLabels.getLabel(triple.subject().type(), ((Resource)triple.subject()).getUri()).getTreeLabel().getPost());

                    if (objectId == DbConstants.getRqlKindFor(RdfSchema.RESOURCE) ||
                        objectId == DbConstants.getRqlKindFor(RdfSuite.GRAPH))
                                pstmt.setString(2, triple.object().isResource() ? ((Resource)triple.object()).getUri().toString() : triple.object().toString());
                    else if (objectId == DbConstants.getRqlKindFor(RdfSchema.CLASS) ||
                             objectId == DbConstants.getRqlKindFor(Rdf.PROPERTY))
                                pstmt.setInt(2, context.updatedLabels.getLabel(triple.object().type(),((Resource)triple.object()).getUri()).getTreeLabel().getPost());
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.INTEGER) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NON_POSITIVE_INTEGER) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NEGATIVE_INTEGER) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.INT) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NON_NEGATIVE_INTEGER) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_INT) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.POSITIVE_INTEGER))
                                pstmt.setInt(2, Integer.parseInt(((LiteralNode)triple.object()).getLiteral().getValue()));
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.STRING) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.HEX_BINARY)||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.ANY_URI)||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.QNAME)||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NOTATION) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NORMALIZED_STRING) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.TOKEN) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.LANGUAGE) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NMTOKEN) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NMTOKENS) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NAME) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.NCNAME) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.ID) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.IDREF) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.IDREFS) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.ENTITY) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.ENTITIES))
                                pstmt.setString(2, ((LiteralNode)triple.object()).getLiteral().getValue());
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.DATE) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.GYEAR_MONTH) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.GYEAR) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.GMONTH_DAY) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.GDAY) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.GMONTH))
                            {
                                java.sql.Date sqlDate = null;

                                try {
                                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                    java.util.Date utilDate = df.parse(((LiteralNode)triple.object()).getLiteral().getValue());
                                    sqlDate = new java.sql.Date(utilDate.getTime());
                                }
                                catch (java.text.ParseException e)
                                {
                                    throw new SQLException(e);
                                }
                                pstmt.setDate(2, sqlDate);
                            }
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.BOOLEAN))
                                pstmt.setBoolean(2, (((LiteralNode)triple.object()).getLiteral().getValue().equalsIgnoreCase("true") || ((LiteralNode)triple.object()).getLiteral().getValue().equals("1")) ? true : false);
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.DECIMAL))
                                pstmt.setBigDecimal(2, java.math.BigDecimal.valueOf(Double.valueOf(((LiteralNode)triple.object()).getLiteral().getValue())));
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.FLOAT))
                                pstmt.setFloat(2, Float.valueOf(((LiteralNode)triple.object()).getLiteral().getValue()));
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.DOUBLE))
                                pstmt.setDouble(2, Double.valueOf(((LiteralNode)triple.object()).getLiteral().getValue()));
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.DURATION))
                                pstmt.setObject(2, (org.postgresql.util.PGInterval)triple.object());
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.DATETIME))
                            {
                                java.util.Date utilDate = null;

                                try {                      
                                    XMLDateTime result = XMLDateTime.parse(((LiteralNode)triple.object()).getLiteral().getValue());
                                    utilDate = result.toDate();
                                }
                                catch (java.text.ParseException e)
                                {
                                    throw new SQLException(e);
                                }
                                pstmt.setTimestamp(2, new java.sql.Timestamp(utilDate.getTime()));
                            }
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.TIME))
                            {
                                java.sql.Time myTime = null;
                                try {
                                    SimpleDateFormat td = new SimpleDateFormat("'T'HH:mm:ss.SSS'Z'");
                                    java.util.Date utilDate = td.parse(((LiteralNode)triple.object()).getLiteral().getValue());
                                    myTime = new java.sql.Time(utilDate.getTime());
                                }
                                catch (java.text.ParseException e)
                                {
                                    throw new SQLException(e);
                                }
                                pstmt.setTime(2, myTime);
                            }
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.BASE64_BINARY))
                                pstmt.setObject(2, (org.postgresql.util.Base64)triple.object());
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.LONG) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_LONG))
                                pstmt.setLong(2, Long.valueOf(((LiteralNode)triple.object()).getLiteral().getValue()));
                    else if (objectId == DbConstants.getRqlKindFor(XmlSchema.SHORT) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.BYTE) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_SHORT) ||
                             objectId == DbConstants.getRqlKindFor(XmlSchema.UNSIGNED_BYTE))
                                pstmt.setShort(2, Short.parseShort(((LiteralNode)triple.object()).getLiteral().getValue()));
                    else if (objectId == DbConstants.getRqlKindFor(RdfSuite.THESAURUS) ||
                             objectId == DbConstants.getRqlKindFor(RdfSuite.ENUMERATION))
                                throw new UnsupportedOperationException("Not supporting ranges of type 'RdfSuite.THESAURUS and 'RdfSuite.ENUMERATION'.");
                
                    pstmt.setInt(3, context.updatedLabels.getLabel(RdfType.PROPERTY, triple.predicate().getUri()).getTreeLabel().getPost());
                    pstmt.setInt(4, newTripleId);

                    pstmt.execute();
                    pstmt.close();

                    //at this point i should add the triple id in the context.tripleIds
                    context.tripleIds.put(newTripleId, triple);
                }
                else
                    throw new UnsupportedOperationException("The subject of a triple should be of type Resource, Class, or Property, subject of this triple = " + triple.subject() + ", type of the subject = " + triple.subject().type());
            }
      }
    }

    @Override
    protected void storeResources(ImportContext context) throws SQLException {
        for (RdfNode node : context.model.findNodes(RdfType.CLASS))
        {
            int classId = context.updatedLabels.getLabel(RdfType.CLASS, ((Resource)node).getUri()).getTreeLabel().getPost();
            for (gr.forth.ics.swkm.model2.views.IndividualView resource : node.asClass().instances())
            {
                String individualName = "";
                if (resource.isResource())
                    individualName = ((Resource)resource).getUri().toString();
                else
                    individualName = resource.toString();
                if (!checkExistenceResourceDB(individualName, classId))
                {
                    int newTripleId = this.commonTables.tripleIdGenerator.nextValue();
                    System.out.println("storeResources: resource Uri = " + individualName);
                    this.hybridTables.resource.insert(this.hybridTables.resource.uri,
                                                      this.hybridTables.resource.classId,
                                                      this.hybridTables.resource.tripleId)
                                              .values(individualName,
                                                      classId,
                                                      newTripleId);
                    java.util.Iterator tripleIterator = context.model.triples().s((ObjectNode)resource).p(Rdf.TYPE).o(node).fetch().iterator();
                    if (tripleIterator.hasNext())
                        context.tripleIds.put(newTripleId, (Triple)tripleIterator.next());
                }
            }
        }
    }

    private boolean checkExistencePropertyInstanceDB(int subjectTableId, int rangeTableId, int postId) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM tp%dk%d WHERE att3 = %d", subjectTableId, rangeTableId, postId));
        try {
            return rs.next();
        }
        finally
        {
            rs.close();
        }
    }

    private boolean checkExistenceResourceDB(String uri, int classId) throws SQLException
    {
        ResultSet rs = Jdbc.query(String.format("SELECT 1 FROM %s WHERE att0 = '%s' and att1 = %d", this.hybridTables.resource.getName(), uri, classId));
        try {
            return rs.next();
        }
        finally
        {
            rs.close();
        }
    }

    

    private Map<Interval, PropertyDomainRange> getPropertyDomainsAndRanges(Collection<Interval> propertyIntervals)
            throws SQLException {
        Map<Integer, Interval> intervalsByPost = Maps.uniqueIndex(propertyIntervals, new Function<Interval, Integer>() {
            public Integer apply(Interval interval) {
                return interval.getPost();
            }
        });

        PreparedStatement domainRangesByPropertyInterval = Jdbc.prepared(
            //post, domainKind, rangeKind
            "SELECT p.att0, p.att4, p.att6 " +
            "FROM t" + DbConstants.getIdFor(Rdf.PROPERTY) + " p " +
            "WHERE p.att0 = ANY(?)",
            Jdbc.connection().createArrayOf("int", intervalsByPost.keySet().toArray()));

        Map<Interval, PropertyDomainRange> results = Maps.newHashMap();
        ResultSet rs = domainRangesByPropertyInterval.executeQuery();
        while (rs.next()) {
            final int post = rs.getInt(1);
            final int domainKind = rs.getInt(2);
            final int rangeKind = rs.getInt(3);

            results.put(intervalsByPost.get(post), new PropertyDomainRange(domainKind, rangeKind));
        }
        rs.close();
        domainRangesByPropertyInterval.close();

        return results;
    }

    private static class PropertyDomainRange {
        final int domainKind;
        final int rangeKind;

        PropertyDomainRange(int domainKind, int rangeKind) {
            this.domainKind = domainKind;
            this.rangeKind = rangeKind;
        }
    }

    protected void updateInstanceTable(Map<Interval, Interval> oldIntervalToNew, RdfType type) throws SQLException {
        if (type == RdfType.CLASS) {
//            String query = "update tc2000000000 set att1=" + newInterval.getPost() + " where att1=" + oldInterval.getPost();
            String upd = createUpdate(oldIntervalToNew, "tc2000000000", "att1", "att1");
            jdbc.execute(upd);
        }
        if (type == RdfType.PROPERTY) {
        }
    }

}
