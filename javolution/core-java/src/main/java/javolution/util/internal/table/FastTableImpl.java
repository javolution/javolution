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
 * The default {@link javolution.util.FastTable FastTable} implementation 
 * based on {@link FractalTableImpl fractal tables}. The memory footprint 
 * is minimal when the table is cleared.
 */
public class FastTableImpl<E> extends TableView<E> {

    /** Internal iterator faster than generic TableIteratorImpl. */
    private class IteratorImpl implements Iterator<E> {
        private int currentIndex = -1;
        private int nextIndex;

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (nextIndex >= size) throw new NoSuchElementException();
            currentIndex = nextIndex++;
            return (E) fractal.get(currentIndex);
        }

        @Override
        public void remove() {
            if (currentIndex < 0) throw new IllegalStateException();
            FastTableImpl.this.remove(currentIndex);
            nextIndex--;
            currentIndex = -1;
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.
    private transient int capacity; // Actual memory allocated is usually far less than capacity since inner fractal tables can be null.
    private final Equality<? super E> comparator;
    private transient FractalTableImpl fractal; // Null if empty (capacity 0)
    private transient int size;

    public FastTableImpl(Equality<? super E> comparator) {
        super(null); // Root class.
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) || (index > size)) indexError(index);
        checkUpsize();
        if (index >= (size >> 1)) {
            fractal.shiftRight(element, index, size - index);
        } else {
            fractal.shiftLeft(element, index - 1, index);
            fractal.offset--;
        }
        size++;
    }

    @Override
    public void addFirst(E element) {
        checkUpsize();
        fractal.offset--;
        fractal.set(0, element);
        size++;
    }

    @Override
    public void addLast(E element) {
        checkUpsize();
        fractal.set(size++, element);
    }

    @Override
    public void clear() {
        fractal = null;
        capacity = 0;
        size = 0;
    }

    @Override
    public FastTableImpl<E> clone() { // Make a copy.
        FastTableImpl<E> copy = new FastTableImpl<E>(comparator());
        copy.addAll(this);
        return copy;
    }

    @Override
    public Equality<? super E> comparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.get(index);
    }

    @Override
    public E getFirst() {
        if (size == 0) emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (size == 0) emptyError();
        return get(size - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        if ((index < 0) || (index >= size)) indexError(index);
        E removed = (E) fractal.get(index);
        if (index >= (size >> 1)) {
            fractal.shiftLeft(null, size - 1, size - index - 1);
        } else {
            fractal.shiftRight(null, 0, index);
            fractal.offset++;
        }
        size--;
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeFirst() {
        if (size == 0) emptyError();
        E first = (E) fractal.set(0, null);
        fractal.offset++;
        size--;
        return first;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeLast() {
        if (size == 0) emptyError();
        E last = (E) fractal.set(--size, null);
        return last;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.set(index, element);
    }

    @Override
    public int size() {
        return size;
    }

    private void checkUpsize() {
        if (size >= capacity) upsize();
    }

    /** For serialization support */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject(); // Deserialize comparator.
        int n = s.readInt();
        for (int i = 0; i < n; i++)
            addLast((E) s.readObject());
    }

    private void upsize() {
        fractal = (fractal == null) ? new FractalTableImpl() : fractal.upsize();
        capacity = fractal.capacity();
    }

    /** For serialization support */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject(); // Serialize comparator.
        s.writeInt(size);
        for (int i = 0; i < size; i++)
            s.writeObject(fractal.get(i));
    }

}
