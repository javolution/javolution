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
import java.util.ListIterator;

import javolution.util.service.TableService;

/**
 * An unmodifiable view over a table.
 */
public class UnmodifiableTableImpl<E> extends TableView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableTableImpl(TableService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            ListIterator<E> it = target().listIterator(index);

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException("Read-Only Iterator");
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public E previous() {
                return it.previous();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read-Only Iterator");
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException("Read-Only Iterator");
            }
        };
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public int size() {
        return target().size();
    }
  
}
