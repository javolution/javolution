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
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * A map with custom equality for its values.
 */
public final class CustomValuesEqualityMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> inner;
    private Equality<? super V> valuesEquality;

    public CustomValuesEqualityMapImpl(FastMap<K, V> inner, Equality<? super V> valuesEquality) {
        this.inner = inner;
        this.valuesEquality = valuesEquality;
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public FastMap<K, V> clone() {
        return new CustomValuesEqualityMapImpl<K, V>(inner, valuesEquality);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return inner.descendingIterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
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
    public Iterator<Entry<K, V>> iterator() {
        return inner.iterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        return inner.iterator(fromKey);
    }

    @Override
    public Entry<K,V> putEntry(Entry<? extends K, ? extends V> entry) {
        return inner.putEntry(entry);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        return inner.removeEntry(key);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return valuesEquality;
    }

    @Override
    public V put(K key, V value) {
        return inner.put(key, value);
    }

}
