/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.annotation.RealTime.Limit.CONSTANT;

import java.util.Set;

import javolution.annotation.RealTime;
import javolution.internal.util.set.FilteredSetImpl;
import javolution.internal.util.set.SharedSetImpl;
import javolution.internal.util.set.UnmodifiableSetImpl;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.SetService;

/**
 * <p> A high-performance set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p> The iteration order over the set elements is deterministic 
 *     (unlike {@link java.util.HashSet}).It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedSet} subclass.
 *     This class permits {@code null} elements.</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSet<E> extends FastCollection<E> implements Set<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the actual service implementation.
     */
    private final SetService<E> service;

    /**
     * Creates an empty set backed up by a {@link FastMap} and having  
     * the same real-time characteristics.
     */
    public FastSet() {
        this(Comparators.STANDARD);
    }

    /**
     * Creates an empty set backed up by a {@link FastMap} and using the 
     * specified comparator for key equality.
    */
    public FastSet(EqualityComparator<? super E> comparator) {
        service = null; // TODO
    }

    /**
      * Creates a fast set backed up by the specified service implementation.
      */
    protected FastSet(SetService<E> service) {
        this.service = service;
    }

    /***************************************************************************
     * Views.
     */

    @Override
    public FastSet<E> unmodifiable() {
        return new FastSet<E>(new UnmodifiableSetImpl<E>(service()));
    }

    @Override
    public FastSet<E> shared() {
        return new FastSet<E>(new SharedSetImpl<E>(service()));
    }

    @Override
    public FastSet<E> filtered(final Predicate<? super E> filter) {
        return new FastSet<E>(new FilteredSetImpl<E>(service(), filter));
    }

    @Override
    public FastSet<E> distinct() {
        return this; // Elements already distinct.
    }

    /***************************************************************************
     * Set operations optimizations.
     */

    @Override
    @RealTime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    @Override
    @RealTime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(limit = CONSTANT)
    public boolean contains(Object obj) {
        return service.contains((E) obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(limit = CONSTANT)
    public boolean remove(Object obj) {
        return service.remove((E) obj);
    }

    /***************************************************************************
     * Misc.
     */

    @Override
    protected SetService<E> service() {
        return service;
    }
}