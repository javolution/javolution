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
 * A reverse view over a table.
 */
public final class ReverseTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public ReverseTableImpl(AbstractTable<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(size() - 1 - index);
    }

    @Override
    public E set(int index, E element) {
        return that.set(size() - 1 - index, element);
    }

    @Override
    public void shiftLeftAt(int index, int shift) {
        that.shiftLeftAt(size() - index - shift, shift);
    }

    @Override
    public void shiftRightAt(int index, int shift) {
        that.shiftRightAt(size() - index, shift);
    }

    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }

    @Override
    public ReverseTableImpl<E> copy() {
        return new ReverseTableImpl<E> (that.copy());
    }

}
