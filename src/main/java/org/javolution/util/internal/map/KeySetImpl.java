/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractMap.Entry;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A key set view over a map.
 */
public final class KeySetImpl<K, V> extends AbstractSet<K> {

    /** The generic iterator over the map keys. */
    private static class KeyIterator<K,V> implements FastIterator<K> {
        final FastIterator<Entry<K, V>> mapItr;

        public KeyIterator(FastIterator<Entry<K, V>> iterator) {
            this.mapItr = iterator;
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
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext(final Predicate<? super K> matching) {
            return mapItr.hasNext(new Predicate<Entry<K,V>>() {

                @Override
                public boolean test(Entry<K, V> param) {
                    return matching.test(param.getKey());
                }});
        }
    }
    
    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K, V> map;

    public KeySetImpl(AbstractMap<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(K key) {
        if (map.containsKey(key)) return false;
        map.put(key, null);
        return true;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public AbstractSet<K> clone() {
        return new KeySetImpl<K, V>(map.clone());
    }

    @Override
    public Order<? super K> order() {
        return map.keyOrder();
    }

    @Override
    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public FastIterator<K> descendingIterator() {
        return new KeyIterator<K,V>(map.descendingIterator());
    }

    @Override
    public FastIterator<K> descendingIterator(K fromElement) {
        return new KeyIterator<K,V>(map.descendingIterator(fromElement));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public FastIterator<K> iterator() {
        return new KeyIterator<K,V>(map.iterator());
    }

    @Override
    public FastIterator<K> iterator(K fromElement) {
        return new KeyIterator<K,V>(map.iterator(fromElement));
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

    @Override
    public boolean removeIf(final Predicate<? super K> filter) {
        return map.removeIf(new Predicate<Entry<K,V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param.getKey());
            }});
    }

}
