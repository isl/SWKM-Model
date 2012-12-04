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

import org.springframework.util.Assert;

/**
 * A label, consisting of a tree label (which is a simple {@link Interval}), and direct and indirect propagated
 * labels ({@link CompoundInterval} both).
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public final class Label {
    private Interval treeLabel;
    private final CompoundInterval directPropagatedLabels;
    private final CompoundInterval indirectPropagatedLabels;

    /**
     * Creates a label with the specified tree label.
     */
    public Label(Interval treeLabel) {
        setTreeLabel(treeLabel);
        this.directPropagatedLabels = new CompoundInterval();
        this.indirectPropagatedLabels = new CompoundInterval();
    }

    private Label(Interval treeLabel,
            CompoundInterval directPropagatedLabels,
            CompoundInterval indirectPropagatedLabels) {
        this.treeLabel = treeLabel;
        this.directPropagatedLabels = directPropagatedLabels;
        this.indirectPropagatedLabels = indirectPropagatedLabels;
    }

    public static Label create(int index, int post) {
        return new Label(new Interval(index, post));
    }

    public Interval getTreeLabel() {
        return treeLabel;
    }

    public void setTreeLabel(Interval treeLabel) {
        if (treeLabel == null) {
            treeLabel = Interval.empty();
        }
        this.treeLabel = treeLabel;
    }

    /**
     * Adds a propagated label to this label, which may be either direct or indirect.
     * @param label the propagated label to add
     * @param direct whether the added label is direct (<tt>true</tt>) or not (<tt>false</tt>).
     */
    public boolean addPropagatedLabel(Interval label, boolean direct) {
        Assert.notNull(label);
        if (treeLabel.contains(label)) {
            return false;
        }
        if (treeLabel.overlapsWith(label)) {
            throw new IllegalArgumentException("Illegal propagated label: " +label + ", " +
                    "must not overlap with, or must be contained the tree label: " + treeLabel);
        }
        CompoundInterval intervals = direct ? directPropagatedLabels : indirectPropagatedLabels;
        return intervals.add(label);
    }

    public boolean removePropagatedLabel(Interval label, boolean direct) {
        Assert.notNull(label);
        CompoundInterval intervals = direct ? directPropagatedLabels : indirectPropagatedLabels;
        return intervals.remove(label);
    }

    /**
     * Returns the propagated labels of this label. The returned {@link CompoundInterval} is safe
     * to modify, i.e. there are no side-effects to this label.
     * @param direct <tt>true</tt> if requesting the direct propagated labels, <tt>false</tt> if requesting the indirect
     */
    public CompoundInterval getPropagatedLabels(boolean direct) {
        CompoundInterval intervals = direct ? directPropagatedLabels : indirectPropagatedLabels;
        return new CompoundInterval(intervals);
    }

    public Label copy() {
        return new Label(treeLabel,
                new CompoundInterval(directPropagatedLabels),
                new CompoundInterval(indirectPropagatedLabels));
    }

    public boolean contains(int point) {
        return treeLabel.contains(point)
                || directPropagatedLabels.contains(point)
                || indirectPropagatedLabels.contains(point);
    }

    public boolean contains(Interval interval) {
        return treeLabel.contains(interval)
                || directPropagatedLabels.contains(interval)
                || indirectPropagatedLabels.contains(interval);
    }

    public boolean contains(Label label) {
        return treeLabel.contains(label.treeLabel)
                || directPropagatedLabels.contains(label.treeLabel)
                || indirectPropagatedLabels.contains(label.treeLabel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("[").append(treeLabel);
        if (!directPropagatedLabels.isEmpty()) {
            sb.append(", direct=" + directPropagatedLabels);
        }
        if (!indirectPropagatedLabels.isEmpty()) {
            sb.append(", indirect=" + indirectPropagatedLabels);
        }
        sb.append("]");
        return sb.toString();
    }

    public static Label newEmpty() {
        return new Label(Interval.empty());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Label)) {
            return false;
        }
        Label that = (Label)o;
        return this.treeLabel.equals(that.treeLabel) &&
                this.directPropagatedLabels.equals(that.directPropagatedLabels) &&
                this.indirectPropagatedLabels.equals(that.indirectPropagatedLabels);
    }

    @Override
    public int hashCode() {
        return 37
                + 11 * treeLabel.hashCode()
                + 17 * directPropagatedLabels.hashCode()
                + 31 * indirectPropagatedLabels.hashCode();
    }
}
