/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Comparator;

import javolution.annotation.ThreadSafe;
import javolution.annotation.StackSafe;
import javolution.util.Comparators;
import javolution.util.FastCollection;
import javolution.util.FastMap;

/**
 * <p> A comparator to be used for equality as well as for ordering.
 *     Instances of this class provide a hashcode function 
 *     consistent with equal (if two objects {@link #areEqual
 *     are equal}, they have the same {@link #hashCodeOf hashcode}),
 *     equality with <code>null</code> values is supported.</p>
 *     
 * <p> Comparators can be employed with {@link FastMap} (e.g. 
 *     {@link Comparators#IDENTITY} for identity maps) or with 
 *     {@link FastCollection} classes.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@ThreadSafe
@StackSafe
public interface ComparatorService<T> extends Comparator<T> {

    /**
     * Returns the hash code for the specified object (consistent with 
     * {@link #areEqual}). Two objects considered {@link #areEqual equal} have 
     * the same hash code. The hash code of <code>null</code> is always 
     * <code>0</code>.
     * 
     * @param  obj the object to return the hashcode for.
     * @return the hashcode for the specified object.
     */
     int hashCodeOf(T obj);

    /**
     * Indicates if the specified objects can be considered equal.
     * 
     * @param o1 the first object (or <code>null</code>).
     * @param o2 the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
     boolean areEqual(T o1, T o2);

    /**
     * Compares the specified objects for order. Returns a negative integer, 
     * zero, or a positive integer as the first argument is less than, equal to,
     * or greater than the second.
     * 
     * @param o1 the first object.
     * @param o2 the second object.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    int compare(T o1, T o2);
    
}
