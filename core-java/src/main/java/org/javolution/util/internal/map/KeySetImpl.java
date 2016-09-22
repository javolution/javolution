/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Iterator;
import java.util.Map.Entry;

import org.javolution.util.FastMap;
import org.javolution.util.FastSet;
import org.javolution.util.function.Order;

/**
 * A key set view over a map.
 */
public final class KeySetImpl<K, V> extends FastSet<K> {

    /** The generic iterator over the map keys. */
    private class KeyIterator implements Iterator<K> {
        final Iterator<Entry<K, V>> mapItr;
        private Entry<K,V> next;

        public KeyIterator(Iterator<Entry<K, V>> mapItr) {
            this.mapItr = mapItr;
        }

        @Override
        public boolean hasNext() {
            return mapItr.hasNext();
        }

        @Override
        public K next() {
            next = mapItr.next();
            return next.getKey();
        }

        @Override
        public void remove() {
            if (next == null) throw new IllegalArgumentException();
            map.remove(next.getKey());
            next = null;
        }
    }
    
    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> map;

    public KeySetImpl(FastMap<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(K key) {
        throw new UnsupportedOperationException("FastMap.keySet() does not support adding new keys.");
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public FastSet<K> clone() {
        return new KeySetImpl<K, V>(map.clone());
    }

    @Override
    public Order<? super K> comparator() {
        return map.comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public Iterator<K> descendingIterator() {
        return new KeyIterator(map.descendingIterator());
    }

    @Override
    public Iterator<K> descendingIterator(K fromElement) {
        return new KeyIterator(map.descendingIterator(fromElement));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator(map.iterator());
    }

    @Override
    public Iterator<K> iterator(K fromElement) {
        return new KeyIterator(map.iterator(fromElement));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        return map.removeEntry((K) obj) != null;
    }

    @Override
    public int size() {
        return map.size();
    }

}
