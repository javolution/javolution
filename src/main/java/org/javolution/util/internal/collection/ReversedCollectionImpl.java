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
import org.javolution.util.function.Predicate;

/**
 * A reversed view over a collection.
 */
public final class ReversedCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> inner;

    public ReversedCollectionImpl(AbstractCollection<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public AbstractCollection<E> clone() {
        return new ReversedCollectionImpl<E>(inner.clone());
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
        return inner.descendingIterator();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return inner.iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return inner.removeIf(filter);
    }

    @Override
    public AbstractCollection<E> reversed() {
        return inner;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public AbstractCollection<E>[] trySplit(int n) {
        return inner.trySplit(n);
    }

}