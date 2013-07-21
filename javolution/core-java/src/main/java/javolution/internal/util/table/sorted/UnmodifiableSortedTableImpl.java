/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table.sorted;

import javolution.internal.util.table.SharedTableImpl;
import javolution.util.service.SortedTableService;

/**
 * A shared view over a sorted table allowing concurrent access and sequential updates.
 */
public class UnmodifiableSortedTableImpl<E> extends SharedTableImpl<E>
        implements SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableSortedTableImpl(SortedTableService<E> target) {
        super(target);
    }

    @Override
    public int addIfAbsent(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int indexOf(E element) {
        return target().indexOf(element);
    }

    @Override
    public boolean remove(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int slotOf(E element) {
        return target().slotOf(element);
    }

    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }
}
