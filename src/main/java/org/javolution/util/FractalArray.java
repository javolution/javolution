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
import org.javolution.util.internal.FractalArrayImpl;

/**
 * <p> A high-performance <a href="http://en.wikipedia.org/wiki/Fractal">fractal-based</a>, fast-rotating array 
 *     whose capacity adjusts automatically up or down for minimal memory footprint.</p>
 *     
 * <p> Fractal array rotation time is almost constant regardless of the number of elements held by the array.</p>  
 *      
 * <p> Note: Fractal arrays support 32-bits unsigned index (double the range of standard arrays).</p>    
 */
@Realtime(limit=CONSTANT)
public abstract class FractalArray<E> implements Cloneable, Serializable {
    
    private static final long serialVersionUID = 0x700L; // Version.

    /** 
     * Returns an empty fractal array (default implementation).
     */
    public static <E> FractalArray<E> empty() {
        return FractalArrayImpl.empty();     }

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
     */
    public abstract E get(int index);

    /** 
     * Returns the element at the specified index or the specified value if {@code null} (convenience method). 
     * 
     * @param index the unsigned 32-bits index of the element to return.
     * @param defaultIfNull the element to return instead of {@code null}.
     * @return the element at the specified index or the specified element if {@code null}.
     */
    public final E get(int index, E defaultIfNull) {
        E element = get(index);
        return (element != null) ? element : defaultIfNull;
    }
           
    /**
     * Sets the value of the element at the specified index and returns the fractal array with 
     * the specified element set.
     * 
     * @param index the unsigned 32 bits index of the element to set.
     * @param element the element being set (can be {@code null} to remove the element).
     * @return the sparse array with the element set or {@code this} if the array capacity has not changed. 
     */
    public abstract FractalArray<E> set(int index, E newElement);

    /**
     * Shifts the specified elements ([first, first + length[) one position to the right by inserting the specified 
     * element at the {0code first} position.
     * 
     * @param inserted the element being inserted at the {@code first} position.
     * @param first the unsigned 32-bits index of the first element to be shifted right.
     * @param length the unsigned 32-bits length of the number of element to be shifted.
     * @return the array with the specified elements shifted or {@code this} if the array capacity is left unchanged. 
     */
    public abstract FractalArray<E> shiftRight(E inserted, int first, int length);
    
    /**
     * Shifts the specified elements(]last - length, last]) one position to the left by inserting the specified object
     * at the {@code last} position.
     * 
     * @param inserted the element being inserted at the {@code last} position.
     * @param last the unsigned 32-bits index of the last element to be shifted left.
     * @param length the unsigned 32-bits length of the number of element to be shifted.
     * @return the array with the specified elements shifted or {@code this} if the array capacity is left unchanged. 
     */
    public abstract FractalArray<E> shiftLeft(E inserted, int last, int length);
    
    /** Returns a copy of this fractal array; updates of the copy should not impact the original. */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public FractalArray<E> clone() {
        try {
            return (FractalArray<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable !");
        }        
    }
   
}