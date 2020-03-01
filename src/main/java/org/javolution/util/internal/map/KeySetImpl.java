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
import org.javolution.util.FastMap;
import org.javolution.util.FastMap.EntryWithoutValue;
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
    public boolean add(K element, boolean allowDuplicate) {
        throw new UnsupportedOperationException("New entries cannot be added through the keySet() view.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object element) {
        return map.removeEntry((K)element) != null;
    }

    @Override
    public boolean removeIf(final Predicate<? super K> filter) {
        return map.entries().removeIf(new Predicate<Entry<K,V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param.getKey());
            }});
    }

    @Override
    public FastIterator<K> iterator(K low) {
        return low != null ? new KeyIterator<K,V>(map.entries().iterator(new EntryWithoutValue<>(low))) :
            new KeyIterator<K,V>(map.entries().iterator(null));
    }

    @Override
    public FastIterator<K> descendingIterator(K high) {
        return high != null ? new KeyIterator<K,V>(map.entries().descendingIterator(new EntryWithoutValue<>(high))) :
            new KeyIterator<K,V>(map.entries().descendingIterator(null));
    }

    @Override
    public boolean isEmpty() {
        return map.entries().isEmpty();
    }

    @Override
    public int size() {
        return map.entries().size();
    }

    @Override
    public K getAny(K key) {
        Entry<K,V> entry = map.getEntry(key);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public K removeAny(K key) {
        Entry<K,V> entry = map.removeEntry(key);
        return entry != null ? entry.getKey() : null;
    }

}
