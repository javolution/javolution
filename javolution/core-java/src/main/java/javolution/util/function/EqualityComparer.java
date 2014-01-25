/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;


/**
 * <p> A functional interface to support the comparison of objects 
 *     for equality.</p>
 * 
 * @param <T> the type of objects that may be compared for equality or order.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see Equalities
 */
public interface EqualityComparer<T>  {

    /**
     * Indicates if the specified objects can be considered equal.
     *  
     * @param left the first object (or <code>null</code>).
     * @param right the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
    boolean equal(T left, T right);

}