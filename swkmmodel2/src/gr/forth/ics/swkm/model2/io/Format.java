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

import org.openrdf.rio.RDFFormat;

/**
 * The format of an RDF serialization.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public enum Format {
    /**
     * N3 format. See <a href="http://www.w3.org/DesignIssues/Notation3.html">N3 specification</a>.
     */
    N3(RDFFormat.N3.supportsContexts()),
    
    /**
     * N-Triples format. See <a href="http://www.w3.org/TR/rdf-testcases/#ntriples">N-Triples specification</a>.
     */
    NTRIPLES(RDFFormat.NTRIPLES.supportsContexts()),
    
    /**
     * RDF/XML format. See <a href="http://www.w3.org/TR/rdf-primer/#rdfxml">RDF/XML specification</a>.
     */
    RDFXML(RDFFormat.RDFXML.supportsContexts()),
    
    /**
     * TriG format. See <a href="http://www4.wiwiss.fu-berlin.de/bizer/TriG/">TriG specification</a>.
     */
    TRIG(RDFFormat.TRIG.supportsContexts()),
    
    /**
     * TriX format. See <a href="http://www.hpl.hp.com/techreports/2004/HPL-2004-56.html">TriX specification</a>.
     */
    TRIX(RDFFormat.TRIX.supportsContexts()),
    
    /**
     * Turtle format. See <a href="http://en.wikipedia.org/wiki/Turtle_(syntax)">Turtle specification</a>.
     */
    TURTLE(RDFFormat.TURTLE.supportsContexts());
    
    RDFFormat toSesameFormat() {
        switch (this) {
            case N3:
                return RDFFormat.N3;
            case NTRIPLES:
                return RDFFormat.NTRIPLES;
            case TRIG:
                return RDFFormat.TRIG;
            case TRIX:
                return RDFFormat.TRIX;
            case RDFXML:
                return RDFFormat.RDFXML;
            case TURTLE:
                return RDFFormat.TURTLE;
            default:
                throw new AssertionError();
        }
    }
    
    private final boolean supportsNamedGraphs;
    
    Format(boolean supportsNamedGraphs) {
        this.supportsNamedGraphs = supportsNamedGraphs;
    }
    
    /**
     * Returns whether this format supports (that is, <em>can express</em>)
     * named graphs.
     * 
     * @return whether this format supports named graphs
     */
    public boolean supportsNamedGraphs() {
        return supportsNamedGraphs;
    }
}
