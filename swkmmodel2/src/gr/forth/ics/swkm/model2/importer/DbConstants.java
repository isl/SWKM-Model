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

import com.google.common.collect.Maps;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class DbConstants {
    private static final Map<Uri, Integer> ids = Maps.newHashMap();
    private static final Map<Uri, Integer> postOrders = Maps.newHashMap();
    private static final Map<Uri, String> tableNames = Maps.newHashMap();
    
    private static final Map<Uri, Integer> rqlKinds = Maps.newHashMap();

    private static final Map<Uri, Integer> xmlTypeIds = Maps.newHashMap();

    public static final int MAX_INT = 2000000000;
    public static final int INIT_POST_ORDER = 300;
    
    static { //init class ids
        ids.put(RdfSchema.CLASS, MAX_INT / 2);
        ids.put(RdfSuite.DATA_PROPERTY, 35);
        ids.put(Rdf.PROPERTY, MAX_INT);
        ids.put(RdfSchema.RESOURCE, MAX_INT);
        ids.put(RdfSuite.GRAPH, 77);
        ids.put(RdfSuite.THESAURUS, 36);
        ids.put(RdfSuite.ENUMERATION, 37);
        ids.put(RdfSchema.LITERAL, 12);
        ids.put(XmlSchema.STRING, 30);
        ids.put(XmlSchema.BOOLEAN, 33);
        ids.put(XmlSchema.INTEGER, 31);
        ids.put(XmlSchema.FLOAT, 32);
        ids.put(XmlSchema.DATETIME, 34);
        ids.put(XmlSchema.DECIMAL, 38);
        ids.put(XmlSchema.DOUBLE, 39);
        ids.put(XmlSchema.DURATION, 40);
        ids.put(XmlSchema.TIME, 41);
        ids.put(XmlSchema.DATE, 42);
        ids.put(XmlSchema.GYEAR_MONTH, 43);
        ids.put(XmlSchema.GYEAR, 44);
        ids.put(XmlSchema.GMONTH_DAY, 45);
        ids.put(XmlSchema.GDAY, 46);
        ids.put(XmlSchema.GMONTH, 47);
        ids.put(XmlSchema.HEX_BINARY, 48);
        ids.put(XmlSchema.BASE64_BINARY, 49);
        ids.put(XmlSchema.ANY_URI, 50);
        ids.put(XmlSchema.QNAME, 51);
        ids.put(XmlSchema.NOTATION, 52);
        ids.put(XmlSchema.NORMALIZED_STRING, 53);
        ids.put(XmlSchema.TOKEN, 54);
        ids.put(XmlSchema.LANGUAGE, 55);
        ids.put(XmlSchema.NMTOKEN, 56);
        ids.put(XmlSchema.NMTOKENS, 57);
        ids.put(XmlSchema.NAME, 58);
        ids.put(XmlSchema.NCNAME, 59);
        ids.put(XmlSchema.ID, 60);
        ids.put(XmlSchema.IDREF, 61);
        ids.put(XmlSchema.IDREFS, 62);
        ids.put(XmlSchema.ENTITY, 63);
        ids.put(XmlSchema.ENTITIES, 64);
        ids.put(XmlSchema.NON_POSITIVE_INTEGER, 65);
        ids.put(XmlSchema.NEGATIVE_INTEGER, 66);
        ids.put(XmlSchema.LONG, 67);
        ids.put(XmlSchema.INT, 68);
        ids.put(XmlSchema.SHORT, 69);
        ids.put(XmlSchema.BYTE, 70);
        ids.put(XmlSchema.NON_NEGATIVE_INTEGER, 71);
        ids.put(XmlSchema.UNSIGNED_LONG, 72);
        ids.put(XmlSchema.UNSIGNED_INT, 73);
        ids.put(XmlSchema.UNSIGNED_SHORT, 74);
        ids.put(XmlSchema.UNSIGNED_BYTE, 75);
        ids.put(XmlSchema.POSITIVE_INTEGER, 76);
    }
    
    static { //init postOrders
        for (Uri uri : ids.keySet()) {
            postOrders.put(uri, 0);
        }
        postOrders.put(RdfSchema.CLASS, INIT_POST_ORDER);
        postOrders.put(Rdf.PROPERTY, (MAX_INT / 2) + 1);
        postOrders.put(RdfSchema.RESOURCE, INIT_POST_ORDER);
    }

    static { //int rql mapping
        rqlKinds.put(RdfSchema.CLASS, 2);
        rqlKinds.put(Rdf.PROPERTY, 3);
        rqlKinds.put(RdfSuite.DATA_PROPERTY, 3);
        rqlKinds.put(RdfSchema.RESOURCE, 7);
        rqlKinds.put(RdfSchema.LITERAL, 8);
        rqlKinds.put(XmlSchema.STRING, 9);
        rqlKinds.put(XmlSchema.INTEGER, 11);
        rqlKinds.put(XmlSchema.BOOLEAN, 12);
        rqlKinds.put(RdfSuite.METACLASS, 13);
        rqlKinds.put(XmlSchema.DATETIME, 20);
        rqlKinds.put(XmlSchema.FLOAT, 21);
        rqlKinds.put(RdfSuite.THESAURUS, 22);
        rqlKinds.put(RdfSuite.ENUMERATION, 27);
        rqlKinds.put(XmlSchema.INT, 31);
        rqlKinds.put(XmlSchema.DECIMAL, 38);
        rqlKinds.put(XmlSchema.DOUBLE, 39);
        rqlKinds.put(XmlSchema.DURATION, 40);
        rqlKinds.put(XmlSchema.TIME, 41);
        rqlKinds.put(XmlSchema.DATE, 42);
        rqlKinds.put(XmlSchema.GYEAR_MONTH, 43);
        rqlKinds.put(XmlSchema.GYEAR, 44);
        rqlKinds.put(XmlSchema.GMONTH_DAY, 45);
        rqlKinds.put(XmlSchema.GDAY, 46);
        rqlKinds.put(XmlSchema.GMONTH, 47);
        rqlKinds.put(XmlSchema.HEX_BINARY, 48);
        rqlKinds.put(XmlSchema.BASE64_BINARY, 49);
        rqlKinds.put(XmlSchema.ANY_URI, 50);
        rqlKinds.put(XmlSchema.QNAME, 51);
        rqlKinds.put(XmlSchema.NOTATION, 52);
        rqlKinds.put(XmlSchema.NORMALIZED_STRING, 53);
        rqlKinds.put(XmlSchema.TOKEN, 54);
        rqlKinds.put(XmlSchema.LANGUAGE, 55);
        rqlKinds.put(XmlSchema.NMTOKEN, 56);
        rqlKinds.put(XmlSchema.NMTOKENS, 57);
        rqlKinds.put(XmlSchema.NAME, 58);
        rqlKinds.put(XmlSchema.NCNAME, 59);
        rqlKinds.put(XmlSchema.ID, 60);
        rqlKinds.put(XmlSchema.IDREF, 61);
        rqlKinds.put(XmlSchema.IDREFS, 62);
        rqlKinds.put(XmlSchema.ENTITY, 63);
        rqlKinds.put(XmlSchema.ENTITIES, 64);
        rqlKinds.put(XmlSchema.NON_POSITIVE_INTEGER, 65);
        rqlKinds.put(XmlSchema.NEGATIVE_INTEGER, 66);
        rqlKinds.put(XmlSchema.LONG, 67);
        rqlKinds.put(XmlSchema.SHORT, 69);
        rqlKinds.put(XmlSchema.BYTE, 70);
        rqlKinds.put(XmlSchema.NON_NEGATIVE_INTEGER, 71);
        rqlKinds.put(XmlSchema.UNSIGNED_LONG, 72);
        rqlKinds.put(XmlSchema.UNSIGNED_INT, 73);
        rqlKinds.put(XmlSchema.UNSIGNED_SHORT, 74);
        rqlKinds.put(XmlSchema.UNSIGNED_BYTE, 75);
        rqlKinds.put(XmlSchema.POSITIVE_INTEGER, 76);
        rqlKinds.put(RdfSuite.GRAPH, 77);
    }

    //don't even ask
    static int getMagicDomain(Resource domain) {
        if (domain.type().isMetaclass()) {
            return 2;
        } else if (domain.is(Rdf.PROPERTY)) {
            return 3;
        } else if (domain.type().isClass()) {
            return 7;
        }
        throw new AssertionError();
    }

    //see above
    static int getMagicRange(Resource range) {
        if (range.is(RdfSchema.CLASS) || range.type().isMetaclass()) {
            return 2;
        } else if (range.type().isXmlType() || range.type().isLiteral()) {
            return getRqlKindFor(range.getUri());
        } else if (range.type().isClass()) {
            return 7;
        }
        throw new AssertionError();
    }

    static int getTripleSubjectRQLKindId(RdfType subjectType)
    {
        final Uri uri;
        switch (subjectType) {
            case CLASS:
                uri = RdfSchema.CLASS; break;
            case PROPERTY:
                uri = Rdf.PROPERTY; break;
            case INDIVIDUAL:
                uri = RdfSchema.RESOURCE; break;
            default:
                throw new AssertionError(
                        "Subject of a triple should be of type CLASS, PROPERTY or INDIVIDUAL.");
        }
        return DbConstants.getRqlKindFor(uri);
    }

    static int getTripleRangeRQLKindId(RdfNode range)
    {
        final Uri uri;
        switch (range.type()) {
            case CLASS:
                uri = RdfSchema.CLASS; break;
            case PROPERTY:
                uri = Rdf.PROPERTY; break;
            case INDIVIDUAL:
                uri = RdfSchema.RESOURCE; break;
            case NAMED_GRAPH:
                uri = RdfSuite.GRAPH; break;
            case XML_TYPE:
                uri = ((Resource)range).getUri(); break;
            case METACLASS: case METAPROPERTY:
                uri = RdfSchema.CLASS; break;
            default:
                throw new AssertionError(
                        "Range of a triple should be of type CLASS, PROPERTY, " +
                        "INDIVIDUAL, NAMED_GRAPH, XML_TYPE, METACLASS, METAPROPERTY.");
        }
        return DbConstants.getRqlKindFor(uri);
    }

    static Map<Uri, Integer> getRqlIds() {
        return Collections.unmodifiableMap(ids);
    }

    static { //init class names
        tableNames.put(RdfSchema.CLASS, "Class");
        tableNames.put(RdfSuite.DATA_PROPERTY, "DProperty");
        tableNames.put(Rdf.PROPERTY, "Property");
        tableNames.put(RdfSchema.LITERAL, "LiteralType");
        tableNames.put(RdfSuite.THESAURUS, "Thesaurus");
        tableNames.put(RdfSuite.ENUMERATION, "Enumeration");
    }

    static { // init xml type ids (see StorageMetaData.dtIDs map of the old model)
        //no clue why or if all this is needed
		xmlTypeIds.put(XmlSchema.STRING, 30);
		xmlTypeIds.put(XmlSchema.BOOLEAN, 33);
		xmlTypeIds.put(XmlSchema.INTEGER, 31);
		xmlTypeIds.put(XmlSchema.FLOAT, 32);
		xmlTypeIds.put(XmlSchema.DATETIME, 34);
		xmlTypeIds.put(XmlSchema.DECIMAL, 38);
		xmlTypeIds.put(XmlSchema.DOUBLE, 39);
		xmlTypeIds.put(XmlSchema.DURATION, 40);
		xmlTypeIds.put(XmlSchema.TIME, 41);
		xmlTypeIds.put(XmlSchema.DATE, 42);
		xmlTypeIds.put(XmlSchema.GYEAR_MONTH, 43);
		xmlTypeIds.put(XmlSchema.GYEAR, 44);
		xmlTypeIds.put(XmlSchema.GMONTH_DAY, 45);
		xmlTypeIds.put(XmlSchema.GDAY, 46);
		xmlTypeIds.put(XmlSchema.GMONTH, 47);
		xmlTypeIds.put(XmlSchema.HEX_BINARY, 48);
		xmlTypeIds.put(XmlSchema.BASE64_BINARY, 49);
		xmlTypeIds.put(XmlSchema.ANY_URI, 50);
		xmlTypeIds.put(XmlSchema.QNAME, 51);
		xmlTypeIds.put(XmlSchema.NOTATION, 52);
		xmlTypeIds.put(XmlSchema.NORMALIZED_STRING, 53);
		xmlTypeIds.put(XmlSchema.TOKEN, 54);
		xmlTypeIds.put(XmlSchema.LANGUAGE, 55);
		xmlTypeIds.put(XmlSchema.NMTOKEN, 56);
		xmlTypeIds.put(XmlSchema.NMTOKENS, 57);
		xmlTypeIds.put(XmlSchema.NAME, 58);
		xmlTypeIds.put(XmlSchema.NCNAME, 59);
		xmlTypeIds.put(XmlSchema.ID, 60);
		xmlTypeIds.put(XmlSchema.IDREF, 61);
		xmlTypeIds.put(XmlSchema.IDREFS, 62);
		xmlTypeIds.put(XmlSchema.ENTITY, 63);
		xmlTypeIds.put(XmlSchema.ENTITIES, 64);
		xmlTypeIds.put(XmlSchema.NON_POSITIVE_INTEGER, 65);
		xmlTypeIds.put(XmlSchema.NEGATIVE_INTEGER, 66);
		xmlTypeIds.put(XmlSchema.LONG, 67);
		xmlTypeIds.put(XmlSchema.INT, 68);
		xmlTypeIds.put(XmlSchema.SHORT, 69);
		xmlTypeIds.put(XmlSchema.BYTE, 70);
		xmlTypeIds.put(XmlSchema.NON_NEGATIVE_INTEGER, 71);
		xmlTypeIds.put(XmlSchema.UNSIGNED_LONG, 72);
		xmlTypeIds.put(XmlSchema.UNSIGNED_INT, 73);
		xmlTypeIds.put(XmlSchema.UNSIGNED_SHORT, 74);
		xmlTypeIds.put(XmlSchema.UNSIGNED_BYTE, 75);
		xmlTypeIds.put(XmlSchema.POSITIVE_INTEGER, 76);
		xmlTypeIds.put(RdfSchema.LITERAL, 77);
    }
    
    private DbConstants() { }
    
    public static int getIdFor(Uri uri) {
        return get(ids, uri, "id");
    }
    
    public static int getPostOrderFor(Uri uri) {
        return get(postOrders, uri, "postOrder");
    }
    
    public static int getRqlKindFor(Uri uri) {
        return get(rqlKinds, uri, "rqlId");
    }

    public static int getXmlTypeIdFor(Uri uri) {
        return get(xmlTypeIds, uri, "xmlType");
    }

    public static String getNameFor(Uri uri) {
        return get(tableNames, uri, "table name");
    }
    
    private static <V> V get(Map<Uri, V> map, Uri uri, String type) {
        V value = map.get(uri);
        if (value == null) {
            throw new NoSuchElementException("No " + type + " for uri: '" + uri + "'");
        }
        return value;
    }

    //amazingly hard-coded constants, thanks to everyone who contributed to this mess
    static int domainKind(Resource domain) {
	   if (domain.is(RdfSuite.GRAPH)) {
           return 2;
       }
       switch (domain.type()) {
           case CLASS: return 2;
           case METACLASS: case METAPROPERTY: return 13;
       }
        throw new AssertionError("Unexpected domain: " + domain + " of type " + domain.type());
    }

    static int rangeKind(Resource range) {
        if (range.is(RdfSuite.GRAPH)) {
            return 2;
        }
        switch (range.type()) {
            case METACLASS: case METAPROPERTY:
                return 13;
            case CLASS:
                return 2;
        }
        if (range.type().isXmlType() || range.asInheritable().isDescendantOf(range.owner().mapResource(RdfSchema.LITERAL))) {
            return 8;
        }
        throw new AssertionError("Unexpexted range: " + range);
    }
}
