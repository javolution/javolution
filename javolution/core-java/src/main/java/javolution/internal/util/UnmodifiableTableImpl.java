/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import javolution.util.AbstractTable;
import javolution.util.FastComparator;

/**
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public UnmodifiableTableImpl(AbstractTable<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }

    @Override
    public UnmodifiableTableImpl<E> copy() {
        return new UnmodifiableTableImpl<E>(that.copy());
    }

}
