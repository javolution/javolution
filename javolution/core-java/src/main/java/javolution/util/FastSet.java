/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.map.CustomKeyComparatorMapImpl;
import javolution.internal.util.map.FractalMapImpl;
import javolution.internal.util.map.SharedMapImpl;
import javolution.internal.util.map.UnmodifiableMapImpl;
import javolution.internal.util.table.FractalTableImpl;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.FastMap.KeySet;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.TableService;

/**
 * <p> Set backed up by a fractal map and benefiting from the 
 *     same characteristics (memory footprint adjusted to current size,
 *     smooth capacity increase, etc).</p>
 * 
 * <p> Fast set, as for any {@link FastCollection} sub-class, supports
 *     closure-based iterations.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSet<E> extends FastCollection<E> implements Set<E> {

    /**
     * Holds the actual map service implementation.
     */
    private final MapService<E, Void> service;

    /**
     * Creates an empty set whose capacity increments/decrements smoothly
     * without large resize operations to best fit the set current size.
     */
    public FastSet() {
        service = new FractalMapImpl<E, Void>();
    }

    /**
     * Creates a set backed up by the specified implementation.
     */
    protected FastSet(MapService<E, Void> service) {
        this.service = service;
    } 

    @Override
    public FastSet<E> unmodifiable() {
        return new FastSet<E>(new UnmodifiableMapImpl<E, Void>(service));
    }

    @Override
    public FastSet<E> shared() {
        return new FastSet<E>(new SharedMapImpl<E, Void>(service, new ReentrantReadWriteLock()));
    }

    public FastSet<E> usingComparator(FastComparator<E> comparator) {
        return new FastSet<E>(new CustomKeyComparatorMapImpl<E, Void>(service, comparator));
    }

    @Override
    protected CollectionService<E> getService() {
        return service.keySet();
    }

}