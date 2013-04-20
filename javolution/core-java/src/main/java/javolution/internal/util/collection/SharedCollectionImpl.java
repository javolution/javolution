/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.lang.Predicate;
import javolution.util.service.CollectionService;

/**
 * A shared view over a collection allowing concurrent access and sequential updates.
 */
public final class SharedCollectionImpl<E> implements CollectionService<E>,
        Serializable {

    private final CollectionService<E> that;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock read = rwl.readLock();
    private final Lock write = rwl.writeLock();

    public SharedCollectionImpl(CollectionService<E> that) {
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
    public void clear() {
        write.lock();
        try {
            that.clear();
        } finally {
            write.unlock();
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
    @Deprecated
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                read.lock();
                try {
                    return thatIterator.hasNext();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public E next() {
                read.lock();
                try {
                    return thatIterator.next();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public void remove() {
                write.lock();
                try {
                    thatIterator.remove();
                } finally {
                    write.unlock();
                }
            }

        };
    }

    private static final long serialVersionUID = -1167317648000125119L;

}
