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

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.internal.function.IdentityOrderImpl;
import org.javolution.util.internal.function.LexicalOrderImpl;
import org.javolution.util.internal.function.StandardOrderImpl;

/**
 * A disposition of things following one after another, smallest first.
 * 
 * Implementing classes should ensure consistency between {@link Equality#areEqual}, 
 * {@link Comparator#compare} and {@link Indexer#indexOf}; specifically they should ensure that 
 * if {@code areEqual(x,y)} then {@code (compare(x,y) == 0)} and {@code indexOf(x) == indexOf(y)}. 
 * Furthermore, if {@code (compare(x,y) < 0)} then {@code indexOf(x) <= indexOf(y)} (unsigned).
 * There is no requirement that {@code (compare(x,y) == 0)} implies {@code areEqual(x,y)}.
 *       
 * @param <T> the type of objects being ordered.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public abstract class Order<T> implements Equality<T>, Comparator<T>, Indexer<T> {
    private static final long serialVersionUID = 0x700L; // Version.
    	
    /**
     * Returns an order based on object equality and hash code.
     */
    @SuppressWarnings("unchecked")
	@Realtime(limit = UNKNOWN)
    public static <T> Order<T> standard() {
    	return (Order<T>) STANDARD;
    }
    private static final StandardOrderImpl STANDARD = new StandardOrderImpl();
    
    /**
     * Returns an order based on object identity and system hash code.
     */
    @SuppressWarnings("unchecked")
	@Realtime(limit = CONSTANT)
    public static <T> Order<T> identity() {
    	return (Order<T>) IDENTITY;
    }
    private static final IdentityOrderImpl IDENTITY = new IdentityOrderImpl();

    /**
     * Returns a lexical order for any {@link CharSequence}, the order is given by the UTF-16 code of the characters.
     */
    @Realtime(limit = LINEAR)
    public static Order<CharSequence> lexical() {
    	return LEXICAL;
    }
    private static final LexicalOrderImpl LEXICAL = new LexicalOrderImpl(0);

  
//    /**
//     * Returns a numeric order for any {@link Number}, the order is consistent with the order of 
//     * the {@link double} representation of the number.
//     */
//    @Realtime(limit = CONSTANT)
//    public static Order<Number> numeric() {
//    	return NUMERIC;
//    }
//    private static final Order<Number> NUMERIC = null; // TODO
//    
//    
//     /**
//     * A two-dimensional order (index-based) preserving space locality.
//     * 
//     * @see <a href="http://en.wikipedia.org/wiki/Quadtree">Wikipedia: Quadtree</a>
//     * @see #INDEX
//     */
//    @Realtime(limit = LOG_N)
//    public static Order<Binary<Index, Index>> QUADTREE = null; // TODO
//    
//    /**
//     * A three-dimensional order (index-based) preserving space locality.
//     * 
//     * @see <a href="http://en.wikipedia.org/wiki/Octree">Wikipedia: Octree</a>
//     * @see #NUMERIC
//     */
//    @Realtime(limit = LOG_N)
//    public static Order<Ternary<Index, Index, Index>> OCTREE = null; // TODO   
//    

    /**
     * Returns the order from the specified indexer. 
     * 
     * @param indexer the object indexer.
     */
    @Realtime(limit = LOG_N)
    public static <T> Order<T> valueOf(final Indexer<T> indexer) {
        return new Order<T>() {
            private static final long serialVersionUID = Order.serialVersionUID;

            @Override
            public boolean areEqual(@Nullable T left, @Nullable T right) {
                return (left == right) || (left != null && left.equals(right)); 
            }

            @Override
            public int compare(@Nullable T left, @Nullable T right) {
                long leftIndex = indexer.indexOf(left);
                long rightIndex = indexer.indexOf(right);
                if (leftIndex == rightIndex) return 0;
                return MathLib.unsignedLessThan(leftIndex, rightIndex) ? -1 : 1;
            }

            @Override
            public long indexOf(@Nullable T obj) {
                return indexer.indexOf(obj);
            }

         };
    }

    @Override
    public abstract int compare(@Nullable T left, @Nullable T right);

    /**
     * Returns the sub-order for the specified object (default none).
     * 
     * @param obj the object for which the sub-order if any is returned.
     * @return the sub-order or {@code null} if none.
     */
    public @Nullable Order<T> subOrder(@Nullable T obj) {
    	return null;
    }
    
}
