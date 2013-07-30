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

import javolution.util.Index;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.internal.ReadWriteLockImpl;
import javolution.util.internal.SharedIteratorImpl;
import javolution.util.internal.collection.SharedCollectionImpl;
import javolution.util.service.BitSetService;

/**
 * A shared view over a bitset allowing concurrent access and sequential updates.
 */
public class SharedBitSetImpl implements BitSetService, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final ReadWriteLockImpl rwLock;
    private final BitSetService target;

    public SharedBitSetImpl(BitSetService target) {
        this.target = target;
        rwLock = new ReadWriteLockImpl();
    }

    @Override
    public boolean add(Index i) {
        rwLock.writeLock().lock();
        try {
            return target.add(i);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void and(BitSetService that) {
        rwLock.writeLock().lock();
        try {
            target.and(that);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void andNot(BitSetService that) {
        rwLock.writeLock().lock();
        try {
            target.andNot(that);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void atomic(Runnable update) {
        rwLock.writeLock().lock();
        try {
            target.atomic(update);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int cardinality() {
        rwLock.readLock().lock();
        try {
            return target.cardinality();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            target.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear(int bitIndex) {
        rwLock.writeLock().lock();
        try {
            target.clear(bitIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        rwLock.writeLock().lock();
        try {
            target.clear(fromIndex, toIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public EqualityComparator<? super Index> comparator() {
        return target.comparator();
    }

    @Override
    public boolean contains(Index i) {
        rwLock.readLock().lock();
        try {
            return target.contains(i);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void flip(int bitIndex) {
        rwLock.writeLock().lock();
        try {
            target.flip(bitIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        rwLock.writeLock().lock();
        try {
            target.flip(fromIndex, toIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super Index> consumer,
            IterationController controller) {
        rwLock.readLock().lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean get(int bitIndex) {
        rwLock.readLock().lock();
        try {
            return target.get(bitIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        rwLock.readLock().lock();
        try {
            return target.get(fromIndex, toIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        rwLock.writeLock().lock();
        try {
            return target.getAndSet(bitIndex, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean intersects(BitSetService that) {
        rwLock.readLock().lock();
        try {
            return target.intersects(that);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SharedIteratorImpl<Index> iterator() {
        return new SharedIteratorImpl<Index>(target.iterator(), rwLock);
    }

    @Override
    public int length() {
        rwLock.readLock().lock();
        try {
            return target.length();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int nextClearBit(int fromIndex) {
        rwLock.readLock().lock();
        try {
            return target.nextClearBit(fromIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int nextSetBit(int fromIndex) {
        rwLock.readLock().lock();
        try {
            return target.nextSetBit(fromIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void or(BitSetService that) {
        rwLock.writeLock().lock();
        try {
            target.or(that);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int previousClearBit(int fromIndex) {
        rwLock.readLock().lock();
        try {
            return target.previousClearBit(fromIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int previousSetBit(int fromIndex) {
        rwLock.readLock().lock();
        try {
            return target.previousSetBit(fromIndex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean remove(Index i) {
        rwLock.writeLock().lock();
        try {
            return target.remove(i);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Index> filter,
            IterationController controller) {
        rwLock.writeLock().lock();
        try {
            return target.removeIf(filter, controller);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void set(int bitIndex) {
        rwLock.writeLock().lock();
        try {
            target.set(bitIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void set(int bitIndex, boolean value) {
        rwLock.writeLock().lock();
        try {
            target.set(bitIndex, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        rwLock.writeLock().lock();
        try {
            target.set(fromIndex, toIndex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        rwLock.writeLock().lock();
        try {
            target.set(fromIndex, toIndex, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return target.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public long[] toLongArray() {
        rwLock.readLock().lock();
        try {
            return target.toLongArray();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SharedCollectionImpl<Index>[] trySplit(int n) {
        return SharedCollectionImpl.splitOf(target, n, rwLock);
    }

    @Override
    public void xor(BitSetService that) {
        rwLock.writeLock().lock();
        try {
            target.xor(that);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
