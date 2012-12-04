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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.util.Assert;

/**
 * A collection of simple {@link Interval intervals}.
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public final class CompoundInterval implements Iterable<Interval> {
    private Set<Interval> intervals = new LinkedHashSet<Interval>();
    
    public CompoundInterval() {
    }
    
    public CompoundInterval(Interval interval) {
        add(interval);
    }
    
    public CompoundInterval(Iterable<Interval> intervals) {
        for (Interval interval : intervals) {
            add(interval);
        }
    }

    public CompoundInterval(CompoundInterval copy) {
        add(copy);
    }
    
    public void set(Interval interval) {
        clear();
        if (interval != null && !interval.isEmpty()) {
            intervals.add(interval);
        }
    }
    
    //returns true if actual modification happened
    public boolean add(CompoundInterval newIntervals) {
        Assert.notNull(newIntervals);
        boolean changed = false;
        for (Interval interval : newIntervals.getIntervals()) {
            changed |= add(interval);
        }
        return changed;
    }
    
    //returns true if actual modification happened
    public boolean add(Interval newInterval) {
        Assert.notNull(newInterval);
        if (newInterval.isEmpty()) {
            return false;
        }
        return intervals.add(newInterval);
    }
    
    //returns true if actual modification happened
    public boolean remove(Interval removedInterval) {
        return intervals.remove(removedInterval);
    }
    
    public boolean contains(int point) {
        for (Interval current : intervals) {
            if (current.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public Iterator<Interval> iterator() {
        return intervals.iterator();
    }
    
    public boolean contains(Interval interval) {
        Assert.notNull(interval);
        for (Interval current : intervals) {
            if (current.contains(interval)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(CompoundInterval compoundInterval) {
        Assert.notNull(compoundInterval);
        for (Interval interval : compoundInterval.getIntervals()) {
            if (!contains(interval)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean overlapsWith(CompoundInterval compoundInterval) {
        Assert.notNull(compoundInterval);
        for (Interval interval : compoundInterval.getIntervals()) {
            if (overlapsWith(interval)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean overlapsWith(Interval interval) {
        Assert.notNull(interval);
        for (Interval ownInterval : getIntervals()) {
            if (ownInterval.overlapsWith(interval)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return intervals.toString();
    }
    
    public Set<Interval> getIntervals() {
        return Collections.unmodifiableSet(intervals);
    }
    
    public boolean isEmpty() {
        return intervals.isEmpty();
    }
    
    public void clear() {
        intervals.clear();
    }
    
    public Interval maxInterval() {
        if (isEmpty()) {
            return Interval.empty();
        }
        Interval max = null;
        for (Interval interval : intervals) {
            if (max == null || max.length() < interval.length()) {
                max = interval;
            }
        }
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompoundInterval)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        CompoundInterval other = (CompoundInterval) obj;
        return intervals.equals(other.intervals);
    }

    @Override
    public int hashCode() {
        return intervals.hashCode();
    }
}
