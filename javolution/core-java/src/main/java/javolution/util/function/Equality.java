/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;
import static javolution.lang.Realtime.Limit.UNKNOWN;
import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.util.internal.function.ArrayEqualityImpl;
import javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import javolution.util.internal.function.HashOrderImpl;
import javolution.util.internal.function.IdentityHashOrderImpl;
import javolution.util.internal.function.LexicalOrderImpl;
import javolution.util.internal.function.NaturalOrderImpl;

/**
 * <p>  A function (functional interface) indicating if two objects 
 *      are considered equals.</p>
 * 
 * @param <T>the type of objects that may be compared for equality.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
public interface Equality<T> {
	
    /**
     * The standard object equality ({@link #HASH} order).
     */
    @Parallelizable
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> STANDARD = HashOrderImpl.INSTANCE;

    /**
     * The identity object equality ({@link #IDENTITY_HASH} order)
     * for which instances are only equals to themselves.
     */
    @Parallelizable
    @Realtime(limit = CONSTANT)
    public static final Equality<Object> IDENTITY 
       = IdentityHashOrderImpl.INSTANCE;

     /**
     * A content based array comparator (recursive). 
     * The {@link #STANDARD standard} comparator is used for non-array elements. 
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final Equality<Object> ARRAY = ArrayEqualityImpl.INSTANCE;
 
    /**
     * A lexical equality for any {@link CharSequence} ({@link #LEXICAL} order).
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final Equality<CharSequence> LEXICAL
        = LexicalOrderImpl.INSTANCE;

    /**
     * A case sensitive lexical equality for any {@link CharSequence} 
     * ({@link #LEXICAL_CASE_INSENSITIVE} order).
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final Equality<CharSequence> LEXICAL_CASE_INSENSITIVE
        = CaseInsensitiveLexicalOrderImpl.INSTANCE;
  
    /**
     * A natural equality for {@link Comparable} instances. Two objects 
     * are considered equals if they compare to {@code 0}.
     *  
     * @throws ClassCastException if used with non {@link Comparable} instances.
     */
    @Parallelizable
    @Realtime(limit = UNKNOWN)
    public static final Equality<Object> NATURAL 
        = NaturalOrderImpl.INSTANCE;
 
	/**
	 * Indicates if two specified objects are considered equal.
	 * 
	 * @param left
	 *            the first object (can be {@code null}).
	 * @param right
	 *            the second object (can be {@code null}).
	 * @return <code>true</code> if both objects are considered equal;
	 *         <code>false</code> otherwise.
	 */
	boolean areEqual(T left, T right);

}