/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import java.util.List;
import java.util.NoSuchElementException;

import org.javolution.util.FastListIterator;
import org.javolution.util.AbstractTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A view over a portion of a table.
 */
public final class SubTableImpl<E> extends AbstractTable<E> implements List<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractTable<E> inner;
    private final int fromIndex; // Inclusive.
    private int toIndex; // Exclusive. 

    public SubTableImpl(AbstractTable<E> inner, int fromIndex, int toIndex) {
        this.inner = inner;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public boolean add(E element) {
        inner.add(toIndex++, element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) || (index > size())) throw new IndexOutOfBoundsException();
        inner.add(index + fromIndex, element);
        toIndex++;
    }

    @Override
    public void clear() {
        removeIf(Predicate.TRUE);
    }

    @Override
    public SubTableImpl<E> clone() {
        return new SubTableImpl<E>(inner.clone(), fromIndex, toIndex);
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    public E get(int index) {
        if ((index < 0) || (index >= size()))  throw new IndexOutOfBoundsException();
        return inner.get(index + fromIndex);
    }

    @Override
    public FastListIterator<E> listIterator(int index) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();
        return new IteratorImpl<E>(inner.listIterator(index + fromIndex), fromIndex, toIndex);
    }

    @Override
    public E remove(int index) {
        if ((index < 0) || (index >= size()))  throw new IndexOutOfBoundsException();
        toIndex--;
        return inner.remove(index + fromIndex);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) || (index >= size())) throw new IndexOutOfBoundsException();
        return inner.set(index + fromIndex, element);
    }

    @Override
    public int size() {
        return toIndex - fromIndex;
    }
    
    /** List Iterator over reversed tables. */
    private static final class IteratorImpl<E> implements FastListIterator<E> {
        private final FastListIterator<E> innerItr;
        private final int fromIndex; // Inclusive.
        private final int toIndex; // Exclusive. 
 
        private IteratorImpl(FastListIterator<E> innerItr, int fromIndex, int toIndex) {
            this.innerItr = innerItr;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public void add(E arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return innerItr.hasNext() && innerItr.nextIndex() < toIndex;
        }

        @Override
        public boolean hasNext(final Predicate<? super E> matching) {
            return innerItr.hasNext(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return innerItr.nextIndex() < toIndex && matching.test(param);
                }});
        }

        @Override
        public boolean hasPrevious() {
            return innerItr.hasPrevious() && innerItr.previousIndex() >= fromIndex;
        }

        @Override
        public boolean hasPrevious(final Predicate<? super E> matching) {
            return innerItr.hasPrevious(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return innerItr.previousIndex() >= fromIndex && matching.test(param);
                }});
        }

        @Override
        public E next() {
            if (innerItr.nextIndex() >= toIndex) throw new NoSuchElementException();
            return innerItr.next();
        }

        @Override
        public int nextIndex() {
            return innerItr.nextIndex() - fromIndex;
        }

        @Override
        public E previous() {
            if (innerItr.previousIndex() < fromIndex) throw new NoSuchElementException();
            return innerItr.previous();
        }

        @Override
        public int previousIndex() {
            return innerItr.previousIndex() - fromIndex;
        }

 
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E arg0) {
            throw new UnsupportedOperationException();
        }

    }
    
}
