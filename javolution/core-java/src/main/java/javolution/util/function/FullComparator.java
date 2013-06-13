/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import java.util.Comparator;

/**
 * <p> A comparator to be used for equality as well as for ordering.
 *     Implementing class should ensures that:
 *     <ul>
 *        <li> The {@link #compare compare} function is consistent with 
 *             {@link #areEqual equals}. If two objects {@link #compare compare}
 *             to {@code 0} then they are {@link #areEqual equals} and the 
 *             the reciprocal is true (to ensure that sorted collections/maps
 *             do not break the general contract of their parent class based on
 *             object equal).</li>
 *        <li> The hashcode function is consistent with equals: If two objects 
 *             {@link #areEqual equals}, they have the same 
 *             {@link #hashCodeOf hashcode} (the reciprocal is not true).</li>
 *        <li> The {@code null} value is supported.</li>
 *     </ul>
 * </p>
 *     
 * @param <T> the type of objects that may be compared for equality or order.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see Comparators
 */
public interface FullComparator<T> extends Comparator<T> {

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
     * This methods is equivalent to {@code (compare(o1, o2) == 0)} but 
     * usually faster.
     * 
     * @param o1 the first object (or <code>null</code>).
     * @param o2 the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
     boolean areEqual(T o1, T o2);

    /**
     * Compares the specified objects for order. Returns a negative integer, 
     * zero, or a positive integer as the first argument is less than, possibly 
     * equal to, or greater than the second.
     * 
     * @param o1 the first object.
     * @param o2 the second object.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, possibly equal to, or greater than the second.
     */
    int compare(T o1, T o2);
    
}
