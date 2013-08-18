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
    public boolean addIfAbsent(E element) {
        if (!contains(element)) return add(element);
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object o) {
        int i = positionOf((E) o);
        if ((i >= size()) || !comparator().areEqual((E) o, get(i))) return -1;
        return i;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int lastIndexOf(Object o) {
        int i = positionOf((E) o);
        int result = -1;
        while ((i < size()) && comparator().areEqual((E) o, get(i))) {
            result = i++;
        }
        return result;
    }

    @Override
    public abstract int positionOf(E element);

    @Override
    public SortedTableService<E> threadSafe() {
        return new SharedSortedTableImpl<E>(this);
    }

    /** Returns the actual target */
    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }

}
