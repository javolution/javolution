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

import org.javolution.util.FastCollection;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A view tracking the insertion order.
 */
public final class LinkedCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastCollection<E> inner;
    private final FractalTable<E> insertionTable;

    public LinkedCollectionImpl(FastCollection<E> inner) {
        this.inner = inner;
        insertionTable = new FractalTable<E>(inner.equality());
    }

    private LinkedCollectionImpl(FastCollection<E> inner, FractalTable<E> insertionTable) {
        this.inner = inner;
        this.insertionTable = insertionTable;
    }

    @Override
    public boolean add(E element) {
        boolean added = inner.add(element);
        if (added)
            insertionTable.add(element);
        return added;
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
    public Iterator<E> iterator() {
        return insertionTable.unmodifiable().iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean modified = inner.removeIf(filter);
        if (modified)
            insertionTable.removeIf(filter);
        return modified;
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
