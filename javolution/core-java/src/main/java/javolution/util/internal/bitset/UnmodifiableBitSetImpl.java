/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.bitset;

import javolution.lang.Index;
import javolution.util.FastBitSet;
import javolution.util.function.Consumer;
import javolution.util.function.Order;
import javolution.util.function.Predicate;

/**
 * A table of indices which cannot be modified.
 */
public final class UnmodifiableBitSetImpl extends FastBitSet {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastBitSet inner;

	public UnmodifiableBitSetImpl(FastBitSet inner) {
		this.inner = inner;
	}

	@Override
	public int cardinality() {
		return inner.cardinality();
	}

	@Override
	public boolean get(int bitIndex) {
		return inner.get(bitIndex);
	}

	@Override
	public FastBitSet get(int fromIndex, int toIndex) {
		return inner.get(fromIndex, toIndex);
	}

	@Override
	public boolean intersects(FastBitSet that) {
		return inner.intersects(that);
	}

	@Override
	public int length() {
		return inner.length();
	}

	@Override
	public int nextClearBit(int fromIndex) {
		return inner.nextClearBit(fromIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		return inner.nextSetBit(fromIndex);
	}

	@Override
	public int previousClearBit(int fromIndex) {
		return inner.previousClearBit(fromIndex);
	}

	@Override
	public int previousSetBit(int fromIndex) {
		return inner.previousSetBit(fromIndex);
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
	public void and(FastBitSet that) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public void andNot(FastBitSet that) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public void or(FastBitSet that) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public void xor(FastBitSet that) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public long[] toLongArray() {
		return inner.toLongArray().clone();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public UnmodifiableBitSetImpl clone() {
		return new UnmodifiableBitSetImpl(inner.clone());
	}

	@Override
	public int size() {
		return inner.size();
	}

	@Override
	public boolean contains(Object obj) {
		return inner.contains(obj);
	}

	@Override
	public boolean remove(Object obj) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public Order<? super Index> comparator() {
		return inner.comparator();
	}

	@Override
	public void forEach(Consumer<? super Index> consumer) {
		inner.forEach(consumer);	
	}

	@Override
	public boolean removeIf(Predicate<? super Index> filter) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public boolean add(Index element) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
    
}
