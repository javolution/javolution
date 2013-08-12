/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import javolution.util.service.TableService;

/**
 * A view over a portion of a table. 
 */
public class SubTableImpl<E> extends TableView<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    
    /** Splits the specified table.  */
    @SuppressWarnings("unchecked")
    public static <E> SubTableImpl<E>[] splitOf(TableService<E> table, int n) {
        if (n <= 1)
            throw new IllegalArgumentException("Invalid argument n: " + n);
        SubTableImpl<E>[] subTables = new SubTableImpl[n];
        int minSize = table.size() / n;
        int start = 0;
        for (int i = 0; i < n - 1; i++) {
            subTables[i] = new SubTableImpl<E>(table, start, start + minSize);
            start += minSize;
        }
        subTables[n - 1] = new SubTableImpl<E>(table, start, table.size());
        return subTables;
    }
    
    protected int fromIndex;
    protected int toIndex;

    public SubTableImpl(TableService<E> target, int from, int to) {
         super(target);
         if ((from < 0) || (to > target.size()) || (from > to))
            throw new IndexOutOfBoundsException("fromIndex: " + from
                    + ", toIndex: " + to + ", size(): " + target.size()); // As per List.subList contract.
        fromIndex = from;
        toIndex = to;
    }

    @Override
    public boolean add(E element) {
        add(size(), element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size()))
            indexError(index);
        target().add(index + fromIndex, element);
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size()))
            indexError(index);
        return target().get(index + fromIndex);
    }

    @Override
    public E remove(int index) {
        if ((index < 0) && (index >= size()))
            indexError(index);
        toIndex--;
        return target().remove(index + fromIndex);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size()))
            indexError(index);
        return target().set(index + fromIndex, element);
    }

    @Override
    public int size() {
        return toIndex - fromIndex;
    }

}
