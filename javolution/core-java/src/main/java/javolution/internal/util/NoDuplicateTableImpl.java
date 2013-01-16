/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Arrays;
import javolution.lang.MathLib;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;

/**
 * A view for which elements are not added if already present.
 */
public final class NoDuplicateTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public NoDuplicateTableImpl(AbstractTable<E> that) {
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
        return that.set(index, element);
    }

    @Override
    public void shiftLeftAt(int index, int shift) {
        that.shiftLeftAt(index, shift);
    }

    @Override
    public void shiftRightAt(int index, int shift) {
        that.shiftRightAt(index, shift);
    }

    // 
    // Overrides methods impacted.
    //
    
    @Override
    public boolean add(E element) {
        if (indexOf(element) >= 0) return false; // Already present.
        return that.add(element);
    }

    @Override
    public void addFirst(E element) {
        if (indexOf(element) >= 0) return; // Already present.
        that.addFirst(element);
    }

    @Override
    public void addLast(E element) {
        if (indexOf(element) >= 0) return; // Already present.
        that.addLast(element);
    }

    @Override
    public void add(int i, E element) {
        if (indexOf(element) >= 0) return; // Already present.
        that.add(i, element);
    }

    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }

    @Override
    public NoDuplicateTableImpl<E> copy() {
        return new NoDuplicateTableImpl<E>(that.copy());
    }

}
