/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;
import java.util.Set;
import javolution.lang.Functor;
import javolution.lang.MathLib;
import javolution.lang.Predicate;

/**
 * <p> A table of bits equivalent to a packed set of non-negative numbers.</p>
 * 
 * <p> This class is integrated with the collection framework (as 
 *     a set of {@link Index indices} and obeys the collection semantic
 *     for methods such as {@link #size} (cardinality) or {@link #equals}
 *     (same set of indices).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, February 24, 2008
 */
public class FastBitSet extends FastCollection<Index> implements Set<Index> {

    /**
     * Holds the bits (64 bits per long).
     */
    private long[] bits;

    /**
     * Creates a bit set of small initial capacity. All bits are initially 
     * {@code false}.
     */
    public FastBitSet() {
        this(64);
    }

    /**
     * Creates a bit set of specified initial capacity (in bits). 
     * All bits are initially {@code false}.  This
     * constructor reserves enough space to represent the indices 
     * from {@code 0} to {@code bitSize-1}.
     * 
     * @param bitSize the initial capacity in bits.
     */
    public FastBitSet(int bitSize) {
        bits = new long[((bitSize - 1) >> 6) + 1];
    }

    /**
     * Adds the specified index to this set. This method is equivalent 
     * to <code>set(index.intValue())</code>.
     * 
     * @param index the integer value to be added to this set.
     * @return {@code true} if this set did not contains the specified
     *         index; {@code false} otherwise.
     */
    @Override
    public boolean add(Index index) {
        int bitIndex = index.intValue();
        if (this.get(bitIndex)) return false; // Already there.
        set(bitIndex);
        return true;
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void and(FastBitSet that) {
        final int n = MathLib.min(this.bits.length, that.bits.length);
        for (int i = 0; i < n; ++i) {
            this.bits[i] &= that.bits[i];
        }
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    public void andNot(FastBitSet that) {
        int i = Math.min(this.bits.length, that.bits.length);
        while (--i >= 0) {
            this.bits[i] &= ~that.bits[i];
        }
    }

    /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < bits.length; i++) {
            sum += MathLib.bitCount(bits[i]);
        }
        return sum;
    }

