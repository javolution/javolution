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
import java.util.SortedSet;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.FastMap.KeySet;

/**
 * <p> Set backed up by an ordered {@link FastTable} and benefiting from the 
 *     same characteristics (memory footprint adjusted to current size,
 *     smooth capacity increase, etc).</p>
 * 
 * <p> Fast set, as for any {@link FastCollection} sub-class, supports
 *     closure-based iterations.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedSet<E> extends FastCollection<E> implements SortedSet<E> {

    /**
     * Holds the backing table.
     */
    private final FastTable table;

    /**
     * Creates an empty set whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastSortedSet() {
        table = new FastTable<E>() {

            @Override
            public FastComparator<E> comparator() {
                return FastSortedSet.this.comparator();
            }

        };
    }

    /**
     * Creates an empty set backed by the specified ordered table.
     */
    private FastSortedSet(FastTable table) {
        this.table = table;
    } 

    @Override
    public FastCollection<E> unmodifiable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FastCollection<E> shared() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FastCollection<E> usingComparator(FastComparator<E> comparator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<E, R> functor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SortedSet<E> headSet(E toElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SortedSet<E> tailSet(E fromElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public E first() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public E last() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
            
  
}