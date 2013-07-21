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

import javolution.internal.util.ReadWriteLockImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.set.SharedSetImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 *  * A shared view over a map.
 */
public class SharedMapImpl<K, V> implements MapService<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    protected final ReadWriteLockImpl rwLock;
    protected final MapService<K, V> target;

    public SharedMapImpl(MapService<K, V> target) {
        this(target, new ReadWriteLockImpl());
    }

    public SharedMapImpl(MapService<K, V> target, ReadWriteLockImpl rwLock) {
        this.target = target;
        this.rwLock = rwLock;
    }

    @Override
    public void atomic(Runnable update) {
        rwLock.writeLock().lock();
        try {
            target.atomic(update);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            target.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        rwLock.readLock().lock();
        try {
            return target.containsKey(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new SharedSetImpl<Entry<K, V>>(target.entrySet(), rwLock);
    }

    @Override
    public V get(K key) {
        rwLock.readLock().lock();
        try {
            return target.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SetService<K> keySet() {
        return new SharedSetImpl<K>(target.keySet(), rwLock);
    }

    @Override
    public V put(K key, V value) {
        rwLock.writeLock().lock();
        try {
            return target.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        rwLock.writeLock().lock();
        try {
            return target.putIfAbsent(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public V remove(K key) {
        rwLock.writeLock().lock();
        try {
            return target.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(K key, V value) {
        rwLock.writeLock().lock();
        try {
            return target.remove(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public V replace(K key, V value) {
        rwLock.writeLock().lock();
        try {
            return target.replace(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        rwLock.writeLock().lock();
        try {
            return target.replace(key, oldValue, newValue);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return target.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public CollectionService<V> values() {
        return new SharedCollectionImpl<V>(target.values(), rwLock);
    }
}
