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
import static javolution.lang.Realtime.Limit.LOG_N;
import static javolution.lang.Realtime.Limit.UNKNOWN;

import java.util.Comparator;

import javolution.lang.Binary;
import javolution.lang.Index;
import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.lang.Ternary;
import javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import javolution.util.internal.function.HashOrderImpl;
import javolution.util.internal.function.IdentityHashOrderImpl;
import javolution.util.internal.function.LexicalOrderImpl;
import javolution.util.internal.function.NaturalOrderImpl;

/**
 * <p> A disposition of things following one after another, smallest first.</p>
 * 
 * <p> Implementing classes should ensure consistency between  
 *     {@link Equality#areEqual}, {@link Comparator#compare} and 
 *     {@link #indexOf}; specifically they should ensure that 
 *     if {@code (areEqual(x,y))} then {@code (compare(x,y) == 0)} and 
 *     of {@code indexOf(x) < indexOf(y)} then {@code (compare(x,y) < 0)}.</p>
 *       
 * @param <T> the type of objects being ordered.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public interface Order<T> extends Equality<T>, Comparator<T> {
	
    /**
     * An order based on the object hash code and equals methods.
     * The index of an object is its hash code value as unsigned 32 bits.
     */
    @Parallelizable
    @Realtime(limit = UNKNOWN)
    public static final Order<Object> HASH = HashOrderImpl.INSTANCE;

    /**
     * An order based on system hash code and operator ({@code == }).
     */
    @Parallelizable
    @Realtime(limit = CONSTANT)
    public static final Order<Object> IDENTITY_HASH
        = IdentityHashOrderImpl.INSTANCE;

    /**
     * A lexicographic order for any {@link CharSequence}.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final Order<CharSequence> LEXICAL
        = LexicalOrderImpl.INSTANCE;

    /**
     * A case insensitive lexicographic order for any {@link CharSequence}.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final Order<CharSequence> LEXICAL_CASE_INSENSITIVE 
       = CaseInsensitiveLexicalOrderImpl.INSTANCE;
    
    /**
     * A numeric order based on {@link Number#doubleValue()}.
     */
    @Parallelizable
    @Realtime(limit = LOG_N)
    public static final Order<Number> NUMERIC = null;
    
    /**
     * An unsigned 32-bits order.
     */
    @Parallelizable
    @Realtime(limit = LOG_N)
    public static final Order<Index> INDEX = null;
    
     /**
     * A two-dimensional order (index-based) preserving space locality.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Quadtree">Wikipedia: Quadtree</a>
     * @see #INDEX
     */
    @Parallelizable
    @Realtime(limit = LOG_N)
    public static Order<Binary<Index, Index>> QUADTREE = null;
    
    /**
     * A three-dimensional order (index-based) preserving space locality.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Octree">Wikipedia: Octree</a>
     * @see #NUMERIC
     */
    @Parallelizable
    @Realtime(limit = LOG_N)
    public static Order<Ternary<Index, Index, Index>> OCTREE = null;    

    //////////////////////////////////////////////////////////////////////////////
	// Comparators.
	//
    
    /**
     * A natural order (only for {@link Comparable} instances). 
     * The method {@link #indexOf indexOf} returns {@code 0})
     * and does not allow direct access in a collection.
     * 
     * @throws ClassCastException if used with non {@link Comparable} instances.
     */
    @Parallelizable
    @Realtime(limit = UNKNOWN)
    public static final Order<Object> NATURAL 
        = NaturalOrderImpl.INSTANCE;
       
    /**
     * Returns the index (unsigned 32-bits value) of the specified object.
     * If an absolute order cannot be determined this method returns 
     * {@code 0} (see {@link #NATURAL}). This methods also returns 
     * {@code 0} if the specified object is {@code null}.
     */
    int indexOf(T object);

    /**
     * Returns the bit length of this order index (maximum 32).
     */
    int bitLength();

    /**
     * Returns the sub-order for the specified object or {@link null} if none.
     */
    Order<T> subOrder(T obj);
    
}
