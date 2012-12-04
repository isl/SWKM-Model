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


package gr.forth.ics.swkm.model2.index.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 * @param <E>
 */
public abstract class IteratorChooser<E> implements Iterator<E> {
    private static final IteratorChooser<?> EMPTY = new EmptyIteratorChooser();

    protected final Iterator<E> iterator;
    protected E next;
    protected E current;

    protected IteratorChooser(Iterator<E> iterator) {
        this.iterator = iterator;
        proceed();
    }

    private IteratorChooser() {
        iterator = null;
    }

    @SuppressWarnings(value="unchecked")
    public static <E> IteratorChooser<E> empty() {
        return (IteratorChooser<E>) EMPTY;
    }

    public boolean hasNext() {
        return next != null;
    }

    public E next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        try {
            return next;
        } finally {
            proceed();
        }
    }

    private void proceed() {
        current = next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (accept(next)) {
                return;
            }
        }
        next = null;
    }

    protected abstract boolean accept(E element);

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    private static class EmptyIteratorChooser extends IteratorChooser<Object> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        protected boolean accept(Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeAll() {}
    }
}
