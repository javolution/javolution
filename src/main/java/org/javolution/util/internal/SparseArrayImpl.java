/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import static org.javolution.lang.MathLib.*;

import org.javolution.lang.MathLib;
import org.javolution.util.FastListIterator;
import org.javolution.util.SparseArray;

/**
 * The sparse array default implementation. There is not constraint on the index range (which is dynamically adjusted),
 * elements are ordered smallest indices first (unsigned). The strategy when collisions occur is to increase 
 * the array capacity as long as the capacity is kept less or equal to {@code (size() << MAX_CAPACITY_SHIFT)}. 
 * The reverse occurs when elements are removed (the array capacity is reduced to avoid reaching the capacity limit). 
 * The same instance is used for size up to {@link #MAX_SIZE}; for greater sizes, {@link SparseArrayFractalImpl fractal} 
 * instances are returned. 
 */
public final class SparseArrayImpl<E> extends SparseArray<E> {
    private static final long serialVersionUID = 0x700L;

    /** Defines the maximum factor between the number of elements and the array capacity (memory footprint). 
     *  The array capacity is automatically adjusted up or down to ensure that this limit is always respected. */
    public static final int MAX_CAPACITY_SHIFT = 2; // Should be >= 1

    /** Holds the maximum size supported by instances of this class (to bound worst case execution time). */
    public static final int MAX_SIZE = 256;

    private static final int[] NONE = new int[0]; // When empty.
    private int count; // Number of elements different from null.
    private int shift; // Minimum unsigned right shift for index to array position.
    private int[] indices = NONE; // In range [0 .. count << MAX_CAPACITY_SHIFT] 
    private E[] elements;

    /** Default constructor (empty instance). */
    public SparseArrayImpl() {
    }

    public int size() {
        return count;
    }

    @Override
    public E get(int index) {
        int slot = index >>> shift;
        return ((slot < indices.length) && (slot >= 0)) ? (index == indices[slot]) ? elements[slot] : searchFrom(slot, index)
                : null;
    }

    private E searchFrom(int slot, int index) {
        while (true) {
            if (++slot >= indices.length) return null;
            int indexFound = indices[slot];
            if (indexFound == index) return elements[slot];
            if ((indexFound == 0) || unsignedLessThan(index, indexFound)) return null;
        }
    }
    

    @Override
    public SparseArray<E> set(int index, E value) {
        if (value == null) clear(index);
        int slot = index >>> shift;
        while (true) {
            if ((slot >= indices.length) || (slot < 0)) slot = overflow(index);
            int indexFound = indices[slot];
            if (indexFound == index) { // Replaces.
                if (isFree(slot, indexFound)) count++; // Resolves element at zero ambiguity.
                elements[slot] = value;
                return this;
            } else if (indexFound == 0) { // Free slot. 
                indices[slot] = index;
                elements[slot] = value;
                count++;
                return this;
            } else if (unsignedLessThan(index, indexFound)) { // Swaps with existing index/value pair.
                E elementFound = elements[slot];
                indices[slot] = index;
                elements[slot] = value;
                index = indexFound;
                value = elementFound;
            }
            // No direct fit, check if capacity can increase.
            if (count << MAX_CAPACITY_SHIFT <= indices.length * 2) return upsize().set(index, value);
            ++slot;
        }
    }
    
    private boolean isFree(int slot, int indexAtSlot) {
        return (indexAtSlot == 0) && ((slot != 0) || (elements[0] == null)); 
    }

    /** Equivalent to {@code set(index, null)} */
    public SparseArrayImpl<E> clear(int index) {
        int slot = index >>> shift;
        while (true) {
            if ((slot >= indices.length) || (slot < 0)) return this; // Not found.
            int indexFound = indices[slot];
            if (indexFound == index) { // Found.
                if (isFree(slot, indexFound)) return this; // Nothing to clear.
                while (true) { // Shift to pos following misplaced elements.
                    if (slot + 1 >= indices.length) break;
                    int i = indices[slot + 1];
                    if (i == 0) break; // Empty slot.
                    if ((i >>> shift) > slot) break; // Not misplaced.
                    indices[slot] = i;
                    elements[slot] = elements[++slot];
                }
                indices[slot] = 0;
                elements[slot] = null;
                return (--count << MAX_CAPACITY_SHIFT < indices.length) ? downsize() : this;
            } else if (indexFound == 0) { // Not found. 
                return this;
            } else if (unsignedLessThan(index, indexFound)) { // Not found.
                return this;
            }
            ++slot;
        }
    }

