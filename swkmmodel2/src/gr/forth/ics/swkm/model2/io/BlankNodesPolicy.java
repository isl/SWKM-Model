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


package gr.forth.ics.swkm.model2.io;

import java.util.concurrent.atomic.AtomicInteger;
import org.openrdf.model.BNode;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines a policy regarding how to create blank node identifiers while parsing an input.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class BlankNodesPolicy {
    protected BlankNodesPolicy() { }

    /**
     * Creates a blank node identifier, given the identifier defined in the input, which formally has only
     * local (file-wide) scope.
     *
     * @param localIdentifier the local identifier of the blank node
     * @return a blank node identifier to use instead of the local one
     */
    public abstract String createIdentifier(String localIdentifier);

    /**
     * Returns a {@link BlankNodesPolicy} which throws away local identifiers and uses
     * globally unique identifiers instead. (Global in the scope of the executing virtual machine
     * and the class loader that loaded this class).
     *
     * @return a {@code BlankNodesPolicy} which throws away local identifiers and uses
     * globally unique identifiers instead
     */
    public static BlankNodesPolicy globallyUnique() {
        return GloballyUniqueBlankNodesPolicy.instance;
    }

    /**
     * Returns a {@link BlankNodesPolicy} which keeps the local blank node identifiers. Note that
     * this may lead to conflicts, if the same identifier is used in two separate RDF files (these
     * blank node identifiers are not in the same scope and do not identify the same blank node,
     * thus the conflict if they are treated as same).
     *
     * @return a {@code BlankNodesPolicy} which keeps the local blank node identifiers
     */
    public static BlankNodesPolicy asDeclared() {
        return AsDeclaredBlankNodesPolicy.instance;
    }

    private static class GloballyUniqueBlankNodesPolicy extends BlankNodesPolicy {
        static final GloballyUniqueBlankNodesPolicy instance = new GloballyUniqueBlankNodesPolicy();

        private static final AtomicInteger anonymousBlankNodeCounter = new AtomicInteger();

        @Override
        public String createIdentifier(String localIdentifier) {
            return "bnode" + anonymousBlankNodeCounter.getAndIncrement();
        }
    }

    private static class AsDeclaredBlankNodesPolicy extends BlankNodesPolicy {
        static final AsDeclaredBlankNodesPolicy instance = new AsDeclaredBlankNodesPolicy();

        @Override
        public String createIdentifier(String localIdentifier) {
            return localIdentifier;
        }
    }

    ValueFactory valueFactory() {
        return new ValueFactoryImpl() {
            @Override
            public BNode createBNode(String blankNodeId) {
                return new BNodeImpl(createIdentifier(blankNodeId));
            }

            @Override
            public BNode createBNode() {
                //in case were there is no identifier, switch to automatic unique identifiers
                return createBNode(GloballyUniqueBlankNodesPolicy.instance.createIdentifier(null));
            }
        };
    }
}
