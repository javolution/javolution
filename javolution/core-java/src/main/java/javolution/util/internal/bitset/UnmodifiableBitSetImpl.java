/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.bitset;

import javolution.util.Index;
import javolution.util.internal.set.UnmodifiableSetImpl;
import javolution.util.service.BitSetService;

/**
 * A table of indices which cannot be modified.
 */
public class UnmodifiableBitSetImpl extends UnmodifiableSetImpl<Index>
		implements BitSetService {

	private static final long serialVersionUID = 0x600L; // Version.

	public UnmodifiableBitSetImpl(BitSetService target) {
		super(target);
	}

	@Override
	public int cardinality() {
		return target().cardinality();
	}

	@Override
	public boolean get(int bitIndex) {
		return target().get(bitIndex);
	}

	@Override
	public BitSetService get(int fromIndex, int toIndex) {
		return target().get(fromIndex, toIndex);
	}

	@Override
	public boolean intersects(BitSetService that) {
		return target().intersects(that);
	}

	@Override
	public int length() {
		return target().length();
	}

	@Override
	public int nextClearBit(int fromIndex) {
		return target().nextClearBit(fromIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		return target().nextSetBit(fromIndex);
	}

	@Override
	public int previousClearBit(int fromIndex) {
		return target().previousClearBit(fromIndex);
	}

	@Override
	public int previousSetBit(int fromIndex) {
		return target().previousSetBit(fromIndex);
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
		return target().toLongArray().clone();
	}

	@Override
	protected BitSetService target() {
		return (BitSetService) super.target();
	}

}
