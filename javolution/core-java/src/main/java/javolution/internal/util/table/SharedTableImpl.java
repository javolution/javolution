/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.internal.util.ReadWriteLockImpl;
import javolution.internal.util.SharedIteratorImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public final class SharedTableImpl<E> implements TableService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final ReadWriteLockImpl rwLock;
    private final TableService<E> target;

    public SharedTableImpl(TableService<E> that) {
        this.target = that;
        this.rwLock = new ReadWriteLockImpl();
    }

    @Override
    public boolean add(E element) {
        rwLock.writeLock().lock();
        try {
            return target.add(element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        rwLock.writeLock().lock();
        try {
            target.add(index, element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addFirst(E element) {
        rwLock.writeLock().lock();
        try {
            target.addFirst(element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addLast(E element) {
        rwLock.writeLock().lock();
        try {
            target.addLast(element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            target.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        rwLock.readLock().lock();
        try {
            return target.comparator();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        rwLock.readLock().lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E get(int index) {
        rwLock.readLock().lock();
        try {
            return target.get(index);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E getFirst() {
        rwLock.readLock().lock();
        try {
            return target.getFirst();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E getLast() {
        rwLock.readLock().lock();
        try {
            return target.getLast();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public ReadWriteLock getLock() {
        return rwLock;
    }

    @Override
    @Deprecated
    public Iterator<E> iterator() {
        return new SharedIteratorImpl<E>(target.iterator(), rwLock);
    }

    @Override
    public E peekFirst() {
        rwLock.readLock().lock();
        try {
            return target.peekFirst();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E peekLast() {
        rwLock.readLock().lock();
        try {
            return target.peekLast();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E pollFirst() {
        rwLock.writeLock().lock();
        try {
            return target.pollFirst();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E pollLast() {
        rwLock.writeLock().lock();
        try {
            return target.pollLast();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E remove(int index) {
        rwLock.writeLock().lock();
        try {
            return target.remove(index);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E removeFirst() {
        rwLock.writeLock().lock();
        try {
            return target.removeFirst();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        rwLock.writeLock().lock();
        try {
            return target.removeIf(filter, controller);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E removeLast() {
        rwLock.writeLock().lock();
        try {
            return target.removeLast();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        rwLock.writeLock().lock();
        try {
            return target.set(index, element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return target.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SharedCollectionImpl<E>[] trySplit(int n) {
        return SharedCollectionImpl.splitOf(target, n, rwLock);
    }

}
