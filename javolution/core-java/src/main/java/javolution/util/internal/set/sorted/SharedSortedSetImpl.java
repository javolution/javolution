/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set.sorted;

import javolution.util.internal.set.SharedSetImpl;
import javolution.util.service.SetService;
import javolution.util.service.SortedSetService;

/**
 * A shared view over a set allowing concurrent access and sequential updates.
 */
public class SharedSortedSetImpl<E> extends SharedSetImpl<E> implements
        SortedSetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedSetImpl(SetService<E> target) {
        super(target);
    }

    @Override
    public E first() {
        lock.readLock.lock();
        try {
            return target().first();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SortedSetService<E> headSet(E toElement) {
        return new SubSortedSetImpl<E>(this, null, toElement);
    }

    @Override
    public E last() {
        lock.readLock.lock();
        try {
            return target().last();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SortedSetService<E> subSet(E fromElement, E toElement) {
        return new SubSortedSetImpl<E>(this, fromElement, toElement);
    }

    @Override
    public SortedSetService<E> tailSet(E fromElement) {
        return new SubSortedSetImpl<E>(this, fromElement, null);
    }

    @Override
    public SortedSetService<E> threadSafe() {
        return this;
    }
    
    @Override
    protected SortedSetService<E> target() {
        return (SortedSetService<E>) super.target();
    }
}
