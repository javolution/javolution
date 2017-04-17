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
import org.javolution.util.internal.SparseArrayImpl;

/**
 * A high-performance [trie-based], unbounded array whose internal capacity adjusts incrementally up or down.
 *      
 * Updates operations on [sparse array] may return a new array holding the added/deleted elements. 
 * 
 * ```java
 *  class SparseVector<E> {
 *      private SparseArray<E> elements = SparseArray.empty();
 *      void set(int index, E element) {
 *           elements = elements.set(index, element);
 *      }
 * }
 * ```
 * 
 * Sparse array access time is almost constant regardless of the number of elements held by the array.  
 * 
 * Sparse arrays support 32-bits unsigned index for a range double the range of standard arrays.   
 *  
 * [trie-based]: http://en.wikipedia.org/wiki/Trie
 * [sparse array]: https://en.wikipedia.org/wiki/Sparse_array
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, April 1st, 2017
 */
@Realtime(limit=CONSTANT)
public abstract class SparseArray<E> implements Cloneable, Serializable {
    
    private static final long serialVersionUID = 0x700L; // Version.

    /** 
     * Returns an empty sparse array (default implementation).
     */
    public static <E> SparseArray<E> empty() {
        return SparseArrayImpl.empty();
    }

    /** 
     * Default constructor.
     */
    protected SparseArray() {        
    }
    
    /** 
     * Returns the element at the specified index.
     * 
     * @param index the unsigned 32-bits index of the element to return.
     * @return the element at the specified index or {@code null} if none.
     */
    public abstract E get(int index);
    
    /** 
     * Returns the element at the specified index or the specified default if {@code null} (convenience method). 
     * 
     * @param index the unsigned 32-bits index of the element to return.
     * @param defaultIfNull the elements to return instead of {@code null}.
     * @return the element at the specified index or the specified default.
     */
    public final E get(int index, E defaultIfNull) {
        E element = get(index);
        return (element != null) ? element : defaultIfNull;
    }
    
    /**
     * Replaces the element at the specified position with the specified element.
     * 
     * @param index the unsigned 32-bits index of the element.
     * @param element the new element at the specified position (can be {@code null} to remove the element).
     * @return a new sparse array or {@code this}. 
     */
    public abstract SparseArray<E> set(int index, E value);

    /**
     * Returns the unsigned index of the next non-null element after the specified index.
     *  
     * @param after the starting point (exclusive).
     * @return the unsigned index greater than {@code after} having a non-null element or {@code 0} if none. 
     */
    public abstract int next(int after);

    /**
     * Returns the unsigned index of the previous non-null element before the specified index.
     *  
     * @param before the starting point (exclusive).
     * @return the unsigned index smaller than {@code before} having a non-null element or {@code -1} if none.
     */
    public abstract int previous(int before);
 
    /** 
     * Returns a copy of this sparse array; updates of the copy should not impact the original. 
     * 
     * @return a copy of this sparse array.
     */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public SparseArray<E> clone() {
        try {
            return (SparseArray<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable !");
        }        
    }
    
 }
