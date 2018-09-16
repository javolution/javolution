/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import java.util.Collection;
import java.util.Comparator;

import org.javolution.annotations.Nullable;
import org.javolution.util.AbstractCollection;
import org.javolution.util.AbstractTable;
import org.javolution.util.FastIterator;
import org.javolution.util.FastListIterator;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * An atomic view over a table. All updates are synchronized, reads are performed on immutable copy.
 */
public final class AtomicTableImpl<E> // implements AbstractTableMethods<E> {
        extends AbstractTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractTable<E> inner;
    private volatile AbstractTable<E> innerConst; // The copy used by readers.

    public AtomicTableImpl(AbstractTable<E> inner) {
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
    public synchronized void add(int index, E element) {
        inner.add(index, element);
        innerConst = inner.clone();
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
    public synchronized boolean addAll(int index, Collection<? extends E> that) {
        boolean changed = inner.addAll(index, that);
        if (changed)
            innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized void addFirst(E element) {
        inner.addFirst(element);
        innerConst = inner.clone();
    }

    @Override
    public synchronized void addLast(E element) {
        inner.addLast(element);
        innerConst = inner.clone();
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
    public AtomicTableImpl<E> clone() {
        return new AtomicTableImpl<E>(innerConst.clone());
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
    public Equality<? super E> equality() {
        return innerConst.equality();
    }

    @Override
    public boolean equals(Object obj) {
        return innerConst.equals(obj);
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        innerConst.forEach(consumer);
    }

    @Override
    public E get(int index) {
        return innerConst.get(index);
    }

    @Override
    public E getFirst() {
        return innerConst.getFirst();
    }

    @Override
    public E getLast() {
        return innerConst.getLast();
    }

    @Override
    public int hashCode() {
        return innerConst.hashCode();
    }

    @Override
    public int indexOf(Object element) {
        return innerConst.indexOf(element);
    }

    @Override
    public int lastIndexOf(Object element) {
        return innerConst.lastIndexOf(element);
    }

    @Override
    public FastListIterator<E> listIterator(int index) {
        return innerConst.listIterator(index);
    }

     @Override
    public E peekFirst() {
        return innerConst.peekFirst();
    }

    @Override
    public E peekLast() {
        return innerConst.peekLast();
    }

    @Override
    public synchronized E pollFirst() {
        if (inner.isEmpty()) return null;
        E result = inner.removeFirst();
        innerConst = inner.clone();
        return result;
    }

    @Override
    public synchronized E pollLast() {
        if (inner.isEmpty()) return null;
        E result = inner.removeLast();
        innerConst = inner.clone();
        return result;
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        return innerConst.reduce(operator);
    }

    @Override
    public synchronized E remove(int index) {
        E result = inner.remove(index);
        innerConst = inner.clone();
        return result;
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
    public synchronized E removeFirst() {
        E result = inner.remove(0);
        innerConst = inner.clone();
        return result;
    }

    @Override
    public synchronized boolean removeFirstOccurrence(Object o) {
        int i = inner.indexOf(o);
        if (i < 0)
            return false;
        inner.remove(i);
        innerConst = inner.clone();
        return true;
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super E> filter) {
        if (inner.removeIf(filter)) {
            innerConst = inner.clone();
            return true;
        }
        return false;
    }

    @Override
    public synchronized E removeLast() {
        E result = inner.remove(size() - 1);
        innerConst = inner.clone();
        return result;
    }

    @Override
    public synchronized boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i < 0) return false;
        inner.remove(i);
        innerConst = inner.clone();
        return true;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> that) {
        boolean changed = inner.retainAll(that);
        if (changed) innerConst = inner.clone();
        return changed;
    }

    @Override
    public synchronized E set(int index, E element) {
        E result = inner.set(index, element);
        innerConst = inner.clone();
        return result;
    }

    @Override
    public int size() {
        return innerConst.size();
    }

    @Override
    public synchronized void sort(Comparator<? super E> cmp) {
        inner.sort(cmp);
        innerConst = inner.clone();
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
    public AbstractTable<E>[] trySplit(int n) {
        return innerConst.trySplit(n);
    }

    @Override
    public AbstractCollection<E> collect() {
        return innerConst.collect();
    }

    @Override
    public FastIterator<E> iterator() {
        return innerConst.iterator();
    }

    @Override
    public boolean isEmpty() {
        return innerConst.isEmpty();
    }

    @Override
    public FastListIterator<E> listIterator() {
        return innerConst.listIterator();
    }

    @Override
    public  AbstractTable<E> subList(int arg0, int arg1) {
        return innerConst.unmodifiable().subTable(arg0, arg1);
    }

    @Override
    public final boolean offer(@Nullable E e) {
        return offerLast(e);
    }

    @Override
    public final @Nullable E remove() {
        return removeFirst();
    }

    @Override
    public final @Nullable E poll() {
        return pollFirst();
    }

    @Override
    public final @Nullable E element() {
        return getFirst();
    }

    @Override
    public final @Nullable E peek() {
        return peekFirst();
    }

    @Override
    public final void push(@Nullable E e) {
        addFirst(e);
    }

    @Override
    public final @Nullable E pop() {
        return removeFirst();
    }
 
    @Override
    public final boolean offerFirst(@Nullable E e) {
        addFirst(e);
        return true;
    }

    @Override
    public final boolean offerLast(@Nullable E e) {
        addLast(e);
        return true;
    }

}
