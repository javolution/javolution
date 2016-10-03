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
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An linked view over a map.
 */
public final class LinkedMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> inner;
    private final FastTable<Entry<K,V>> insertionTable;

    public LinkedMapImpl(FastMap<K, V> inner) {
        this.inner = inner;
        insertionTable = FastTable.newTable();
    }

    private LinkedMapImpl(FastMap<K, V> inner, FastTable<Entry<K,V>> insertionTable) {
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
    public Iterator<Entry<K, V>> descendingIterator() {
        return insertionTable.reversed().unmodifiable().iterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        int i = indexOfKey(fromKey);
        if (i < 0)
            throw new IllegalArgumentException("Not found: " + fromKey);
        return insertionTable.subTable(0, i+1).reversed().unmodifiable().iterator();
    }
    
    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return insertionTable.unmodifiable().iterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        int i = indexOfKey(fromKey);
        if (i < 0)
            throw new IllegalArgumentException("Not found: " + fromKey);
        return insertionTable.subTable(i, insertionTable.size()).unmodifiable().iterator();
    }

    @Override
    public V put(K key, V value) {
        Entry<K, V> entry = inner.getEntry(key);
        if (entry != null)
            return entry.setValue(value);
        inner.put(key, value);
        insertionTable.add(inner.getEntry(key));
        return null; 
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K, V> entry = inner.removeEntry(key);
        if (entry == null) return null;
        int i = indexOfKey(key);
        insertionTable.remove(i);
        return entry;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

    // Returns the index of the entry having the specified key.
    private int indexOfKey(K key) {
        final Order<? super K> cmp = inner.comparator();
        for (int i=0, n = insertionTable.size(); i < n; i++)
            if (cmp.areEqual(insertionTable.get(i).getKey(), key)) return i;
        return -1;
    }

}
