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

import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.collection.UnmodifiableIteratorImpl;
import javolution.util.Index;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.BitSetService;

/**
 * An unmodifiable bit set.
 */
public class UnmodifiableBitSetImpl implements BitSetService, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final BitSetService target;

    public UnmodifiableBitSetImpl(BitSetService target) {
        this.target = target;
    }

    @Override
    public boolean add(Index element) {
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
    public void atomic(Runnable action) {
        target.atomic(action);
    }

    @Override
    public int cardinality() {
        return target.cardinality();
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
    public EqualityComparator<? super Index> comparator() {
        return target.comparator();
    }

    @Override
    public boolean contains(Index i) {
        return target.contains(i);
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
    public void forEach(Consumer<? super Index> consumer,
            IterationController controller) {
        target.forEach(consumer, controller);
    }

    @Override
    public boolean get(int bitIndex) {
        return target.get(bitIndex);
    }

    @Override
    public BitSetService get(int fromIndex, int toIndex) {
        return target.get(fromIndex, toIndex);
    }

    @Override
    public boolean getAndSet(int bitIndex, boolean value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean intersects(BitSetService that) {
        return target.intersects(that);
    }

    @Override
    public UnmodifiableIteratorImpl<Index> iterator() {
        return new UnmodifiableIteratorImpl<Index>(target.iterator());
    }

    @Override
    public int length() {
        return target.length();
    }

    @Override
    public int nextClearBit(int fromIndex) {
        return target.nextClearBit(fromIndex);
    }

    @Override
    public int nextSetBit(int fromIndex) {
        return target.nextSetBit(fromIndex);
    }

    @Override
    public void or(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int previousClearBit(int fromIndex) {
        return target.previousClearBit(fromIndex);
    }

    @Override
    public int previousSetBit(int fromIndex) {
        return target.previousSetBit(fromIndex);
    }

    @Override
    public boolean remove(Index e) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean removeIf(Predicate<? super Index> filter,
            IterationController controller) {
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
    public int size() {
        return target.size();
    }

    @Override
    public long[] toLongArray() {
        return target.toLongArray();
    }

    @Override
    public UnmodifiableCollectionImpl<Index>[] trySplit(int n) {
        return UnmodifiableCollectionImpl.splitOf(this, n);
    }

    @Override
    public void xor(BitSetService that) {
        throw new UnsupportedOperationException("Unmodifiable");
    }
}
