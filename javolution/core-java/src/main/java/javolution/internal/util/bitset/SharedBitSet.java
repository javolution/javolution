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
 * A shared bit set.
 */
public class SharedBitSet implements BitSetService, Serializable {

    private final BitSetService impl;

    public SharedBitSet(BitSetService impl) {
        this.impl = impl;
    }

    @Override
    public int cardinality() {
        synchronized (impl) {
            return impl.cardinality();
        }
    }

    @Override
    public boolean get(int bitIndex) {
        synchronized (impl) {
            return impl.get(bitIndex);
        }
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        synchronized (impl) {
            return impl.get(fromIndex, toIndex);
        }
    }

    @Override
    public boolean intersects(BitSetService that) {
        synchronized (impl) {
            return impl.intersects(that);
        }
    }

    @Override
    public int length() {
        synchronized (impl) {
            return impl.length();
        }
    }

    @Override
    public int nextClearBit(int fromIndex) {
        synchronized (impl) {
            return impl.nextClearBit(fromIndex);
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        synchronized (impl) {
            return impl.nextSetBit(fromIndex);
        }
    }

    @Override
    public void clear() {
        synchronized (impl) {
            impl.clear();
        }
    }

    @Override
    public void clear(int bitIndex) {
        synchronized (impl) {
            impl.clear(bitIndex);
        }
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        synchronized (impl) {
            impl.clear(fromIndex, toIndex);
        }
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        synchronized (impl) {
            return impl.getAndSet(bitIndex, value);
        }
    }

    @Override
    public void set(int bitIndex) {
        synchronized (impl) {
            impl.set(bitIndex);
        }
    }

    @Override
    public void set(int bitIndex, boolean value) {
        synchronized (impl) {
            impl.set(bitIndex, value);
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        synchronized (impl) {
            impl.set(fromIndex, toIndex);
        }
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        synchronized (impl) {
            impl.set(fromIndex, toIndex, value);
        }
    }

    @Override
    public void flip(int bitIndex) {
        synchronized (impl) {
            impl.flip(bitIndex);
        }
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        synchronized (impl) {
            impl.flip(fromIndex, toIndex);
        }
    }

    @Override
    public void and(BitSetService that) {
        synchronized (impl) {
            impl.and(that);
        }
    }

    @Override
    public void andNot(BitSetService that) {
        synchronized (impl) {
            impl.andNot(that);
        }
    }

    @Override
    public void or(BitSetService that) {
        synchronized (impl) {
            impl.or(that);
        }
    }

    @Override
    public void xor(BitSetService that) {
        synchronized (impl) {
            impl.xor(that);
        }
    }

    @Override
    public long[] toLongArray() {
        synchronized (impl) {
            return impl.toLongArray();
        }
    }

    private static final long serialVersionUID = -8040461335204870677L;
}
