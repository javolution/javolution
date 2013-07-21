/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set.sorted;

import javolution.internal.util.ReadWriteLockImpl;
import javolution.internal.util.set.SharedSetImpl;
import javolution.util.service.SortedSetService;

/**
 * A shared view over a sorted set allowing concurrent access and sequential updates.
 */
public class SharedSortedSetImpl<E> extends SharedSetImpl<E> implements SortedSetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedSetImpl(SortedSetService<E> target) {
        super(target, new ReadWriteLockImpl());
    }

    public SharedSortedSetImpl(SortedSetService<E> target, ReadWriteLockImpl rwLock) {
        super(target, rwLock);
    }

    @Override
    public E first() {
        rwLock().readLock().lock();
        try {
            return target().first();
        } finally {
            rwLock().readLock().unlock();
        }
    }

    @Override
    public E last() {
        rwLock().readLock().lock();
        try {
            return target().last();
        } finally {
            rwLock().readLock().unlock();
        }
    }

    @Override
    public SortedSetService<E> subSet(E fromElement, E toElement) {
        rwLock().readLock().lock();
        try {
            return target().subSet(fromElement, toElement);
        } finally {
            rwLock().readLock().unlock();
        }
    }
    
    @Override
    protected SortedSetService<E> target() {
        return (SortedSetService<E>)super.target();
    }
    
}
