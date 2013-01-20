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
 * A sorted view over a table.
 */
public final class SortedTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public SortedTableImpl(AbstractTable<E> that) {
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
    public void add(int index, E element) {
       throw new UnsupportedOperationException(
            "Sorted tables don't allow arbitrary insertions (add(E) should be used)");
    }

    @Override
    public E remove(int index) {
        return that.remove(index);
    }
    
    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }

    @Override
    public SortedTableImpl<E> copy() {
        return new SortedTableImpl<E>(that.copy());
    }

    // 
    // Overrides methods impacted.
    //
    
    @Override
    public boolean add(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        that.add(i, element);
        return true;
    }
    
    @Override
    public int indexOf(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        if ((i < size()) && comparator().areEqual(element, get(i))) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        if ((i < size()) && comparator().areEqual(element, get(i))) {
            while ((++i < size()) && comparator().areEqual(element, get(i))) {}
            return --i;
        }
        return -1;
    }
    
    @Override
    public void sort() {
        // Do nothing, already sorted.
    }
    
    // Utility to find the "should be" position of the specified element.
    private int indexIfSortedOf(E element, FastComparator<E> comparator, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        return (comparator.compare(element, get(start + half)) <= 0)
                ? indexIfSortedOf(element, comparator, start, half)
                : indexIfSortedOf(element, comparator, start + half + 1, length - half - 1);
    }

}
