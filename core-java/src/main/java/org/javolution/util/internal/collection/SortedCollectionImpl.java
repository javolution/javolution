/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.Comparator;
import java.util.Iterator;

import org.javolution.util.FastCollection;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastCollection<E> inner;
    private final Comparator<? super E> cmp;

    public SortedCollectionImpl(FastCollection<E> inner, Comparator<? super E> cmp) {
        this.inner = inner;
        this.cmp = cmp;
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
    public SortedCollectionImpl<E> clone() {
        return new SortedCollectionImpl<E>(inner.clone(), cmp);
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
    public Iterator<E> iterator() {
        FractalTable<E> sorted = new FractalTable<E>();
        sorted.addAllSorted(inner, cmp); // TODO: Compare performance with FastTable.sort()
        return sorted.unmodifiable().iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return inner.removeIf(filter);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public FastCollection<E>[] trySplit(int n) {
        return inner.trySplit(n);
    }

}
