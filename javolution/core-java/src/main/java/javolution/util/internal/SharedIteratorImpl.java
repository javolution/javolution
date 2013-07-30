/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal;

import java.util.Iterator;


/**
 * An iterator allowing concurrent access and sequential removals.
 */
public final class SharedIteratorImpl<E> implements Iterator<E> {

    private final Iterator<E> target;
    private final ReadWriteLockImpl rwLock;

    public SharedIteratorImpl(Iterator<E> target, ReadWriteLockImpl rwLock) {
        this.target = target;
        this.rwLock = rwLock;
    }

    @Override
    public boolean hasNext() {
        rwLock.readLock().lock();
        try {
            return target.hasNext();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public E next() {
        rwLock.readLock().lock();
        try {
            return target.next();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void remove() {
        rwLock.writeLock().lock();
        try {
            target.remove();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
