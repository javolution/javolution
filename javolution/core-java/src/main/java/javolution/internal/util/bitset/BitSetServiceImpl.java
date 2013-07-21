/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.bitset;

import java.io.Serializable;
import java.util.Iterator;

import javolution.lang.MathLib;
import javolution.util.Index;
import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.BitSetService;

/**
 * A table of indices implemented using packed bits (long[]).
 */
public class BitSetServiceImpl implements BitSetService, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /** Holds the bits (64 bits per long), trimmed. */
    private long[] bits;

    /** Creates a bit set  (all bits are cleared). */
    public BitSetServiceImpl() {
        bits = new long[0];
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
        trim();
    }

    @Override
    public void andNot(BitSetService that) {
        long[] thatBits = that.toLongArray();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= ~thatBits[i];
        }
        trim();
    }

    @Override
    public void atomic(Runnable update) {
        update.run();
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
        bits = new long[0];
    }

    @Override
    public void clear(int bitIndex) {
        int longIndex = bitIndex >> 6;
        if (longIndex >= bits.length)
            return;
        bits[longIndex] &= ~(1L << bitIndex);
        trim();
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
        trim();
    }

    @Override
    public EqualityComparator<? super Index> comparator() {
        return Comparators.IDENTITY;
    }

    @Override
    public boolean contains(Index index) {
        return get(index.intValue());
    }

    @Override
    public void flip(int bitIndex) {
        int i = bitIndex >> 6;
        setLength(i + 1);
        bits[i] ^= 1L << bitIndex;
        trim();
    }

    @Override
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
        trim();
    }

    @Override
    public void forEach(Consumer<? super Index> consumer,
            IterationController controller) {
        if (!controller.doReversed()) {
            for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
                consumer.accept(Index.valueOf(i));
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = length(); (i = previousSetBit(i - 1)) >= 0;) {
                consumer.accept(Index.valueOf(i));
                if (controller.isTerminated())
                    break;
            }
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
        if (i >= bits.length) {
            setLength(i + 1);
        }
        boolean previous = (bits[i] & (1L << bitIndex)) != 0;
        if (value) { 
            bits[i] |= 1L << bitIndex;
        } else {
            bits[i] &= ~(1L << bitIndex);
        }
        trim();
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
        if (thatBits.length > this.bits.length) {
            setLength(thatBits.length);
        }
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] |= thatBits[i];
        }
        trim();
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
    public boolean remove(Index index) {
        return getAndSet(index.intValue(), false);
    }

    @Override
    public boolean removeIf(Predicate<? super Index> filter,
            IterationController controller) {
        boolean modified = false;
        if (!controller.doReversed()) {
            for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
                if (filter.test(Index.valueOf(i))) {
                    clear(i);
                    modified = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = length(); (i = previousSetBit(i - 1)) >= 0;) {
                if (filter.test(Index.valueOf(i))) {
                    clear(i);
                    modified = true;
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return modified;
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
    public int size() {
        return cardinality();
    }

    @Override
    public long[] toLongArray() {
        return bits;
    }

    @Override
    public BitSetServiceImpl[] trySplit(int n) {
        return new BitSetServiceImpl[] { this }; // No split.
    }

    @Override
    public void xor(BitSetService that) {
        long[] thatBits = (that instanceof BitSetServiceImpl) ? ((BitSetServiceImpl) that).bits
                : that.toLongArray();
        if (thatBits.length > this.bits.length) {
            setLength(thatBits.length);
        }
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] ^= thatBits[i];
        }
        trim();
    }

    /**
     * Sets the new length of the bit set.
     */
    private void setLength(int newLength) {
        long[] tmp = new long[newLength];
        if (newLength >= bits.length) {
            System.arraycopy(bits, 0, tmp, 0, bits.length);
        } else { // Truncates.
            System.arraycopy(bits, 0, tmp, 0, newLength);
        }
        bits = tmp;
    }
    
    /**
     * Removes the tails words if cleared.
     */
    private void trim() {
        int n = bits.length;
        while ((--n >= 0) && (bits[n] == 0L)) {}
        if (++n < bits.length) setLength(n);   
    }
}
