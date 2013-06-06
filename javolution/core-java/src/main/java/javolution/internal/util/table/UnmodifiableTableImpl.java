/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.util.Iterator;

import javolution.util.function.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> extends AbstractTableImpl<E>{

    private final TableService<E> that;

    public UnmodifiableTableImpl(TableService<E> that) {
        this.that = that;
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

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
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
    public boolean removeIf(Predicate<? super E> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean remove(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void sort() {
        throw new UnsupportedOperationException("Unmodifiable");
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

    @Override
    public void setComparator(ComparatorService<? super E> cmp) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public TableService<E>[] trySplit(int n) {
        return trySplitDefault(n);
    }
    
    //
    // If no impact, forwards to inner table.
    // 
  
    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(index);
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
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
    public E getFirst() {
        return that.getFirst();
    }

    @Override
    public E getLast() {
        return that.getLast();
    }

    @Override
    public E peekFirst() {
        return that.peekFirst();
    }

    @Override
    public E peekLast() {
        return that.peekLast();
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return that.doWhile(predicate);
    }
    
    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    private static final long serialVersionUID = -800081761156821069L;
}
