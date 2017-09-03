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
import static org.javolution.annotations.Realtime.Limit.LOG_N;

import java.io.Serializable;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;
import org.javolution.util.internal.FractalArrayImpl;

/**
 * A [fractal-based], fast-rotating, variable length array maintaining its own memory footprint minimal. 
 * Using fractal arrays, there is no "memory leak" due to the reduction of the array usage and there 
 * is no large "resize/copy" operations ever performed. 
 * 
 * Updates operations on fractal arrays may return new instances with greater or lesser capacity. 
 * 
 * ```java
 * class FractalTable<E> {
 *     private final FractalArray<E> elements = FractalArray.empty(); 
 *      
 *     ​@Realtime(limit = CONSTANT)
 *     public int size() {
 *         return elements.length(); 
 *     }
 *     
 *     ​@Realtime(limit = CONSTANT)
 *     public void add(E element) {
 *         elements = elements.append(element); 
 *     }
 *      
 *     ​@Realtime(limit = LOG_N) // Versus LINEAR for ArrayList
 *     public void add(int index, E element) {
 *         elements = elements.insert(index, element);
 *     }
 *     
 *     ​@Realtime(limit = LOG_N) // Versus LINEAR for ArrayList
 *     public E remove(int index) {
 *         E removed = elements.get(index); 
 *         elements = elements.remove(index); 
 *         return removed;
 *     }
 *     
 *     ​@Realtime(limit = CONSTANT) 
 *     E set(int index, E element) {
 *         return elements.replace(index, element);
 *     }
 * }
 * ```
 * Fractal array element insertion / deletion time is in {@link Realtime.Limit#LOG_N O(Log(n))}
 * regardless of the number of elements held by the array.  
 * 
 * [fractal-based]: http://en.wikipedia.org/wiki/Fractal
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, July 1st, 2017
 */
@Realtime(limit = CONSTANT)
public abstract class FractalArray<E> implements Iterable<E>, Cloneable, Serializable {
    private static final long serialVersionUID = 0x700L; // Version.

    /** 
     * Returns an empty instance.
     */
    @Realtime(limit = CONSTANT)
    public static <E> FractalArray<E> empty() {
        return new FractalArrayImpl<E>();
    }

    /** 
     * Returns a copy of this fractal array; updates of the copy will not impact the original.
     * 
     * @return a copy of this fractal array.
     */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public FractalArray<E> clone() {
        try {
            return (FractalArray<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable!");
        }
    }

    /** 
     * Returns the element at the specified position.
     * 
     * @param index the index position of the element to return.
     * @return the element at the specified position.
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index >= length()} 
     */
    @Realtime(limit = CONSTANT)
    public abstract @Nullable E get(int index);
    
    /** 
     * Returns the element at the specified position or the specified default if {@code null} (convenience method). 
     * 
     * @param index the index position of the element to return.
     * @param defaultIfNull the elements to return instead of {@code null}.
     * @return the element at the specified position or the specified default if {@code null}.
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index >= length()} 
     */
    @Realtime(limit = CONSTANT)
    public final E get(int index, E defaultIfNull) {
        E element = get(index);
        return (element != null) ? element : defaultIfNull;
    }
    
    /**
     * Appends the specified element at the end of this array (increments the length of this array).
     * 
     * @param element the element being appended.
     * @return the array with the specified element appended.
     */
    @Realtime(limit = CONSTANT)
    public abstract FractalArray<E> append(@Nullable E element);
    
    /**
     * Inserts the specified element at the {@code index} position, shifting the previous elements from {@code index} 
     * one position to the right (increments the length of this array).
     * 
     * @param index the index position of the element to be inserted.
     * @param element the element being inserted.
     * @return the array with the specified element inserted.
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index > length()} 
     */
    @Realtime(limit = LOG_N)
    public abstract FractalArray<E> insert(int index, @Nullable E element);
    
    /** 
     * Indicates if this array is empty (length is zero).
     */
    @Realtime(limit = CONSTANT)
    public final boolean isEmpty() {
        return length() == 0;
    }

    /** 
     * Returns a list iterator over this array starting from the specified index.
     * 
     * The specified index indicates the first element that would be returned by an initial call to 
     * {@link FastListIterator#next}. An initial call to {@link FastListIterator#previous} would 
     * return the element with the specified index minus one.
     * 
     * @param index the index of the first element to be returned from the list iterator (by a call to next).
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index > length()} 
     */  
    @Realtime(limit = CONSTANT)
    public abstract FastListIterator<E> iterator(int index);
  
    /** 
     * Returns the current length of this array.
     */
    @Realtime(limit = CONSTANT)
    public abstract int length();    
    
    /**
     * Removes from this array the element at the specified {@code index} position, shifting the elements from 
     * {@code index} one position to the left (decrements the length of this array).
     * 
     * @param index the index position of the element to remove.
     * @return the array with the specified element removed.
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index >= length()} 
     */
    @Realtime(limit = LOG_N)
    public abstract FractalArray<E> remove(int index);

    /**
     * Replaces the element at the specified position with the specified element (does not change the length of 
     * this array).
     * 
     * @param index the index position of the element.
     * @param element the new element at the specified position.
     * @return the previous element at the specified position.
     * @throws ArrayIndexOutOfBoundsException if {@code (index < 0) || (index >= length()} 
     */
    @Realtime(limit = CONSTANT)
    public abstract @Nullable E replace(int index, @Nullable E element);
    
    /** 
     * Returns an unmodifiable view over this array. 
     */  
    @Realtime(limit = CONSTANT)
    public abstract FractalArray<E> unmodifiable();

}