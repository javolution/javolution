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

import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> inner;
    private final Comparator<? super E> cmp;

    public SortedCollectionImpl(AbstractCollection<E> inner, Comparator<? super E> cmp) {
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
    public FastIterator<E> iterator() {
        FastTable<E> sorted = new FastTable<E>();
        sorted.addAll(inner);
        sorted.sort(cmp);
        return sorted.iterator();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        FastTable<E> sorted = new FastTable<E>();
        sorted.addAll(inner);
        sorted.sort(cmp);
        return sorted.descendingIterator();
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
    public AbstractCollection<E>[] trySplit(int n) {
        return inner.trySplit(n);
    }

}
