/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import javolution.lang.Functor;
import javolution.lang.MathLib;
import javolution.lang.Predicate;
import javolution.util.AbstractBitSet;
import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.Index;

/**
 * A table of indices implemented using packed bits (long[]).
 */
public class FastBitSetImpl extends AbstractBitSet {

    /** Holds the bits (64 bits per long). */
    private long[] bits;

    /** Creates a bit set of specified capacity in bits (all bits are cleared). */
    public FastBitSetImpl(int bitSize) {
        bits = new long[((bitSize - 1) >> 6) + 1];
    }

    //
    // Read Accessors.
    //

    @Override
    public long[] toBits() {
        return bits;
    }

    @Override
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < bits.length; i++) {
            sum += MathLib.bitCount(bits[i]);
        }
        return sum;
    }

    @Override
    public boolean get(int bitIndex) {
        int i = bitIndex >> 6;
        return (i >= bits.length) ? false : (bits[i] & (1L << bitIndex)) != 0;
    }

    @Override
    public FastBitSetImpl get(int fromIndex, int toIndex) {
        FastBitSetImpl bitSet = new FastBitSetImpl(toIndex);
        int length = MathLib.min(bits.length, (toIndex >>> 6) + 1);
        bitSet.setLength(length);
        System.arraycopy(bits, 0, bitSet.bits, 0, length);
        bitSet.clear(0, fromIndex);
        bitSet.clear(toIndex, length << 6);
        return bitSet;
    }

    @Override
    public boolean intersects(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        int i = MathLib.min(this.bits.length, thatBits.length);
        while (--i >= 0) {
            if ((bits[i] & thatBits[i]) != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int length() {
        for (int i = bits.length; --i >= 0;) {
            long l = bits[i];
            if (l != 0) {
                return i << 6 + 64 - MathLib.numberOfTrailingZeros(l);
            }
        }
        return 0;
    }

    //
    // Iterations
    //

    @Override
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

    @Override
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

    @Override
    public <R> FastCollection<R> forEach(Functor<Index, R> functor) {
        FastTable<R> results = new FastTable<R>();
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            R result = functor.evaluate(Index.valueOf(i));
            if (result != null)
                results.add(result);
        }
        return results;
    }

    @Override
    public void doWhile(Predicate<Index> predicate) {
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            if (!predicate.evaluate(Index.valueOf(i)))
                return;
        }
    }

    @Override
    public boolean removeAll(Predicate<Index> predicate) {
        boolean modified = false;
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            if (predicate.evaluate(Index.valueOf(i))) {
                clear(i);
                modified = true;
            }
        }
        return modified;
    }

    //
    // Clear/Set/Flip Operations
    //

    @Override
    public void clear() {
        bits = new long[0];
    }

    @Override
    public void clear(int bitIndex) {
        int longIndex = bitIndex >> 6;
        if (longIndex >= bits.length)
            return;
        bits[longIndex] &= ~(1L << bitIndex);
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
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

    /** Sets the specified bit, returns <code>true</code> if previously set. */
    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        int i = bitIndex >> 6;
        if (i >= bits.length)
            setLength(i + 1);
        boolean previous = (bits[i] & (1L << bitIndex)) != 0;
        if (value) bits[i] |= 1L << bitIndex;
        else  bits[i] &= ~(1L << bitIndex);
        return previous;
    }

    @Override
    public void set(int bitIndex) {
        int i = bitIndex >> 6;
        if (i >= bits.length) {
            setLength(i + 1);
        }
        bits[i] |= 1L << bitIndex;
    }

    @Override
    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
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

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    @Override
    public void flip(int bitIndex) {
        int i = bitIndex >> 6;
        setLength(i + 1);
        bits[i] ^= 1L << bitIndex;
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
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

    //
    // Logical Operations
    //

    @Override
    public void and(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        final int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; ++i) {
            this.bits[i] &= thatBits[i];
        }
    }

    @Override
    public void andNot(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        int i = Math.min(this.bits.length, thatBits.length);
        while (--i >= 0) {
            this.bits[i] &= ~thatBits[i];
        }
    }

    @Override
    public void or(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        if (thatBits.length > this.bits.length) {
            setLength(thatBits.length);
        }
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] |= thatBits[i];
        }
    }

    @Override
    public void xor(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        if (thatBits.length > this.bits.length) {
            setLength(thatBits.length);
        }
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] ^= thatBits[i];
        }
    }

    //
    // Object/Copyable Method Override.
    // 

    @Override
    public boolean equals(AbstractBitSet that) {
        long[] thatBits = that.toBits();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; ++i) {
            if (bits[i] != thatBits[i])
                return false;
        }
        for (int i = n; i < this.bits.length; i++) {
            if (this.bits[i] != 0)
                return false;
        }
        for (int i = n; i < thatBits.length; i++) {
            if (thatBits[i] != 0)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
            h += i;
        }
        return h;
    }

    @Override
    public FastBitSetImpl copy() {
        return this.get(0, this.length());
    }

    /**
     * Sets the new length of the table (all new bits are <code>false</code>).
     *
     * @param newLength the new length of the table.
     */
    private void setLength(final int newLength) {
        if (bits.length >= newLength)
            return; // No need.
        long[] tmp = new long[newLength];
        System.arraycopy(bits, 0, tmp, 0, bits.length);
        bits = tmp;
    }

}
