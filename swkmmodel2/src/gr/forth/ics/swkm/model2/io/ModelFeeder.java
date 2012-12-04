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

import com.google.common.base.Predicate;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * An RDFHandler that feeds input triples to a {@link Model}.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class ModelFeeder implements RDFHandler {
    private final Model target;
    private final Resource defaultNamedGraph;
    private final Predicate<? super Statement> statementFilter;
    
    ModelFeeder(Model target, Uri defaultNamedGraph, Predicate<? super Statement> statementFilter) {
        this.target = target;
        this.statementFilter = statementFilter;
        if (defaultNamedGraph == null) defaultNamedGraph = RdfSuite.DEFAULT_GRAPH_URI;
        this.defaultNamedGraph = target.mapResource(defaultNamedGraph);
    }
    
    public void startRDF() throws RDFHandlerException {
    }
    
    public void handleComment(String comment) throws RDFHandlerException {
    }

    public void handleNamespace(String prefix, String namespace) throws RDFHandlerException {
    }

    public void handleStatement(Statement statement) throws RDFHandlerException {
        if (!statementFilter.apply(statement)) {
            return;
        }
        ObjectNode subject = (ObjectNode)map(statement.getSubject());
        Resource predicate = (Resource)map(statement.getPredicate());
        RdfNode object = map(statement.getObject());
        Resource namedGraph = statement.getContext() == null ?
            defaultNamedGraph : (Resource)map(statement.getContext());
        target.add(namedGraph, subject, predicate, object);
    }
    
    public void endRDF() throws RDFHandlerException {
    }

    private RdfNode map(Value object) {
        if (object instanceof URI) {
            URI uri = (URI)object;
            return target.mapResource(new Uri(uri.getNamespace(), uri.getLocalName()));
        } else if (object instanceof BNode) {
            BNode bNode = (BNode)object;
            return target.mapBlankNode(bNode.getID());
        } else {
            Literal literal = (Literal)object;
            gr.forth.ics.swkm.model2.Literal swkmLiteral = null;
            if (literal.getDatatype() != null) {
                swkmLiteral = gr.forth.ics.swkm.model2.Literal.createWithType(literal.getLabel(), Uri.parse(literal.getDatatype().toString()));
            } else {
                swkmLiteral = gr.forth.ics.swkm.model2.Literal.createWithLanguage(literal.getLabel(), literal.getLanguage());
            }
            return target.mapLiteral(swkmLiteral);
        }
    }
}
