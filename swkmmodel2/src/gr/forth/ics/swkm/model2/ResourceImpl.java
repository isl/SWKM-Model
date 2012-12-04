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

import com.google.common.base.Preconditions;

/**
 * An implementation of Resource.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
final class ResourceImpl extends ObjectNodeImpl implements Resource {
    private final Uri uri;

    ResourceImpl(ModelImpl owner, Uri uri) {
        super(owner);
        this.uri = Preconditions.checkNotNull(uri);
        resetType();
    }

    public boolean is(Uri uri) {
        return this.uri.equals(uri);
    }

    public Uri getUri() {
        return uri;
    }

    public String getValue() {
        return uri.toString();
    }

    @Override
    public boolean isResource() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return uri.toString();
    }

    @Override
    public Triples predicateTriples() {
        return owner().triples().p(this).fetch();
    }

    public Resource mappedTo(Model model) {
        return model.mapResource(this);
    }
}
