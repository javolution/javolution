/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;

import java.util.Set;

import javolution.lang.Realtime;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;
import javolution.util.internal.map.FastMapImpl;
import javolution.util.internal.set.AtomicSetImpl;
import javolution.util.internal.set.FilteredSetImpl;
import javolution.util.internal.set.SharedSetImpl;
import javolution.util.internal.set.UnmodifiableSetImpl;
import javolution.util.service.SetService;

/**
 * <p> A high-performance hash set with {@link Realtime real-time} behavior.</p>
 *     
 * <p> The iteration order over the set elements is deterministic 
 *     (unlike {@link java.util.HashSet}).It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedSet} subclass.
 *     This class permits {@code null} elements.</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
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
        this(Equalities.STANDARD);
    }

    /**
     * Creates an empty set backed up by a {@link FastMap} and using the 
     * specified comparator for key equality.
    */
    public FastSet(Equality<? super E> comparator) {
        service = new FastMapImpl<E, Void>(comparator, Equalities.IDENTITY)
                .keySet();
    }

    /**
      * Creates a fast set backed up by the specified service implementation.
      */
    protected FastSet(SetService<E> service) {
        this.service = service;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
    public FastSet<E> atomic() {
        return new FastSet<E>(new AtomicSetImpl<E>(service()));
    }

    @Override
    public FastSet<E> filtered(final Predicate<? super E> filter) {
        return new FastSet<E>(new FilteredSetImpl<E>(service(), filter));
    }

    @Override
    public FastSet<E> shared() {
        return new FastSet<E>(new SharedSetImpl<E>(service()));
    }

    @Override
    public FastSet<E> unmodifiable() {
        return new FastSet<E>(new UnmodifiableSetImpl<E>(service()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Set operations new annotations.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean contains(Object obj) {
        return service.contains(obj);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean remove(Object obj) {
        return service.remove(obj);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    @Override
    public FastSet<E> addAll(E... elements) {
        return (FastSet<E>) super.addAll(elements);
    }

    @Override
    public FastSet<E> addAll(FastCollection<? extends E> that) {
        return (FastSet<E>) super.addAll(that);
    }

    @Override
    protected SetService<E> service() {
        return service;
    }
}