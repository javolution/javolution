/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.internal.table.AtomicTableImpl;
import javolution.util.service.SortedTableService;
import javolution.util.service.TableService;

/**
 * An atomic view over a sorted table.
 */
public class AtomicSortedTableImpl<E> extends AtomicTableImpl<E> implements
        SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicSortedTableImpl(TableService<E> target) {
        super(target);
    }

    @Override
    public synchronized boolean addIfAbsent(E element) {
        boolean changed = target().addIfAbsent(element);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public int positionOf(E element) {
        return targetView().positionOf(element);
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

    @Override
    protected SortedTableService<E> targetView() {
        return (SortedTableService<E>) super.targetView();
    }

}
