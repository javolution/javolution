/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Set;

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.map.FractalMapImpl;
import javolution.util.service.CollectionService;

/**
 * <p> A customizable set with real-time behavior; smooth capacity increase and 
 *     <i>thread-safe</i> behavior without external synchronization when
 *     {@link #shared shared}. The set capacity of the default implementation
 *     is automatically adjusted to best fit its size (e.g. when the set is 
 *     cleared its memory footprint is minimal).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSet<E> extends FastCollection<E> implements Set<E> {
  
    /**
     * Holds the actual implementation.
     */
    private final CollectionService<E> impl;

    /**
     * Creates a default set whose capacity increments/decrements smoothly
     * without large resize operations to best fit the set current size.
     */
    public FastSet() {
        impl = new FractalMapImpl<E, Void>().keySet();
    }

    /**
     * Creates a set backed up by the specified implementation.
     */
    protected FastSet(CollectionService<E> service) {
        this.impl = service;
    } 

    @Override
    public FastSet<E> unmodifiable() {
        return new FastSet<E>(new UnmodifiableCollectionImpl<E>(impl));
    }

    @Override
    public FastSet<E> shared() {
        return new FastSet<E>(new SharedCollectionImpl<E>(impl));
    }

    @Override
    public FastSet<E> setComparator(FastComparator<E> cmp) {
        super.setComparator(cmp);
        return this;
    }

    @Override
    protected CollectionService<E> getService() {
        return impl;
    }

    private static final long serialVersionUID = 6795336745095625093L;

}