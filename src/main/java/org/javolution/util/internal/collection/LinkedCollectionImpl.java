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
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A view tracking the insertion order.
 */
public final class LinkedCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> inner;
    private final FastTable<E> insertionTable = new FastTable<E>();

    public LinkedCollectionImpl(AbstractCollection<E> inner) {
        this.inner = inner;
    }

    private LinkedCollectionImpl(AbstractCollection<E> inner, FastTable<E> insertionTable) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element) ? insertionTable.add(element) : false;
    }

    @Override
    public void clear() {
        inner.clear();
        insertionTable.clear();
    }

    @Override
    public LinkedCollectionImpl<E> clone() {
        return new LinkedCollectionImpl<E>(inner.clone(), insertionTable.clone());
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
        return insertionTable.iterator();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return insertionTable.descendingIterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return inner.removeIf(filter) ? insertionTable.removeIf(filter) : false;
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
