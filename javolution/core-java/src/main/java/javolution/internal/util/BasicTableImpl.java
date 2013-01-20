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
 * A basic table implementation with fast insertion/delete capabilities. 
 */
public final class BasicTableImpl<E> extends AbstractTable<E> {

    private static final int BOUNDED_CAPACITY_MIN = 16;
    private static final int BOUNDED_CAPACITY_BITS = 10;
    private static final int BOUNDED_CAPACITY = 1 << BOUNDED_CAPACITY_BITS;
    private static final int BOUNDED_CAPACITY_MASK = BOUNDED_CAPACITY - 1;
    private Bounded<E> bounded;
    private Unbounded<E> unbounded;
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
        if ((index < 0) && (index >= size)) throw indexError(index);
        return (index < BOUNDED_CAPACITY) ? bounded.get(index) : unbounded.get(index);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size)) throw indexError(index);
        return (index < BOUNDED_CAPACITY) ? bounded.set(index, element) : unbounded.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size)) throw indexError(index);
        if (size >= capacity) upsize();
        if (size < BOUNDED_CAPACITY) {
            bounded.add(index, element, size++);
        } else {
            unbounded.add(index, element, size++);
        }
    }

    @Override
    public E remove(int index) {
        if ((index < 0) && (index >= size)) throw indexError(index);
        if (size <= BOUNDED_CAPACITY) {
            return bounded.remove(index, size--);
        } else {
            if (size + BOUNDED_CAPACITY < capacity) unbounded.downsize();
            return unbounded.remove(index, size--);
        }
    }

    @Override
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }

    @Override
    public BasicTableImpl<E> copy() {
        BasicTableImpl<E> that = new BasicTableImpl<E>();
        for (int i = 0; i < size; i++) {
            E e = (i < BOUNDED_CAPACITY) ? bounded.get(i) : unbounded.get(i);
            that.add((e instanceof Copyable) ? ((Copyable<E>) e).copy() : e);
        }
        return that;
    }

    //
    // Optimizations.
    //
    @Override
    public boolean add(E element) {
        if (size >= capacity) upsize();
        if (size < BOUNDED_CAPACITY) {
            bounded.add(element, size++);
        } else {
            unbounded.add(element, size++);
        }
        return true;
    }

    @Override
    public void addLast(E element) {
        add(element);
    }

    //
    // Utilities.
    //
    private void upsize() {
        if (capacity < BOUNDED_CAPACITY) {
            if (bounded == null) {
                capacity = BOUNDED_CAPACITY_MIN;
                bounded = new Bounded(capacity);
            } else {
                capacity = bounded.upsize();
            }
        } else { // Unbounded.
            if (unbounded == null) {
                unbounded = new Unbounded(bounded);
            }
            capacity = unbounded.upsize();
        }
    }

    // Table whose capacity is bounded.
    private static final class Bounded<E> {

        private int offset; // Index of first element (modulo data.length - 1)
        private E[] data;
        private int mask;

        public Bounded(int capacity) { // Empty table. 
            data = (E[]) new Object[capacity];
            mask = data.length - 1;
        }

        public void add(E element, int size) {
            data[(size + offset) & mask] = element;
        }

        public E get(int index) {
            return data[(index + offset) & mask];
        }

        public E set(int index, E element) {
            int i = (index + offset) & mask;
            E previous = data[i];
            data[i] = element;
            return previous;
        }

        public void add(int index, E element, int size) {
            if (index * 2 >= size) {
                int first = (index + offset) & mask;
                shiftRight(first, size - index);
                data[first] = element;
            } else {
                int last = (index + --offset) & mask;
                shiftLeft(last, index);
                data[last] = element;
            }
        }

        public E remove(int index, int size) {
            int i = (index + offset) & mask;
            E removed = data[i];
            if (index * 2 >= size) {
                int last = (size - 1 + offset) & mask;
                shiftLeft(last, size - 1 - index);
            } else {
                shiftRight(offset++ & mask, index);
            }
            return removed;
        }

        private void shiftRight(int first, int length) {
            if (length == 0) return; // Nothing to shift.
            int w = first + length - data.length;
            if (w >= 0) { // Wrapping.
                System.arraycopy(data, 0, data, 1, w);
                data[0] = data[mask];
                length -= ++w;
            }
            System.arraycopy(data, first, data, first + 1, length);
        }

        private void shiftLeft(int last, int length) {
            if (length == 0) return; // Nothing to shift.
            int w = length - last - 1;
            if (w >= 0) { // Wrapping.
                System.arraycopy(data, data.length - w, data, data.length - w - 1, w);
                data[mask] = data[0];
                length -= ++w;
            }
            System.arraycopy(data, last - length + 1, data, last - length, length);
        }

        public int upsize() { // Returns new capacity.
            E[] tmp = (E[]) new Object[data.length * 2];
            int first = offset & mask;
            System.arraycopy(data, first, tmp, 0, data.length - first);
            System.arraycopy(data, 0, tmp, data.length - first, first);
            data = tmp;
            offset = 0;
            mask = data.length - 1;
            return data.length;
        }
    }

    // Table whose capacity is unbounded.
    private static final class Unbounded<E> {

        private Bounded<E>[] data;
        private int length;

        public Unbounded(Bounded bounded) { // Empty table. 
            data = (Bounded<E>[]) new Bounded[]{bounded};
            length = 1;
        }

        public E get(int index) {
            return data[index >> BOUNDED_CAPACITY_BITS].get(index);
        }

        public E set(int index, E element) {
            return data[index >> BOUNDED_CAPACITY_BITS].set(index, element);
        }

        // Called only if size >= BOUNDED_CAPACITY
        public void add(int index, E element, int size) {
            int i = index >> BOUNDED_CAPACITY_BITS;
            int s = size >> BOUNDED_CAPACITY_BITS;
            for (int j = s; j > i; j--) {
                data[j].offset--; // Full shift right.
                data[j].set(0, data[j - 1].get(BOUNDED_CAPACITY_MASK));
            }
            Bounded<E> bounded = data[i];
            int j = index & BOUNDED_CAPACITY_MASK;
            if (s == i) { // Insertion in last bounded table.
                bounded.add(j, element, size & BOUNDED_CAPACITY_MASK);
            } else { // Bounded table at full capacity.
                E first = bounded.get(0); // Keep first (overwritten by insertion at full capacity).
                bounded.add(j, element, BOUNDED_CAPACITY);
                if (j != 0) bounded.set(0, first);
            }
        }

        public E remove(int index, int size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean add(E element, int size) {
            data[size >> BOUNDED_CAPACITY_BITS].add(element, size & BOUNDED_CAPACITY_MASK);
            return true;
        }

        public int upsize() { // Returns new capacity.
            if (length >= data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }
            data[length++] = new Bounded<E>(BOUNDED_CAPACITY);
            return length << BOUNDED_CAPACITY_BITS;
        }

        public int downsize() { // Returns new capacity.
            data[--length] = null;
            return length << BOUNDED_CAPACITY_BITS;
        }
    }

}
