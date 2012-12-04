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

import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import java.util.Collection;

/**
 * Offers reification, {@literal i.e.} the ability to handle reified statements as normal triples.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class Reifier {
    private Reifier() { }

    /**
     * Finds all reifiable triples in a {@link Model} and adds them back to it.
     * 
     * <p>Reified triples are quadruplets of triples of this form:
     * <pre>
     * {@code
     *<S rdf:type rdf:Statement>
     *<S rdf:subject X>
     *<S rdf:predicate Y>
     *<S rdf:object Z>
     * }
     * </pre>
     * For all such triple patterns found, the respective triple {@code <X Y Z>}
     * will be added back to the {@code Model}.
     *
     * <p>The subject of a reified statement must be an {@linkplain ObjectNode}, and the predicate must be
     * a {@linkplain Resource}, or else {@code IllegalReifiedStatementException} is thrown.
     * 
     * @param model the triple store to search for reified triples and add them back to
     * @param reifiedStatements the collection to which to add all found reified triples. Can be
     * {@code null} if the client does not care about getting these triples back
     * @throws IllegalReifiedStatementException if an reified statement is attempted to be created, i.e.
     * if the subject is not an {@linkplain ObjectNode} or the predicate is not a {@linkplain Resource}
     */
    public static void includeReifiedTriples(Model model, Collection<Triple> reifiedStatements)
    throws IllegalReifiedStatementException {
        for (ObjectNode node : model.triples()
            .p(Rdf.TYPE)
            .o(Rdf.STATEMENT)
            .fetch().subjects()) {
            
            Iterable<RdfNode> subjects = model.triples()
                .s(node)
                .p(Rdf.SUBJECT)
                .fetch().objects();
            
            Iterable<RdfNode> predicates = model.triples()
                .s(node)
                .p(Rdf.PREDICATE)
                .fetch().objects();
            
            Iterable<RdfNode> objects = model.triples()
                .s(node)
                .p(Rdf.OBJECT)
                .fetch().objects();
            
            for (RdfNode subject : subjects) {
                for (RdfNode predicate : predicates) {
                    for (RdfNode object : objects) {
                        if (!subject.isObjectNode()) {
                            throw new IllegalReifiedStatementException("Subject is not an ObjectNode", subject, predicate, object);
                        } else if (!predicate.isResource()) {
                            throw new IllegalReifiedStatementException("Predicate is not a Resource", subject, predicate, object);
                        }
                        Triple reifiedTriple = model.add(null, (ObjectNode)subject, (Resource)predicate, object);
                        if (reifiedStatements != null) {
                            reifiedStatements.add(reifiedTriple);
                        }
                    }
                }
            }
        }
    }

    public static class IllegalReifiedStatementException extends RuntimeException {
        public IllegalReifiedStatementException(String message, RdfNode subject,
                RdfNode predicate, RdfNode object) {
            super("Reified statement: [" + subject + ", " + predicate + "," + object + "] is illegal. " + message);
        }
    }
}
