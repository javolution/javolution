/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table.sorted;

import javolution.util.service.SortedTableService;
import javolution.internal.util.table.SubTableImpl;
/**
 * A view over a portion of a sorted table.
 */
public class SubSortedTableImpl<E> extends SubTableImpl<E>
        implements SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SubSortedTableImpl(SortedTableService<E> target, int fromIndex, int toIndex) {
        super(target, fromIndex, toIndex);
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
        int i = target().indexOf(element);
        if ((i < fromIndex) || (i >= toIndex)) return -1;
        return i - fromIndex;
    }

    @Override
    public boolean remove(E element) {
        int i = indexOf(element);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    @Override
    public int slotOf(E element) {
        int i = target().slotOf(element);
        if (i < fromIndex) return 0;
        if (i >= toIndex) return size();
        return i - fromIndex;
    }

    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }
}
