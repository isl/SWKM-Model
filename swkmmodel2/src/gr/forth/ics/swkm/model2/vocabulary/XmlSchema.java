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


package gr.forth.ics.swkm.model2.vocabulary;

import gr.forth.ics.swkm.model2.Uri;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of terms from the XML Schema ({@code http://www.w3.org/2001/XMLSchema}) namespace.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class XmlSchema {
    private XmlSchema() {
    }
    
    public static final Uri NAMESPACE = new Uri("http://www.w3.org/2001/XMLSchema#", "");
    
    public static final Uri STRING =  new Uri(NAMESPACE.getNamespace(), "string");
    public static final Uri INTEGER =  new Uri(NAMESPACE.getNamespace(), "integer");
    public static final Uri FLOAT =  new Uri(NAMESPACE.getNamespace(), "float");
    public static final Uri BOOLEAN =  new Uri(NAMESPACE.getNamespace(), "boolean");
    public static final Uri DATETIME =  new Uri(NAMESPACE.getNamespace(), "dateTime");
    public static final Uri DECIMAL =  new Uri(NAMESPACE.getNamespace(), "decimal");
    public static final Uri DOUBLE =  new Uri(NAMESPACE.getNamespace(), "double");
    public static final Uri DURATION =  new Uri(NAMESPACE.getNamespace(), "duration");
    public static final Uri TIME =  new Uri(NAMESPACE.getNamespace(), "time");
    public static final Uri DATE =  new Uri(NAMESPACE.getNamespace(), "date");
    public static final Uri GYEAR_MONTH =  new Uri(NAMESPACE.getNamespace(), "gYearMonth");
    public static final Uri GYEAR =  new Uri(NAMESPACE.getNamespace(), "gYear");
    public static final Uri GMONTH_DAY =  new Uri(NAMESPACE.getNamespace(), "gMonthDay");
    public static final Uri GDAY =  new Uri(NAMESPACE.getNamespace(), "gDay");
    public static final Uri GMONTH =  new Uri(NAMESPACE.getNamespace(), "gMonth");
    public static final Uri HEX_BINARY =  new Uri(NAMESPACE.getNamespace(), "hexBinary");
    public static final Uri BASE64_BINARY =  new Uri(NAMESPACE.getNamespace(), "base64Binary");
    public static final Uri ANY_URI =  new Uri(NAMESPACE.getNamespace(), "anyURI");
    public static final Uri QNAME =  new Uri(NAMESPACE.getNamespace(), "QName");
    public static final Uri NOTATION =  new Uri(NAMESPACE.getNamespace(), "NOTATION");
    public static final Uri NORMALIZED_STRING =  new Uri(NAMESPACE.getNamespace(), "normalizedString");
    public static final Uri TOKEN =  new Uri(NAMESPACE.getNamespace(), "token");
    public static final Uri LANGUAGE =  new Uri(NAMESPACE.getNamespace(), "language");
    public static final Uri NMTOKEN =  new Uri(NAMESPACE.getNamespace(), "NMTOKEN");
    public static final Uri NMTOKENS =  new Uri(NAMESPACE.getNamespace(), "NMTOKENS");
    public static final Uri NAME =  new Uri(NAMESPACE.getNamespace(), "Name");
    public static final Uri NCNAME =  new Uri(NAMESPACE.getNamespace(), "NCName");
    public static final Uri ID =  new Uri(NAMESPACE.getNamespace(), "ID");
    public static final Uri IDREF =  new Uri(NAMESPACE.getNamespace(), "IDREF");
    public static final Uri IDREFS =  new Uri(NAMESPACE.getNamespace(), "IDREFS");
    public static final Uri ENTITY =  new Uri(NAMESPACE.getNamespace(), "ENTITY");
    public static final Uri ENTITIES =  new Uri(NAMESPACE.getNamespace(), "ENTITIES");
    public static final Uri NON_POSITIVE_INTEGER =  new Uri(NAMESPACE.getNamespace(), "nonPositiveInteger");
    public static final Uri NEGATIVE_INTEGER =  new Uri(NAMESPACE.getNamespace(), "negativeInteger");
    public static final Uri LONG =  new Uri(NAMESPACE.getNamespace(), "long");
    public static final Uri INT =  new Uri(NAMESPACE.getNamespace(), "int");
    public static final Uri SHORT =  new Uri(NAMESPACE.getNamespace(), "short");
    public static final Uri BYTE =  new Uri(NAMESPACE.getNamespace(), "byte");
    public static final Uri NON_NEGATIVE_INTEGER =  new Uri(NAMESPACE.getNamespace(), "nonNegativeInteger");
    public static final Uri UNSIGNED_LONG =  new Uri(NAMESPACE.getNamespace(), "unsignedLong");
    public static final Uri UNSIGNED_INT =  new Uri(NAMESPACE.getNamespace(), "unsignedInt");
    public static final Uri UNSIGNED_SHORT =  new Uri(NAMESPACE.getNamespace(), "unsignedShort");
    public static final Uri UNSIGNED_BYTE =  new Uri(NAMESPACE.getNamespace(), "unsignedByte");
    public static final Uri POSITIVE_INTEGER =  new Uri(NAMESPACE.getNamespace(), "positiveInteger");
    
    public static Collection<Uri> getAllTypes() {
        List<Uri> types = new ArrayList<Uri>();
        for (Field f : XmlSchema.class.getDeclaredFields()) {
            if (f.getName().equals("NAMESPACE")) {
                continue;
            }
            try {
                types.add((Uri)f.get(null));
            } catch (IllegalAccessException ignore) { }
        }
        return types;
    }
}
