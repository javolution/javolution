/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import javolution.util.FastBitSet;
import javolution.util.FastTable;
import javolution.util.Index;

/**
 * The set of related functionalities which can be used/reused to 
 * implement bit-sets collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see FastTable
 */
public interface BitSetService extends SetService<Index> {

    //
    // Read Accessors.
    //

    /** 
     * See {@link FastBitSet#cardinality() }
     * @return cardinality of the bit set
     */
    int cardinality();

    /** 
     * See {@link FastBitSet#get(int) }
     * @param bitIndex Index of the bit to get
     * @return true if the bit is set, false otherwise 
     */
    boolean get(int bitIndex);

    /** 
     * See {@link FastBitSet#get(int, int) } 
     * @param fromIndex Index to Start From
     * @param toIndex Index to End At
     * @return BitSetService from between the specified indexes 
     */
    BitSetService get(int fromIndex, int toIndex);

    /** 
     * See {@link FastBitSet#intersects(FastBitSet) }
     * @param that BitSetService to check for intersection
     * @return True fi the bitset intersects, false otherwise 
     */
    boolean intersects(BitSetService that);

    /** 
     * See {@link FastBitSet#length() }
     * @return length of the BitSet 
     */
    int length();

    //
    // Iterations
    //

    /** See {@link FastBitSet#nextClearBit(int) }
     * @param fromIndex Index to start searching at
     * @return Position of the next clear bit 
     */
    int nextClearBit(int fromIndex);

    /** See {@link FastBitSet#nextSetBit(int) }
     * @param fromIndex Index to start searching at
     * @return Position of the next set bit 
     */
    int nextSetBit(int fromIndex);

    /** See {@link FastBitSet#previousClearBit(int) }
     * @param fromIndex Index to start searching at
     * @return Position of the previous clear bit 
     */
    int previousClearBit(int fromIndex);

    /** See {@link FastBitSet#previousSetBit(int) }
     * @param fromIndex Index to start searching at
     * @return Position of the previous set bit 
     */
    int previousSetBit(int fromIndex);

    //
    // Clear/Set/Flip Operations
    //

    /** See {@link FastBitSet#clear(int) }
     * @param bitIndex Index to clear 
     */
    void clear(int bitIndex);

    /** See {@link FastBitSet#clear(int, int) }
     * @param fromIndex Starting index to Clear
     * @param toIndex Ending index to Clear 
     */
    void clear(int fromIndex, int toIndex);

    /** Clear or sets the specified bit,
     * @param bitIndex Index to modify
     * @param value true to set the bit, false to clear it 
     * @return <code>true</code> if previously set; <code>false</code> otherwise. 
     */
    boolean getAndSet(int bitIndex, boolean value);

    /** See {@link FastBitSet#set(int) }
     * @param bitIndex Index of the bit to set 
     */
    void set(int bitIndex);

    /** See {@link FastBitSet#set(int, boolean) }
     * @param bitIndex Index to set
     * @param value true to set the bit, false to clear it 
     */
    void set(int bitIndex, boolean value);

    /** See {@link FastBitSet#set(int, int) }
     * @param fromIndex Index to start setting at
     * @param toIndex Index to end setting at 
     */
    void set(int fromIndex, int toIndex);

    /** See {@link FastBitSet#set(int, int, boolean) }
     * @param fromIndex Index to start setting at
     * @param toIndex Index to end setting at
     * @param value true to set the bit, false to clear it 
     */
    void set(int fromIndex, int toIndex, boolean value);

    /** See {@link FastBitSet#flip(int) }
     * @param bitIndex Index of the bit to flip 
     */
    void flip(int bitIndex);

    /** See {@link FastBitSet#flip(int, int) }
     * @param fromIndex Index to start flipping at
     * @param toIndex Index to end flipping at 
     */
    void flip(int fromIndex, int toIndex);

    //
    // Operators Operations
    //

    /** See {@link FastBitSet#and(FastBitSet) } 
     * @param that BitSetService to perform an AND against
     */
    void and(BitSetService that);

    /** See {@link FastBitSet#andNot(FastBitSet) }
     * @param that BitSetService to perform an AND NOT against 
     */
    void andNot(BitSetService that);

    /** See {@link FastBitSet#or(FastBitSet) }
     * @param that BitSetService to perform an OR against 
     */
    void or(BitSetService that);

    /** See {@link FastBitSet#xor(FastBitSet) }
     * @param that BitSetService to perform an XOR against 
     */
    void xor(BitSetService that);

    //
    // Misc.
    //

    /** Returns the minimal length <code>long[]</code> representation of this 
     * bitset.
     * @return Array of longs representing this bitset 
     */
    long[] toLongArray();

}
