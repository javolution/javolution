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
import java.util.Map;
import java.util.Map.Entry;

import org.javolution.util.FastMap;
import org.javolution.util.FastSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An entry set view over a map.
 */
public final class EntrySetImpl<K, V> extends FastSet<Entry<K, V>> {

    /** The entry order (must support generic Map.Entry) */
    private static class EntryOrder<K, V> implements Order<Map.Entry<K, V>> {
        private static final long serialVersionUID = 0x700L; // Version.
        private final Order<? super K> keyOrder;
        private final Equality<? super V> valueEquality;

        public EntryOrder(Order<? super K> keyOrder, Equality<? super V> valueEquality) {
            this.keyOrder = keyOrder;
            this.valueEquality = valueEquality;
        }

        @Override
        public boolean areEqual(Map.Entry<K, V> left, Map.Entry<K, V> right) {
            if (left == null)
                return right == null;
            if (right == null)
                return false;
            return keyOrder.areEqual(left.getKey(), right.getKey())
                    && valueEquality.areEqual(left.getValue(), right.getValue());
        }

        @Override
        public int compare(Map.Entry<K, V> left, Map.Entry<K, V> right) {
            if (left == null)
                return (right == null) ? 0 : -1;
            if (right == null)
                return 1;
            return keyOrder.compare(left.getKey(), right.getKey());
        }

        @Override
        public int indexOf(Map.Entry<K, V> entry) {
            return entry != null ? keyOrder.indexOf(entry.getKey()) : 0;
        }

        @Override
        public Order<Map.Entry<K, V>> subOrder(Map.Entry<K, V> entry) {
            return entry != null ? new EntryOrder<K, V>(keyOrder.subOrder(entry.getKey()), valueEquality) : null;
        }

    }

    /** The generic iterator over the map entries (support entry removal). */
    private class EntryIterator implements Iterator<Entry<K,V>> {
        final Iterator<Entry<K, V>> mapItr;
        private Entry<K,V> next;

        public EntryIterator(Iterator<Entry<K, V>> mapItr) {
            this.mapItr = mapItr;
        }

        @Override
        public boolean hasNext() {
            return mapItr.hasNext();
        }

        @Override
        public Entry<K,V> next() {
            next = mapItr.next();
            return next;
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

    public EntrySetImpl(FastMap<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(Entry<K, V> element) {
        throw new UnsupportedOperationException("FastMap.entrySet() does not support adding new entries.");
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public FastSet<Entry<K, V>> clone() {
        return new EntrySetImpl<K, V>(map.clone());
    }

    @Override
    public Order<? super Entry<K, V>> comparator() {
        return new EntryOrder<K, V>(map.comparator(), map.valuesEquality());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Map.Entry))
            return false;
        Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
        Entry<K, V> mapEntry = map.getEntry(entry.getKey());
        return map.valuesEquality().areEqual(mapEntry.getValue(), entry.getValue());
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new EntryIterator(map.descendingIterator());
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(Entry<K, V> fromElement) {
        return  new EntryIterator(map.descendingIterator(fromElement.getKey()));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new EntryIterator(map.iterator());
    }

    @Override
    public Iterator<Entry<K, V>> iterator(Entry<K, V> fromElement) {
        return new EntryIterator(map.iterator(fromElement.getKey()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Map.Entry))
            return false;
        Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
        return map.remove(entry.getKey(), entry.getValue());
    }

    @Override
    public int size() {
        return map.size();
    }

}
