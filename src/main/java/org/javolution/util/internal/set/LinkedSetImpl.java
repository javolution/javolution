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

import org.javolution.util.ConstSet;
import org.javolution.util.FastSet;
import org.javolution.util.FastTable;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * A linked view over a set.
 */
public final class LinkedSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private FastSet<E> inner;
    private FastTable<E> insertionTable = new FractalTable<E>(Equality.IDENTITY);

    public LinkedSetImpl(FastSet<E> inner) {
        this.inner = inner;
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
        LinkedSetImpl<E> copy = (LinkedSetImpl<E>)super.clone();
        copy.inner = inner.clone();
        copy.insertionTable = insertionTable.clone();
        return copy;
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @Override
    public boolean contains(Object obj) {
        return inner.contains(obj);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return insertionTable.reversed().unmodifiable().iterator();
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        E start = inner.floor(fromElement);
        if (start == null) return ConstSet.<E>empty().iterator();
        FastTable<E> reversedTable = insertionTable.reversed();
        int index = reversedTable.indexOf(start);
        if (index < 0) throw new AssertionError();
        return reversedTable.unmodifiable().listIterator(index);
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
    public Iterator<E> iterator(E fromElement) {
        E start = inner.ceiling(fromElement);
        if (start == null) return ConstSet.<E>empty().iterator();
        int index = insertionTable.indexOf(start);
        if (index < 0) throw new AssertionError();
        return insertionTable.unmodifiable().listIterator(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        boolean modified = inner.remove(obj);
        if (modified)
            insertionTable.remove(inner.ceiling((E)obj)); // Remove the stored instance.
        return modified;
    }

    @Override
    public int size() {
        return inner.size();
    }
    
}
