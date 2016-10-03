/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Collection;
import java.util.Iterator;

import org.javolution.util.FastSet;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * An atomic view over a set (copy-on-write).
 */
public final class AtomicSetImpl<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastSet<E> inner;
    private volatile FastSet<E> innerConst; // The copy used by readers.

    public AtomicSetImpl(FastSet<E> inner) {
        this.inner = inner;
        this.innerConst = inner.clone();
    }

    @Override
    public synchronized boolean add(E element) {
        boolean changed = inner.add(element);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> that) {
        boolean changed = inner.addAll(that);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized boolean addAll(E... elements) {
        boolean changed = inner.addAll(elements);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public E any() {
        return innerConst.any();
    }

    @Override
    public E ceiling(E element) {
        return innerConst.ceiling(element);
    }

    @Override
    public synchronized void clear() {
        inner.clear();
        innerConst = inner.clone();
    }

    @Override
    public FastSet<E> clone() {
        return new AtomicSetImpl<E>(innerConst.clone());
    }

    @Override
    public Order<? super E> comparator() {
        return innerConst.comparator();
    }

    @Override
    public boolean contains(Object searched) {
        return innerConst.contains(searched);
    }

    @Override
    public boolean containsAll(Collection<?> that) {
        return innerConst.containsAll(that);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return innerConst.unmodifiable().descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return innerConst.unmodifiable().descendingIterator(fromElement);
    }

    @Override
    public boolean equals(Object obj) {
        return innerConst.equals(obj);
    }

    @Override
    public E first() {
        return innerConst.first();
    }

    @Override
    public E floor(E element) {
        return innerConst.floor(element);
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        innerConst.forEach(consumer);
    }

    @Override
    public int hashCode() {
        return innerConst.hashCode();
    }

    @Override
    public E higher(E element) {
        return innerConst.higher(element);
    }

    @Override
    public boolean isEmpty() {
        return innerConst.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return innerConst.unmodifiable().iterator();
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return innerConst.unmodifiable().iterator(fromElement);
    }

    @Override
    public E last() {
        return innerConst.last();
    }

    @Override
    public E lower(E element) {
        return innerConst.lower(element);
    }

    @Override
    public E max() {
        return innerConst.max();
    }

    @Override
    public E min() {
        return innerConst.min();
    }

    @Override
    public E pollFirst() {
        return innerConst.pollFirst();
    }

    @Override
    public E pollLast() {
        return innerConst.pollLast();
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        return innerConst.reduce(operator);
    }

    @Override
    public synchronized boolean remove(Object searched) {
        boolean changed = inner.remove(searched);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> that) {
        boolean changed = inner.removeAll(that);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super E> filter) {
        boolean changed = inner.removeIf(filter);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> that) {
        boolean changed = inner.retainAll(that);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public int size() {
        return innerConst.size();
    }

    @Override
    public Object[] toArray() {
        return innerConst.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return innerConst.toArray(array);
    }

    @Override
    public String toString() {
        return innerConst.toString();
    }

    @Override
    public FastSet<E>[] trySplit(int n) {
        return innerConst.trySplit(n);
    }

    @Override
    public boolean until(Predicate<? super E> matching) {
        return innerConst.until(matching);
    }

}
