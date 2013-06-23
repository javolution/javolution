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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.SharedIteratorImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public final class SharedTableImpl<E> implements TableService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Lock read;
    private final TableService<E> target;
    private final Lock write;

    public SharedTableImpl(TableService<E> that) {
        this.target = that;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    @Override
    public boolean add(E element) {
        write.lock();
        try {
            return target.add(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        write.lock();
        try {
            target.add(index, element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void addFirst(E element) {
        write.lock();
        try {
            target.addFirst(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void addLast(E element) {
        write.lock();
        try {
            target.addLast(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void atomic(Runnable action) {
        write.lock();
        try {
            target.atomic(action);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear() {
        write.lock();
        try {
            target.clear();
        } finally {
            write.unlock();
        }
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        read.lock();
        try {
            return target.comparator();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        read.lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            read.unlock();
        }
    }

    @Override
    public E get(int index) {
        read.lock();
        try {
            return target.get(index);
        } finally {
            read.unlock();
        }
    }

    @Override
    public E getFirst() {
        read.lock();
        try {
            return target.getFirst();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E getLast() {
        read.lock();
        try {
            return target.getLast();
        } finally {
            read.unlock();
        }
    }

    @Override
    @Deprecated
    public Iterator<E> iterator() {
        return new SharedIteratorImpl<E>(target.iterator(), read, write);
    }

    @Override
    public E peekFirst() {
        read.lock();
        try {
            return target.peekFirst();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E peekLast() {
        read.lock();
        try {
            return target.peekLast();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E pollFirst() {
        write.lock();
        try {
            return target.pollFirst();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E pollLast() {
        write.lock();
        try {
            return target.pollLast();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E remove(int index) {
        write.lock();
        try {
            return target.remove(index);
        } finally {
            write.unlock();
        }
    }

    @Override
    public E removeFirst() {
        write.lock();
        try {
            return target.removeFirst();
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        write.lock();
        try {
            return target.removeIf(filter, controller);
        } finally {
            write.unlock();
        }
    }

    @Override
    public E removeLast() {
        write.lock();
        try {
            return target.removeLast();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        write.lock();
        try {
            return target.set(index, element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public int size() {
        read.lock();
        try {
            return target.size();
        } finally {
            read.unlock();
        }
    }

    @Override
    public SharedCollectionImpl<E>[] trySplit(int n) {
        return SharedCollectionImpl.splitOf(target, n, read, write);
    }

}
