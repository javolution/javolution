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

import java.util.SortedSet;

import javolution.lang.Realtime;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.map.sorted.FastSortedMapImpl;
import javolution.util.internal.set.sorted.AtomicSortedSetImpl;
import javolution.util.internal.set.sorted.SharedSortedSetImpl;
import javolution.util.internal.set.sorted.UnmodifiableSortedSetImpl;
import javolution.util.service.SortedSetService;

/**
 * <p> A high-performance sorted set with {@link Realtime real-time} behavior.</p>
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
        this(Equalities.STANDARD);
    }

    /**
    * Creates an empty sorted set ordered using the specified comparator.
    */
    public FastSortedSet(Equality<? super E> comparator) {
        super(new FastSortedMapImpl<E, Void>(comparator, Equalities.IDENTITY)
                .keySet());
    }

    /**
     * Creates a sorted set backed up by the specified service implementation.
     */
    protected FastSortedSet(SortedSetService<E> service) {
        super(service);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
    public FastSortedSet<E> atomic() {
        return new FastSortedSet<E>(new AtomicSortedSetImpl<E>(service()));
    }

    @Override
    public FastSortedSet<E> shared() {
        return new FastSortedSet<E>(new SharedSortedSetImpl<E>(service()));
    }

    @Override
    public FastSortedSet<E> unmodifiable() {
        return new FastSortedSet<E>(new UnmodifiableSortedSetImpl<E>(service()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

    @Override
    @Realtime(limit = LOG_N)
    public boolean add(E e) {
        return super.add(e);
    }

    @Override
    @Realtime(limit = LOG_N)
    public boolean contains(Object obj) {
        return super.contains(obj);
    }

    @Override
    @Realtime(limit = LOG_N)
    public boolean remove(Object obj) {
        return super.remove(obj);
    }

    ////////////////////////////////////////////////////////////////////////////
    // SortedSet Interface.
    //

    /** Returns a view of the portion of this set whose elements range from fromElement, inclusive, to toElement, exclusive. */
    @Override
    @Realtime(limit = LOG_N)
    public FastSortedSet<E> subSet(E fromElement, E toElement) {
        return new FastSortedSet<E>(service().subSet(fromElement, toElement));
    }

    /** Returns a view of the portion of this set whose elements are strictly less than toElement. */
    @Override
    @Realtime(limit = LOG_N)
    public FastSortedSet<E> headSet(E toElement) {
        return subSet(first(), toElement);
    }

    /** Returns a view of the portion of this set whose elements are greater than or equal to fromElement. */
    @Override
    @Realtime(limit = LOG_N)
    public FastSortedSet<E> tailSet(E fromElement) {
        return subSet(fromElement, last());
    }

    /** Returns the first (lowest) element currently in this set. */
    @Override
    public E first() {
        return service().first();
    }

    /** Returns the last (highest) element currently in this set. */
    @Override
    public E last() {
        return service().last();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    @Override
    public FastSortedSet<E> addAll(E... elements) {
        return (FastSortedSet<E>) super.addAll(elements);
    }

    @Override
    public FastSortedSet<E> addAll(FastCollection<? extends E> that) {
        return (FastSortedSet<E>) super.addAll(that);
    }

    @Override
    protected SortedSetService<E> service() {
        return (SortedSetService<E>) super.service();
    }

}