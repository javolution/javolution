/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.internal.table.UnmodifiableTableImpl;
import javolution.util.service.SortedTableService;

/**
 * An unmodifiable view over a sorted table.
 */
public class UnmodifiableSortedTableImpl<E> extends UnmodifiableTableImpl<E>
        implements SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableSortedTableImpl(SortedTableService<E> target) {
        super(target);
    }

    @Override
    public boolean addIfAbsent(E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public int positionOf(E element) {
        return target().positionOf(element);
    }

    @Override
    public SortedTableService<E> threadSafe() {
        return this;
    }

    /** Returns the actual target */
    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }
}
