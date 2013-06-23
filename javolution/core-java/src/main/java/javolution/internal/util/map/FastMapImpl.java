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

import javolution.util.function.EqualityComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 * The default {@link javolution.util.FastMap FastMap} implementation 
 * based on {@link FractalMapImpl fractal maps}. 
 * This implementation ensures that no more than 3/4 of the map capacity is
 * ever wasted.
 */
public final class FastMapImpl<K, V> implements MapService<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private FractalMapImpl fractal = new FractalMapImpl();
    private final EqualityComparator<? super K> keyComparator;

    public FastMapImpl(EqualityComparator<? super K> keyComparator) {
        this.keyComparator = keyComparator;
    }

    @Override
    public void atomic(Runnable action) {
        action.run();
    }

    @Override
    public boolean containsKey(K key) {
        return fractal.containsKey(key, keyComparator.hashCodeOf(key));
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new EntrySetImpl<K, V>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        return (V) fractal.get(key, keyComparator.hashCodeOf(key));
    }

    @Override
    public SetService<K> keySet() {
        return new KeySetImpl<K, V>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        return (V) fractal.put(key, value, keyComparator.hashCodeOf(key));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(K key, V value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public V replace(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CollectionService<V> values() {
        return new ValuesImpl<K, V>(this);
    }

}
