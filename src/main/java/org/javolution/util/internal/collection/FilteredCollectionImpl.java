/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Predicate<? super E> filter;

    private final AbstractCollection<E> inner;

    public FilteredCollectionImpl(AbstractCollection<E> inner, Predicate<? super E> filter) {
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
    public AbstractCollection<E> clone() {
        return new FilteredCollectionImpl<E>(inner.clone(), filter);
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public FastIterator<E> iterator() {
        return new IteratorImpl<E>(inner.iterator(), filter);
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return new IteratorImpl<E>(inner.descendingIterator(), filter);
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
    public AbstractCollection<E>[] trySplit(int n) {
        AbstractCollection<E>[] subViews = inner.trySplit(n);
        for (int i = 0; i < subViews.length; i++)
            subViews[i] = new FilteredCollectionImpl<E>(subViews[i], filter);
        return subViews;
    }

    /** Returns an iterator filtering elements iterated. */
    public static final class IteratorImpl<E> implements FastIterator<E> {
        private final FastIterator<E> innerItr;
        private final Predicate<? super E> filter;
        
        public IteratorImpl(FastIterator<E> innerItr, Predicate<? super E> filter) {
            this.innerItr = innerItr;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return innerItr.hasNext(filter);
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            return innerItr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext(final Predicate<? super E> matching) {
            return innerItr.hasNext(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return filter.test(param) && matching.test(param);
                }});
        }

    }
}
