/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.RealTime.Limit.LOG_N;

import java.util.SortedSet;

import javolution.internal.util.map.sorted.FastSortedMapImpl;
import javolution.internal.util.set.sorted.SharedSortedSetImpl;
import javolution.internal.util.set.sorted.UnmodifiableSortedSetImpl;
import javolution.lang.RealTime;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.service.SortedSetService;

/**
 * <p> A high-performance sorted set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class FastSortedSet<E> extends FastSet<E> implements SortedSet<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Creates an empty sorted set ordered on elements natural order.
     */
    public FastSortedSet() {
        this(Comparators.STANDARD);
    }

    /**
    * Creates an empty sorted set ordered using the specified comparator.
    */
    public FastSortedSet(EqualityComparator<? super E> comparator) {
        super(new FastSortedMapImpl<E, Void>(comparator, Comparators.IDENTITY).keySet());
    }

    /**
     * Creates a sorted set backed up by the specified service implementation.
     */
    protected FastSortedSet(SortedSetService<E> service) {
        super(service);
    }

    /***************************************************************************
     * Views.
     */

    @Override
    public FastSortedSet<E> unmodifiable() {
        return new FastSortedSet<E>(new UnmodifiableSortedSetImpl<E>(service()));
    }

    @Override
    public FastSortedSet<E> shared() {
        return new FastSortedSet<E>(new SharedSortedSetImpl<E>(service()));
    }

    /***************************************************************************
     * FastSet operations with different time limit behavior.
     */

    @Override
    @RealTime(limit = LOG_N)
    public boolean add(E e) {
        return super.add(e);
    }

    @Override
    @RealTime(limit = LOG_N)
    public boolean contains(Object obj) {
        return super.contains(obj);
    }

    @Override
    @RealTime(limit = LOG_N)
    public boolean remove(Object obj) {
        return super.remove(obj);
    }

    /***************************************************************************
     * SortedSet operations.
     */

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedSet<E> subSet(E fromElement, E toElement) {
        return new FastSortedSet<E>(service().subSet(fromElement, toElement));
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedSet<E> headSet(E toElement) {
        return subSet(first(), toElement);
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedSet<E> tailSet(E fromElement) {
        return subSet(fromElement, last());
    }

    @Override
    public E first() {
        return service().first();
    }

    @Override
    public E last() {
        return service().last();
    }

    /***************************************************************************
     * Misc.
     */

    @Override
    public FastSortedSet<E> addAll(E... elements) {
        return (FastSortedSet<E>) super.addAll(elements);
    }

    @Override
    protected SortedSetService<E> service() {
        return (SortedSetService<E>) super.service();
    }

}