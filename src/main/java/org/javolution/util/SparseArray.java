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

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.util.internal.SparseArrayImpl;

/**
 * A [trie-based], fast access, unbounded array maintaining its own memory footprint minimal.
 *      
 * Updates operations on sparse arrays may return new instances with greater or lesser capacity. 
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
 * Sparse array access/update time is {@link Realtime.Limit#CONSTANT constant / bounded} regardless of 
 * the number of elements held by the array.  
 *  
 * [trie-based]: http://en.wikipedia.org/wiki/Trie
 * [sparse array]: https://en.wikipedia.org/wiki/Sparse_array
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, July 1st, 2017
 */
@Realtime(limit=CONSTANT)
public abstract class SparseArray<E> implements Iterable<E>, Cloneable, Serializable {
    private static final long serialVersionUID = 0x700L; // Version.
    
    /** 
     * Returns an empty instance.
     */
    public static <E> SparseArray<E> empty() {
        return new SparseArrayImpl<E>();
    }

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
    
    /** 
     * Returns the value at the specified index (unsigned 32-bits).
     * 
     * @param index the unsigned 32-bits index of the value to return.
     * @return the value at the specified index or {@code null} if none.
     */
    @Realtime(limit=CONSTANT)
    public abstract @Nullable E get(int index);
    
    /** 
     * Returns the value at the specified index or the specified default if that value is {@code null} 
     * (convenience method). 
     * 
     * @param index the unsigned 32-bits index of the value to return.
     * @param defaultIfNull the value to return instead of {@code null}.
     * @return the value at the specified index or the specified default.
     */
    @Realtime(limit=CONSTANT)
    public final E get(int index, E defaultIfNull) {
        E value = get(index);
        return (value != null) ? value : defaultIfNull;
    }
    
    /** 
     * Indicates if this array is empty (all elements are {@code null}).
     */
    @Realtime(limit=CONSTANT)
    public abstract boolean isEmpty();

    /** 
     * Returns a list iterator over the {@code non-null} elements of this array starting from the specified index.
     * 
     * The specified index indicates the first element that could be returned by an initial call to 
     * {@link FastListIterator#next}. An initial call to {@link FastListIterator#previous} would 
     * return an element having an unsigned-32 bits index strictly less than the specified index.
     * 
     * @param index the unsigned 32-bits minimum index to be returned from the list iterator (by a call to nextIndex).
     */  
    @Realtime(limit = CONSTANT)
    public abstract FastListIterator<E> iterator(int index);
    
    /**
     * Sets the element at the specified position (can be {@code null} to remove the previous element).
     * 
     * @param index the unsigned 32-bits index of the value to be set.
     * @param element the element at the specified position (can be {@code null}). 
     * @return a new sparse array or {@code this}. 
     */
    @Realtime(limit=CONSTANT)
    public abstract SparseArray<E> set(int index,  @Nullable E element);
    
    /** 
     * Returns an unmodifiable view over this array. 
     */  
    @Realtime(limit=CONSTANT)
    public abstract SparseArray<E> unmodifiable();
    
 }
