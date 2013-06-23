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

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.SharedIteratorImpl;
import javolution.util.Index;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.BitSetService;

/**
 * A shared view over a bitset allowing concurrent access and sequential updates.
 */
public class SharedBitSetImpl implements BitSetService, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Lock read;
    private final BitSetService target;
    private final Lock write;

    public SharedBitSetImpl(BitSetService target) {
        this.target = target;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    @Override
    public boolean add(Index i) {
        write.lock();
        try {
            return target.add(i);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void and(BitSetService that) {
        write.lock();
        try {
            target.and(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void andNot(BitSetService that) {
        write.lock();
        try {
            target.andNot(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void atomic(Runnable action) {
        write.lock();
        try {
            action.run();
        } finally {
            write.unlock();
        }
    }

    @Override
    public int cardinality() {
        read.lock();
        try {
            return target.cardinality();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void clear() {
        write.lock();
        try {
            target.clear();
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear(int bitIndex) {
        write.lock();
        try {
            target.clear(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        write.lock();
        try {
            target.clear(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public EqualityComparator<? super Index> comparator() {
        return target.comparator();
    }

    @Override
    public boolean contains(Index i) {
        read.lock();
        try {
            return target.contains(i);
        } finally {
            read.unlock();
        }
    }

    @Override
    public void flip(int bitIndex) {
        write.lock();
        try {
            target.flip(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        write.lock();
        try {
            target.flip(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super Index> consumer,
            IterationController controller) {
        read.lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean get(int bitIndex) {
        read.lock();
        try {
            return target.get(bitIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        read.lock();
        try {
            return target.get(fromIndex, toIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        write.lock();
        try {
            return target.getAndSet(bitIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean intersects(BitSetService that) {
        read.lock();
        try {
            return target.intersects(that);
        } finally {
            read.unlock();
        }
    }

    @Override
    public SharedIteratorImpl<Index> iterator() {
        return new SharedIteratorImpl<Index>(target.iterator(), read, write);
    }

    @Override
    public int length() {
        read.lock();
        try {
            return target.length();
        } finally {
            read.unlock();
        }
    }

    @Override
    public int nextClearBit(int fromIndex) {
        read.lock();
        try {
            return target.nextClearBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        read.lock();
        try {
            return target.nextSetBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public void or(BitSetService that) {
        write.lock();
        try {
            target.or(that);
        } finally {
            write.unlock();
        }
    }

    @Override
    public int previousClearBit(int fromIndex) {
        read.lock();
        try {
            return target.previousClearBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public int previousSetBit(int fromIndex) {
        read.lock();
        try {
            return target.previousSetBit(fromIndex);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean remove(Index i) {
        write.lock();
        try {
            return target.remove(i);
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Index> filter,
            IterationController controller) {
        write.lock();
        try {
            return target.removeIf(filter, controller);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int bitIndex) {
        write.lock();
        try {
            target.set(bitIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int bitIndex, boolean value) {
        write.lock();
        try {
            target.set(bitIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        write.lock();
        try {
            target.set(fromIndex, toIndex);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        write.lock();
        try {
            target.set(fromIndex, toIndex, value);
        } finally {
            write.unlock();
        }
    }

    @Override
    public int size() {
        read.lock();
        try {
            return target.size();
        } finally {
            read.unlock();
        }
    }

    @Override
    public long[] toLongArray() {
        read.lock();
        try {
            return target.toLongArray();
        } finally {
            read.unlock();
        }
    }

    @Override
    public SharedCollectionImpl<Index>[] trySplit(int n) {
        return SharedCollectionImpl.splitOf(target, n, read, write);
    }

    @Override
    public void xor(BitSetService that) {
        write.lock();
        try {
            target.xor(that);
        } finally {
            write.unlock();
        }
    }
}
