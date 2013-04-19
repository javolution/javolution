/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.lang.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public final class SharedTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock read = rwl.readLock();
    private final Lock write = rwl.writeLock();

    public SharedTableImpl(TableService<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        read.lock();
        try {
            return that.size();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E get(int index) {
        read.lock();
        try {
            return that.get(index);
        } finally {
            read.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        write.lock();
        try {
            return that.set(index, element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        write.lock();
        try {
            that.add(index, element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public E remove(int index) {
        write.lock();
        try {
            return that.remove(index);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void clear() {
        write.lock();
        try {
            that.clear();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E getFirst() {
        read.lock();
        try {
            return that.getFirst();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E getLast() {
        read.lock();
        try {
            return that.getLast();
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean add(E element) {
        write.lock();
        try {
            return that.add(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void addFirst(E element) {
        write.lock();
        try {
            that.addFirst(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public void addLast(E element) {
        write.lock();
        try {
            that.addLast(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public E removeFirst() {
        write.lock();
        try {
            return that.removeFirst();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E removeLast() {
        write.lock();
        try {
            return that.removeLast();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E pollFirst() {
        write.lock();
        try {
            return that.pollFirst();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E pollLast() {
        write.lock();
        try {
            return that.pollLast();
        } finally {
            write.unlock();
        }
    }

    @Override
    public E peekFirst() {
        read.lock();
        try {
            return that.peekFirst();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E peekLast() {
        read.lock();
        try {
            return that.peekLast();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        read.lock();
        try {
            that.doWhile(predicate);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        write.lock();
        try {
            return that.removeAll(predicate);
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean contains(E element) {
        read.lock();
        try {
            return that.contains(element);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean remove(E element) {
        write.lock();
        try {
            return that.remove(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public int indexOf(E element) {
        read.lock();
        try {
            return that.indexOf(element);
        } finally {
            read.unlock();
        }
    }

    @Override
    public int lastIndexOf(E element) {
        read.lock();
        try {
            return that.lastIndexOf(element);
        } finally {
            read.unlock();
        }
    }

    @Override
    public void sort() {
        write.lock();
        try {
            that.sort();
        } finally {
            write.unlock();
        }
    }

    @Override
    public ComparatorService<E> comparator() {
        return that.comparator();
    }

    private static final long serialVersionUID = 2003570192853175381L;
}
