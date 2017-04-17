/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;

import java.io.Serializable;

import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;
import org.javolution.util.internal.FractalArrayImpl;

/**
 * A [fractal-based], fast-rotating, unbounded array whose memory footprint is minimized.
 *     
 * 
 * ```java
 * class FractalTable<E> {
 *     private FractalArray<E> elements = FractalArray.newArray(); // See FractalArray::CopyOnWrite for atomic tables.
 *     int size; // Keeps track of the size since fractal arrays are unbounded.
 *      
 *     void add(E element) {
 *         elements = elements.set(size++, element); 
 *     }
 *      
 *     void add(int index, E element) {
 *         if (index > size) throw new IndexOutOfBoundsException();
 *         elements = elements.add(index, element);
 *         size++;
 *     }
 *     
 *     E remove(int index) {
 *         if (index >= size) throw new IndexOutOfBoundsException();
 *         E removed = elements.get(index); 
 *         elements = elements.remove(index); 
 *         size--;
 *         return removed;
 *     }
 * }
 * ```
 * 
 * Fractal array elements insertion/deletion is almost constant regardless of the number of elements held by the array.  
 * 
 * [fractal-based]: http://en.wikipedia.org/wiki/Fractal
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, April 1st, 2017
 */
@Realtime(limit = CONSTANT)
public abstract class FractalArray<E> implements Cloneable, Serializable {
    private static final long serialVersionUID = 0x700L; // Version.

    /**
    *  Unbounded fractal array for which any update results into a new array. 
    */
    public static abstract class CopyOnWrite<E> extends FractalArray<E> implements Immutable {
        private static final long serialVersionUID = 0x700L; // Version.

        /** 
         * Returns a new copy-on-write fractal array holding the specified elements.
         */
        public static <E> FractalArray<E> newArray(E...elements) {
            throw new UnsupportedOperationException("Not implemented yet"); // TODO
        }

        /** 
         * Returns this instance (immutable).
         * 
         * @return {@code this}.
         */
        @Realtime(limit = CONSTANT)
        public CopyOnWrite<E> clone() {
            return this;
        }

    }

    /** 
     * Returns a new unbounded fractal array (mutable).
     */
    public static <E> FractalArray<E> newArray() {
        return FractalArrayImpl.newInstance();
    }

    /** 
     * Default constructor.
     */
    protected FractalArray() {
    }

    /** 
     * Returns the element at the specified index.
     * 
     * @param index the unsigned 32-bits index of the element to return.
     * @return the element at the specified index or {@code null} if none.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
     */
    public abstract E get(int index);

    /**
     * Replaces the element at the specified position with the specified element.
     * 
     * @param index the unsigned 32 bits index of the element to replace.
     * @param element the element being set or {@code null} to remove the previous element.
     * @return {@code this} or a new fractal array if this array should be replaced (capacity changed up or down).
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
     */
    public abstract FractalArray<E> set(int index, E element);

    /**
     * Inserts the specified element at {@code index} position, shifting the elements from {@code index} 
     * one position to the right.
     * 
     * @param index the insertion index.
     * @param inserted the element being inserted (can be {@code null}).
     * @return {@code this} or a new fractal array if this array should be replaced with a new array.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
    */
    public abstract FractalArray<E> add(int index, E inserted);

    /**
     * Removes from this array the element at {@code index} position, shifting the elements from {@code index} 
     * one position to the left.
     * 
     * @param index the deletion index.
     * @return {@code this} or a new fractal array if this array should be replaced with a new array.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
     */
    public abstract FractalArray<E> remove(int index);

    /** 
     * Returns a copy of this fractal array; updates of the copy should not impact the original.
     * 
     * @return a copy of this fractal array.
     */
    @Realtime(limit = LINEAR, comment = "Constant for FractalArray::CopyOnWrite instances.")
    @SuppressWarnings("unchecked")
    public FractalArray<E> clone() {
        try {
            return (FractalArray<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable!");
        }
    }

}