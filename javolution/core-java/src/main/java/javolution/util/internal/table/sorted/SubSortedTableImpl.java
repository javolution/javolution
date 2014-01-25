/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import javolution.util.internal.table.SubTableImpl;
import javolution.util.service.SortedTableService;
import javolution.util.service.TableService;

/**
 * A view over a portion of a sorted table. 
 */
public class SubSortedTableImpl<E> extends SubTableImpl<E> implements SortedTableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /** Splits the specified table.  */
    @SuppressWarnings("unchecked")
    public static <E> SortedTableService<E>[] splitOf(SortedTableService<E> table,
            int n, boolean updateable) {
        if (updateable) table = new SharedSortedTableImpl<E>(table);
        if (n < 1) throw new IllegalArgumentException("Invalid argument n: "
                + n);
        SortedTableService<E>[] subTables = new SortedTableService[n];
        int minSize = table.size() / n;
        int start = 0;
        for (int i = 0; i < n - 1; i++) {
            subTables[i] = new SubSortedTableImpl<E>(table, start, start + minSize);
            start += minSize;
        }
        subTables[n - 1] = new SubSortedTableImpl<E>(table, start, table.size());
        return subTables;
    }

     public SubSortedTableImpl(TableService<E> target, int from, int to) {
        super(target, from, to);
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
    public int indexOf(Object o) {
        int i = positionOf((E)o);
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
    public int positionOf(E element) {
        int i = target().positionOf(element);
        if (i >= 0) { // Actual index.
            if (i < fromIndex) return -1;
            if (i >= toIndex) return -size()-1;
            return i - fromIndex;	
        } else { // Would be index.
        	i = -i - 1;
            if (i < fromIndex) return -1;
            if (i >= toIndex) return -size()-1;
        	return -(i - fromIndex) - 1;
        }
    }
    
    @Override
    public SortedTableService<E>[] split(int n, boolean updateable) {
        return SubSortedTableImpl.splitOf(this, n, updateable); // Sub-views over this.
    }

    @Override
    protected SortedTableService<E> target() {
        return (SortedTableService<E>) super.target();
    }
    
}
