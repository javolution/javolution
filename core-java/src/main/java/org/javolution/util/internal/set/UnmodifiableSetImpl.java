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
import org.javolution.util.function.Order;
import org.javolution.util.internal.collection.UnmodifiableCollectionImpl.ReadOnlyIterator;

/**
 * An unmodifiable view over a set.
 */
public final class UnmodifiableSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Unmodifiable View.";
    private final FastSet<E> inner;

    public UnmodifiableSetImpl(FastSet<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public UnmodifiableSetImpl<E> clone() {
        return new UnmodifiableSetImpl<E>(inner.clone());
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
        return new ReadOnlyIterator<E>(inner.descendingIterator());
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return new ReadOnlyIterator<E>(inner.descendingIterator(fromElement));
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIterator<E>(inner.iterator());
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return new ReadOnlyIterator<E>(inner.iterator(fromElement));
    }

    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public UnmodifiableSetImpl<E> unmodifiable() {
        return this;
    }

}
