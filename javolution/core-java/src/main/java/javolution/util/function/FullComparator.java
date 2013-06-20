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
 * <p> A coherent comparator to be used for equality as well as for 
 *     ordering. Implementing classes should ensure that:
 *     <ul>
 *        <li> The {@link #compare compare} function is consistent with 
 *             {@link #areEqual equals}. If two objects {@link #compare compare}
 *             to {@code 0} then they are {@link #areEqual equals} and the 
 *             the reciprocal is true (this ensures that sorted collections/maps
 *             do not break the general contract of their parent class based on
 *             object equal).</li>
 *        <li> The {@link #hashCodeOf hashcode} function is consistent with
 *             {@link #areEqual equals}: If two objects are equals, they have 
 *             the same hashcode (the reciprocal is not true).</li>
 *        <li> The {@code null} value is supported (even for 
 *             {@link #compare comparisons}) and its {@link #hashCodeOf(Object)
 *             hashcode} value is always {@code 0}.</li>
 *     </ul>
 * </p>
 * 
 * <p> Note: In future versions, this interface will be a functional interface
 *           with {@link #areEqual(Object, Object) areEquals} abstract.</p>
 *           
 * @param <T> the type of objects that may be compared for equality or order.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see Comparators
 */
public interface EqualityComparator<T> extends Comparator<T> {

    /**
     * Returns the hash code for the specified object (consistent with 
     * {@link #areEqual}). Two objects considered {@link #areEqual equal} have 
     * the same hash code. The hash code of <code>null</code> is always 
     * <code>0</code>.
     * 
     * @param  object the object to return the hashcode for.
     * @return the hashcode for the specified object.
     */
     int hashCodeOf(T object);

    /**
     * Indicates if the specified objects can be considered equal.
     * This methods is equivalent to {@code (compare(o1, o2) == 0)} but 
     * usually faster.
     * 
     * @param left the first object (or <code>null</code>).
     * @param right the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
     boolean areEqual(T left, T right);

    /**
     * Compares the specified objects for order. Returns a negative integer, 
     * zero, or a positive integer as the first argument is less than, possibly 
     * equal to, or greater than the second. Implementation classes should 
     * ensure that comparisons with {@code null} is supported.
     * 
     * @param left the first object.
     * @param right the second object.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, possibly equal to, or greater than the second.
     */
    int compare(T left, T right);
    
}
