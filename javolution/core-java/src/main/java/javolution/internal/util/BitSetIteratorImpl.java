/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.AbstractBitSet;
import javolution.util.Index;

/**
 * An iterator over a bit set.
 */
public final class BitSetIteratorImpl implements Iterator<Index> {

    private final AbstractBitSet that;

    private int nextIndex;

    private int currentIndex = -1;

    public BitSetIteratorImpl(AbstractBitSet that, int index) {
        this.that = that;
        this.nextIndex = that.nextSetBit(index);
    }

    public boolean hasNext() {
        return (nextIndex >= 0);
    }

    public Index next() {
        if (nextIndex < 0)
            throw new NoSuchElementException();
        currentIndex = nextIndex;
        nextIndex = that.nextSetBit(nextIndex);
        return Index.valueOf(currentIndex);
    }

    public void remove() {
        if (currentIndex < 0) throw new IllegalStateException();
        that.clear(currentIndex);
        currentIndex = -1;
    }

}
