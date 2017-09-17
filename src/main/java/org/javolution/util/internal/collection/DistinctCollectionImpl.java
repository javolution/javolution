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
import org.javolution.util.FastSet;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A view which does not iterate twice over the same elements.
 */
public final class DistinctCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> inner;

    public DistinctCollectionImpl(AbstractCollection<E> inner) {
        this.inner = inner;
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
        return new DistinctCollectionImpl<E>(inner.clone());
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public FastIterator<E> iterator() {
        return new IteratorImpl<E>(inner.iterator(), inner.equality());
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return new IteratorImpl<E>(inner.descendingIterator(), inner.equality());
    }

    @Override
    public boolean remove(final Object searched) { // Remove all occurrences.
        return inner.removeIf(new Predicate<E>() {
            final Equality<? super E> equality = inner.equality();
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
    public AbstractCollection<E>[] trySplit(int n) {
        return new AbstractCollection[] { this }; // Does not split.
    }

    /** Iterator not returning twice the same element. */
    private static final class IteratorImpl<E> implements FastIterator<E> {
        private final FastIterator<E> innerItr;
        private final AbstractCollection<E> iterated;
        private E current;
        private boolean currentIsNext;

        @SuppressWarnings("unchecked")
        private IteratorImpl(FastIterator<E> innerItr, Equality<? super E> equality) {
            this.innerItr = innerItr;
            iterated = equality instanceof Order ? new FastSet<E>((Order<E>) equality)
                    : new FastTable<E>().equality(equality);
       }

        @Override
        public boolean hasNext() {
            if (currentIsNext) return true;
            while (innerItr.hasNext()) {
                current = innerItr.next();
                if (iterated.contains(current)) continue; // Ignore.
                currentIsNext = true;
                iterated.add(current);
                return true;
            }
            return false;
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext(Predicate<? super E> matching) {
            while (hasNext()) {
                if (matching.test(current)) return true; // currentIsNext always true.
                next(); 
            }
            return false;
        }
    }

}
