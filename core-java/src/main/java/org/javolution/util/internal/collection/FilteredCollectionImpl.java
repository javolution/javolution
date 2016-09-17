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

import org.javolution.util.FastCollection;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Predicate<? super E> filter;
    private final FastCollection<E> inner;

    public FilteredCollectionImpl(FastCollection<E> inner, Predicate<? super E> filter) {
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
    public FastCollection<E> clone() {
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
    public FastCollection<E>[] trySplit(int n) {
        FastCollection<E>[] subViews = inner.trySplit(n);
        for (int i = 0; i < subViews.length; i++)
            subViews[i] = new FilteredCollectionImpl<E>(subViews[i], filter);
        return subViews;
    }

    /** Returns an iterator filtering elements iterated. */
    public static class FilteredIterator<E> implements Iterator<E> {
        final Iterator<E> itr;
        final Predicate<? super E> filter;
        public FilteredIterator(Iterator<E> itr, Predicate<? super E> filter){
            this.itr = itr;
            this.filter = filter;
        }
        
        boolean currentIsNext;
        E current;

        @Override
        public boolean hasNext() {
            if (currentIsNext)
                return true;
            while (itr.hasNext()) {
                current = itr.next();
                if (!filter.test(current))
                    continue; // Ignore.
                currentIsNext = true;
                return true;
            }
            return false;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            if (currentIsNext)
                throw new IllegalStateException();
            itr.remove();
        }    
    }
}
