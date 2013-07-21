/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table.sorted;

import javolution.internal.util.table.FastTableImpl;
import javolution.util.function.EqualityComparator;
import javolution.util.service.SortedTableService;

/**
 * The default {@link javolution.util.FastSortedTable FastSortedTable} implementation.
 */
public class FastSortedTableImpl<E> extends FastTableImpl<E> implements
        SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public FastSortedTableImpl(EqualityComparator<? super E> comparator) {
        super(comparator);
    }
    @Override
    public boolean add(E element) {
        add(slotOf(element), element);
        return true;
    }

    @Override
    public int addIfAbsent(E element) {
        int i = slotOf(element);
        if (i >= size() || !comparator().areEqual(get(i), element)) { // Absent.
            add(i, element);
        }
        return i;
    }

    @Override
    public int indexOf(E element) {
        int i = slotOf(element);
        if (i >= size() || !comparator().areEqual(get(i), element))
            return -1;
        return i;
    }

    @Override
    public boolean remove(E element) {
        int i = indexOf(element);
        if (i < 0)
            return false;
        remove(i);
        return true;
    }

    @Override
    public int slotOf(E element) {
        return slotOf(element, 0, size(), comparator());
    }
    private int slotOf(E element, int start, int length,
            EqualityComparator<? super E> cmp) {
        if (length == 0)
            return start;
        int half = length >> 1;
        return (cmp.compare(element, get(start + half)) <= 0) ? slotOf(element,
                start, half, cmp) : slotOf(element, start + half + 1, length
                - half - 1, cmp);
    }

}
