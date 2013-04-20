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

import javolution.util.service.BitSetService;

/**
 * An unmodifiable bit set.
 */
public class UnmodifiableBitSet implements BitSetService, Serializable {

    private final BitSetService impl;

    public UnmodifiableBitSet(BitSetService impl) {
        this.impl = impl;
    }

    @Override
    public int cardinality() {
        return impl.cardinality();
    }

    @Override
    public boolean get(int bitIndex) {
        return impl.get(bitIndex);
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        return impl.get(fromIndex, toIndex);
    }

    @Override
    public boolean intersects(BitSetService that) {
        return impl.intersects(that);
    }

    @Override
    public int length() {
        return impl.length();
    }

    @Override
    public int nextClearBit(int fromIndex) {
        return impl.nextClearBit(fromIndex);
    }

    @Override
    public int nextSetBit(int fromIndex) {
        return impl.nextSetBit(fromIndex);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear(int bitIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void set(int bitIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void set(int bitIndex, boolean value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void flip(int bitIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void and(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void andNot(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void or(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void xor(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public long[] toLongArray() {       
        return impl.toLongArray();
    }

    private static final long serialVersionUID = -5430958136508543474L;
}
