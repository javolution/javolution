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
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> NEVER_EQUALS = new Equality<Object>() {
        private static final long serialVersionUID = 0x700L; // Version.

        @Override
        public boolean areEqual(Object left, Object right) {
            return false;
        }
    };

    /**
     * An equality always returning {@code false}.
     */
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> ALWAYS_EQUALS = new Equality<Object>() {
        private static final long serialVersionUID = 0x700L; // Version.

        @Override
        public boolean areEqual(Object left, Object right) {
            return false;
        }
    };
    
    /**
     * The standard object equality (based on {@link Object#equals}). 
     */
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> STANDARD = new Equality<Object>() {
        private static final long serialVersionUID = 0x700L; // Version.

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left == right) || (left != null && left.equals(right));
        }};
    
    
    /**
     * The identity object equality (instances are only equals to themselves).
     */
    @Realtime(limit = CONSTANT)
    public static final Equality<Object> IDENTITY = new Equality<Object>() {
        private static final long serialVersionUID = 0x700L; // Version.

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left == right) || (left != null && left.equals(right));
        }};
    

    /**
    * A content based array comparator (recursive). 
    * The {@link #STANDARD standard} equality is used for non-array elements. 
    */
    @Realtime(limit = LINEAR)
    public static final Equality<Object> ARRAY = new Equality<Object>() {
        private static final long serialVersionUID = 0x700L; // Version.

        @Override
        public boolean areEqual(@Nullable Object left, @Nullable Object right) {
            if (left == right)
                return true;
            if ((left == null) || (right == null))
                return false;
            if (left instanceof Object[] && right instanceof Object[])
                return Arrays.deepEquals((Object[]) left, (Object[]) right);
            if (left instanceof byte[] && right instanceof byte[])
                return Arrays.equals((byte[]) left, (byte[]) right);
            if (left instanceof short[] && right instanceof short[])
                return Arrays.equals((short[]) left, (short[]) right);
            if (left instanceof int[] && right instanceof int[])
                return Arrays.equals((int[]) left, (int[]) right);
            if (left instanceof long[] && right instanceof long[])
                return Arrays.equals((long[]) left, (long[]) right);
            if (left instanceof char[] && right instanceof char[])
                return Arrays.equals((char[]) left, (char[]) right);
            if (left instanceof float[] && right instanceof float[])
                return Arrays.equals((float[]) left, (float[]) right);
            if (left instanceof double[] && right instanceof double[])
                return Arrays.equals((double[]) left, (double[]) right);
            if (left instanceof boolean[] && right instanceof boolean[])
                return Arrays.equals((boolean[]) left, (boolean[]) right);
            return left.equals(right);
        }
    };

    
    /**
     * Indicates if two specified objects are considered equal.
     * 
     * @param left the first object.
     * @param right the second object.
     * @return {@code true} if both objects are considered equal; {@code false} otherwise.
     */
    boolean areEqual(@Nullable T left, @Nullable T right);

}