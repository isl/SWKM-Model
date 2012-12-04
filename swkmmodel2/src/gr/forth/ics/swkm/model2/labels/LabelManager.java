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

import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;

/**
 * A manager of labeling structures, which has the responsibility of providing labeling
 * services to a {@linkplain Model}.
 *
 * <p>This interface also defines triple event callbacks ({@linkplain #tripleAdded(Triple)},
 * {@linkplain #tripleDeleted(Triple)})
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface LabelManager {
    /**
     * Notifies this LabelManager that a new triple is added to
     * this LabelManager provides services for the model
     * (updates to the triple named graphs are irrelevant).
     *
     * @param triple the triple that was added to the model this LabelManager provides services for
     */
    void tripleAdded(Triple triple);

    /**
     * Notifies this LabelManager that a triple is deleted from the model this LabelManager provides services for
     * (updates to the triple named graphs are irrelevant).
     *
     * @param triple the triple that was deleted from the model this LabelManager provides services for
     */
    void tripleDeleted(Triple triple);

    /**
     * Ensures that the labels are consistent with the model that this LabelManager provides services for.
     * This may need recalculating all indexes from scratch, updating missing labels, or even nothing
     * if this implementation incrementally maintains its labels based on the triple additions/deletions
     * notifications.
     *
     * <p>The caller may indicate predefined labels for any model's schema resource by providing a
     * {@link PredefinedLabels} implementation. It is not guaranteed that predefined labels will
     * always be respected.
     *
     * @param predefinedLabels a source of predefined labels, that must be respected by the underlying labeling
     * algorithm. If {@code predefinedLabels} is {@code null}, it will be ignored
     */
    void updateLabels(PredefinedLabels predefinedLabels);

    /**
     * Returns true if the first resource is an ancestor of the second resource (or if they are the same) or false otherwise, <em>according to the labels</em>.
     *
     * @param maybeAncestor the first resource, which is tested whether it is an ancestor of the second according to the labels
     * @param maybeDescendant the second resource, which is tested whether it is a descendant of the first according to the labels
     * @return true if the first resource is an ancestor of the second resource, false otherwise
     */
    boolean isFirstAncestorOfSecond(Resource maybeAncestor, Resource maybeDescendant);

    /**
     * Provides a hint whether a subsequent call to {@linkplain #isFirstAncestorOfSecond(Resource, Resource)} will be fast.
     *
     * @return true if labels are available (and subsequent calls to {@linkplain #isFirstAncestorOfSecond(Resource, Resource)} will be fast),
     * or false otherwise.
     */
    boolean areLabelsAvailable();

    /**
     * Returns the model that this LabelManager provides labeling services for.
     * 
     * @return the model that this LabelManager provides labeling services for
     */
    Model getTargetModel();
}
