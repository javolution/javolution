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
import org.javolution.util.function.Function;

/**
 * A mapped view over a table.
 */
public final class MappedTableImpl<E, R> extends FastTable<R> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastTable<E> inner;
    private final Function<? super E, ? extends R> function;

    public MappedTableImpl(FastTable<E> inner, Function<? super E, ? extends R> function) {
        this.inner = inner;
        this.function = function;
    }

    @Override
    public void add(int index, R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public MappedTableImpl<E, R> clone() {
        return new MappedTableImpl<E, R>(inner.clone(), function);
    }

    @Override
    public Equality<? super R> equality() {
        return Equality.DEFAULT;
    }

    @Override
    public R get(int index) {
        return function.apply(inner.get(index));
    }

    @Override
    public R remove(int index) {
        return function.apply(inner.remove(index));
    }

    @Override
    public R set(int index, R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public int size() {
        return inner.size();
    }

}
