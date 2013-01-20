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
 * A view using a custom comparator for object comparison and sorting.
 */
public final class CustomComparatorTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    private final FastComparator<E> comparator;

    public CustomComparatorTableImpl(AbstractTable<E> that, FastComparator<E> comparator) {
        this.that = that;
        this.comparator = comparator;
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
        return that.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        that.add(index, element);
    }

    @Override
    public E remove(int index) {
        return that.remove(index);
    }

    @Override
    public FastComparator<E> comparator() {
        return comparator;
    }

    @Override
    public CustomComparatorTableImpl<E> copy() {
        return new CustomComparatorTableImpl<E>(that.copy(), comparator.copy());
    }

}
