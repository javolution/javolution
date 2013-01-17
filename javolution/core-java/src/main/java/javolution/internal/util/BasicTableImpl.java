/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Arrays;
import javolution.lang.Copyable;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;

/**
 * A basic table implementation. 
 * The actual implementation is dynamically
 * adjusted based on the table current size.
 */
public final class BasicTableImpl<E> extends AbstractTable<E> {

    private static final int BOUNDED_MAX_CAPACITY = 256; // Todo: Use configurable.

    private BoundedTable<E> boundedTable;

    private UnboundedTable<E> unboundedTable;

    private int size;

    private int capacity;

    public BasicTableImpl() {
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        return (unboundedTable == null) ? boundedTable.get(index) : unboundedTable.get(index);
    }

    @Override
    public E set(int index, E element) {
        rangeCheck(index);
        if (unboundedTable == null) {
            E previous = boundedTable.get(index);
            boundedTable.set(index, element);
            return previous;
        } else {
            E previous = unboundedTable.get(index);
            unboundedTable.set(index, element);
            return previous;
        }
    }

    @Override
    public void shiftLeftAt(int index, int shift) {
        if (unboundedTable == null) {
            boundedTable.copy(index + shift, index, size - index - shift);
            size -= shift;
        } else {
            unboundedTable.copy(index + shift, index, size - index - shift);
            size -= shift;
        }
    }

    @Override
    public void shiftRightAt(int index, int shift) {
        while (size + shift > capacity) {
            resize();
        }
        if (unboundedTable == null) {
            boundedTable.copy(index, index + shift, size - index);
            size += shift;
        } else {
            unboundedTable.copy(index, index + shift, size - index);
            size += shift;
        }
    }

    @Override
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }

    @Override
    public BasicTableImpl<E> copy() {
        BasicTableImpl<E> that = new BasicTableImpl<E>();
        for (int i = 0; i < size;) {
            E e = get(i++);
            that.add((e instanceof Copyable) ? ((Copyable<E>) e).copy() : e);
        }
        return that;
    }

    //
    // Optimizations.
    //
    @Override
    public boolean add(E element) {
        if (size >= capacity) resize();
        if (unboundedTable == null) {
            boundedTable.set(size++, element);
        } else {
            unboundedTable.set(size++, element);
        }
        return true;
    }

    //
    // Utilities.
    //
    private void resize() {
        boundedTable = (boundedTable == null) ? new BoundedTable(16) : boundedTable.resize();
        capacity = boundedTable.capacity();
    }

    private void rangeCheck(int index) {
        if ((index < 0) && (index >= size))
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    // Raw table (no check performed)
    private static abstract class RawTable<E> {

        public abstract int capacity();

        public abstract E get(int index);

        public abstract void set(int index, E element);

        public abstract void copy(int srcPos, int destPos, int length);

        public abstract RawTable<E> resize();

    }

    // Table whose capacity is bounded.
    private static final class BoundedTable<E> extends RawTable<E> {

        private int offset; // Index of first element (modulo data.length - 1)

        private E[] data;

        private int mask;

        public BoundedTable(int capacity) { // Empty table. 
            data = (E[]) new Object[capacity];
            mask = data.length - 1;
        }

        @Override
        public int capacity() {
            return data.length;
        }

        @Override
        public E get(int index) {
            return data[(index + offset) & mask];
        }

        @Override
        public void set(int index, E element) {
            data[(index + offset) & mask] = element;
        }

        @Override
        public void copy(int srcPos, int destPos, int length) {
            System.arraycopy(data, srcPos, data, destPos, length);
        }

        @Override
        public BoundedTable<E> resize() {
            data = Arrays.copyOf(data, data.length * 2);
            mask = data.length - 1;
            return this;
        }

    }

    // Table whose capacity is unbounded.
    private static final class UnboundedTable<E> extends RawTable<E> {

        @Override
        public int capacity() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public E get(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void set(int index, E element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copy(int srcPos, int destPos, int length) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RawTable<E> resize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
