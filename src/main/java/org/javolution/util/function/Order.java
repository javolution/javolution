/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

import static org.javolution.annotations.Realtime.Limit.LINEAR;
import static org.javolution.annotations.Realtime.Limit.LOG_N;

import java.util.Comparator;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Binary;
import org.javolution.lang.Index;
import org.javolution.lang.MathLib;
import org.javolution.lang.Ternary;
import org.javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import org.javolution.util.internal.function.IdentityOrderImpl;
import org.javolution.util.internal.function.IndexOrderImpl;
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
     * An order based on object equality and hash code.
     */
    @Realtime(limit = LINEAR)
    public static final StandardOrderImpl STANDARD = new StandardOrderImpl();

    /**
     * An order based on object identity and system hash code.
     */
    @Realtime(limit = LINEAR)
    public static final IdentityOrderImpl IDENTITY = new IdentityOrderImpl();

    /**
     * A lexical order for any {@link CharSequence}.
     * 
     * For the characters that are alphabetical letters, the character order coincides with the alphabetical order.
     * Digits precede letters, uppercase letters precede lowercase ones.
     * 
     */
    @Realtime(limit = LINEAR)
    public static final LexicalOrderImpl LEXICAL = new LexicalOrderImpl();

    /**
     * A case insensitive lexical order for any {@link CharSequence}.
     * 
     * For the characters that are alphabetical letters, the character order coincides with the alphabetical order.
     * Digits precede letters, uppercase letters and lowercase have the same order.
     */
    @Realtime(limit = LINEAR)
    public static final CaseInsensitiveLexicalOrderImpl LEXICAL_CASE_INSENSITIVE 
        = new CaseInsensitiveLexicalOrderImpl();

    /**
     * An order for any {@link Number}.
     */
    @Realtime(limit = LOG_N)
    public static final Order<Number> NUMERIC = null; // TODO
    
    /**
     * An order for unsigned 32-bits indices (direct mapping).
     */
    @Realtime(limit = LOG_N)
    public static final IndexOrderImpl INDEX = new IndexOrderImpl();
    
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
     * Returns the order from the specified indexer. 
     * 
     * @param indexer the object indexer.
     */
    @Realtime(limit = LOG_N)
    public static <T> Order<T> valueOf(final Indexer<T> indexer) {
        return new Order<T>() {
            private static final long serialVersionUID = Order.serialVersionUID;

            @Override
            public boolean areEqual(T left, T right) {
                return left.equals(right); // E cannot be null.
            }

            @Override
            public int compare(T left, T right) {
                int leftIndex = indexer.indexOf(left);
                int rightIndex = indexer.indexOf(right);
                if (leftIndex == rightIndex) return 0;
                return MathLib.unsignedLessThan(leftIndex, rightIndex) ? -1 : 1;
            }

            @Override
            public int indexOf(T obj) {
                return indexer.indexOf(obj);
            }

         };
    }

    
    /**
     * Returns the sub-order for the specified object (default none).
     * 
     * @param obj the object for which the sub-order if any is returned.
     * @return the sub-order or {@code null} if none.
     * @throws NullPointerException if the specified object is {@code null}.
     */
    public @Nullable Order<T> subOrder(T obj) {
        return new Order<T>() {

            @Override
            public boolean areEqual(T left, T right) {
                return Order.this.areEqual(left, right);
            }

            @Override
            public int compare(T o1, T o2) {
                return Order.this.compare(o1, o2);
            }

            @Override
            public int indexOf(T obj) {
                // TODO Auto-generated method stub
                return 0;
            }
        };
    }
    
}
