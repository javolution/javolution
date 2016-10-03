/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;

/**
 * An unmodifiable view over a table. 
 */
public final class UnmodifiableTableImpl<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Unmodifiable View.";
    private final FastTable<E> inner;

    public UnmodifiableTableImpl(FastTable<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public UnmodifiableTableImpl<E> clone() {
        return new UnmodifiableTableImpl<E>(inner.clone());
    }

    @Override
    public Equality<? super E> equality() { // Immutable.
        return inner.equality();
    }

    @Override
    public E get(int index) {
        return inner.get(index);
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public UnmodifiableTableImpl<E> unmodifiable() {
        return this;
    }

}
