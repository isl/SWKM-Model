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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * An enumeration that denotes whether to apply a rule or request
 * transitively or not transitively.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public enum Transitively {
    /**
     * Denotes transitivety.
     */
    YES() {
        @Override
        public <T> Set<T> collect(T initialElement, Function<? super T, ? extends Iterable<? extends T>> function) {
            Set<T> set = Sets.newHashSet();
            LinkedList<T> unvisitedElements = Lists.newLinkedList();
            unvisitedElements.add(initialElement);
            while (!unvisitedElements.isEmpty()) {
                T element = unvisitedElements.removeFirst();
                Iterable<? extends T> nextElements = function.apply(element);
                if (nextElements == null) continue;
                for (T next : nextElements) {
                    if (set.contains(next)) continue;
                    set.add(next);
                    unvisitedElements.addLast(next);
                }
            }
            return set;
        }
    },

    /**
     * Denotes non-transitivety.
     */
    NO() {
        @Override
        public <T> Set<T> collect(T initialElement, Function<? super T, ? extends Iterable<? extends T>> function) {
            Iterable<? extends T> elements = function.apply(initialElement);
            if (elements instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<? extends T> set = (Set<? extends T>)elements;
                return Collections.unmodifiableSet(set);
            }
            if (elements == null) {
                return Collections.<T>emptySet();
            }
            return ImmutableSet.copyOf(elements);
        }
    };

    /**
     * Collects (transitively or not, depending on this instance) all elements that can be traversed through an initial element,
     * given an a exploring function that finds subsequent elements from an element.
     *
     * <p>If this method is called through {@linkplain Transitively#NO}, only the directly accessible elements from the initial one are returned.
     * If this method is called through {@linkplain Transitively#YES}, <em>all</em> accessible (directly or indirectly)
     * elements from the initial one are returned.
     *
     * <p>The initial element will only be contained in the result if it was explicitly returned by the exploring exploringFunction.
     *
     * <p>For example, consider a tree structure of elements of {@code Node} class, representing a tree node, which has a method {@code kids()} returning
     * its direct kids in the tree. The following is a function that computes for any element its direct kids:
     * {@code
     *<pre>
     *Function<Node, Iterable<Node>> kids = new Function<Node, Iterable<Node>>() {
     *    public Iterable<Node> apply(Node node) {
     *        return node.kids();
     *    }
     *}
     *</pre>}
     *Now, the following capabilities are available:
     * <ul>
     * <li>To collect the direct kids of a node {@code n}, invoke {@code Transitively.NO.collect(n, kids)}
     * <li>To collect <em>all</em> kids of a node, direct or indirect, {@code n}, invoke {@code Transitively.YES.collect(n, kids)}
     *</pre>}
     * <li>
     *
     * <p>So, for methods that offer arbitrary kinds of traversals, and offer the option of transitivity, it is convenient to accept
     * a single {@code Transitively} parameter, and directly calling {@linkplain #collect(Object, Function)}, with an appropriate function.
     * </ul>
     *
     * @param <T> the type of the elements to collect and collect
     * @param initialElement the element from which to start the traversing
     * @param exploringFunction the exploringFunction that explores any element and produces a set of subsequent elements;
     * may return an empty collection or null to indicate that a specific element leads to no subsequent elements
     * @return the set of traversed elements
     */
    public abstract <T> Set<T> collect(T initialElement,
            Function<? super T, ? extends Iterable<? extends T>> exploringFunction);
}
