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
import javolution.lang.Predicate;
import javolution.util.AbstractBitSet;
import javolution.util.FastCollection;
import javolution.util.Index;

/**
 * An unmodifiable bit set.
 */
public class UnmodifiableBitSetImpl extends AbstractBitSet {

    private final AbstractBitSet that;

    public UnmodifiableBitSetImpl(AbstractBitSet that) {
        this.that = that;
    }

    //
    // Read Accessors.
    //

    @Override
    public long[] toBits() {
        return that.toBits();
    }

    @Override
    public int cardinality() {
        return that.cardinality();
    }

    @Override
    public boolean get(int bitIndex) {
        return that.get(bitIndex);
    }

    @Override
    public AbstractBitSet get(int fromIndex, int toIndex) {
        return that.get(fromIndex, toIndex);
    }

    @Override
    public boolean intersects(AbstractBitSet abs) {
        return that.intersects(abs);
    }

    @Override
    public int length() {
        return that.length();
    }

    //
    // Iterations
    //

    @Override
    public int nextClearBit(int fromIndex) {
        return that.nextClearBit(fromIndex);
    }

    @Override
    public int nextSetBit(int fromIndex) {
        return that.nextSetBit(fromIndex);
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<Index, R> functor) {
        return that.forEach(functor);
    }

    @Override
    public void doWhile(Predicate<Index> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<Index> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    //
    // Clear/Set/Flip Operations
    //

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

    //
    // Logical Operations
    //

    @Override
    public void and(AbstractBitSet abs) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void andNot(AbstractBitSet abs) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void or(AbstractBitSet abs) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void xor(AbstractBitSet abs) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    //
    // Miscalleneous.
    // 

    public boolean equals(AbstractBitSet abs) {
        return that.equals(abs);
    }

    @Override
    public UnmodifiableBitSetImpl copy() {
        return new UnmodifiableBitSetImpl(that.copy());
    }

}
