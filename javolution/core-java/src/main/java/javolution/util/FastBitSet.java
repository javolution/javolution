/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.LINEAR;
import javolution.lang.Constant;
import javolution.lang.Index;
import javolution.lang.Realtime;
import javolution.util.internal.bitset.BitSetImpl;
import javolution.util.internal.bitset.UnmodifiableBitSetImpl;

/**
 * <p> A high-performance bitset integrated with the collection framework as 
 *     a set of {@link Index indices} and obeying the collection semantic
 *     for methods such as {@link #size} (cardinality) or {@link #equals}
 *     (same set of indices).</p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public abstract class FastBitSet extends FastSet<Index> {

    private static final long serialVersionUID = 0x700L; // Version.
    
    /**
     * Returns an empty bit set.
     */
	public static FastBitSet newBitSet() {
        return new BitSetImpl();
    }
    
    /**
     * Returns a bit set holding the specified bits.
     * 
     * @param bits a long array containing a little-endian representation
     *        of a sequence of bits to be used as the initial bits of the
     *        new bit set.
     */
    public static FastBitSet newBitSet(@Constant long[] bits) {
        return new BitSetImpl(bits);
    }

    /**
     * Default constructor.
     */
     protected FastBitSet() {
     }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

     @Override
     public FastBitSet unmodifiable() {
         return new UnmodifiableBitSetImpl(this); 
     }

     ////////////////////////////////////////////////////////////////////////////
     // BitSet Operations.
     //

     /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public abstract void and(FastBitSet that);

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    @Realtime(limit = LINEAR)
    public abstract void andNot(FastBitSet that);

    /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public abstract int cardinality();

    /**
     * Sets all bits in the set to {@code false} (empty the set).
     */
    @Override
    public abstract void clear();

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public abstract void clear(int bitIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared.
     * @param  toIndex index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public abstract void clear(int fromIndex, int toIndex);

    @Override
	public abstract FastBitSet clone();

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public abstract void flip(int bitIndex);

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public abstract void flip(int fromIndex, int toIndex);
   
    /**
     * Returns {@code true } if the specified integer is in 
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public abstract boolean get(int bitIndex);

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public abstract FastBitSet get(int fromIndex, int toIndex);

    /** 
     * Sets the specified bit, returns <code>true</code>
     * if previously set. */
    public abstract boolean getAndSet(int bitIndex, boolean value);

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    @Realtime(limit = LINEAR)
    public abstract boolean intersects(FastBitSet that);

    /**
     * Returns {@code true} if no bits in this set are set to 1 (cardinality == 0)
     *
     * @return {@code true} if all bits are 0, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return cardinality() == 0;
    }
    
    /**
     * Returns the logical number of bits actually used by this bit
     * set.  It returns the index of the highest set bit plus one.
     * 
     * <p> Note: This method does not return the number of set bits
     *           which is returned by {@link #size} </p>
     *
     * @return the index of the highest set bit plus one.
     */
    public abstract int length();

    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public abstract int nextClearBit(int fromIndex);

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *    for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i+1)) {
     *         ...
     *    }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public abstract int nextSetBit(int fromIndex);

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public abstract void or(FastBitSet that);

    /**
     * Returns the index of the previous {@code false} bit, 
     * from the specified bit (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1} 
     */
    public abstract int previousClearBit(int fromIndex);

    /**
     * Returns the index of the previous {@code true} bit, from the 
     * specified bit (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *     for (int i = length(); (i = previousSetBit(i-1)) >= 0; ) {
     *        ...
     *     }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1} 
     */
    public abstract int previousSetBit(int fromIndex);

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public abstract void set(int bitIndex);

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public abstract void set(int bitIndex, boolean value);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set.
     * @param  toIndex index after the last bit to be set.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public abstract void set(int fromIndex, int toIndex);

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex the end range (exclusive).
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    @Realtime(limit = LINEAR)
    public abstract void set(int fromIndex, int toIndex, boolean value);

    /** Returns the minimal length <code>long[]</code> representation of this 
     * bitset.
     * @return Array of longs representing this bitset 
     */
    public abstract long[] toLongArray();

	/**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public abstract void xor(FastBitSet that);
}
