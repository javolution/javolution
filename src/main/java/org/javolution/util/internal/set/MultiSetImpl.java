/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import org.javolution.util.FastIterator;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.AbstractSet;

/**
 * A view for which default 'add' allows duplicate.
 */
public final class MultiSetImpl<E> extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;

    public MultiSetImpl(AbstractSet<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element, true);
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
        return inner.add(element, true);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public MultiSetImpl<E> clone() {
        return new MultiSetImpl<E>(inner.clone());
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return inner.descendingIterator();
    }

    @Override
    public FastIterator<E> iterator() {
        return inner.iterator();
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @Override
    public FastIterator<E> iterator(E from) {
        return inner.iterator(from);
    }

    @Override
    public FastIterator<E> descendingIterator(E from) {
        return inner.descendingIterator(from);
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return inner.removeIf(filter);
    }

    @Override
    public E getAny(E element) {
        return inner.removeAny(element);
    }

    @Override
    public E removeAny(E element) {
        return inner.removeAny(element);
    }
    
}
