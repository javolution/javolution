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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 *  * A shared view over a map.
 */
public final class SharedMapImpl<K, V> implements MapService<K, V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Lock read;
    private final MapService<K, V> target;
    private final Lock write;

    public SharedMapImpl(MapService<K, V> target) {
        this.target = target;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    @Override
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsKey(K key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V get(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SetService<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }
}
