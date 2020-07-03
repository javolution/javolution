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
import org.javolution.util.FractalArray;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.AbstractSet;

/**
 * A sorted set relying on order comparison  only. The performance of the set is degraded in O(Log(n)) or even O(n) 
 * when the comparator returns 0 for all elements (bag).
 */
public final class SortedSetImpl<E> extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Order<? super E> comparator;
    private FractalArray<E> sorted = FractalArray.empty();
    private int size;
    
    public SortedSetImpl(Order<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
        int i = firstIndex(element, 0, size);
        if (!allowDuplicate && (i < size) && comparator.areEqual(element, sorted.get(i))) return false;
        sorted.insert(i, element);
        size++;
        return true;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        int initialSize = size;
        for (FractalArray.Iterator<E> itr = sorted.iterator(); itr.hasNext(filter);) {
            sorted.set(itr.nextIndex(), null);
            --size;
        }
        return initialSize != size;
    }

    @Override
    public E findAny() {
        return sorted.get(0);
    }

    @Override
    public E getAny(E element) {
        int first = firstIndex(element, 0, size);
        int last = lastIndex(element, 0, size);
        for (int i = first; i <= last; i++) {
            E e = sorted.get(i);
            if (comparator.areEqual(element, e)) return e; 
        }
        return null;
    }

    @Override
    public E removeAny(E element) {
        int first = firstIndex(element, 0, size);
        int last = lastIndex(element, 0, size);
        for (int i = first; i <= last; i++) {
            E e = sorted.get(i);
            if (comparator.areEqual(element, e)) {
                sorted = sorted.delete(i);
                size--;
                return e;
            }
        }
        return null;
    }

    @Override
    public Order<? super E> order() {
        return comparator;
    }

    @Override
    public FastIterator<E> iterator(E low) {
        return sorted.iterator(firstIndex(low, 0, size));
    }

    @Override
    public FastIterator<E> descendingIterator(E high) {
        return sorted.descendingIterator(lastIndex(high, 0, size));
    }

    @Override
    public boolean isEmpty() {
        return size != 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        sorted = FractalArray.empty();
        size = 0;
    } 

    /** Find the first position real or "would be" of the specified element in the given range. */
    private int firstIndex(E element, int start, int end) {
        if (start == end) return start;
        int midIndex = (start + end) >> 1;
        return comparator.compare(element, sorted.get(midIndex)) <= 0 ? firstIndex(element, start, midIndex) :
            firstIndex(element, midIndex + 1, end);
    }

    /** In sorted table find the last position real or "would be" of the specified element in the given range. */
    private <K> int lastIndex(E element, int start, int end) {
        if (start == end) return start;
        int midIndex = (start + end) >> 1;
        return comparator.compare(element, sorted.get(midIndex)) < 0 ? lastIndex(element, start, midIndex) :
            lastIndex(element, midIndex + 1, end);
    }
    
}
