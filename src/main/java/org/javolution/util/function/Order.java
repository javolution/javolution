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

import org.javolution.annotations.Realtime;
import org.javolution.lang.Binary;
import org.javolution.lang.Index;
import org.javolution.lang.Ternary;
import org.javolution.util.internal.function.CaseInsensitiveLexicalOrderImpl;
import org.javolution.util.internal.function.IndexOrderImpl;
import org.javolution.util.internal.function.LexicalOrderImpl;

/**
 * A disposition of things following one after another, smallest first.
 * 
 * Implementing classes should ensure consistency between {@link Equality#areEqual}, 
 * {@link Comparator#compare} and {@link Indexer#indexOf}; specifically they should ensure that 
 * if {@code areEqual(x,y)} then {@code (compare(x,y) == 0)} and {@code indexOf(x) == indexOf(y)}. 
 * Furthermore, if {@code (compare(x,y) < 0)} then {@code indexOf(x) <= indexOf(y)} (unsigned).
 * There is no constraint on indices when {@code (compare(x,y) == 0)} (e.g. hash order).
 *       
 * @param <T> the type of objects being ordered.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public interface Order<T> extends Equality<T>, Comparator<T>, Indexer<T> {
	
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
        
}
