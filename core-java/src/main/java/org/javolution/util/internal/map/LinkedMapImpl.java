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
public final class LinkedMapImpl<K,V> extends FastMap<K,V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K,V> inner;
    private final FastTable<K> insertionTable;

    public LinkedMapImpl(FastMap<K,V> inner) {
        this.inner = inner;
        insertionTable = FastTable.newTable(inner.comparator()); 
    }

    private LinkedMapImpl(FastMap<K,V> inner, FastTable<K> insertionTable) {
        this.inner = inner;
        this.insertionTable = insertionTable;
    }

    @Override
    public void clear() {
        inner.clear();
        insertionTable.clear();
    }

    @Override
    public LinkedMapImpl<K,V> clone() {
        return new LinkedMapImpl<K,V>(inner.clone(), insertionTable.clone());
    }

    @Override
    public Entry<K,V> putEntry(K key, V value) {
        Entry<K,V> previous = inner.putEntry(key, value);
        if (previous == null)
            insertionTable.add(key);
        return previous;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new KeyToEntryIterator(insertionTable.iterator());
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new KeyToEntryIterator(insertionTable.reversed().iterator());
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        int index = insertionTable.indexOf(fromKey);
        if (index < 0) throw new IllegalArgumentException("Not found: " + fromKey);
        return new KeyToEntryIterator(insertionTable.listIterator(index));
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        FastTable<K> reversedTable = insertionTable.reversed();
        int index = reversedTable.indexOf(fromKey);
        if (index < 0) throw new IllegalArgumentException("Not found: " + fromKey);
        return new KeyToEntryIterator(reversedTable.listIterator(index));
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        return inner.removeEntry(key);
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

    @Override
    public Order<? super K> comparator() {
        return inner.comparator();
    }

    /** Read-only entry iterator view insertion table */ 
    private class KeyToEntryIterator implements Iterator<Entry<K,V>> {
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
        
        @Override
        public void remove() {
            keyIterator.remove();
            insertionTable.remove(nextKey);
        }
    }

    @Override
    public int size() {
        return insertionTable.size();
    }

    @Override
    public boolean isEmpty() {
        return insertionTable.isEmpty();
    }
}
