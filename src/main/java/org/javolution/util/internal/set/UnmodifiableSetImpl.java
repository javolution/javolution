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
 * An unmodifiable view over a set.
 */
public final class UnmodifiableSetImpl<E> extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Unmodifiable View.";
    private final AbstractSet<E> inner;

    public UnmodifiableSetImpl(AbstractSet<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
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
    public int size() {
        return inner.size();
    }

    @Override
    public UnmodifiableSetImpl<E> unmodifiable() {
        return this;
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
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public E getAny(E element) {
        return inner.getAny(element);
    }

    @Override
    public E removeAny(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }
    
}
