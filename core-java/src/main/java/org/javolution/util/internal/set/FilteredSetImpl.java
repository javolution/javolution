/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;

import org.javolution.util.FastSet;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.FilteredCollectionImpl.FilteredIterator;

/**
 * A filtered view over a set.
 */
public final class FilteredSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Predicate<? super E> filter;
    private final FastSet<E> inner;

    public FilteredSetImpl(FastSet<E> inner, Predicate<? super E> filter) {
        this.inner = inner;
        this.filter = filter;
    }

    @Override
    public boolean add(E element) {
        if (!filter.test(element))
            return false;
        return inner.add(element);
    }

    @Override
    public void clear() {
        removeIf(Predicate.TRUE);
    }

    @Override
    public FastSet<E> clone() {
        return new FilteredSetImpl<E>(inner.clone(), filter);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public Iterator<E> iterator() {
        return new FilteredIterator<E>(inner.iterator(), filter);
    }

    @Override
    public boolean removeIf(final Predicate<? super E> toRemove) {
        return inner.removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return filter.test(param) && toRemove.test(param);
            }
        });
    }

    @Override
    public int size() {
        int count = 0;
        for (Iterator<E> itr = iterator(); itr.hasNext(); itr.next())
            count++;
        return count;
    }

    @Override
    public FastSet<E>[] trySplit(int n) {
        FastSet<E>[] subViews = inner.trySplit(n);
        for (int i = 0; i < subViews.length; i++)
            subViews[i] = new FilteredSetImpl<E>(subViews[i], filter);
        return subViews;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        if (!filter.test((E)obj))
            return false;
        return inner.contains(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        if (!filter.test((E)obj))
            return false;
        return inner.remove(obj);
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return new FilteredIterator<E>(inner.iterator(fromElement), filter);
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return new FilteredIterator<E>(inner.descendingIterator(fromElement), filter);
    }

    @Override
    public Order<? super E> comparator() {
        return inner.comparator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new FilteredIterator<E>(inner.descendingIterator(), filter);
    }

}
