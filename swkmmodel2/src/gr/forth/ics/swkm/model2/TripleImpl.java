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


package gr.forth.ics.swkm.model2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.util.Assert;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
final class TripleImpl implements Triple {
    private final ObjectNodeImpl subject;
    private final ResourceImpl predicate;
    private final RdfNodeImpl object;

    //accessed directly from ModelImpl
    Set<ResourceImpl> namedGraphs = Collections.emptySet();

    TripleImpl(ObjectNodeImpl subject, ResourceImpl predicate, RdfNodeImpl object) {
        Assert.notNull(subject, "subject");
        Assert.notNull(predicate, "predicate");
        Assert.notNull(object, "object");
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    @SuppressWarnings("unchecked") //returned collection is immutable
    public Collection<Resource> graphs() {
        return (Collection)Collections.unmodifiableCollection(namedGraphs);
    }

    public Model owner() {
        return subject.owner();
    }

    public ObjectNodeImpl subject() {
        return subject;
    }

    public ResourceImpl predicate() {
        return predicate;
    }

    public RdfNodeImpl object() {
        return object;
    }

    @Override
    public int hashCode() {
        return 7 + 961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TripleImpl)) {
            return false;
        }
        final TripleImpl other = (TripleImpl) obj;
        return object == other.object &&
                subject == other.subject &&
                predicate == other.predicate;
    }

    @Override
    public String toString() {
        return "[<" + subject + " " + predicate + " " + object + "> {" + namedGraphs +"}]";
    }

    @Override
    public String toSimpleString() {
        return "<" + subject + " " + predicate + " " + object + ">";
    }

    String toStringWithTypes() {
        return String.format("[<%s>(%s) <%s> <%s>(%s) {%s}]",
                subject, subject.type(),
                predicate,
                object, object.type(),
                namedGraphs);
    }

    ObjectNodeImpl getObjectResource() {
        RdfNode obj = object();
        if (!obj.isObjectNode()) {
            throw new IllegalStateException("In triple: " + this + ", object is not a resource");
        }
        return (ObjectNodeImpl)obj;
    }

    public Iterable<RdfNode> nodes() {
        return Arrays.<RdfNode>asList(subject, predicate, object);
    }

    public boolean delete() {
        return owner().delete(this);
    }
}
