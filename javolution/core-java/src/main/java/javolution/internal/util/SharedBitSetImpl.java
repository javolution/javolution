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
 * A shared bit set.
 */
public class SharedBitSetImpl extends AbstractBitSet {

    private final AbstractBitSet that;

    public SharedBitSetImpl(AbstractBitSet that) {
        this.that = that;
    }

    //
    // Read Accessors.
    //

    @Override
    public long[] toBits() {
        synchronized (that) {
            return that.toBits();
        }
    }

    @Override
    public int cardinality() {
        synchronized (that) {
            return that.cardinality();
        }
    }

    @Override
    public boolean get(int bitIndex) {
        synchronized (that) {
            return that.get(bitIndex);
        }
    }

    @Override
    public AbstractBitSet get(int fromIndex, int toIndex) {
        synchronized (that) {
            return that.get(fromIndex, toIndex);
        }
    }

    @Override
    public boolean intersects(AbstractBitSet abs) {
        synchronized (that) {
            return that.intersects(abs);
        }
    }

    @Override
    public int length() {
        synchronized (that) {
            return that.length();
        }
    }

    //
    // Iterations
    //

    @Override
    public int nextClearBit(int fromIndex) {
        synchronized (that) {
            return that.nextClearBit(fromIndex);
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        synchronized (that) {
            return that.nextSetBit(fromIndex);
        }
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<Index, R> functor) {
        synchronized (that) {
            return that.forEach(functor);
        }
    }

    @Override
    public void doWhile(Predicate<Index> predicate) {
        synchronized (that) {
            that.doWhile(predicate);
        }
    }

    @Override
    public boolean removeAll(Predicate<Index> predicate) {
        synchronized (that) {
            return that.removeAll(predicate);
        }
    }

    //
    // Clear/Set/Flip Operations
    //

    @Override
    public void clear() {
        synchronized (that) {
            that.clear();
        }
    }

    @Override
    public void clear(int bitIndex) {
        synchronized (that) {
            that.clear(bitIndex);
        }
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        synchronized (that) {
            that.clear(fromIndex, toIndex);
        }
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        synchronized (that) {
            return that.getAndSet(bitIndex, value);
        }
    }

    @Override
    public void set(int bitIndex) {
        synchronized (that) {
            that.set(bitIndex);
        }
    }

    @Override
    public void set(int bitIndex, boolean value) {
        synchronized (that) {
            that.set(bitIndex, value);
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        synchronized (that) {
            that.set(fromIndex, toIndex);
        }
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        synchronized (that) {
            that.set(fromIndex, toIndex, value);
        }
    }

    @Override
    public void flip(int bitIndex) {
        synchronized (that) {
            that.flip(bitIndex);
        }
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        synchronized (that) {
            that.flip(fromIndex, toIndex);
        }
    }

    //
    // Logical Operations
    //

    @Override
    public void and(AbstractBitSet abs) {
        synchronized (that) {
            that.and(abs);
        }
    }

    @Override
    public void andNot(AbstractBitSet abs) {
        synchronized (that) {
            that.andNot(abs);
        }
    }

    @Override
    public void or(AbstractBitSet abs) {
        synchronized (that) {
            that.or(abs);
        }
    }

    @Override
    public void xor(AbstractBitSet abs) {
        synchronized (that) {
            that.xor(abs);
        }
    }

    //
    // Miscalleneous.
    // 

    public boolean equals(AbstractBitSet abs) {
        synchronized (that) {
            return that.equals(abs);
        }
    }

    @Override
    public SharedBitSetImpl copy() {
        synchronized (that) {
            return new SharedBitSetImpl(that.copy());
        }
    }

}
