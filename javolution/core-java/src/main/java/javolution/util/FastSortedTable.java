/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.LOG_N;

import java.util.Comparator;

import javolution.lang.Realtime;
import javolution.util.function.Equalities;
import javolution.util.internal.table.sorted.AtomicSortedTableImpl;
import javolution.util.internal.table.sorted.FastSortedTableImpl;
import javolution.util.internal.table.sorted.SharedSortedTableImpl;
import javolution.util.internal.table.sorted.UnmodifiableSortedTableImpl;
import javolution.util.service.SortedTableService;

/**
 * <p> A high-performance sorted table with {@link Realtime real-time} behavior.
 *      Sorted table have significantly faster {@link #contains}, 
 *     {@link #indexOf} and {@link #remove} methods).</p>
 *     
 * <p>This class is comparable to {@link FastSortedSet} in performance, 
 *    but it allows for duplicate and implements the {@link java.util.List}
 *    interface.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class FastSortedTable<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
      * Creates an empty table sorted using its elements natural order.     
     */
    public FastSortedTable() {
        this(Equalities.STANDARD);
    }

    /**
     * Creates an empty table sorted using the specified element comparator.
     */
    public FastSortedTable(Comparator<? super E> comparator) {
        super(new FastSortedTableImpl<E>(comparator));
    }

    /**
     * Creates a sorted table initially populated with the specified elements
     * (convenience method).
     */
    public FastSortedTable(E... elements) {
    	this();
    	for (E e : elements) add(e);
    }
    
   /**
     * Creates a sorted table initially populated with the specified elements 
     * and using the specified comparator for sorting (convenience method).
     */
    public FastSortedTable(Comparator<? super E> comparator, E... elements) {
    	this(comparator);
    	for (E e : elements) add(e);
    }
    
   /**
     * Creates a sorted table backed up by the specified service implementation.
     */
    protected FastSortedTable(SortedTableService<E> service) {
        super(service);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
    public FastSortedTable<E> atomic() {
        return new FastSortedTable<E>(new AtomicSortedTableImpl<E>(service()));
    }

    @Override
    public FastSortedTable<E> shared() {
        return new FastSortedTable<E>(new SharedSortedTableImpl<E>(service()));
    }

    @Override
    public FastSortedTable<E> unmodifiable() {
        return new FastSortedTable<E>(new UnmodifiableSortedTableImpl<E>(
                service()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

    @Realtime(limit = LOG_N)
    public boolean contains(Object obj) {
        return service().contains(obj);
    }

    @Realtime(limit = LOG_N)
    public boolean remove(Object obj) {
        return service().remove(obj);
    }

    @Override
    @Realtime(limit = LOG_N)
    public int indexOf(final Object obj) {
        return service().indexOf(obj);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /** 
     * Adds the specified element only if not already present.
     *  
     * @return {@code true} if the element has been added; 
     *         {@code false} otherwise.
     */
    @Realtime(limit = LOG_N)
    public boolean addIfAbsent(E element) {
        return service().addIfAbsent(element);
    }

    /** 
     * Returns the index of the specified element if present; or a negative 
     * number equals to {@code -n} with {@code n} being the index next to 
     * the "would be" index of the specified element if the specified element 
     * was to be added.
     */
    @Realtime(limit = LOG_N)
    public int positionOf(E element) {
        return service().positionOf(element);
    }

    @Override
    protected SortedTableService<E> service() {
        return (SortedTableService<E>) super.service();
    }

}