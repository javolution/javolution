/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.RealTime.Limit.LINEAR;
import javolution.internal.util.bitset.BitSetServiceImpl;
import javolution.internal.util.bitset.SharedBitSetImpl;
import javolution.internal.util.bitset.UnmodifiableBitSetImpl;
import javolution.lang.RealTime;
import javolution.util.service.BitSetService;

/**
 * <p> A high-performance bit set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 * 
 * <p> This class is integrated with the collection framework as 
 *     a set of {@link Index indices} and obeys the collection semantic
 *     for methods such as {@link #size} (cardinality) or {@link #equals}
 *     (same set of indices).</p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class FastBitSet extends FastSet<Index> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
    * Holds the bit set implementation.
    */
    private final BitSetService service;

    /**
    * Creates an empty bit set.
    */
    public FastBitSet() {
        service = new BitSetServiceImpl();
    }

    /**
     * Creates a fast bit set based on the specified implementation.
     */
    protected FastBitSet(BitSetService impl) {
        this.service = impl;
    }

    /***************************************************************************
     * Views.
     */

    @Override
    public FastBitSet unmodifiable() {
        return new FastBitSet(new UnmodifiableBitSetImpl(service));
    }

    @Override
    public FastBitSet shared() {
        return new FastBitSet(new SharedBitSetImpl(service));
    }

    /***************************************************************************
     * Bit set operations.
     */

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @RealTime(limit = LINEAR)
    public void and(FastBitSet that) {
        service.and(that.service);
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    @RealTime(limit = LINEAR)
    public void andNot(FastBitSet that) {
        service.andNot(that.service);
    }

    /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public int cardinality() {
        return service.cardinality();
    }

    /**
     * Sets all bits in the set to {@code false} (empty the set).
     */
    @Override
    public void clear() {
        service.clear();
    }

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public void clear(int bitIndex) {
        service.clear(bitIndex);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared.
     * @param  toIndex index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @RealTime(limit = LINEAR)
    public void clear(int fromIndex, int toIndex) {
        service.clear(fromIndex, toIndex);
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void flip(int bitIndex) {
        service.flip(bitIndex);
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @RealTime(limit = LINEAR)
    public void flip(int fromIndex, int toIndex) {
        service.flip(fromIndex, toIndex);
    }

    /**
     * Returns {@code true } if the specified integer is in 
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public boolean get(int bitIndex) {
        return service.get(bitIndex);
    }

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @RealTime(limit = LINEAR)
    public FastBitSet get(int fromIndex, int toIndex) {
        return new FastBitSet(service.get(fromIndex, toIndex));
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    @RealTime(limit = LINEAR)
    public boolean intersects(FastBitSet that) {
        return service.intersects(that.service);
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
    public int length() {
        return service.length();
    }

    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextClearBit(int fromIndex) {
        return service.nextClearBit(fromIndex);
    }

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
    public int nextSetBit(int fromIndex) {
        return service.nextSetBit(fromIndex);
    }

    /**
     * Returns the index of the previous {@code false} bit, 
     * from the specified bit (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1} 
     */
    public int previousClearBit(int fromIndex) {
        return service.previousClearBit(fromIndex);
    }

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
    public int previousSetBit(int fromIndex) {
        return service.previousSetBit(fromIndex);
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @RealTime(limit = LINEAR)
    public void or(FastBitSet that) {
        service.or(that.service);
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex) {
        service.set(bitIndex);
    }

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex, boolean value) {
        service.set(bitIndex, value);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set.
     * @param  toIndex index after the last bit to be set.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @RealTime(limit = LINEAR)
    public void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        service.set(fromIndex, toIndex);
    }

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex the end range (exclusive).
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    @RealTime(limit = LINEAR)
    public void set(int fromIndex, int toIndex, boolean value) {
        service.set(fromIndex, toIndex, value);
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @RealTime(limit = LINEAR)
    public void xor(FastBitSet that) {
        service.xor(that.service);
    }

    /***************************************************************************
     * Misc.
     */

    @Override
    @RealTime(limit = LINEAR)
    public FastBitSet copy() {
        return this.get(0, length());
    }

    @Override
    public FastBitSet addAll(Index... elements) {
        return (FastBitSet) super.addAll(elements);
    }

    @Override
    protected BitSetService service() {
        return service;
    }

}
