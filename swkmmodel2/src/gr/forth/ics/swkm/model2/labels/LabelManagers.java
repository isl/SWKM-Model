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


package gr.forth.ics.swkm.model2.labels;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gr.forth.ics.graph.Node;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A collection of {@linkplain LabelManager} implementations.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class LabelManagers {
    private LabelManagers() { }

    /**
     * A non-incremental LabelManager that invalidates its labels upon <em>any</em>
     * model modification.
     */
    public static class NonIncrementalMainMemoryManager implements LabelManager {
        private final Model model;
        private final Labeler labeler;

        private PredefinedLabels predefinedLabels;

        private Map<Resource, Label> resourcesToLabels = null;
        
        public NonIncrementalMainMemoryManager(Model model, Labeler labeler) {
            this.model = Preconditions.checkNotNull(model);
            this.labeler = Preconditions.checkNotNull(labeler);
        }

        private void clearLabels() {
            resourcesToLabels = null;
        }

        public Model getTargetModel() {
            return model;
        }

        /**
         * Invalidates any existing labeling.
         * 
         * @param triple a triple that was added
         */
        public void tripleAdded(Triple triple) {
            clearLabels();
        }

        /**
         * Invalidates any existing labeling.
         *
         * @param triple a triple that was deleted
         */
        public void tripleDeleted(Triple triple) {
            clearLabels();
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        public boolean areLabelsAvailable() {
            return resourcesToLabels != null;
        }

        /**
         * Recalculates the labels for all {@code is-a} relations, specifically for classes,
         * metaclasses, properties and metaproperties.
         */
        public void updateLabels(PredefinedLabels predefinedLabels) {
            /**
             * The following optimization depends on modifications clearing the existing labels;
             * so if labels are available, they must be consistent.
             */
            if (areLabelsAvailable()) {
                return;
            }
            /*
             * After this call, hierarchies and map from Resource to Node must be consistent
             * and ready to be used.
             */
            Collection<Callable<Hierarchy>> hierarchyFactories = Lists.newArrayListWithExpectedSize(4);
            hierarchyFactories.add(new Callable<Hierarchy>() { public Hierarchy call() {
                return MainMemoryHierarchy.newClassHierarchy(model);
            } });
            hierarchyFactories.add(new Callable<Hierarchy>() { public Hierarchy call() {
                return MainMemoryHierarchy.newMetaclassHierarchy(model);
            } });
            hierarchyFactories.add(new Callable<Hierarchy>() { public Hierarchy call() {
                return MainMemoryHierarchy.newPropertyHierarchy(model);
            } });
            hierarchyFactories.add(new Callable<Hierarchy>() { public Hierarchy call() {
                return MainMemoryHierarchy.newMetapropertyHierarchy(model);
            } });

            resourcesToLabels = Maps.newHashMapWithExpectedSize(128);
            for (Callable<Hierarchy> hierarchyFactory : hierarchyFactories) {
                try {
                    Hierarchy hierarchy = hierarchyFactory.call();
                    if (hierarchy == null) {
                        continue;
                    }
                    labeler.assignLabels(hierarchy, null);
                    for (Node n : hierarchy.exploredGraph().nodes()) {
                        resourcesToLabels.put((Resource)n.getValue(), hierarchy.getLabelOf(n));
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean isFirstAncestorOfSecond(Resource maybeAncestor, Resource maybeDescendant) {
            //the following test will not succeed if calling code first checks if #areLabelsAvailable is true
            if (resourcesToLabels == null) {
                updateLabels(predefinedLabels);
            }
            if (maybeAncestor.type() != maybeDescendant.type()) {
                //different type means by definition incomparable labels
                return false;
            }
            Label ancestorLabel = resourcesToLabels.get(maybeAncestor);
            Label descendantLabel = resourcesToLabels.get(maybeDescendant);
            return ancestorLabel.contains(descendantLabel);
        }
    }
}
