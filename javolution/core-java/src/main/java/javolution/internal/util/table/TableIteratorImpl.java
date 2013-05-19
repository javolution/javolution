/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import javolution.util.service.TableService;

/**
 * A generic iterator over a table.
 */
public final class TableIteratorImpl<E> implements ListIterator<E> {

    private final TableService<E> that;

    private int nextIndex;

    private int currentIndex = -1;

    private int end;

    public TableIteratorImpl(TableService<E> that, int index) {
        this.that = that;
        this.nextIndex = index;
        this.end = that.size();
    }

    public boolean hasNext() {
        return (nextIndex < end);
    }

    public E next() {
        if (nextIndex >= end)
            throw new NoSuchElementException();
        currentIndex = nextIndex++;
        return that.get(currentIndex);
    }

    public int nextIndex() {
        return nextIndex;
    }

    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    public E previous() {
        if (nextIndex <= 0)
            throw new NoSuchElementException();
        currentIndex = --nextIndex;
        return that.get(currentIndex);
    }

    public int previousIndex() {
        return nextIndex - 1;
    }

    public void add(E e) {
        that.add(nextIndex++, e);
        end++;
        currentIndex = -1;
    }

    public void set(E e) {
        if (currentIndex >= 0) {
            that.set(currentIndex, e);
        } else {
            throw new IllegalStateException();
        }
    }

    public void remove() {
        if (currentIndex < 0)
            throw new IllegalStateException();
        that.remove(currentIndex);
        end--;
        if (currentIndex < nextIndex) {
            nextIndex--;
        }
        currentIndex = -1;
    }

}
