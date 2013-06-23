/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.annotation.RealTime.Limit.LOG_N;

import java.util.SortedSet;

import javolution.annotation.RealTime;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.service.SortedSetService;

/**
 * <p> A high-performance sorted set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
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
        // TODO
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
        return null; // TODO
    }

    @Override
    public FastSortedSet<E> shared() {
        return null; // TODO
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
    public SortedSet<E> subSet(E fromElement, E toElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @RealTime(limit = LOG_N)
    public SortedSet<E> headSet(E toElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @RealTime(limit = LOG_N)
    public SortedSet<E> tailSet(E fromElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E first() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E last() {
        // TODO Auto-generated method stub
        return null;
    }

    /***************************************************************************
     * Misc.
     */

    @Override
    protected SortedSetService<E> service() {
        return (SortedSetService<E>) super.service();
    }

}