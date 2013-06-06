/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.io.Serializable;
import java.util.Iterator;

import javolution.util.FastSet;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A view which does not iterate twice over the same elements and which 
 * maintains element unicity.
 */
public class DistinctCollectionImpl<E> implements CollectionService<E>,
        Serializable {

    protected final CollectionService<E> that;

    public DistinctCollectionImpl(CollectionService<E> that) {
        this.that = that;
    }

    @Override
    public boolean add(E element) {
        return contains(element) ? false : that.add(element);
    }

    private boolean contains(final E element) {
        final ComparatorService<? super E> cmp = that.comparator();
        return !that.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                return !cmp.areEqual(element, param);
            }
        });
    }

    @Override
    public boolean doWhile(final Predicate<? super E> predicate) {
        final FastSet<E> iterated = new FastSet<E>(that.comparator());
        return that.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                if (iterated.contains(param)) return true; // Ignores.
                iterated.add(param);
                return predicate.test(param);
            }});
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return that.removeIf(predicate);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        final FastSet<E> iterated = new FastSet<E>(that.comparator());
        return new Iterator<E>() {
            E next = null; // Next element not already iterated over. 
            boolean peekNext; // If the next element has been read in advance.

            @Override
            public boolean hasNext() {
                if (peekNext) return true;
                while (true) {
                    if (!thatIterator.hasNext()) return false;
                    next = thatIterator.next();
                    if (!iterated.contains(next)) {
                        iterated.add(next);
                        peekNext = true;
                        return true;
                    }
                }
            }

            @Override
            public E next() {                
                if (peekNext) { // Usually true (hasNext has been called before). 
                    peekNext = false;
                    return next;
                }
                while (true) {
                    next = thatIterator.next();
                    if (!iterated.contains(next)) {
                        iterated.add(next);
                        return next;
                    }
                }
            }

            @Override
            public void remove() {
                thatIterator.remove();
            }

        };
    }  

    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null)
            return null;
        DistinctCollectionImpl<E>[] sorteds = new DistinctCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            sorteds[i] = new DistinctCollectionImpl<E>(tmp[i]);
        }
        return sorteds;
    }

    private static final long serialVersionUID = 3758464317713857912L; 
}
