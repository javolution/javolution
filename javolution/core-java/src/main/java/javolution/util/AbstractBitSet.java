/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.Index;

/**
 * The parent class for all bit set implementations.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see FastTable
 */
public abstract class AbstractBitSet implements Copyable<AbstractBitSet> {

    //
    // Read Accessors.
    //

    /** Returns the packed bits of this bit set as <code>long[]</code> */
    public abstract long[] toBits();

    /** See {@link FastBitSet#cardinality() } */
    public abstract int cardinality();

    /** See {@link FastBitSet#get(int) } */
    public abstract boolean get(int bitIndex);

    /** See {@link FastBitSet#get(int, int) } */
    public abstract AbstractBitSet get(int fromIndex, int toIndex);

    /** See {@link FastBitSet#intersects(FastBitSet) } */
    public abstract boolean intersects(AbstractBitSet that);

    /** See {@link FastBitSet#length() } */
    public abstract int length();

    //
    // Iterations
    //

    /** See {@link FastBitSet#nextClearBit(int) } */
    public abstract int nextClearBit(int fromIndex);
    
    /** See {@link FastBitSet#nextSetBit(int) } */
    public abstract int nextSetBit(int fromIndex);

    /** See {@link FastBitSet#forEach(Functor) } */
    public abstract <R> FastCollection<R> forEach(Functor<Index, R> functor);

    /** See {@link FastBitSet#doWhile(Predicate) } */
    public abstract void doWhile(Predicate<Index> predicate);

    /** See {@link FastBitSet#removeAll(Predicate) } */
    public abstract boolean removeAll(Predicate<Index> predicate);

    //
    // Clear/Set/Flip Operations
    //

    /** See {@link FastBitSet#clear() } */
    public abstract void clear();
    
    /** See {@link FastBitSet#clear(int) } */
    public abstract void clear(int bitIndex);

    /** See {@link FastBitSet#clear(int, int) } */
    public abstract void clear(int fromIndex, int toIndex);
    
    /** Clear or sets the specified bit, returns <code>true</code> 
     * if previously set; <code>false</code> otherwise. */
    public abstract boolean getAndSet(int bitIndex, boolean value);
    
    /** See {@link FastBitSet#set(int) } */
    public abstract void set(int bitIndex);

    /** See {@link FastBitSet#set(int, boolean) } */
    public abstract void set(int bitIndex, boolean value);

    /** See {@link FastBitSet#set(int, int) } */
    public abstract void set(int fromIndex, int toIndex);
    
    /** See {@link FastBitSet#set(int, int, boolean) } */
    public abstract void set(int fromIndex, int toIndex, boolean value);

    /** See {@link FastBitSet#flip(int) } */
    public abstract void flip(int bitIndex);

    /** See {@link FastBitSet#flip(int, int) } */
    public abstract void flip(int fromIndex, int toIndex);
    
    //
    // Logical Operations
    //

    /** See {@link FastBitSet#and(FastBitSet) } */
    public abstract void and(AbstractBitSet that);

    /** See {@link FastBitSet#andNot(FastBitSet) } */
    public abstract void andNot(AbstractBitSet that);

    /** See {@link FastBitSet#or(FastBitSet) } */
    public abstract void or(AbstractBitSet that);

    /** See {@link FastBitSet#xor(FastBitSet) } */
    public abstract void xor(AbstractBitSet that);
    
    //
    // Miscalleneous.
    // 

    /** See {@link FastBitSet#equals(Object) } */
    public abstract boolean equals(AbstractBitSet that);

}
