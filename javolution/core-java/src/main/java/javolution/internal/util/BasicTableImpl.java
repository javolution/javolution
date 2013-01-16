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
import javolution.lang.MathLib;
import javolution.lang.Predicate;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;

/**
 * A basic table implementation.
 */
public final class BasicTableImpl<E> extends AbstractTable<E> {

    private static final Object[] NO_DATA = new Object[0];

    private int size;

    private int capacity;

    private int offset; // Index of [0]

    private E[] data = (E[]) NO_DATA; 
         // data.length == capacity if offset == 0
         // data.length == 2 * capacity if offset != 0 
         
    public BasicTableImpl() {
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        if (index >= size) throw new IndexOutOfBoundsException();
        return data[index + offset];
    }

    @Override
    public boolean add(E element) {
        if (capacity <= size) ensureCapacity(size + 1);
        data[offset + size++] = element;
        return true;
    }

    @Override
    public void addLast(E element) {
        add(element);
    }

    @Override
    public E set(int index, E element) {
        if (index >= size) throw new IndexOutOfBoundsException();
        int i = index + offset;
        E previous = data[i];
        data[i] = element;
        return previous;
    }

    @Override
    public void shiftLeftAt(int index, int shift) {
        int length = size - index - shift; // Nbr of elements moving.
        if ((length < 0) || (index < 0) || (shift < 0)) throw new IndexOutOfBoundsException();
        if (length != 0) {
            System.arraycopy(data, offset + index + shift, data, offset + index, length);
        }
        // Clears the references (for GC).
        int newSize = size - shift;
        while (size != newSize) {
            data[offset + --size] = null;
        }        
    }

    @Override
    public void shiftRightAt(int index, int shift) {
    }

    @Override
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }

    @Override
    public BasicTableImpl<E> copy() {
        final BasicTableImpl<E> tmp = new BasicTableImpl<E>();
        this.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                tmp.add((param instanceof Copyable) ? (E)((Copyable)param).copy() : param);
                return true;
            }

        });
        return tmp;
    }

    private void ensureCapacity(int min) {
        if (capacity >= min) return;
        capacity = MathLib.max(16, capacity);
        while (capacity < min) {
            capacity <<= 1;
        }
        data = Arrays.copyOf(data, capacity);
    }

}
