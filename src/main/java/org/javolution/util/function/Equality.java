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

import org.javolution.annotations.Nullable;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;
import org.javolution.util.internal.function.ArrayEqualityImpl;
import org.javolution.util.internal.function.IdentityOrderImpl;
import org.javolution.util.internal.function.StandardOrderImpl;

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
     * It also defines an arbitrary order based on {@link Object#hashCode}.
     */
    @Realtime(limit = UNKNOWN)
    public static final StandardOrderImpl STANDARD = new StandardOrderImpl();

    /**
     * The identity object equality (instances are only equals to themselves).
     * It also defines an arbitrary order based on {@link System#identityHashCode)}.
     */
    @Realtime(limit = CONSTANT)
    public static final IdentityOrderImpl IDENTITY = new IdentityOrderImpl();
    

    /**
    * A content based array comparator (recursive). 
    * The {@link #STANDARD standard} equality is used for non-array elements. 
    */
    @Realtime(limit = LINEAR)
    public static final ArrayEqualityImpl ARRAY = new ArrayEqualityImpl();

    /**
     * Indicates if two specified objects are considered equal.
     * 
     * @param left the first object.
     * @param right the second object.
     * @return {@code true} if both objects are considered equal; {@code false} otherwise.
     */
    boolean areEqual(@Nullable T left, @Nullable T right);

}