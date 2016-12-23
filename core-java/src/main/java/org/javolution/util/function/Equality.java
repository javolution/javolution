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

import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;
import org.javolution.util.internal.function.ArrayEqualityImpl;
import org.javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import org.javolution.util.internal.function.HashOrderImpl;
import org.javolution.util.internal.function.IdentityHashOrderImpl;
import org.javolution.util.internal.function.LexicalOrderImpl;

/**
 * <p>  A function (functional interface) indicating if two objects are considered equals.</p>
 * 
 * @param <T>the type of objects that may be compared for equality.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
@ReadOnly
public interface Equality<T> extends  Immutable, Serializable {
	
    /**
     * A default object equality (based on {@link Object#equals}).
     */
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> DEFAULT = HashOrderImpl.INSTANCE;

    /**
     * An identity object equality (instances are only equals to themselves).
     */
    @Realtime(limit = CONSTANT)
    public static final Equality<Object> IDENTITY = IdentityHashOrderImpl.INSTANCE;

     /**
     * A content based array comparator (recursive). 
     * The {@link #DEFAULT default} equality is used for non-array elements. 
     */
    @Realtime(limit = LINEAR)
    public static final Equality<Object> ARRAY = ArrayEqualityImpl.INSTANCE;
 
    /**
     * A lexical equality for any {@link CharSequence}.
     */
    @Realtime(limit = LINEAR)
    public static final Equality<CharSequence> LEXICAL = LexicalOrderImpl.INSTANCE;

    /**
     * A case insensitive lexical equality for any {@link CharSequence}.
     */
    @Realtime(limit = LINEAR)
    public static final Equality<CharSequence> LEXICAL_CASE_INSENSITIVE
        = CaseInsensitiveLexicalOrderImpl.INSTANCE;
  
	/**
	 * Indicates if two specified objects are considered equal.
	 * 
	 * @param left the first object (can be {@code null}).
	 * @param right the second object (can be {@code null}).
	 * @return <code>true</code> if both objects are considered equal; <code>false</code> otherwise.
	 */
	boolean areEqual(@Nullable T left, @Nullable T right);

}