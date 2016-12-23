/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.javolution.util.FastSet;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.ParallelCollectionImpl;

/**
 * A parallel view over a set.
 */
public final class ParallelSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastSet<E> inner;

    public ParallelSetImpl(FastSet<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element);
    }

    @Override
    public void clear() { // Parallel.
        removeIf(Predicate.TRUE);
    }

    @Override
    public ParallelSetImpl<E> clone() {
        return new ParallelSetImpl<E>(inner.clone());
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @Override
    public boolean contains(Object obj) { // Not parallel.
        return inner.contains(obj);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return inner.descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return inner.descendingIterator(fromElement);
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        ParallelCollectionImpl.forEach(inner, consumer);
    }

    @Override
    public boolean isEmpty() { // Not parallel.
        return inner.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return inner.iterator();
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return inner.iterator(fromElement);
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        return ParallelCollectionImpl.reduce(inner, operator);
    }

    @Override
    public boolean remove(Object obj) { // Not parallel.
        return inner.remove(obj);
    }

    @Override
    public boolean removeIf(Predicate<? super E> matching) {
        return ParallelCollectionImpl.removeIf(inner, matching);
    }

    @Override
    public FastSet<E> sequential() {
        return inner.sequential();
    }

    @Override
    public int size() { // Parallel.
        final AtomicInteger count = new AtomicInteger(0);
        forEach(new Consumer<E>() {
            @Override
            public void accept(E param) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    @Override
    public boolean until(Predicate<? super E> matching) {
        return ParallelCollectionImpl.until(inner, matching);
    }

}
