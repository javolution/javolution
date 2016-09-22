/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import org.javolution.util.FastTable;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.ParallelCollectionImpl;

/**
 * A parallel view over a table.
 */
public final class ParallelTableImpl<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastTable<E> inner;

    public ParallelTableImpl(FastTable<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element);
    }

    @Override
    public void add(int index, E element) {
        inner.add(index, element);
    }

    @Override
    public void clear() { // Parallel.
        removeIf(Predicate.TRUE);
    }

    @Override
    public FastTable<E> clone() {
        return new ParallelTableImpl<E>(inner.clone());
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        ParallelCollectionImpl.forEach(inner, consumer);
    }

    @Override
    public E get(int index) {
        return inner.get(index);
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        return ParallelCollectionImpl.reduce(inner, operator);
    }

    @Override
    public E remove(int index) {
        return inner.remove(index);
    }

    @Override
    public boolean removeIf(Predicate<? super E> matching) {
        return ParallelCollectionImpl.removeIf(inner, matching);
    }

    @Override
    public FastTable<E> sequential() {
        return inner.sequential();
    }

    @Override
    public E set(int index, E element) {
        return inner.set(index, element);
    }

    @Override
    public int size() { // Not parallel.
        return inner.size();
    }

    @Override
    public boolean until(Predicate<? super E> matching) {
        return ParallelCollectionImpl.until(inner, matching);
    }

}
