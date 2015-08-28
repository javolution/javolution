/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import javolution.lang.Realtime;
import javolution.util.FastCollection;
import javolution.util.FastMap;
import javolution.util.FastSortedSet;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;
import javolution.util.internal.map.FastIdentityMapImpl;
import javolution.util.internal.map.FastMapImpl;
import javolution.util.internal.set.AtomicSetImpl;
import javolution.util.internal.set.FilteredSetImpl;
import javolution.util.internal.set.SharedSetImpl;
import javolution.util.internal.set.UnmodifiableSetImpl;

import java.util.Collection;
import java.util.Set;

import static javolution.lang.Realtime.Limit.CONSTANT;

public class FastIdentitySet<E> extends FastCollection<E> implements Set<E> {

    private static final long serialVersionUID = 0x620L; // Version.

    /**
     * Holds the actual service implementation.
     */
    private final SetService<E> service;

    /**
     * Returns a new set holding the specified elements
     * (convenience method).
     * @param <E> Type of the FastSet
     * @param elements Elements to create a FastSet of
     * @return FastSet containing the specified elements
     */
    public static <E> FastIdentitySet<E> of(E... elements) {
    	FastIdentitySet<E> set = new FastIdentitySet<E>();
    	for (E e : elements) set.add(e);
        return set;
    }

    /**
     * Returns a new set holding the same elements as the specified
     * collection (convenience method).
     * @param <E> Type of the FastSet
     * @param that Collection to convert into a FastSet
     * @return FastSet containing the elements in the specified collection
     */
    public static <E> FastIdentitySet<E> of(Collection<? extends E> that) {
    	FastIdentitySet<E> set = new FastIdentitySet<E>();
    	set.addAll(that);
        return set;
    }

    /**
     * Creates an empty set backed up by a {@link FastMap} and having
     * the same real-time characteristics.
     */
    public FastIdentitySet() {
        service = new FastIdentityMapImpl<E, Void>().keySet();
    }

    /**
      * Creates a fast set backed up by the specified service implementation.
      * @param service SetService to back the FastSet with
      */
    protected FastIdentitySet(SetService<E> service) {
        this.service = service;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
    public FastIdentitySet<E> atomic() {
        return new FastIdentitySet<E>(new AtomicSetImpl<E>(service()));
    }

    @Override
    public FastIdentitySet<E> filtered(final Predicate<? super E> filter) {
        return new FastIdentitySet<E>(new FilteredSetImpl<E>(service(), filter));
    }

    @Override
    public FastIdentitySet<E> shared() {
        return new FastIdentitySet<E>(new SharedSetImpl<E>(service()));
    }

    @Override
    public FastIdentitySet<E> unmodifiable() {
        return new FastIdentitySet<E>(new UnmodifiableSetImpl<E>(service()));
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
    protected SetService<E> service() {
        return service;
    }
}