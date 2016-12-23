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

import org.javolution.util.ConstMap;
import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An linked view over a map.
 */
public final class LinkedMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private FastMap<K, V> inner;
    private FastTable<Entry<K, V>> insertionTable = new FractalTable<Entry<K, V>>(Equality.IDENTITY);

    public LinkedMapImpl(FastMap<K, V> inner) {
        this.inner = inner;
    }

    @Override
    public void clear() {
        inner.clear();
        insertionTable.clear();
    }

    @Override
    public LinkedMapImpl<K, V> clone() {
        LinkedMapImpl<K, V> copy = (LinkedMapImpl<K, V>) super.clone();
        copy.inner = inner.clone();
        copy.insertionTable = insertionTable.clone();
        return copy;
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return insertionTable.reversed().unmodifiable().iterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        Entry<K, V> start = inner.floorEntry(fromKey);
        if (start == null)
            return ConstMap.<K, V>empty().iterator();
        FastTable<Entry<K, V>> reversedTable = insertionTable.reversed();
        int index = reversedTable.indexOf(start);
        if (index < 0)
            throw new AssertionError();
        return reversedTable.unmodifiable().listIterator(index);
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    // Returns the index of the specified entry.
    private int indexOf(Entry<K, V> entry) {
        for (int i = 0, n = insertionTable.size(); i < n; i++)
            if (insertionTable.get(i) == entry)
                return i;
        return -1;
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
        Entry<K, V> start = inner.ceilingEntry(fromKey);
        if (start == null)
            return ConstMap.<K, V>empty().iterator();
        int index = insertionTable.indexOf(start);
        if (index < 0)
            throw new AssertionError();
        return insertionTable.unmodifiable().listIterator(index);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public V put(K key, V value) {
        Entry<K, V> entry = new Entry<K,V>(key, value);
        Entry<K, V> previous = putEntry(entry);
        return (previous != null) ? previous.getValue() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> putEntry(Entry<? extends K, ? extends V> entry) {
        Entry<K, V> previous = inner.putEntry(entry);
        if (previous != null) {
            int i = indexOf(previous);
            insertionTable.set(i, (Entry<K, V>) entry);
        } else {
            insertionTable.add((Entry<K, V>) entry);
        }
        return previous;
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K, V> previous = inner.removeEntry(key);
        if (previous != null)
            insertionTable.remove(indexOf(previous));
        return previous;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

}
