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
public class SharedSortedTableImpl<E> extends SharedTableImpl<E> implements SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedTableImpl(SortedTableService<E> target) {
        super(target);
    }

    @Override
    public int addIfAbsent(E element) {
        rwLock().writeLock().lock();
        try {
            return target().addIfAbsent(element);
        } finally {
            rwLock().writeLock().unlock();
        }
    }

    @Override
    public int indexOf(E element) {
        rwLock().readLock().lock();
        try {
            return target().indexOf(element);
        } finally {
            rwLock().readLock().unlock();
        }
    }

    @Override
    public boolean remove(E element) {
        rwLock().writeLock().lock();
        try {
            return target().remove(element);
        } finally {
            rwLock().writeLock().unlock();
        }
    }

    @Override
    public int slotOf(E element) {
        rwLock().readLock().lock();
        try {
            return target().slotOf(element);
        } finally {
            rwLock().readLock().unlock();
        }
    }
 
    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>)super.target();
    }
}
