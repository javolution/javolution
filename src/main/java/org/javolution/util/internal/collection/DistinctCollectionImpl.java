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
import org.javolution.util.FractalTable;
import org.javolution.util.SparseSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A view which does not iterate twice over the same elements.
 */
public final class DistinctCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastCollection<E> inner;
    private final Equality<? super E> equality;

    public DistinctCollectionImpl(FastCollection<E> inner, Equality<? super E> equality) {
        this.inner = inner;
        this.equality = equality;
    }

    @Override
    public boolean add(E element) {
        return contains(element) ? false : inner.add(element);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public DistinctCollectionImpl<E> clone() {
        return new DistinctCollectionImpl<E>(inner.clone(), equality);
    }

    @Override
    public Equality<? super E> equality() {
        return equality;
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<E> iterator() {
        final FastCollection<E> iterated = equality instanceof Order ? new SparseSet<E>((Order<E>)equality) :
            new FractalTable<E>(equality);
        return new Iterator<E>() {
            Iterator<E> itr = inner.iterator();
            boolean currentIsNext;
            E current;

            @Override
            public boolean hasNext() {
                if (currentIsNext)
                    return true;
                while (itr.hasNext()) {
                    current = itr.next();
                    if (iterated.contains(current))
                        continue; // Ignore.
                    currentIsNext = true;
                    iterated.add(current);
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
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean remove(final Object searched) { // Remove all occurrences.
        return inner.removeIf(new Predicate<E>() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean test(E param) {
                return equality.areEqual((E) searched, param);
            }
        });
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return inner.removeIf(filter);
    }

    @Override
    public int size() {
        int count = 0;
        for (Iterator<E> itr = iterator(); itr.hasNext(); itr.next())
            count++;
        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FastCollection<E>[] trySplit(int n) {
        return new FastCollection[] { this }; // Does not split.
    }

}
