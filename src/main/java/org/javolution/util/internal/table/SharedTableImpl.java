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
import org.javolution.util.internal.ReadWriteLockImpl;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public final class SharedTableImpl<E> //implements AbstractTableMethods<E> {
         extends AbstractTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractTable<E> inner;
    private final ReadWriteLockImpl lock;

    public SharedTableImpl(AbstractTable<E> inner) {
        this.inner = inner;
        this.lock = new ReadWriteLockImpl();
    }

    private SharedTableImpl(AbstractTable<E> inner,  ReadWriteLockImpl lock) {
        this.inner = inner;
        this.lock = lock;
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
    public boolean addAll(@SuppressWarnings("unchecked") E... elements) {
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
    public E findAny() {
        lock.readLock.lock();
        try {
            return inner.findAny();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate) {
        lock.readLock.lock();
        try {
            return inner.anyMatch(predicate);
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
    public FastIterator<E> descendingIterator() {
        lock.readLock.lock();
        try {
            return inner.clone().descendingIterator();
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
    public int lastIndexOf(Object element) {
        lock.readLock.lock();
        try {
            return inner.lastIndexOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public FastListIterator<E> listIterator(int index) {
        lock.readLock.lock();
        try {
            return inner.clone().listIterator(index);
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
    public AbstractTable<E>[] trySplit(int n) {
        lock.readLock.lock();
        try {
            return inner.clone().trySplit(n);
        } finally {
            lock.readLock.unlock();
        }
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

    @Override
    public AbstractCollection<E> collect() {
        lock.readLock.lock();
        try {
            return inner.collect();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public FastIterator<E> iterator() {
        lock.readLock.lock();
        try {
            return inner.clone().iterator();
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
    public FastListIterator<E> listIterator() {
        lock.readLock.lock();
        try {
            return inner.clone().listIterator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SharedTableImpl<E> subList(int arg0, int arg1) {
        lock.readLock.lock();
        try {
            return new SharedTableImpl<E>(inner.subTable(arg0, arg1), lock); // Share the same lock.
        } finally {
            lock.readLock.unlock();
        }
    }

    
}