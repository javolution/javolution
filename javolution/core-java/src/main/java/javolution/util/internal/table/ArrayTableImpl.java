/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.function.Equality;

/**
 * A fixed-size table backed by an array.
 */
public class ArrayTableImpl<E> extends TableView<E> {

    /** Internal iterator faster than generic TableIteratorImpl. */
    private class IteratorImpl implements Iterator<E> {
        private int nextIndex;

        @Override
        public boolean hasNext() {
            return nextIndex < elements.length;
        }

        @Override
        public E next() {
            if (nextIndex >= elements.length) throw new NoSuchElementException();
            return elements[nextIndex++];
        }

        @Override
        public void remove() {
        	throw new UnsupportedOperationException("Fixed-size table");
        }
    }

    private static final long serialVersionUID = 0x610L; // Version.
    private final Equality<? super E> comparator;
    private final E[] elements;

    public ArrayTableImpl(Equality<? super E> comparator, E[] elements) {
        super(null); // Root class.
        this.elements = elements;
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
    	throw new UnsupportedOperationException("Fixed-size table");
    }

    @Override
    public void add(int index, E element) {
    	throw new UnsupportedOperationException("Fixed-size table");
    }

    @Override
    public void clear() {
    	throw new UnsupportedOperationException("Fixed-size table");
    }

    @Override
    public ArrayTableImpl<E> clone() { // Make a copy.
        return new ArrayTableImpl<E>(comparator, elements.clone());
    }

    @Override
    public Equality<? super E> comparator() {
        return comparator;
    }

    @Override
    public E get(int index) {
        return elements[index];
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
    }

    @Override
    public E remove(int index) {
    	throw new UnsupportedOperationException("Fixed-size table");
    }

    @Override
    public E set(int index, E element) {
        E previous = elements[index];
        elements[index] = element;
        return previous;
    }

    @Override
    public int size() {
        return elements.length;
    }
}
