/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Predicate;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E, R> extends AbstractCollection<R> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> inner;
    private final Function<? super E, ? extends R> function;

    public MappedCollectionImpl(AbstractCollection<E> inner, Function<? super E, ? extends R> function) {
        this.inner = inner;
        this.function = function;
    }

    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public void clear() {
        inner.clear();

    }

    @Override
    public MappedCollectionImpl<E, R> clone() {
        return new MappedCollectionImpl<E, R>(inner.clone(), function);
    }

    @Override
    public Equality<? super R> equality() {
        return Equality.standard();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public FastIterator<R> iterator() {
        return new IteratorImpl<E, R>(inner.iterator(), function);
    }

    @Override
    public FastIterator<R> descendingIterator() {
        return new IteratorImpl<E, R>(inner.descendingIterator(), function);
    }

    @Override
    public boolean removeIf(final Predicate<? super R> toRemove) {
        return inner.removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return toRemove.test(function.apply(param));
            }
        });
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AbstractCollection<R>[] trySplit(int n) {
        AbstractCollection[] subViews = inner.trySplit(n);
        for (int i = 0; i < subViews.length; i++)
            subViews[i] = new MappedCollectionImpl(subViews[i], function);
        return subViews;
    }

    /** Iterator over mapped collections. */
    private static final class IteratorImpl<E, R> implements FastIterator<R> {
        private final FastIterator<E> innerItr;
        private final Function<? super E, ? extends R> function;

        private IteratorImpl(FastIterator<E> innerItr, Function<? super E, ? extends R> function) {
            this.innerItr = innerItr;
            this.function = function;
        }

        @Override
        public boolean hasNext() {
            return innerItr.hasNext();
        }

        @Override
        public R next() {
            return function.apply(innerItr.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext(final Predicate<? super R> matching) {
            return innerItr.hasNext(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return matching.test(function.apply(param));
                }});
        }

    }

}
