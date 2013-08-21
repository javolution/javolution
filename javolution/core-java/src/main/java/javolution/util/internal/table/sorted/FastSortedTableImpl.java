/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.function.Equality;
import javolution.util.internal.table.FastTableImpl;
import javolution.util.service.SortedTableService;

/**
 * The default {@link javolution.util.FastSortedTable FastSortedTable} implementation.
 */
public class FastSortedTableImpl<E> extends FastTableImpl<E> implements
        SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public FastSortedTableImpl(Equality<? super E> comparator) {
        super(comparator);
    }

    @Override
    public boolean add(E element) {
        add(positionOf(element), element);
        return true;
    }
 
    @Override
    public boolean addIfAbsent(E element) {
        int i = positionOf(element);
        if ((i < size()) && comparator().areEqual(element, get(i))) return false; // Already there.
        add(i, element);
        return true;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object element) {
        int i = positionOf((E) element);
        if (i >= size() || !comparator().areEqual(get(i), (E) element)) return -1;
        return i;
    }

    @Override
    public int positionOf(E element) {
        return positionOf(element, 0, size());
    }

    @Override
    public SortedTableService<E>[] split(int n, boolean updateable) {
        return SubSortedTableImpl.splitOf(this, n, updateable); // Sub-views over this.
    }

    private int positionOf(E element, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        return (comparator().compare(element, get(start + half)) <= 0) ? positionOf(
                element, start, half) : positionOf(element, start + half + 1,
                length - half - 1);
    }

}
