/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.internal.table.SharedTableImpl;
import javolution.util.service.SortedTableService;

/**
 * A shared view over a sorted table allowing concurrent access and sequential updates.
 */
public class SharedSortedTableImpl<E> extends SharedTableImpl<E> implements
        SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedTableImpl(SortedTableService<E> target) {
        super(target);
    }

    @Override
    public boolean addIfAbsent(E element) {
        lock.writeLock.lock();
        try {
            return target().addIfAbsent(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int positionOf(E element) {
        lock.readLock.lock();
        try {
            return target().positionOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public SortedTableService<E>[] split(int n, boolean updateable) {
        return SubSortedTableImpl.splitOf(this, n, false); // Sub-views over this.
    }

    /** Returns the actual target */
    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }

}