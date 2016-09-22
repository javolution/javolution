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
import java.util.Iterator;
import java.util.ListIterator;

import org.javolution.lang.Parallel;
import org.javolution.util.FastTable;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.ReadWriteLockImpl;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public final class SharedTableImpl<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastTable<E> inner;
    private final ReadWriteLockImpl lock;

    public SharedTableImpl(FastTable<E> inner) {
        this.inner = inner;
        this.lock = new ReadWriteLockImpl();
    }

    @Override
    public boolean add(E element) {
        lock.writeLock.lock();
        try {
            return inner.add(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        lock.writeLock.lock();
        try {
            inner.add(index, element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> that) {
        lock.writeLock.lock();
        try {
            return inner.addAll(that);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(E... elements) {
        lock.writeLock.lock();
        try {
            return inner.addAll(elements);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> that) {
        lock.writeLock.lock();
        try {
            return inner.addAll(index, that);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAllSorted(Collection<? extends E> that, Comparator<? super E> cmp) {
        lock.writeLock.lock();
        try {
            return inner.addAllSorted(that, cmp);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void addFirst(E element) {
        lock.writeLock.lock();
        try {
            inner.addFirst(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void addLast(E element) {
        lock.writeLock.lock();
        try {
            inner.addLast(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int addSorted(E element, Comparator<? super E> cmp) {
        lock.writeLock.lock();
        try {
            return inner.addSorted(element, cmp);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E any() {
        lock.readLock.lock();
        try {
            return inner.any();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock.lock();
        try {
            inner.clear();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public SharedTableImpl<E> clone() {
        lock.readLock.lock();
        try {
            return new SharedTableImpl<E>(inner.clone());
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean contains(Object searched) {
        lock.readLock.lock();
        try {
            return inner.contains(searched);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> that) {
        lock.readLock.lock();
        try {
            return inner.containsAll(that);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Iterator<E> descendingIterator() {
        lock.readLock.lock();
        try {
            return inner.clone().unmodifiable().descendingIterator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality(); // Immutable.
    }

    @Override
    public boolean equals(Object obj) {
        lock.readLock.lock();
        try {
            return inner.equals(obj);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        lock.readLock.lock();
        try {
            inner.forEach(consumer);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E get(int index) {
        lock.readLock.lock();
        try {
            return inner.get(index);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E getFirst() {
        lock.readLock.lock();
        try {
            return inner.getFirst();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E getLast() {
        lock.readLock.lock();
        try {
            return inner.getLast();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock.lock();
        try {
            return inner.hashCode();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int indexOf(Object element) {
        lock.readLock.lock();
        try {
            return inner.indexOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock.lock();
        try {
            return inner.isEmpty();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ReadOnlyIterator<E> iterator() {
        lock.readLock.lock();
        try {
            return ReadOnlyIterator.of(inner.clone().iterator());
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int lastIndexOf(Object element) {
        lock.readLock.lock();
        try {
            return inner.lastIndexOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        lock.readLock.lock();
        try {
            return inner.clone().unmodifiable().listIterator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        lock.readLock.lock();
        try {
            return inner.clone().unmodifiable().listIterator(index);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E max() {
        lock.readLock.lock();
        try {
            return inner.max();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E min() {
        lock.readLock.lock();
        try {
            return inner.min();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E peekFirst() {
        lock.readLock.lock();
        try {
            return inner.peekFirst();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E peekLast() {
        lock.readLock.lock();
        try {
            return inner.peekLast();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E pollFirst() {
        lock.writeLock.lock();
        try {
            return inner.pollFirst();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E pollLast() {
        lock.writeLock.lock();
        try {
            return inner.pollLast();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        lock.readLock.lock();
        try {
            return inner.reduce(operator);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E remove(int index) {
        lock.writeLock.lock();
        try {
            return inner.remove(index);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object searched) {
        lock.writeLock.lock();
        try {
            return inner.remove(searched);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock.writeLock.lock();
        try {
            return inner.removeAll(c);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E removeFirst() {
        lock.writeLock.lock();
        try {
            return inner.removeFirst();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        lock.writeLock.lock();
        try {
            return inner.removeFirstOccurrence(o);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        lock.writeLock.lock();
        try {
            return inner.removeIf(filter);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E removeLast() {
        lock.writeLock.lock();
        try {
            return inner.removeLast();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        lock.writeLock.lock();
        try {
            return inner.removeLastOccurrence(o);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int removeSorted(E element, Comparator<? super E> cmp) {
        lock.writeLock.lock();
        try {
            return inner.removeSorted(element, cmp);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> elements) {
        lock.writeLock.lock();
        try {
            return inner.retainAll(elements);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        lock.writeLock.lock();
        try {
            return inner.set(index, element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock.lock();
        try {
            return inner.size();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public void sort(Comparator<? super E> cmp) {
        lock.writeLock.lock();
        try {
            inner.sort(cmp);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        lock.readLock.lock();
        try {
            return inner.toArray();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        lock.readLock.lock();
        try {
            return inner.toArray(array);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock.lock();
        try {
            return inner.toString();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public FastTable<E>[] trySplit(int n) {
        lock.readLock.lock();
        try {
            return inner.clone().trySplit(n);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Parallel
    public boolean until(Predicate<? super E> matching) {
        lock.readLock.lock();
        try {
            return inner.until(matching);
        } finally {
            lock.readLock.unlock();
        }
    }

}