    /**
     * Sets all bits in the set to {@code false} (empty the set).
     */
    @Override
    public void clear() {
        bits = new long[1];
    }

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public void clear(int bitIndex) {
        int longIndex = bitIndex >> 6;
        if (longIndex >= bits.length)
            return;
        bits[longIndex] &= ~(1L << bitIndex);
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
    public void clear(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        if (i >= bits.length)
            return; // Ensures that i < _length
        int j = toIndex >>> 6;
        if (i == j) {
            bits[i] &= ((1L << fromIndex) - 1) | (-1L << toIndex);
            return;
        }
        bits[i] &= (1L << fromIndex) - 1;
        if (j < bits.length) {
            bits[j] &= -1L << toIndex;
        }
        for (int k = i + 1; (k < j) && (k < bits.length); k++) {
            bits[k] = 0;
        }
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void flip(int bitIndex) {
        int i = bitIndex >> 6;
        setLength(i + 1);
        bits[i] ^= 1L << bitIndex;
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void flip(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        int j = toIndex >>> 6;
        setLength(j + 1);
        if (i == j) {
            bits[i] ^= (-1L << fromIndex) & ((1L << toIndex) - 1);
            return;
        }
        bits[i] ^= -1L << fromIndex;
        bits[j] ^= (1L << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] ^= -1;
        }
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
        int i = bitIndex >> 6;
        return (i >= bits.length) ? false : (bits[i] & (1L << bitIndex)) != 0;
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
    public FastBitSet get(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        FastBitSet bitSet = new FastBitSet();
        int length = MathLib.min(bits.length, (toIndex >>> 6) + 1);
        bitSet.setLength(length);
        System.arraycopy(bits, 0, bitSet.bits, 0, length);
        bitSet.clear(0, fromIndex);
        bitSet.clear(toIndex, length << 6);
        return bitSet;
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    public boolean intersects(FastBitSet that) {
        int i = MathLib.min(this.bits.length, that.bits.length);
        while (--i >= 0) {
            if ((bits[i] & that.bits[i]) != 0) {
                return true;
            }
        }
        return false;
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
        for (int i = bits.length; --i >= 0;) {
            long l = bits[i];
            if (l != 0) {
                return i << 6 + 64 - MathLib.numberOfTrailingZeros(l);
            }
        }
        return 0;
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
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0) {
                    return fromIndex;
                }
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return fromIndex;
    }

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *    for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
     *         ...
     *    }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextSetBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return -1;
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void or(FastBitSet that) {
        if (that.bits.length > this.bits.length) {
            setLength(that.bits.length);
        }
        for (int i = that.bits.length; --i >= 0;) {
            bits[i] |= that.bits[i];
        }
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex) {
        int i = bitIndex >> 6;
        if (i >= bits.length) {
            setLength(i + 1);
        }
        bits[i] |= 1L << bitIndex;
    }

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
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
    public void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        int j = toIndex >>> 6;
        setLength(j + 1);
        if (i == j) {
            bits[i] |= (-1L << fromIndex) & ((1L << toIndex) - 1);
            return;
        }
        bits[i] |= -1L << fromIndex;
        bits[j] |= (1L << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] = -1;
        }
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
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    /**
     * Returns the cardinality of this bit set (number of bits set).
     * 
     * <P>Note: Unlike {@code java.util.BitSet} this method does not 
     *          returns an approximation of the number of bits of space 
     *          actually in use. This method is compliant with 
     *          java.util.Collection meaning for size().</p>
     *
     * @return the cardinality of this bit set.
     */
    @Override
    public int size() {
        return cardinality();
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void xor(FastBitSet that) {
        if (that.bits.length > this.bits.length) {
            setLength(that.bits.length);
        }
        for (int i = that.bits.length; --i >= 0;) {
            bits[i] ^= that.bits[i];
        }
    }

    // Optimization.
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FastBitSet))
            return false;
        FastBitSet that = (FastBitSet) obj;
        int n = MathLib.min(this.bits.length, that.bits.length);
        for (int i = 0; i < n; ++i) {
            if (bits[i] != that.bits[i])
                return false;
        }
        for (int i = n; i < this.bits.length; i++) {
            if (this.bits[i] != 0)
                return false;
        }
        for (int i = n; i < that.bits.length; i++) {
            if (that.bits[i] != 0)
                return false;
        }
        return true;
    }

    // Optimization.
    @Override
    public int hashCode() {
        int h = 0;
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            h += i;
        }
        return h;
    }

    /**
     * Sets the new length of the table (all new bits are <code>false</code>).
     *
     * @param newLength the new length of the table.
     */
    private void setLength(final int newLength) {
        if (bits.length >= newLength) return; // No need.
        long[] tmp = new long[newLength];
        System.arraycopy(bits, 0, tmp, 0, bits.length);
        bits = tmp;
    }
    
    //
    // FastCollection Methods
    //

    @Override
    public FastCollection<Index> unmodifiable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FastCollection<Index> shared() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<Index, R> functor) {
        FastTable<R> results = new FastTable();
        for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            R result = functor.evaluate(Index.valueOf(i));
            if (result != null) results.add(result);
        }
        return results;
    }

    @Override
    public void doWhile(Predicate<Index> predicate) {
        for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            if (!predicate.evaluate(Index.valueOf(i))) return;
        }
    }

    @Override
    public boolean removeAll(Predicate<Index> predicate) {
        boolean modified = false;
        for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            if (predicate.evaluate(Index.valueOf(i))) {
                clear(i);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public Iterator<Index> iterator() {
        throw new UnsupportedOperationException("Closures should be used to iterate.");
    }

    @Override
    public FastCollection<Index> usingComparator(FastComparator<Index> comparator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
