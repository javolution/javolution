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

import javolution.annotation.RealTime;
import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.NoDuplicateTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SortedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.service.ComparatorService;
import javolution.util.service.SetService;
import javolution.util.service.TableService;

/**
 * <p> A high-performance sorted set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedSet<E> extends FastSet<E> implements SortedSet<E> {

    /**
     * Creates an empty set ordered on elements natural order.
     */
    public FastSortedSet() {
        super(...);
    }

    /**
    * Creates an empty set ordered using the specified element comparator.
    */
   public FastSortedSet(ComparatorService<? super E> comparator) {
       super(...);
   }
    /**
     * Creates a set backed up by the specified implementation.
     */
    protected FastSet(SetService<E> implementation) {
        super(implementation);        
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
    public TableService<E> service() {
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