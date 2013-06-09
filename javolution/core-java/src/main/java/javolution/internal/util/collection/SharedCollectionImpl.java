/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.util.FastCollection;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A shared view over a collection allowing concurrent access and sequential updates.
 */
public final class SharedCollectionImpl<E> extends FastCollection<E> implements CollectionService<E> {

    private static final long serialVersionUID = 1496271733380089832L;
    private final CollectionService<E> target;
    private final Lock read;
    private final Lock write;

    public SharedCollectionImpl(CollectionService<E> target) {
        this.target = target;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    private SharedCollectionImpl(CollectionService<E> target, Lock read,
            Lock write) {
        this.target = target;
        this.read = read;
        this.write = write;
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
    public boolean doWhile(Predicate<? super E> predicate) {
        read.lock();
        try {
            return target.doWhile(predicate);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        write.lock();
        try {
            return target.removeIf(predicate);
        } finally {
            write.unlock();
        }
    }

    @Override
    @Deprecated
    public Iterator<E> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                read.lock();
                try {
                    return targetIterator.hasNext();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public E next() {
                read.lock();
                try {
                    return targetIterator.next();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public void remove() {
                write.lock();
                try {
                    targetIterator.remove();
                } finally {
                    write.unlock();
                }
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return target.comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null)
            return null;
        SharedCollectionImpl<E>[] shareds = new SharedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            shareds[i] = new SharedCollectionImpl<E>(tmp[i], read, write);
        }
        return shareds;
    }

    @Override
    public SharedCollectionImpl<E> service() {
        return this;
    }
}
