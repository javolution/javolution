/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Comparator;
import java.util.SortedSet;

import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.NoDuplicateTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SortedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.service.TableService;

/**
 * <p> A set backed up by an sorted table and benefiting from the 
 *     same characteristics (memory footprint adjusted to current size,
 *     smooth capacity increase, etc).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedSet<E> extends FastCollection<E> implements SortedSet<E> {

    /**
     * Holds the backing table.
     */
    private final TableService<E> table;

    /**
     * Creates an empty set whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastSortedSet() {
        table = new NoDuplicateTableImpl<E>(new SortedTableImpl<E>(new FractalTableImpl<E>()));
    }

    /**
     * Creates a sorted set backed up by the specified table implementation.
     */
    protected FastSortedSet(TableService<E> table) {
        this.table = table;
    } 
    
    @Override
    public FastSortedSet<E> unmodifiable() {
        return new FastSortedSet<E>(new UnmodifiableTableImpl<E>(table));
    }

    @Override
    public FastSortedSet<E> shared() {
        return new FastSortedSet<E>(new SharedTableImpl<E>(table));
    }
        
    @Override
    protected TableService<E> getService() {
        return table;
    }

    /***************************************************************************
     * SortedSet operations.
     */    
    
    @SuppressWarnings("unchecked")
    @Override
    public Comparator<? super E> comparator() {
        return (Comparator<? super E>) table.getComparator();
    }

    @Override
    public FastSortedSet<E> subSet(E fromElement, E toElement) {
        int fromIndex = SortedTableImpl.indexIfSortedOf(fromElement, table, 0, table.size());
        int toIndex = SortedTableImpl.indexIfSortedOf(fromElement, table, 0, table.size());
        return new FastSortedSet<E>(
                    new SubTableImpl<E>(table, fromIndex, toIndex));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toIndex = SortedTableImpl.indexIfSortedOf(toElement, table, 0, table.size());
        return new FastSortedSet<E>(
                new SubTableImpl<E>(table, 0, toIndex));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int fromIndex = SortedTableImpl.indexIfSortedOf(fromElement, table, 0, table.size());
        return new FastSortedSet<E>(
                new SubTableImpl<E>(table, fromIndex, table.size()));
    }

    @Override
    public E first() {
        return table.getFirst();
    }

    @Override
    public E last() {
        return table.getLast();
    }
 
}