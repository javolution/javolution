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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.util.service.BitSetService;

/**
 * A shared view over a bitset allowing concurrent access and sequential updates.
 */
public class SharedBitSet implements BitSetService, Serializable {

    private final BitSetService impl;
    private final Lock read;
    private final Lock write;

    public SharedBitSet(BitSetService impl) {
        this.impl = impl;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    @Override
    public int cardinality() {
        read.lock();
        try {
            return impl.cardinality();
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean get(int bitIndex) {
        read.lock();
        try {
            return impl.get(bitIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        read.lock();
        try {
            return impl.get(fromIndex, toIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean intersects(BitSetService that) {
        read.lock();
        try {
            return impl.intersects(that);
        } finally {
            read.unlock();
        }
    }

    @Override
    public int length() {
        read.lock();
        try {
            return impl.length();
        } finally {
            read.unlock();
        }
    }

    @Override
    public int nextClearBit(int fromIndex) {
        read.lock();
        try {
            return impl.nextClearBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        read.lock();
        try {
            return impl.nextSetBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public void clear() {
        write.lock();
        try {
            impl.clear();
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear(int bitIndex) {
        write.lock();
        try {
            impl.clear(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        write.lock();
        try {
            impl.clear(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        write.lock();
        try {
            return impl.getAndSet(bitIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int bitIndex) {
        write.lock();
        try {
            impl.set(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int bitIndex, boolean value) {
        write.lock();
        try {
            impl.set(bitIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        write.lock();
        try {
            impl.set(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        write.lock();
        try {
            impl.set(fromIndex, toIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void flip(int bitIndex) {
        write.lock();
        try {
            impl.flip(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        write.lock();
        try {
            impl.flip(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void and(BitSetService that) {
        write.lock();
        try {
            impl.and(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void andNot(BitSetService that) {
        write.lock();
        try {
            impl.andNot(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void or(BitSetService that) {
        write.lock();
        try {
            impl.or(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void xor(BitSetService that) {
        write.lock();
        try {
            impl.xor(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public long[] toLongArray() {
        read.lock();
        try {
            return impl.toLongArray();
        } finally {
            read.unlock();
        }
    }
    
    private static final long serialVersionUID = 7702817787801107786L;
}
