/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Iterator;

import org.javolution.util.FastMap.Entry;
import org.javolution.util.function.Order;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> 
 *     implementation of {@link FastSet}.</p> 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see SparseMap
 */
public class SparseSet<E> extends FastSet<E> {

    /** Generic iterator over the map keys */
    private static class KeyIterator<K, V> implements Iterator<K> {
        final Iterator<Entry<K, V>> mapItr;

        public KeyIterator(Iterator<Entry<K, V>> mapItr) {
            this.mapItr = mapItr;
        }

        @Override
        public boolean hasNext() {
            return mapItr.hasNext();
        }

        @Override
        public K next() {
            return mapItr.next().getKey();
        }

        @Override
        public void remove() {
            mapItr.remove();
        }

    }
    private static final long serialVersionUID = 0x700L; // Version.
    private static final Object PRESENT = new Object();
    private final SparseMap<E, Object> sparse;

    /**
     * Creates an empty set arbitrarily ordered (hash based).
     */
    public SparseSet() {
        this(Order.DEFAULT);
    }

    /**
     * Creates an empty set ordered using the specified comparator.
     * 
     * @param order the ordering of the set.
     */
    public SparseSet(Order<? super E> order) {
        sparse = new SparseMap<E, Object>(order);
    }

    /** Structural constructor (for cloning) */
    private SparseSet(SparseMap<E, Object> sparse) {
        this.sparse = sparse;
    }

    @Override
    public boolean add(E element) {
        return sparse.put(element, PRESENT) == null;
    }

    @Override
    public void clear() {
        sparse.clear();
    }

    @Override
    public SparseSet<E> clone() {
        return new SparseSet<E>(sparse.clone());
    }

    @Override
    public Order<? super E> comparator() {
        return sparse.comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return sparse.containsKey(obj);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new KeyIterator<E, Object>(sparse.descendingIterator());
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return new KeyIterator<E, Object>(sparse.descendingIterator(fromElement));
    }

    @Override
    public boolean isEmpty() {
        return sparse.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new KeyIterator<E, Object>(sparse.iterator());
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return new KeyIterator<E, Object>(sparse.iterator(fromElement));
    }

    @Override
    public boolean remove(Object obj) {
        return sparse.remove(obj) != null;
    }

    @Override
    public int size() {
        return sparse.size();
    }

}