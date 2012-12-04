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

import gr.forth.ics.swkm.model2.TypeInference.InternalType;
import gr.forth.ics.swkm.model2.event.TypeChange;
import org.springframework.util.Assert;

/**
 * A LiteralNode implementation.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class LiteralNodeImpl extends RdfNodeImpl implements LiteralNode {
    private final Literal literal;
    
    LiteralNodeImpl(ModelImpl owner, Literal literal) {
        super(owner);
        Assert.notNull(literal, "literal");
        this.literal = literal;
        owner.handleTypeChange(TypeInference.initialChange(this, RdfType.UNKNOWN, RdfType.LITERAL));
    }

    public RdfType type() {
        return RdfType.LITERAL;
    }

    @Override InternalType internalType() {
        return InternalType.LITERAL;
    }

    @Override boolean isTypePossible(RdfType type) {
        return type == RdfType.LITERAL;
    }

    @Override
    void setInternalType(InternalType internalType, TypeChange change) {
        if (internalType != InternalType.LITERAL) {
            throw new IllegalStateException(String.valueOf(internalType));
        }
    }

    public boolean is(Uri uri) {
        return false;
    }

    public Literal getLiteral() {
        return literal;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }
    
    @Override
    public String toString() {
        return literal.toString();
    }

    public LiteralNode mappedTo(Model model) {
        return model.mapLiteral(literal);
    }

    public final boolean hasConstantType() {
        return true;
    }
}
