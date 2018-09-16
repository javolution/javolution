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
import java.util.Comparator;

import org.javolution.annotations.Nullable;
import org.javolution.util.AbstractCollection;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastIterator;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * An atomic view over a set (copy-on-write).
 */
public final class AtomicSetImpl<E>  // implements AbstractSetMethods<E> {
        extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;
    private volatile AbstractSet<E> innerConst; // The copy used by readers.

    public AtomicSetImpl(AbstractSet<E> inner) {
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
    public synchronized boolean add(E element, boolean allowDuplicate) {
        boolean changed = inner.add(element, allowDuplicate);
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
    public synchronized boolean addAll(@SuppressWarnings("unchecked") E... elements) {
        boolean changed = inner.addAll(elements);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public E findAny() {
        return innerConst.findAny();
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate) {
        return innerConst.anyMatch(predicate);
    }

    @Override
    public synchronized void clear() {
        inner.clear();
        innerConst = inner.clone();
    }

    @Override
    public AtomicSetImpl<E> clone() {
        return new AtomicSetImpl<E>(innerConst.clone());
    }

    @Override
    public AbstractCollection<E> collect() {
        return innerConst.collect();
    }

    @Override
    public Comparator<? super E> comparator() {
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
    public FastIterator<E> descendingIterator() {
        return innerConst.descendingIterator();
    }

    @Override
    public FastIterator<E> descendingIterator(@Nullable E from) {
        return innerConst.descendingIterator(from);
    }

    @Override
    public Equality<? super E> equality() {
        return innerConst.equality();
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
    public void forEach(Consumer<? super E> consumer) {
        innerConst.forEach(consumer);
    }

    @Override
    public E getAny(E element) {
        return innerConst.getAny(element);
    }

    @Override
    public int hashCode() {
        return innerConst.hashCode();
    }

    @Override
    public AbstractSet<E> headSet(E arg0) {
        return innerConst.headSet(arg0).unmodifiable();
    }

    @Override
    public boolean isEmpty() {
        return innerConst.isEmpty();
    }

    @Override
    public FastIterator<E> iterator() {
        return innerConst.iterator();
    }

    @Override
    public FastIterator<E> iterator(@Nullable E from) {
        return innerConst.iterator(from);
    }

    @Override
    public E last() {
        return innerConst.last();
    }

    @Override
    public Order<? super E> order() {
        return innerConst.order();
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
    public synchronized E removeAny(E element) {
        E removed = inner.removeAny(element);
        if (removed != null)
            innerConst = inner.clone();
        return removed;
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
    public AbstractSet<E> subSet(E element) {
        return innerConst.subSet(element).unmodifiable();
    }

    @Override
    public AbstractSet<E> subSet(E arg0, E arg1) {
        return innerConst.subSet(arg0, arg1).unmodifiable();
    }

    @Override
    public AbstractSet<E> tailSet(E arg0) {
        return innerConst.tailSet(arg0).unmodifiable();
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
    public AbstractSet<E>[] trySplit(int n) {
        return innerConst.trySplit(n);
    }
 
}
