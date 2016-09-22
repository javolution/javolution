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

import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An linked view over a map.
 */
public final class LinkedMapImpl<K, V> extends FastMap<K, V> {

    /** Read-only entry iterator view insertion table */
    private class KeyToEntryIterator extends ReadOnlyIterator<Entry<K, V>> {
        final Iterator<K> keyIterator;
        K nextKey;

        public KeyToEntryIterator(Iterator<K> keyIterator) {
            this.keyIterator = keyIterator;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            nextKey = keyIterator.next();
            return inner.getEntry(nextKey);
        }

    }
    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> inner;

    private final FastTable<K> insertionTable;

    public LinkedMapImpl(FastMap<K, V> inner) {
        this.inner = inner;
        insertionTable = FastTable.newTable(inner.comparator());
    }

    private LinkedMapImpl(FastMap<K, V> inner, FastTable<K> insertionTable) {
        this.inner = inner;
        this.insertionTable = insertionTable;
    }

    @Override
    public void clear() {
        inner.clear();
        insertionTable.clear();
    }

    @Override
    public LinkedMapImpl<K, V> clone() {
        return new LinkedMapImpl<K, V>(inner.clone(), insertionTable.clone());
    }

    @Override
    public Order<? super K> comparator() {
        return inner.comparator();
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> descendingIterator() {
        return new KeyToEntryIterator(insertionTable.reversed().iterator());
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> descendingIterator(K fromKey) {
        FastTable<K> reversedTable = insertionTable.reversed();
        int index = reversedTable.indexOf(fromKey);
        if (index < 0)
            throw new IllegalArgumentException("Not found: " + fromKey);
        return new KeyToEntryIterator(reversedTable.listIterator(index));
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public boolean isEmpty() {
        return insertionTable.isEmpty();
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> iterator() {
        return new KeyToEntryIterator(insertionTable.iterator());
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> iterator(K fromKey) {
        int index = insertionTable.indexOf(fromKey);
        if (index < 0)
            throw new IllegalArgumentException("Not found: " + fromKey);
        return new KeyToEntryIterator(insertionTable.listIterator(index));
    }

    @Override
    public V put(K key, V value) {
        Entry<K, V> entry = inner.getEntry(key);
        if (entry != null)
            return entry.setValue(value);
        insertionTable.add(key);
        return inner.put(key, value); // Returns null.
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K, V> entry = inner.removeEntry(key);
        if (entry != null)
            insertionTable.remove(key);
        return entry;
    }

    @Override
    public int size() {
        return insertionTable.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }
}
