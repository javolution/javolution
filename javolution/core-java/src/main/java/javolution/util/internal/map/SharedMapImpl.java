/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.Iterator;
import java.util.Map;

import javolution.util.function.Equality;
import javolution.util.internal.collection.ReadWriteLockImpl;
import javolution.util.service.MapService;

/**
 * A shared view over a map.
 */
public class SharedMapImpl<K, V> extends MapView<K, V> {

    /** Thread-Safe Iterator. */
    private class IteratorImpl implements Iterator<Entry<K, V>> {
        private Entry<K, V> next;
        private final Iterator<Entry<K, V>> targetIterator;

        public IteratorImpl() {
            lock.readLock.lock();
            try {
                targetIterator = cloneTarget().entrySet().iterator(); // Copy.
            } finally {
                lock.readLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            return targetIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            next = targetIterator.next();
            return next;
        }

        @Override
        public void remove() {
            if (next == null) throw new IllegalStateException();
            SharedMapImpl.this.remove(next.getKey());
            next = null;
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.
    protected ReadWriteLockImpl lock;
    protected transient Thread updatingThread; // The thread executing an update.

    public SharedMapImpl(MapService<K, V> target) {
        this(target, new ReadWriteLockImpl());
    }

    public SharedMapImpl(MapService<K, V> target, ReadWriteLockImpl lock) {
        super(target);
        this.lock = lock;
    }

    @Override
    public void clear() {
        lock.writeLock.lock();
        try {
            target().clear();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        lock.readLock.lock();
        try {
            return target().containsKey(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        lock.readLock.lock();
        try {
            return target().containsValue(value);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        lock.readLock.lock();
        try {
            return target().get(key);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock.lock();
        try {
            return target().isEmpty();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new IteratorImpl();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return target().keyComparator();
    }

    @Override
    public V put(K key, V value) {
        lock.writeLock.lock();
        try {
            return target().put(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        lock.writeLock.lock();
        try {
            target().putAll(m);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        lock.writeLock.lock();
        try {
            return target().putIfAbsent(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.writeLock.lock();
        try {
            return target().remove(key);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        lock.writeLock.lock();
        try {
            return target().remove(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public V replace(K key, V value) {
        lock.writeLock.lock();
        try {
            return target().replace(key, value);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        lock.writeLock.lock();
        try {
            return target().replace(key, oldValue, newValue);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock.lock();
        try {
            return target().size();
        } finally {
            lock.readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapService<K, V>[] split(int n) {
        MapService<K, V>[] tmp;
        lock.readLock.lock();
        try {
            tmp = target().split(n);
        } finally {
            lock.readLock.unlock();
        }
        MapService<K, V>[] result = new MapService[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new SharedMapImpl<K, V>(tmp[i], lock); // Same lock.
        }
        return result;
    }

    @Override
    public MapService<K, V> threadSafe() {
        return this;
    }

    @Override
    public Equality<? super V> valueComparator() {
        return target().valueComparator();
    }

    /** Returns a clone copy of target. */
    protected MapService<K, V> cloneTarget() {
        try {
            return target().clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Cannot happen since target is Cloneable.");
        }
    }

}
