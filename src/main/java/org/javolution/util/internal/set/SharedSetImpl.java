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

import org.javolution.util.AbstractCollection;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastIterator;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.ReadWriteLockImpl;

/**
 * A shared view over a set (reads-write locks).
 */
public final class SharedSetImpl<E> // implements AbstractSetMethods<E> {
     extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;
    private final ReadWriteLockImpl lock;

    public SharedSetImpl(AbstractSet<E> inner) {
        this.inner = inner;
        this.lock = new ReadWriteLockImpl();
    }

    public SharedSetImpl(AbstractSet<E> inner, ReadWriteLockImpl lock) {
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
    public boolean add(E element, boolean allowDuplicate) {
        lock.writeLock.lock();
        try {
            return inner.add(element, allowDuplicate);
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
    public SharedSetImpl<E> clone() {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<E>(inner.clone());
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean contains(final Object searched) {
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
    public boolean equals(Object obj) {
        lock.readLock.lock();
        try {
            return inner.equals(obj);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E first() {
        lock.readLock.lock();
        try {
            return inner.first();
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
    public int hashCode() {
        lock.readLock.lock();
        try {
            return inner.hashCode();
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
    public FastIterator<E> iterator() {
        lock.readLock.lock();
        try {
            return inner.clone().iterator();
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
    public FastIterator<E> iterator(E from) {
        lock.readLock.lock();
        try {
            return inner.clone().iterator(from);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public FastIterator<E> descendingIterator(E from) {
        lock.readLock.lock();
        try {
            return inner.clone().descendingIterator(from);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E last() {
        lock.readLock.lock();
        try {
            return inner.last();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
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
    public boolean remove(Object searched) {
        lock.writeLock.lock();
        try {
            return inner.remove(searched);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> that) {
        lock.writeLock.lock();
        try {
            return inner.removeAll(that);
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
    public boolean retainAll(Collection<?> that) {
        lock.writeLock.lock();
        try {
            return inner.retainAll(that);
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
    public AbstractSet<E>[] trySplit(int n) {
        lock.readLock.lock();
        try {
            return inner.clone().trySplit(n);
        } finally {
            lock.readLock.unlock();
        }
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
    public Equality<? super E> equality() {
        lock.readLock.lock();
        try {
            return inner.equality();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        lock.readLock.lock();
        try {
            return inner.comparator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SharedSetImpl<E> headSet(E arg0) {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<E>(inner.headSet(arg0), lock); // Share the same lock.
        } finally {
            lock.readLock.unlock();
        }
    }
  
    @Override
    public SharedSetImpl<E> subSet(E arg0, E arg1) {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<E>(inner.subSet(arg0, arg1), lock); // Share the same lock.
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SharedSetImpl<E> tailSet(E arg0) {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<E>(inner.tailSet(arg0), lock); // Share the same lock.
        } finally {
            lock.readLock.unlock();
        }
      }

    @Override
    public SharedSetImpl<E> subSet(E element) {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<E>(inner.subSet(element), lock); // Share the same lock.
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E getAny(E element) {
        lock.readLock.lock();
        try {
            return inner.getAny(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E removeAny(E element) {
        lock.writeLock.lock();
        try {
            return inner.removeAny(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

}
