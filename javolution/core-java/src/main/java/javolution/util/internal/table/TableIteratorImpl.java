/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import javolution.util.service.TableService;

/**
 * A generic iterator over a table.
 */
public final class TableIteratorImpl<E> implements ListIterator<E> {

    private int currentIndex = -1;
    private int end;
    private int nextIndex;
    private final TableService<E> table;

    public TableIteratorImpl(TableService<E> table, int index) {
        this.table = table;
        this.nextIndex = index;
        this.end = table.size();
    }

    @Override
    public void add(E e) {
        table.add(nextIndex++, e);
        end++;
        currentIndex = -1;
    }

    @Override
    public boolean hasNext() {
        return (nextIndex < end);
    }

    @Override
    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    @Override
    public E next() {
        if (nextIndex >= end) throw new NoSuchElementException();
        currentIndex = nextIndex++;
        return table.get(currentIndex);
    }

    @Override
    public int nextIndex() {
        return nextIndex;
    }

    @Override
    public E previous() {
        if (nextIndex <= 0) throw new NoSuchElementException();
        currentIndex = --nextIndex;
        return table.get(currentIndex);
    }

    @Override
    public int previousIndex() {
        return nextIndex - 1;
    }

    @Override
    public void remove() {
        if (currentIndex < 0) throw new IllegalStateException();
        table.remove(currentIndex);
        end--;
        if (currentIndex < nextIndex) {
            nextIndex--;
        }
        currentIndex = -1;
    }

    @Override
    public void set(E e) {
        if (currentIndex >= 0) {
            table.set(currentIndex, e);
        } else {
            throw new IllegalStateException();
        }
    }

}
