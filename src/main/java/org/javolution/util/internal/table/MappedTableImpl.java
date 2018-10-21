/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import org.javolution.util.FastListIterator;
import org.javolution.util.AbstractTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Predicate;

/**
 * A mapped view over a table.
 */
public final class MappedTableImpl<E, R> extends AbstractTable<R> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractTable<E> inner;
    private final Function<? super E, ? extends R> function;

    public MappedTableImpl(AbstractTable<E> inner, Function<? super E, ? extends R> function) {
        this.inner = inner;
        this.function = function;
    }

    @Override
    public void add(int index, R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public MappedTableImpl<E, R> clone() {
        return new MappedTableImpl<E, R>(inner.clone(), function);
    }

    @Override
    public Equality<? super R> equality() {
        return Equality.standard();
    }

    @Override
    public R get(int index) {
        return function.apply(inner.get(index));
    }

    @Override
    public FastListIterator<R> listIterator(int index) {
        return new IteratorImpl<E,R>(inner.listIterator(index), function);
    }

    @Override
    public R remove(int index) {
        return function.apply(inner.remove(index));
    }

    @Override
    public R set(int index, R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public int size() {
        return inner.size();
    }
    
    /** List Iterator over mapped collections. */
    private static final class IteratorImpl<E, R> implements FastListIterator<R> {
        private final FastListIterator<E> innerItr;
        private final Function<? super E, ? extends R> function;

        private IteratorImpl(FastListIterator<E> innerItr, Function<? super E, ? extends R> function) {
            this.innerItr = innerItr;
            this.function = function;
        }

        @Override
        public void add(R arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return innerItr.hasNext();
        }

        @Override
        public boolean hasNext(final Predicate<? super R> matching) {
            return innerItr.hasNext(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return matching.test(function.apply(param));
                }});
        }

        @Override
        public boolean hasPrevious() {
            return innerItr.hasPrevious();
        }

        @Override
        public boolean hasPrevious(final Predicate<? super R> matching) {
            return innerItr.hasPrevious(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    return matching.test(function.apply(param));
                }});
        }

        @Override
        public R next() {
            return function.apply(innerItr.next());
        }

        @Override
        public int nextIndex() {
            return innerItr.nextIndex();
        }

        @Override
        public R previous() {
            return function.apply(innerItr.previous());
        }

        @Override
        public int previousIndex() {
            return innerItr.previousIndex();
        }

 
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(R arg0) {
            throw new UnsupportedOperationException();
        }

    }

}
