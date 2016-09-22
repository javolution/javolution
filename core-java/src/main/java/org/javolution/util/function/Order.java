/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

import static org.javolution.lang.Realtime.Limit.CONSTANT;
import static org.javolution.lang.Realtime.Limit.LINEAR;
import static org.javolution.lang.Realtime.Limit.LOG_N;
import static org.javolution.lang.Realtime.Limit.UNKNOWN;

import java.util.Comparator;

import org.javolution.lang.Binary;
import org.javolution.lang.Index;
import org.javolution.lang.Realtime;
import org.javolution.lang.Ternary;
import org.javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import org.javolution.util.internal.function.HashOrderImpl;
import org.javolution.util.internal.function.IdentityHashOrderImpl;
import org.javolution.util.internal.function.IndexOrderImpl;
import org.javolution.util.internal.function.LexicalOrderImpl;

/**
 * <p> A disposition of things following one after another, smallest first.</p>
 * 
 * <p> Implementing classes should ensure consistency between  
 *     {@link Equality#areEqual}, {@link Comparator#compare} and 
 *     {@link #indexOf}; specifically they should ensure that 
 *     if {@code (areEqual(x,y))} then {@code (compare(x,y) == 0)} and 
 *     if {@code indexOf(x) < indexOf(y)} then {@code (compare(x,y) < 0)}.</p>
 *       
 * @param <T> the type of objects being ordered.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public interface Order<T> extends Equality<T>, Comparator<T> {
	
    /**
     * A default object order (based on {@link Object#hashCode} as 32-bits 
     * unsigned index).
     */
    @Realtime(limit = UNKNOWN)
    public static final Order<Object> DEFAULT = HashOrderImpl.INSTANCE;

    /**
     * An identity object order (based on {@link System#identityHashCode}
     * as 32-bits unsigned index).
     */
    @Realtime(limit = CONSTANT)
    public static final Order<Object> IDENTITY
        = IdentityHashOrderImpl.INSTANCE;

    /**
     * A lexicographic order for any {@link CharSequence}.
     */
    @Realtime(limit = LINEAR)
    public static final Order<CharSequence> LEXICAL
        = LexicalOrderImpl.INSTANCE;

    /**
     * A case insensitive lexicographic order for any {@link CharSequence}.
     */
    @Realtime(limit = LINEAR)
    public static final Order<CharSequence> LEXICAL_CASE_INSENSITIVE 
       = CaseInsensitiveLexicalOrderImpl.INSTANCE;
    
    /**
     * A numeric order based on {@link Number#doubleValue()}.
     */
    @Realtime(limit = LOG_N)
    public static final Order<Number> NUMERIC = null; // TODO
    
    /**
     * An unsigned 32-bits order ({@code null} values not supported}).
     */
    @Realtime(limit = LOG_N)
    public static final Order<Index> INDEX = IndexOrderImpl.INSTANCE;
    
     /**
     * A two-dimensional order (index-based) preserving space locality.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Quadtree">Wikipedia: Quadtree</a>
     * @see #INDEX
     */
    @Realtime(limit = LOG_N)
    public static Order<Binary<Index, Index>> QUADTREE = null; // TODO
    
    /**
     * A three-dimensional order (index-based) preserving space locality.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Octree">Wikipedia: Octree</a>
     * @see #NUMERIC
     */
    @Realtime(limit = LOG_N)
    public static Order<Ternary<Index, Index, Index>> OCTREE = null; // TODO   

    //////////////////////////////////////////////////////////////////////////////
	// Comparators.
	//
    
    /**
     * Returns the index (unsigned 32-bits value) of the specified object.
     */
    int indexOf(T object);

    /**
     * Returns the sub-order for the specified object or {@link null} if none.
     */
    Order<T> subOrder(T obj);
    
}
