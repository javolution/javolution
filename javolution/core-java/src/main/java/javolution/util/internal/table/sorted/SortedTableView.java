/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.internal.table.TableView;
import javolution.util.service.SortedTableService;

/**
 * Sorted table view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 */
public abstract class SortedTableView<E> extends TableView<E> implements
        SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public SortedTableView(SortedTableService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E element) {
        int i = positionOf(element);
        add((i < 0) ? -i-1 : i, element);
        return true;
    }
 
    @Override
    public boolean addIfAbsent(E element) {
        int i = positionOf(element);
        if (i >= 0) return false;
        add(-i-1, element);
        return true;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object element) {
        int i = positionOf((E)element);
        return (i >= 0) ? i : -1;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public int lastIndexOf(Object o) {
        int i = positionOf((E) o);
        if (i < 0) return -1;
        while ((++i < size()) && comparator().areEqual((E) o, get(i))) {
        }
        return --i;
    }

    @Override
    public abstract int positionOf(E element);

    @Override
    public SortedTableService<E>[] split(int n, boolean updateable) {
        return SubSortedTableImpl.splitOf(this, n, updateable); // Sub-views over this.
    }

    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }

}
