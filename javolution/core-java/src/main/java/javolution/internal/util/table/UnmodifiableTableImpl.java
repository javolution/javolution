/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.io.Serializable;
import java.util.Iterator;

import javolution.lang.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> implements TableService<E>,
        Serializable {

    private final TableService<E> that;

    public UnmodifiableTableImpl(TableService<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    //
    // Non-abstract methods should forward to the actual table (unless impacted).
    //
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E getFirst() {
        return that.getFirst();
    }

    @Override
    public E getLast() {
        return that.getLast();
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void addFirst(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void addLast(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E peekFirst() {
        return (size() == 0) ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return (size() == 0) ? null : getLast();
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int indexOf(E element) {
        return that.indexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return that.lastIndexOf(element);
    }

    @Override
    public void sort() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public ComparatorService<E> comparator() {
        return that.comparator();
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return thatIterator.hasNext();
            }

            @Override
            public E next() {
                return thatIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Unmodifiable");
            }

        };
    }

    private static final long serialVersionUID = 7449328167407252680L;
}
