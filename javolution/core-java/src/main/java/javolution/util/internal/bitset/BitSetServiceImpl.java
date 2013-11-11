/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.bitset;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import javolution.lang.MathLib;
import javolution.util.Index;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.set.SetView;
import javolution.util.service.BitSetService;

/**
 * A table of indices implemented using packed bits (long[]).
 */
public class BitSetServiceImpl extends SetView<Index> implements BitSetService, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private static final long[] ALL_CLEARED = new long[0];
    
    /** Holds the bits (64 bits per long). */
    private long[] bits;
    

    /** Creates a bit set (all bits cleared). */
    public BitSetServiceImpl() {
        super(null); // Root.
        bits = ALL_CLEARED;
    }

    /** Creates a bit set having the specified bits. */
    public BitSetServiceImpl(long[] bits) {
        super(null); // Root.
        this.bits = bits;
    }

    @Override
    public boolean add(Index index) {
        return !getAndSet(index.intValue(), true);
    }

    @Override
    public void and(BitSetService that) {
        long[] thatBits = that.toLongArray();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= thatBits[i];
        }
        for (int i = n; i < bits.length; i++) {
            this.bits[i] = 0L;
        }
    }

    @Override
    public void andNot(BitSetService that) {
        long[] thatBits = that.toLongArray();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= ~thatBits[i];
        }
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
    public void clear() {
        bits = ALL_CLEARED;
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

    @Override
    public Equality<? super Index> comparator() {
        return Equalities.IDENTITY;
    }

    @Override
    public boolean contains(Object index) {
        return get(((Index)index).intValue());
    }

    @Override
    public void flip(int bitIndex) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
        bits[i] ^= 1L << bitIndex;
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        int j = toIndex >>> 6;
        ensureCapacity(j + 1);
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

    @Override
    public boolean get(int bitIndex) {
        int i = bitIndex >> 6;
        return (i >= bits.length) ? false : (bits[i] & (1L << bitIndex)) != 0;
    }

    @Override
    public BitSetServiceImpl get(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        BitSetServiceImpl bitSet = new BitSetServiceImpl();
        int length = MathLib.min(bits.length, (toIndex >>> 6) + 1);
        bitSet.bits = new long[length];
        System.arraycopy(bits, 0, bitSet.bits, 0, length);
        bitSet.clear(0, fromIndex);
        bitSet.clear(toIndex, length << 6);
        return bitSet;
    }

    /** Sets the specified bit, returns <code>true</code> if previously set. */
    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
        boolean previous = (bits[i] & (1L << bitIndex)) != 0;
        if (value) { 
            bits[i] |= 1L << bitIndex;
        } else {
            bits[i] &= ~(1L << bitIndex);
        }
        return previous;
    }

    @Override
    public boolean intersects(BitSetService that) {
        long[] thatBits = that.toLongArray();
        int i = MathLib.min(this.bits.length, thatBits.length);
        while (--i >= 0) {
            if ((bits[i] & thatBits[i]) != 0) return true; 
        }
        return false;
    }

   @Override
    public Iterator<Index> iterator() {
        return new BitSetIteratorImpl(this, 0);
    }

    @Override
    public int length() {
        trim();
        if (bits.length == 0) return 0;
        return (bits.length << 6) - MathLib.numberOfLeadingZeros(bits[bits.length -1]);
    }

    @Override
    public int nextClearBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0) { return fromIndex; }
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
    public void or(BitSetService that) {
        long[] thatBits = (that instanceof BitSetServiceImpl) ? ((BitSetServiceImpl) that).bits
                : that.toLongArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] |= thatBits[i];
        }
    }

    @Override
    public int previousClearBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1L << 63;
            offset--;
        }
        return -1;
    }

    @Override
    public int previousSetBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1L << 63;
            offset--;
        }
        return -1;
    }

    @Override
    public boolean remove(Object index) {
        return getAndSet(((Index)index).intValue(), false);
    }

    @Override
    public void set(int bitIndex) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
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
        ensureCapacity(j + 1);
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
    public int size() {
        return cardinality();
    }

    @Override
    public long[] toLongArray() {
        trim();
        return bits;
    }
    
    @Override
    public void xor(BitSetService that) {
        long[] thatBits = (that instanceof BitSetServiceImpl) ? ((BitSetServiceImpl) that).bits
                : that.toLongArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] ^= thatBits[i];
        }
    }

    // Checks capacity.
    private void ensureCapacity(int capacity) {
        if (bits.length < capacity) {
            bits = Arrays.copyOf(bits, MathLib.max(bits.length * 2, capacity));
        }
    }

    // Removes trailing zeros.
    private void trim() {
        int n = bits.length;
        while ((--n >= 0) && (bits[n] == 0L)) {}
        if (++n != bits.length) { // Trim.
            bits = Arrays.copyOf(bits, n);
        }        
    }
    
}
