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
import org.javolution.util.FastIterator;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An linked view over a map.
 */
public final class LinkedMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private AbstractMap<K, V> inner;
    private FastTable<Entry<K, V>> insertionTable;
 
    public LinkedMapImpl(AbstractMap<K,V> inner) {
        this.inner = inner;
        this.insertionTable = new FastTable<Entry<K,V>>();
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
    public FastIterator<Entry<K, V>> descendingIterator() {
        return insertionTable.reversed().unmodifiable().iterator();
    }

    @Override
    public FastIterator<Entry<K, V>> descendingIterator(K fromKey) {
        return inner.descendingIterator(fromKey);
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
    public FastIterator<Entry<K, V>> iterator() {
        return insertionTable.iterator();
    }

    @Override
    public FastIterator<Entry<K, V>> iterator(K fromKey) {
        return inner.iterator(fromKey);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public boolean addEntry(Entry<K, V> entry) {        
        insertionTable.add(entry);
        return inner.addEntry(entry);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K, V> previous = inner.removeEntry(key);
        if (previous != null) insertionTable.equality(Equality.IDENTITY).remove(previous);
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
