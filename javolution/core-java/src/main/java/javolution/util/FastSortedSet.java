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
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.internal.util.map.CustomKeyComparatorMapImpl;
import javolution.internal.util.map.SharedMapImpl;
import javolution.internal.util.map.UnmodifiableMapImpl;
import javolution.internal.util.table.CustomComparatorTableImpl;
import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.NoDuplicateTableImpl;
import javolution.internal.util.table.SetTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SortedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.FastMap.KeySet;
import javolution.util.function.Functor;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.TableService;

/**
 * <p> A set backed up by an ordered table and benefiting from the 
 *     same characteristics (memory footprint adjusted to current size,
 *     smooth capacity increase, etc).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedSet<E> extends FastCollection<E> implements SortedSet<E> {

    /**
     * Holds the backing table.
     */
    private final SetTableImpl<E> service;

    /**
     * Creates an empty set whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastSortedSet() {
        service = new SetTableImpl<E>(new FractalTableImpl<E>());
    }

    /**
     * Creates a sorted set backed up by the specified table implementation.
     */
    protected FastSortedSet(TableService<E> service) {
        this.service = new SetTableImpl<E>(service);
    } 
    
    @Override
    public FastSortedSet<E> unmodifiable() {
        return new FastSortedSet<E>(new UnmodifiableTableImpl<E>(service));
    }

    @Override
    public FastSortedSet<E> shared() {
        return new FastSortedSet<E>(new SharedTableImpl<E>(service));
    }
    
    @Override
    public FastSortedSet<E> setComparator(FastComparator<E> cmp) {
        super.setComparator(cmp);
        return this;
    }
    
    @Override
    protected TableService<E> getService() {
        return service;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Comparator<? super E> comparator() {
        return (Comparator<? super E>) service.getComparator();
    }

    @Override
    public FastSortedSet<E> subSet(E fromElement, E toElement) {
        int fromIndex = service.indexOf(fromElement);
        int toIndex = service.indexOf(toElement);
        return new FastSortedSet<E>(
                    new SubTableImpl<E>(service, fromIndex, toIndex));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toIndex = service.indexOf(toElement);
        return new FastSortedSet<E>(
                new SubTableImpl<E>(service, 0, toIndex));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        to fix
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E first() {
        return service.getFirst();
    }

    @Override
    public E last() {
        return service.getLast();
    }
 
}