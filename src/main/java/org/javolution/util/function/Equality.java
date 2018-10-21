/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;
import static org.javolution.annotations.Realtime.Limit.UNKNOWN;

import java.io.Serializable;
import java.util.Arrays;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;

/**
 * A function (functional interface) indicating if two objects are considered equals.
 * 
 * @param <T>the type of objects that may be compared for equality.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
@ReadOnly
public interface Equality<T> extends Immutable, Serializable {

	
    /**
     * An equality always returning {@code false}.
     */
	@Realtime(limit = CONSTANT)
    static <T> Equality<T> neverEqual() {
    	return (obj1, obj2) -> false;
	}
	
    /**
     * An equality always returning {@code true}.
     */
	@Realtime(limit = CONSTANT)
    static <T> Equality<T> alwaysEqual() {
	   	 return (obj1, obj2) -> true;
	}
	
    
    /**
     * The standard object equality (based on {@link Object#equals}). 
     */
    @Realtime(limit = UNKNOWN)
    static <T> Equality<T> standard() {
    	return (obj1, obj2) -> (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
    }
    
    
    /**
     * The identity object equality (instances are only equals to themselves).
     */
    @Realtime(limit = CONSTANT)
    static <T> Equality<T> identity() {
    	return (obj1, obj2) -> (obj1 == obj2);
    }

    /**
    * A content based array comparator (recursive). 
    * The {@link #standard} equality is used for non-array elements. 
    */
    @Realtime(limit = LINEAR)
    static <T> Equality<T> deepArray() {
    	return (left, right) -> 
            (left == right) ? true : 
            ((left == null) || (right == null)) ? false :
            (left instanceof Object[] && right instanceof Object[]) ?
                Arrays.deepEquals((Object[]) left, (Object[]) right) :
            (left instanceof byte[] && right instanceof byte[]) ?
                Arrays.equals((byte[]) left, (byte[]) right) :
            (left instanceof short[] && right instanceof short[]) ?
                Arrays.equals((short[]) left, (short[]) right) :
            (left instanceof int[] && right instanceof int[]) ?
                Arrays.equals((int[]) left, (int[]) right) :
            (left instanceof long[] && right instanceof long[]) ?
                Arrays.equals((long[]) left, (long[]) right) :
            (left instanceof char[] && right instanceof char[]) ?
                Arrays.equals((char[]) left, (char[]) right) :
            (left instanceof float[] && right instanceof float[]) ?
                Arrays.equals((float[]) left, (float[]) right) :
            (left instanceof double[] && right instanceof double[]) ?
                Arrays.equals((double[]) left, (double[]) right) :
            (left instanceof boolean[] && right instanceof boolean[]) ?
                Arrays.equals((boolean[]) left, (boolean[]) right) : left.equals(right);
    }

    
    /**
     * Indicates if two specified objects are considered equal.
     * 
     * @param left the first object.
     * @param right the second object.
     * @return {@code true} if both objects are considered equal; {@code false} otherwise.
     */
    boolean areEqual(@Nullable T left, @Nullable T right);

}