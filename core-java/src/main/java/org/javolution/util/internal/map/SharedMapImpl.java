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

import org.javolution.util.FastMap;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.ReadWriteLockImpl;

/**
 * A shared view over a map.
 */
public final class SharedMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> inner;
    private final ReadWriteLockImpl lock;

    public SharedMapImpl(FastMap<K, V> inner) {
        this.inner = inner;
        this.lock = new ReadWriteLockImpl();
    }

    public SharedMapImpl(FastMap<K, V> inner, ReadWriteLockImpl lock) {
        this.inner = inner;
        this.lock = lock;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        lock.readLock.lock();
        try {
            return inner.ceilingEntry(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K ceilingKey(K key) {
        lock.readLock.lock();
        try {
            return inner.ceilingKey(key);
        } finally {
            lock.readLock.unlock();
        }
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
    public Order<? super K> comparator() {
        return inner.comparator(); // Immutable.
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
    public ReadOnlyIterator<Entry<K, V>> descendingIterator() {
        lock.readLock.lock();
        try {
            return inner.clone().descendingIterator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> descendingIterator(K fromKey) {
        lock.readLock.lock();
        try {
            return inner.clone().descendingIterator(fromKey);
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
    public Entry<K, V> firstEntry() {
        lock.readLock.lock();
        try {
            return inner.firstEntry();
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
    public Entry<K, V> floorEntry(K key) {
        lock.readLock.lock();
        try {
            return inner.floorEntry(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K floorKey(K key) {
        lock.readLock.lock();
        try {
            return inner.floorKey(key);
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
    public Entry<K, V> higherEntry(K key) {
        lock.readLock.lock();
        try {
            return inner.higherEntry(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K higherKey(K key) {
        lock.readLock.lock();
        try {
            return inner.higherKey(key);
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
    public ReadOnlyIterator<Entry<K, V>> iterator() {
        lock.readLock.lock();
        try {
            return inner.clone().iterator();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> iterator(K fromKey) {
        lock.readLock.lock();
        try {
            return inner.clone().iterator(fromKey);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Entry<K, V> lastEntry() {
        lock.readLock.lock();
        try {
            return inner.lastEntry();
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
    public Entry<K, V> lowerEntry(K key) {
        lock.readLock.lock();
        try {
            return inner.lowerEntry(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public K lowerKey(K key) {
        lock.readLock.lock();
        try {
            return inner.lowerKey(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        lock.writeLock.lock();
        try {
            return inner.pollFirstEntry();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        lock.writeLock.lock();
        try {
            return inner.pollLastEntry();
        } finally {
            lock.writeLock.unlock();
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
    public void putAll(K key, V value, Object... others) {
        lock.writeLock.lock();
        try {
            inner.putAll(key, value, others);
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

}