    /** Increases shift in case of overflow  (compacting elements to the head). */
    @SuppressWarnings("unchecked")
    private int overflow(int index) {
        if (indices == NONE) { // Empty array.
            shift = max(0, 32 - MAX_CAPACITY_SHIFT - numberOfLeadingZeros(index));
            indices = new int[1 << MAX_CAPACITY_SHIFT];
            elements = (E[]) new Object[1 << MAX_CAPACITY_SHIFT];
        } else { // Either index too large or no slot available at the end of array.
            shift = max(0, numberOfLeadingZeros(indices.length) - 1 - numberOfLeadingZeros(index));
            int[] newIndices = new int[indices.length];
            E[] newElements = (E[]) new Object[elements.length];
            copyTo(newIndices, newElements);
            indices = newIndices;
            elements = newElements;
        }
        return index >>> shift;
    }

    /** Doubles capacity or move to fractal implementation. */
    @SuppressWarnings("unchecked")
    private SparseArray<E> upsize() {
        if (count >= MAX_SIZE) return newFractal();
        shift = max(0, shift - 1); // Reduces shift (expands)
        int[] newIndices = new int[indices.length * 2];
        E[] newElements = (E[]) new Object[elements.length * 2];
        copyTo(newIndices, newElements);
        indices = newIndices;
        elements = newElements;
        return this;
    }

    private SparseArrayFractalImpl<E> newFractal() {
        SparseArrayFractalImpl<E> sparse = new SparseArrayFractalImpl<E>(shift >> 1);
        for (int i = 0; i < elements.length; i++) {
            E element = elements[i];
            if (element != null) sparse.set(indices[i], element);
        }
        return sparse;
    }

    private void copyTo(int[] dstI, E[] dstE) {
        int dstLast = -1;
        for (int i = 0; i < elements.length; i++) {
            E e = elements[i];
            if (e == null) continue;
            int index = indices[i];
            int slot = index >>> shift;
            dstLast = dstLast < slot ? slot : dstLast + 1;
            dstI[dstLast] = index;
            dstE[dstLast] = e;
        }
    }

    /** Divides by two capacity (or empty). */
    @SuppressWarnings("unchecked")
    private SparseArrayImpl<E> downsize() {
        if (count == 0) {
            indices = NONE;
            elements = null;
        } else {
            ++shift;
            int[] newIndices = new int[indices.length >> 1];
            E[] newElements = (E[]) new Object[elements.length >> 1];
            copyTo(newIndices, newElements);
            indices = newIndices;
            elements = newElements;
        }
        return this;
    }

    @Override
    public IteratorImpl iterator(int from, int to) {
        return new IteratorImpl(from, to);
    }

    /** ListIterator Implementation. */
    public final class IteratorImpl implements FastListIterator<E> {
        int slotFrom; // position of first non-null element with index >= from (indices.length if none)
        int slotTo; // position of last non-null element with index <= to (-1 if none)
        int slotNext; // (posTo + 1) if none.
        int slotPrev; // (posFrom - 1) if none.
        byte direction;

        private IteratorImpl(int from, int to) {
            slotFrom = from >>> shift;
            if ((slotFrom >= indices.length) || (slotFrom < 0)) slotFrom = indices.length; // Hard-Limit
            while (slotFrom < indices.length) {
                int index = indices[slotFrom];
                if (!isFree(slotFrom, index) && !unsignedLessThan(index, from)) break; // index >= from 
                slotFrom++;
            }
            
            slotTo = from >>> shift;
            if ((slotTo >= indices.length) || (slotTo < 0)) slotTo = indices.length; // Hard-Limit
            while (slotFrom < indices.length) {
                int index = indices[slotFrom];
                if (!isFree(slotFrom, index) && !unsignedLessThan(index, from)) break; // index >= from 
                slotFrom++;
            }
            
            
            slotNext = from >>> shift;
            if ((slotNext >= indices.length) || (slotNext < 0)) slotNext = posEnd; // Overflow.
            while (slotNext < posEnd) {
                int index = indices[slotNext];
                if (!unsignedLessThan(index, from)) // (index >= from), found it ? 
                    if ((index != 0) || (elements[0] != null)) break; // Confirmation...
                slotNext++;
            }
            slotPrev = slotNext;
            while (--slotPrev > 0)
                if (elements[slotPrev] != null) break;
        }

        @Override
        public boolean hasNext() {
            return slotNext > slotTo;
        }

        @Override
        public boolean hasPrevious() {
            return slotPrev < slotFrom;
        }

        @Override
        public E next() {
            direction = 1;
            slotPrev = slotNext;
            while (++slotNext <= slotTo)
                if (elements[slotNext] != null) break;
            return elements[slotPrev];
        }

        @Override
        public int nextIndex() {
            return (slotNext <= slotTo) ? indices[slotNext] : 0;
        }

        @Override
        public E previous() {
            direction = -1;
            slotNext = slotPrev;
            while (--slotPrev >= slotFrom)
                if (elements[slotPrev] != null) break;
            return elements[slotNext];
        }

        @Override
        public int previousIndex() {
            return (slotPrev >= slotFrom) ? indices[slotPrev] : -1;
        }

    }

}
