/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map;

import java.io.Serializable;
import java.util.Map.Entry;

import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.MapService;

/**
 *  * An unmodifiable view over a map.
 */
public final class UnmodifiableMapImpl<K, V> implements MapService<K, V>, Serializable {

    private final MapService<K,V> that;

    public UnmodifiableMapImpl(MapService<K,V> that) {
        this.that = that;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean containsKey(K key) {
        return that.containsKey(key);
    }

    @Override
    public V get(K key) {
        return that.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public CollectionService<Entry<K, V>> entrySet() {
        return new UnmodifiableCollectionImpl<Entry<K, V>>(that.entrySet());
    }

    @Override
    public CollectionService<V> values() {
        return new UnmodifiableCollectionImpl<V>(that.values());
    }

    @Override
    public CollectionService<K> keySet() {
        return new UnmodifiableCollectionImpl<K>(that.keySet());
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean remove(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V replace(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public ComparatorService<K> keyComparator() {
        return that.keyComparator();
    }
    
    private static final long serialVersionUID = -654546354439540409L;
 }
