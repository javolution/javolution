/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;
import java.util.Iterator;

import javolution.internal.util.ReadWriteLockImpl;
import javolution.internal.util.SharedIteratorImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.SetService;

/**
 * A shared view over a set allowing concurrent access and sequential updates.
 */
public class SharedSetImpl<E> implements SetService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final ReadWriteLockImpl rwLock;
    private final SetService<E> target;

    public SharedSetImpl(SetService<E> target) {
        this(target,new ReadWriteLockImpl());
    }

    public SharedSetImpl(SetService<E> target, ReadWriteLockImpl rwLock) {
        this.target = target;
        this.rwLock = rwLock;
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
        return target.comparator();
    }

    @Override
    public boolean contains(E e) {
        rwLock.readLock().lock();
        try {
            return target.contains(e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void forEach(
            Consumer<? super E> consumer, IterationController controller) {
        rwLock.readLock().lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Deprecated
    @Override
    public Iterator<E> iterator() {
        return new SharedIteratorImpl<E>(target.iterator(), rwLock);
    }

    @Override
    public boolean remove(E e) {
        rwLock.writeLock().lock();
        try {
            return target.remove(e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(
            Predicate<? super E> filter, IterationController controller) {
        rwLock.writeLock().lock();
        try {
            return target.removeIf(filter, controller);
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

    @Override
    public void atomic(Runnable update) {
        rwLock.writeLock().lock();
        try {
            target.atomic(update);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
        
    protected SetService<E> target() {
        return target;
    }
    
    protected ReadWriteLockImpl rwLock() {
        return rwLock;
    }
}
