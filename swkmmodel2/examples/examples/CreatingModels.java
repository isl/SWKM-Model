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


package examples;

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Uri;

/**
 * Demonstrates the various configurations a model can be created.
 * 
 * @author andreou
 */
public class CreatingModels {
    public static void main(String[] args) {
        {
            //Simplest case. This creates a model appropriate for most usages
            Model model = ModelBuilder.newSparse().build();
        }

        {
            //As the first example, but also overrides the default named graph URI
            Model model = ModelBuilder.newSparse()
                    .withDefaultNamedGraphUri(Uri.parse("http://myDomain/myCustomDefaultNamedGraph"))
                    .build();
            //this node represents our default named graph
            Resource myCustomDefaultNamedGraph = model.defaultNamedGraph();
            //this accesses the Uri we specified
            Uri myCustomDefaultNamedGraphUri = myCustomDefaultNamedGraph.getUri();
        }
        
        {
            //As the second example, but types of nodes will not be inferenced
            Model model = ModelBuilder.newSparse()
                    .withoutTypeInference()
                    .withDefaultNamedGraphUri(Uri.parse("http://myDomain/myCustomDefaultNamedGraph"))
                    .build();
        }
        
        {
            //Creates a model with higher memory requirements, but faster in 
            //answering triple queries
            Model model = ModelBuilder.newFull()
                    .withTypeInference()
                    .build();
            
        }
    }
}
