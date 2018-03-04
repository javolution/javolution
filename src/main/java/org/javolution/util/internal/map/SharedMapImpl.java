/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Map;

import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastMap;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.UnaryOperator;
import org.javolution.util.internal.ReadWriteLockImpl;
import org.javolution.util.internal.set.SharedSetImpl;

/**
 * A shared view over a map.
 */
public final class SharedMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K, V> inner;
    private final ReadWriteLockImpl lock;

    public SharedMapImpl(AbstractMap<K, V> inner) {
        this.inner = inner;
        this.lock = new ReadWriteLockImpl();
    }

    public SharedMapImpl(FastMap<K, V> inner, ReadWriteLockImpl lock) {
        this.inner = inner;
        this.lock = lock;
    }

 
    @Override
    public void clear() {
        lock.writeLock.lock();
        try {
            inner.clear();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public SharedMapImpl<K, V> clone() {
        lock.readLock.lock();
        try {
            return new SharedMapImpl<K, V>(inner.clone());
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder(); // Immutable.
    }

    @Override
    public boolean containsKey(Object key) {
        lock.readLock.lock();
        try {
            return inner.containsKey(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        lock.readLock.lock();
        try {
            return inner.containsValue(value);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean equals(Object obj) {
        lock.readLock.lock();
        try {
            return inner.equals(obj);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K firstKey() {
        lock.readLock.lock();
        try {
            return inner.firstKey();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        lock.readLock.lock();
        try {
            return inner.get(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        lock.readLock.lock();
        try {
            return inner.getEntry(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock.lock();
        try {
            return inner.hashCode();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock.lock();
        try {
            return inner.isEmpty();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K lastKey() {
        lock.readLock.lock();
        try {
            return inner.lastKey();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.writeLock.lock();
        try {
            return inner.put(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public Entry<K,V> addEntry(K key, V value) {
        lock.writeLock.lock();
        try {
            return inner.addEntry(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> that) {
        lock.writeLock.lock();
        try {
            inner.putAll(that);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        lock.writeLock.lock();
        try {
            return inner.putIfAbsent(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.writeLock.lock();
        try {
            return inner.remove(key);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        lock.writeLock.lock();
        try {
            return inner.remove(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        lock.writeLock.lock();
        try {
            return inner.removeEntry(key);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V replace(K key, V value) {
        lock.writeLock.lock();
        try {
            return inner.replace(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        lock.writeLock.lock();
        try {
            return inner.replace(key, oldValue, newValue);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock.lock();
        try {
            return inner.size();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock.lock();
        try {
            return inner.toString();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality(); // Immutable.
    }

    @Override
    public V put(K key, UnaryOperator<V> update) {
        lock.writeLock.lock();
        try {
            return inner.put(key, update);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public AbstractSet<Entry<K, V>> entries() {
        lock.readLock.lock();
        try {
            return new SharedSetImpl<Entry<K,V>>(inner.entries(), lock);
        } finally {
            lock.readLock.unlock();
        }
    }

}
