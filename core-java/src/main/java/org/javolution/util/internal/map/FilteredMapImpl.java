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
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.FilteredCollectionImpl.FilteredIterator;

/**
 * A filtered view over a map.
 */
public final class FilteredMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Predicate<? super K> keyFilter;
    private final Predicate<? super Entry<K, V>> entryFilter;
    private final FastMap<K, V> inner;

    public FilteredMapImpl(FastMap<K, V> inner, final Predicate<? super K> keyFilter) {
        this.inner = inner;
        this.keyFilter = keyFilter;
        this.entryFilter = new Predicate<Entry<K, V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return keyFilter.test(param.getKey());
            }
        };
    }

    @Override
    public void clear() {
        entrySet().removeIf(Predicate.TRUE);
    }

    @Override
    public FastMap<K, V> clone() {
        return new FilteredMapImpl<K, V>(inner.clone(), keyFilter);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new FilteredIterator<Entry<K, V>>(inner.descendingIterator(), entryFilter);
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new FilteredIterator<Entry<K, V>>(inner.descendingIterator(fromKey), entryFilter);
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return keyFilter.test(key) ? inner.getEntry(key) : null;
    }

    @Override
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new FilteredIterator<Entry<K, V>>(inner.iterator(), entryFilter);
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        return new FilteredIterator<Entry<K, V>>(inner.iterator(fromKey), entryFilter);
    }

    @Override
    public V put(K key, V value) {
        return keyFilter.test(key) ? inner.put(key, value) : null;
    }

    @Override
    public Entry<K,V> putEntry(Entry<? extends K, ? extends V> entry) {
        return keyFilter.test(entry.getKey()) ? inner.putEntry(entry) : null;
    }
    
    @Override
    public Entry<K, V> removeEntry(K key) {
        return keyFilter.test(key) ? inner.removeEntry(key) : null;
    }

    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

}
