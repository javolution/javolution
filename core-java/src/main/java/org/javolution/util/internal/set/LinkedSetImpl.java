/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;

import org.javolution.util.FastSet;
import org.javolution.util.FastTable;
import org.javolution.util.function.Order;
import org.javolution.util.internal.collection.ReadOnlyIteratorImpl;

/**
 * A linked view over a set.
 */
public final class LinkedSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastSet<E> inner;
    private final FastTable<E> insertionTable;

    public LinkedSetImpl(FastSet<E> inner) {
        this.inner = inner;
        insertionTable = FastTable.newTable(inner.equality()); 
    }

    private LinkedSetImpl(FastSet<E> inner, FastTable<E> insertionTable) {
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
    public LinkedSetImpl<E> clone() {
        return new LinkedSetImpl<E>(inner.clone(), insertionTable.clone());
    }

    @Override
    public Order<? super E> comparator() {
        return inner.comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return inner.contains(obj);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return ReadOnlyIteratorImpl.of(insertionTable.reversed().iterator());
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        FastTable<E> reversedTable = insertionTable.reversed();
        int index = reversedTable.indexOf(fromElement);
        if (index < 0) throw new IllegalArgumentException("Not found: " + fromElement);
        return ReadOnlyIteratorImpl.of(reversedTable.listIterator(index));
    }

    @Override
    public Iterator<E> iterator() {
        return ReadOnlyIteratorImpl.of(insertionTable.iterator());
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        int index = insertionTable.indexOf(fromElement);
        if (index < 0) throw new IllegalArgumentException("Not found: " + fromElement);
        return ReadOnlyIteratorImpl.of(insertionTable.listIterator(index));
    }

    @Override
    public boolean remove(Object obj) {
        boolean modified = inner.remove(obj);
        if (modified) insertionTable.remove(obj);
        return modified;
    }

    @Override
    public int size() {
        return insertionTable.size();
    }

    @Override
    public boolean isEmpty() {
        return insertionTable.isEmpty();
    }
}
