/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import org.javolution.util.FractalArray;

/**
 * The dense array default implementation. The strategy when adding elements is to increase 
 * the array capacity as long as the capacity is kept less or equal to {@code (size() << MAX_CAPACITY_SHIFT)}. 
 * The reverse occurs when elements are removed (the array capacity is reduced to avoid reaching the capacity limit). 
 * The same instance is used for size up to {@link #MAX_SIZE}; for greater sizes, {@link DenseArrayFractalImpl fractal} 
 * instances are returned. 
 */
public final class DenseArrayImpl<E> extends FractalArray<E> {
    private static final long serialVersionUID = 0x700L;
    private static final Object[] NONE = new Object[0];

    /** Defines the maximum factor between the number of elements and the array capacity (memory footprint). 
     *  The array capacity is automatically adjusted up or down to ensure that this limit is always respected. */
    public static final int MAX_CAPACITY_SHIFT = 2; // Should be >= 1

    /** Holds the maximum size supported by instances of this class (to bound worst case execution time). */
    public static final int MAX_SIZE = 256;

    @SuppressWarnings("unchecked")
    private E[] elements = (E[]) NONE;
    private int offset; // Index of first element.
    private int last = -1; // Index of the last element different from null
    
    /** Default constructor (empty instance). */
    public DenseArrayImpl() {
    }
    
    @Override
    public boolean isEmpty() {
        return last < 0;
    }

    @Override
    public E get(int index) {
        if (index < 0) negativeIndexException(index);
        return (index <= last) ? getNoCheck(index) : null;
    }

    @Override
    public FractalArray<E> set(int index, E element) {
        if (index < 0) negativeIndexException(index);
        if (element == null) return clear(index);
        if (index > last) {
            if (index >= elements.length) return upsize().set(index, element);
            last = index;
        }
        setNoCheck(index, element);
        return this;
    }

    @Override
    public DenseArrayImpl<E> remove(int index) {
        if (index < 0) negativeIndexException(index);
        if (index >= last) return clear(index); // Nothing to shift.
        if (last - index < index) {
            shiftLeft(index, last, null);
        } else {
            shiftRight(0, index, null);
            offset++;
        }
        return (last-- << MAX_CAPACITY_SHIFT < elements.length) ? downsize() : this;
    }

    @Override
    public FractalArray<E> add(int index, E inserted) {
        if (index < 0) negativeIndexException(index);
        if (index > last) return set(index, inserted); // Nothing to shift.
        if (last + 1 >= elements.length) return upsize().add(index, inserted);
        if (++last - index < index) {
            shiftRight(index, last, inserted);
        } else {
            offset--;
            shiftLeft(0, index, inserted);
        }
        return this;
    }

    private DenseArrayImpl<E> clear(int index) {
        if (index > last) return this; // Nothing to clear.
        setNoCheck(index, null);
        if (index != last) return this;
        while (last >= 0)
            if (getNoCheck(--last) != null) break;
        if ((last + 1) << MAX_CAPACITY_SHIFT < elements.length) downsize();
        return this;
    }
    
    private static void negativeIndexException(int index) {
        throw new ArrayIndexOutOfBoundsException("Negative index: " + index);
    }

    private void shiftLeft(int head, int tail, E insertTail) {
        for (int i = head; i != tail;)
            setNoCheck(i++, getNoCheck(i));
        setNoCheck(tail, insertTail);
    }

    public void shiftRight(int head, int tail, E insertHead) {
        for (int i = tail; i != head;)
            setNoCheck(i--, getNoCheck(i));
        setNoCheck(head, insertHead);
    }

    @Override
    public DenseArrayImpl<E> clone() {
        DenseArrayImpl<E> copy = (DenseArrayImpl<E>) super.clone();
        copy.elements = elements.clone();
        return copy;
    }

    @SuppressWarnings("unchecked")
    private FractalArray<E> upsize() { // Doubles the capacity.
        if (elements.length == 0) {
            elements = (E[]) new Object[1 << MAX_CAPACITY_SHIFT];
            return this;
        } else if (elements.length * 2 <= MAX_SIZE) {
            E[] newElements = (E[]) new Object[elements.length * 2];
            for (int i=0; i <= last; i++)
                newElements[i] = getNoCheck(i);
            offset = 0;
            elements = newElements;
            return this;
        } else {
            return null; // TODO Returns fractal implementation.
        }
    }

    @SuppressWarnings("unchecked")
    private DenseArrayImpl<E> downsize() { // Reduces by a half or more the capacity (depends on last).
        if (last < 0) {
            elements = (E[]) NONE;
        } else {
            int capacity = elements.length;
            while ((last+1) << MAX_CAPACITY_SHIFT < capacity) 
                capacity <<= 1;
            E[] newElements = (E[]) new Object[capacity];
            for (int i=0; i <= last; i++)
                newElements[i] = getNoCheck(i);
             offset = 0;
             elements = newElements;
        }
        return this;
    }

    /////////////////////////////////////////////
    // Small convenient methods to be inlined. // 
    /////////////////////////////////////////////

    private E getNoCheck(int index) {
        return elements[(index + offset) & (elements.length - 1)];
    }

    private void setNoCheck(int index, E element) {
        elements[(index + offset) & (elements.length - 1)] = element;
    }

}
