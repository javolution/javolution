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

/**
 * A reversed view over a set.
 */
public final class ReversedSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastSet<E> inner;

    public ReversedSetImpl(FastSet<E> inner) {
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
    public ReversedSetImpl<E> clone() {
        return new ReversedSetImpl<E>(inner.clone());
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
        return inner.iterator();
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return inner.iterator(fromElement);
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return inner.descendingIterator();
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return inner.descendingIterator(fromElement);
    }

    @Override
    public boolean remove(Object obj) {
        return inner.remove(obj);
    }

    @Override
    public int size() {
        return inner.size();
    }

}
