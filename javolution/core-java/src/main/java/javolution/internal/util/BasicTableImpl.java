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

    private static final int BOUNDED_CAPACITY = 256;
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
        if (index < BOUNDED_CAPACITY) {
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
                capacity = 16;
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
            if (index >= (size >> 1)) {
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
            if (index >= (size >> 1)) {
                int last = (size - 1 + offset) & mask;
                shiftLeft(last , size - 1 - index);
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
            data = Arrays.copyOf(data, data.length * 2);
            mask = data.length - 1;
            return data.length;
        }
    }

    // Table whose capacity is unbounded.
    private static final class Unbounded<E> {

        public Unbounded(Bounded bounded) { // Empty table. 
        }

        public E get(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int upsize() { // Returns new capacity.
            return 0;
        }

        public int downsize() { // Returns new capacity.
            return 0;
        }

        public E set(int index, E element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void add(int index, E element, int size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public E remove(int index, int size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean add(E element, int size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
