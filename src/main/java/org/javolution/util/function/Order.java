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
import static org.javolution.annotations.Realtime.Limit.LOG_N;
import static org.javolution.annotations.Realtime.Limit.UNKNOWN;

import java.util.Comparator;

import org.javolution.annotations.Realtime;
import org.javolution.lang.Binary;
import org.javolution.lang.Index;
import org.javolution.lang.Ternary;
import org.javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import org.javolution.util.internal.function.HashOrderImpl;
import org.javolution.util.internal.function.IdentityHashOrderImpl;
import org.javolution.util.internal.function.IndexOrderImpl;
import org.javolution.util.internal.function.LexicalOrderImpl;
import org.javolution.util.internal.function.MultiOrderImpl;
import org.javolution.util.internal.function.NaturalOrderImpl;

/**
 * <p> A disposition of things following one after another, smallest first.</p>
 * 
 * <p> Implementing classes should ensure consistency between {@link Equality#areEqual}, 
 *     {@link Comparator#compare} and {@link #indexOf}; specifically they should ensure that 
 *     if {@code areEqual(x,y)} then {@code (compare(x,y) == 0)} and then {@code indexOf(x) == indexOf(y)}. 
 *     Furthermore, if {@code (compare(x,y) < 0)} then {@code MathLib.unsignedLessThan(indexOf(x),indexOf(y))}.</p>
 *     
 * <p> It is <i>not</i> required that if {@code (compare(x,y) == 0)} then {@code areEqual(x,y) == true}.
 *     For example, two objects may compare to zero according to their hash value (default order) and still
 *     be two different objects. For specific orders such as the {@link #MULTI} order (multi-maps /multi-sets) 
 *     it is never the case either (allowing for elements duplication).</p>
 *       
 * @param <T> the type of objects being ordered.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public interface Order<T> extends Equality<T>, Comparator<T> {
	
    /**
     * A default object order (based on {@link Object#hashCode} as 32-bits unsigned index).
     */
    @Realtime(limit = UNKNOWN)
    public static final Order<Object> DEFAULT = HashOrderImpl.INSTANCE;

    /**
     * The natural order, this order is not efficient to be used with ordered collections as its 
     * index always returns {@code 0} (relative order).
     */
    @Realtime(limit = LOG_N)
    public static final Order<Comparable<?>> NATURAL = NaturalOrderImpl.INSTANCE;

    /**
     * An order (based on {@link Object#hashCode} as 32-bits unsigned index) for which all elements 
     * are considered distinct (ref. multi-maps/multi-sets).
     */
    @Realtime(limit = CONSTANT)
    public static final Order<Object> MULTI = MultiOrderImpl.INSTANCE;

    /**
     * An identity object order (based on {@link System#identityHashCode} as 32-bits unsigned index).
     */
    @Realtime(limit = CONSTANT)
    public static final Order<Object> IDENTITY = IdentityHashOrderImpl.INSTANCE;

    /**
     * A lexicographic order for any {@link CharSequence}.
     */
    @Realtime(limit = LINEAR)
    public static final Order<CharSequence> LEXICAL = LexicalOrderImpl.INSTANCE;

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
     * An unsigned 32-bits order.
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

    /**
     * Returns the index (unsigned 32-bits value) of the specified object.
     * 
     * @param obj the object for which the index is calculated (cannot be {@code null}).
     * @return the corresponding index (unsigned).
     * @throws NullPointerException if {@code obj == null}
     */
    int indexOf(T obj);

    /**
     * Returns the sub-order for the specified object or {@link null} if none.
     * 
     * @param obj the object for which the index is calculated (cannot be {@code null}).
     * @return the sub-order for the specified element.
     * @throws NullPointerException if {@code obj == null}
     */
    Order<T> subOrder(T obj);
    
    /**
     * Compares the two arguments for order.  Returns a negative integer, zero, or a positive integer as the 
     * first argument is less than, equal to, or greater than the second.
     * 
     * @param left the first object to be compared.
     * @param right the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, 
     *         or greater than the second.
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @Override
    int compare(T left, T right);
    
}
