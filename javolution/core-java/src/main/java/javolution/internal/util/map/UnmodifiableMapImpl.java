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
import javolution.internal.util.set.UnmodifiableSetImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 *  * An unmodifiable view over a map.
 */
public class UnmodifiableMapImpl<K, V> implements MapService<K, V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    protected final MapService<K, V> target;

    public UnmodifiableMapImpl(MapService<K, V> target) {
        this.target = target;
    }

    @Override
    public void atomic(Runnable update) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean containsKey(K key) {
        return target.containsKey(key);
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new UnmodifiableSetImpl<Entry<K, V>>(target.entrySet());
    }

    @Override
    public V get(K key) {
        return target.get(key);
    }

    @Override
    public SetService<K> keySet() {
        return new UnmodifiableSetImpl<K>(target.keySet());
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V remove(K key) {
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
    public int size() {
        return target.size();
    }

    @Override
    public CollectionService<V> values() {
        return new UnmodifiableCollectionImpl<V>(target.values());
    }
}
