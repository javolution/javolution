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
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.FastMap.KeySet;

/**
 * <p> Set backed up by a {@link HashMap} and benefiting from the 
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
     * Holds the backing map.
     */
    private final FastMap map;

    /**
     * Creates an empty set whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastSet() {
        map = new FastMap<E, E>() {

            @Override
            public FastComparator<E> keyComparator() {
                return FastSet.this.comparator();
            }

        };
    }

    @Override
    public FastCollection<E> unmodifiable() {
        return map.keySet().unmodifiable();
    }

    @Override
    public KeySet<E> shared() {
        return map.shared().keySet();
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<E, R> functor) {
        return map.keySet().forEach(functor);
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        map.keySet().doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return map.keySet().removeAll(predicate);
    }
    
    //
    // Collection methods.
    //
    
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(E value) {
        return map.put(value, value) == null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public FastCollection<E> usingComparator(FastComparator<E> comparator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}