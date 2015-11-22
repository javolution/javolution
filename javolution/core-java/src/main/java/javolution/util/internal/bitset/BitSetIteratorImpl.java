/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.bitset;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.lang.Index;
import javolution.util.FastBitSet;
import javolution.util.FastIterator;

/**
 * An iterator over a bit set.
 */
public final class BitSetIteratorImpl implements FastIterator<Index> {

    private final FastBitSet that;
    private int nextIndex;
    private int currentIndex = -1;
    private final boolean unmodifiable;

    public BitSetIteratorImpl(FastBitSet that, int index, boolean unmodifiable) {
        this.that = that;
        this.nextIndex = that.nextSetBit(index);
        this.unmodifiable = unmodifiable;
    }

    public boolean hasNext() {
        return (nextIndex >= 0);
    }

    public Index next() {
        if (nextIndex < 0)
            throw new NoSuchElementException();
        currentIndex = nextIndex;
        nextIndex = that.nextSetBit(nextIndex + 1);
        return Index.of(currentIndex);
    }

    public void remove() {
    	if (unmodifiable) 
    		throw new UnsupportedOperationException("Read-Only BitSet.");
        if (currentIndex < 0)
            throw new IllegalStateException();
        that.clear(currentIndex);
        currentIndex = -1;
    }

	@Override
	public FastIterator<Index>[] split(FastIterator<Index>[] subIterators) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastIterator<Index> reversed() {
		// TODO Auto-generated method stub
		return null;
	}
}
