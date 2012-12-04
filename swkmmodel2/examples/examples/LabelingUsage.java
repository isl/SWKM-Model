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
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates a simple schema, performs isAncestorOf/isDescendantOf queries, then uses labeling
 * to optimize such queries to constant time.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class LabelingUsage {
    private static final Random random = new Random(0);

    public static void main(String[] args) {
        Model model = ModelBuilder.newSparse().build();

        createRandomHierarchy(model);

        model.updateLabels(); //this call just makes the following code faster; the code would work anyway
        //To ensure optimal ancestry queries performance, this method needs to be called after
        //all changes made to hierarchy structures

        for (RdfNode c1 : model.findNodes(RdfType.CLASS)) {
            for (RdfNode c2 : model.findNodes(RdfType.CLASS)) {
                boolean isAncestor = c1.asInheritable().isAncestorOf(c2.asInheritable());
                boolean isDescendant = c1.asInheritable().isDescendantOf(c2.asInheritable());
            }
        }
    }

    /**
     * Creates a random class hierarchy with 10 classes.
     */
    private static void createRandomHierarchy(Model model) {
        final int totalClasses = 10;

        List<Resource> classes = new ArrayList<Resource>(totalClasses);
        for (int i = 0; i < totalClasses; i++) {
            Resource parent = null;
            if (!classes.isEmpty()) {
                parent = classes.get(random.nextInt(classes.size())); //choose a random parent
            }
            String newClassName = parent != null ? parent.toString() + "_" + i : "http://examples#Root";
            Resource newClass = model.mapResource(newClassName);
            classes.add(newClass);
            if (parent != null) {
                model.add().s(newClass).p(RdfSchema.SUBCLASSOF).o(parent);
            }
        }
    }


}
