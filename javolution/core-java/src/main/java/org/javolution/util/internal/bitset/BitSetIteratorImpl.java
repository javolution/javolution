/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.bitset;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javolution.lang.Index;
import org.javolution.util.BitSet;

/**
 * An iterator over a bit set.
 */
public final class BitSetIteratorImpl implements Iterator<Index> {

    private final BitSet that;
    private int nextIndex;
    private int currentIndex = -1;
    private boolean reversed;

    public BitSetIteratorImpl(BitSet that, boolean reversed) {
        this.that = that;
        this.nextIndex = reversed ? that.previousSetBit(that.length()-1) : that.nextSetBit(0);
        this.reversed = reversed;
    }

    public boolean hasNext() {
        return (nextIndex >= 0);
    }

    public Index next() {
        if (nextIndex < 0)
            throw new NoSuchElementException();
        currentIndex = nextIndex;
        nextIndex = reversed ? that.previousSetBit(nextIndex - 1) : that.nextSetBit(nextIndex + 1);
        return Index.of(currentIndex);
    }

    public void remove() {
        if (currentIndex < 0)
            throw new IllegalStateException();
        that.clear(currentIndex);
        currentIndex = -1;
    }

}
