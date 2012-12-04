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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.util.Assert;

/**
 * An interval defines a continuous, closed segment of integers. The segment is defined by
 * the <em>index</em> (lower integer) and the <em>post</em> (higher integer),
 * and contains all integers between index and post, including index and post themselves.
 * <p>
 * An interval is <em>empty</em> if <pre>index > post</pre>, and contains
 * no integer.
 * <p>
 * Intervals are immutable.
 * <p>
 * Methods of this class do not allow null arguments.
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public final class Interval {
    private static final Interval empty = new Interval(0, -1);
    
    private final int index;
    private final int post;
    
    /**
     * Creates an interval with the given index and post integers.
     * @param index the index (the lowest integer of the created interval)
     * @param post the post (the highest integer of the created interval)
     */
    public Interval(int index, int post) {
        this.index = index;
        this.post = post;
    }
    
    /**
     * Returns the index, i.e.&nbsp;the lowest integer of this interval.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Returns the post, i.e.&nbsp;the highest integer of this interval.
     */
    public int getPost() {
        return post;
    }
    
    /**
     * Returns whether this interval is empty (or if <pre>getPost() &lt; getIndex()</pre>).
     * An empty interval contains and overlaps with nothing.
     */
    public boolean isEmpty() {
        return post < index;
    }
    
    /**
     * Returns the intersection of this interval with another, or the greatest shared
     * interval of the two. The intersection of a non-empty
     * interval with an empty is the empty interval. 
     * 
     * @see #isEmpty()
     */
    public Interval intersection(Interval other) {
        Assert.notNull(other);
        if (this.isEmpty()) {
            return this;
        } else if (other.isEmpty()) {
            return other;
        } else {
            return new Interval(
                    Math.max(this.index, other.index),
                    Math.min(this.post, other.post));
        }
    }
    
    /**
     * Returns the union of this interval with another, or the smallest 
     * interval that contains both. The union of a non-empty
     * interval with an empty is the non-empty interval. 
     * 
     * @see #isEmpty()
     */
    public Interval union(Interval other) {
        Assert.notNull(other);
        if (this.isEmpty()) {
            return other;
        } else if (other.isEmpty()) {
            return this;
        } else {
            return new Interval(
                    Math.min(this.index, other.index),
                    Math.max(this.post, other.post));
        }
    }
    
    /**
     * Returns whether a given integer is contained in this interval, i.e.&nbsp;whether
     * <pre>index &lt;= point &amp;&amp; point &lt;= post</pre> holds.
     */
    public boolean contains(int point) {
        return index <= point && point <= post;
    }
    
    /**
     * Returns whether a this interval contains entirely another interval. If the
     * specified interval is empty, <pre>false</pre> is returned; an empty interval
     * is assumed to not be contained anywhere.
     */
    public boolean contains(Interval other) {
        Assert.notNull(other);
        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }
        return contains(other.index) && contains(other.post);
    }
    
    /**
     * Returns whether the intersection of this and another interval is non-empty. 
     */
    public boolean overlapsWith(Interval other) {
        Assert.notNull(other);
        if (this.isEmpty() || other.isEmpty()) {
            return false;
        } else {
            return contains(other.index) || other.contains(this.index);
        }
    }
    
    /**
     * Returns whether this interval is overlapping or immediately neighboring with the given one, so both
     * can be safely replaced by their union. For example,
     * <tt>[1, 5]</tt> and <tt>[6, 10]</tt> can be merged (to their union, <tt>[1, 10]</tt>),
     * as can <tt>[1, 5]</tt> and <tt>[4, 10]</tt> too,
     * while <tt>[1, 5]</tt> and <tt>[7, 10]</tt> cannot; their union would also contain <tt>6</tt>
     * which was included in neither interval.
     */
    public boolean canBeMergedWith(Interval other) {
        Interval union = this.union(other);
        return union.length() <= this.length() + other.length();
    }
    
    /**
     * Returns the number of points contained in this interval.
     */
    public int length() {
        return Math.max(0, post - index + 1);
    }
    
    /**
     * Creates a new interval with the specified index, and the same post number as this interval.
     */
    public Interval withIndex(int index) {
        return new Interval(index, post);
    }
    
    /**
     * Creates a new interval with the specified post, and the same index number as this interval.
     */
    public Interval withPost(int post) {
        return new Interval(index, post);
    }
    
    /**
     * Returns an empty interval.
     */
    public static Interval empty() {
        return empty;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Interval)) {
            return false;
        }
        Interval other = (Interval) obj;
        return this.index == other.index && this.post == other.post;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.index;
        hash = 59 * hash + this.post;
        return hash;
    }
    
    @Override
    public String toString() {
        return "[" + index + ", " + post + "]";
    }
}
