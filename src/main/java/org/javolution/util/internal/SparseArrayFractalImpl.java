/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import java.util.NoSuchElementException;

import org.javolution.util.SparseArray;

/**
 * Fractal sparse array implementation (for high capacity sparse arrays).
 */
public final class SparseArrayFractalImpl<E> extends SparseArray<E> {
    private static final long serialVersionUID = 0x700L;
    private final int split;
    private SparseArray<SparseArray<E>> sparses = SparseArray.empty();
    private int size;

    /** Holds the minimum size supported (below that size non-fractal instances are returned). */
    public static final int MIN_SIZE = SparseArrayImpl.MAX_SIZE / 2;
    
    /** 
     * Creates a new fractal array having the specified index split.
     * 
     * @param split the number of bits for inner arrays indices.
     */
    public SparseArrayFractalImpl(int split) {
        this.split = split; 
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        SparseArray<E> sparse = sparses.get(index >>> split);
        return (sparse != null) ? sparse.get(index << (32 - split) >>> (32 - split)) : null;
    }

    @Override
    public SparseArray<E> set(int index, E value) {
        SparseArray<E> sparse = sparses.get(index >>> split);
        if (sparse == null) {
            if (value == null) return this; // Nothing to clear. 
            sparses = sparses.set(index >>> split, sparse = SparseArray.empty());
        }
        int prevSize = sparse.size();
        SparseArray<E> newSparse = sparse.set(index << (32 - split) >>> (32 - split), value);
        int newSize = newSparse.size();
        size += newSize - prevSize;
        if (newSize == 0) newSparse = null;
        if (newSparse != sparse) sparses = sparses.set(index >>> split, newSparse);
        return (value == null) && (size < MIN_SIZE) ? downsize() : this;
    }

    private SparseArray<E> downsize() {
        SparseArray<E> newSparse = SparseArray.empty();
        for (Iterator<E> itr=iterator(0); itr.hasNext();) 
            newSparse.set(itr.nextIndex(), itr.next());
        return newSparse;
    }

    @Override
    public Iterator<E> iterator(int index) {
        return new IteratorImpl(index);
    }

    /** List Iterator Implementation. */
    private final class IteratorImpl extends Iterator<E> {
        private final Iterator<SparseArray<E>> highIterator;
        private int highIndex = 0;
        private Iterator<E> lowIterator;

        private IteratorImpl(int from) {
            highIterator = sparses.iterator(from >>> split);
            if (highIterator.hasNext()) {
                highIndex = highIterator.nextIndex();
                lowIterator = highIterator.next().iterator(from << (32 - split) >>> (32 - split));
            } else if (highIterator.hasPrevious()) {
                highIndex = highIterator.previousIndex();
                lowIterator = highIterator.previous().iterator(from << (32 - split) >>> (32 - split));
            } else {
                lowIterator = new SparseArrayImpl<E>().iterator(0); // Empty.
            }
        }

        @Override
        public boolean hasNext() {
            return lowIterator.hasNext() || highIterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return lowIterator.hasPrevious() || highIterator.hasPrevious();
        }

        @Override
        public E next() {
            if (lowIterator.hasNext()) return lowIterator.next();
            if (!highIterator.hasNext()) throw new NoSuchElementException();
            highIndex = highIterator.nextIndex();
            lowIterator = highIterator.next().iterator(0);
            return lowIterator.next();
        }

        @Override
        public int nextIndex() {
            if (lowIterator.hasNext()) return (highIndex << split) | lowIterator.nextIndex();
            if (!highIterator.hasNext()) return 0;
            int index = highIterator.nextIndex() + highIterator.next().iterator(0).nextIndex();
            highIterator.previous(); // Move back.
            return index;
        }

        @Override
        public E previous() {
            if (lowIterator.hasPrevious()) return lowIterator.previous();
            if (!highIterator.hasPrevious()) throw new NoSuchElementException();
            highIndex = highIterator.previousIndex();
            lowIterator = highIterator.previous().iterator(-1);
            return lowIterator.previous();
        }

        @Override
        public int previousIndex() {
            if (lowIterator.hasPrevious()) return (highIndex << split) | lowIterator.previousIndex();
            if (!highIterator.hasPrevious()) return -1;
            int index = highIterator.previousIndex() + highIterator.previous().iterator(-1).previousIndex();
            highIterator.next(); // Move back.
            return index;
        }
    }

}
