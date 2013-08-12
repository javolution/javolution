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

import javolution.util.service.MapService;

/**
 * An atomic view over a map  (copy-on-write).
 */
public class AtomicMapImpl<K,V> extends MapView<K,V> {

    private static final long serialVersionUID = 0x600L; // Version.

    protected Object lock;
    protected volatile MapService<K,V> targetCopy; // The copy used by readers.
    protected transient Thread updatingThread; // The thread executing an update.

    public AtomicMapImpl(MapService<K,V> target) {
        this(target, new Object());
    }

    public AtomicMapImpl(MapService<K,V> target, Object lock) {
        super(target);
        this.lock = lock;
        this.targetCopy = cloneTarget();
    }

    @Override
    public void clear() {
        synchronized (lock) {
            clear();
            if (!updateInProgress()) {
                targetCopy = cloneTarget();
            }
        }
    }

    @Override
    public AtomicMapImpl<K,V> clone() {
        synchronized (lock) { // Necessary since a copy of target is performed.
            AtomicMapImpl<K,V> copy = (AtomicMapImpl<K,V>) super.clone();
            copy.updatingThread = null;
            copy.lock = new Object(); // No need to share the same lock.
            return copy;
        }
    }

    @Override
    protected Iterator<Entry<K,V>> iterator() { 
        // If not within an update scope, the iterator is read-only.
        return updateInProgress() ? target().entrySet().iterator() : 
            new UnmodifiableMapImpl<K,V>(targetCopy).iterator();
    }

    @Override
    public int size() {
        return targetCopy.size();
    }

    /** 
     * The default implementation shares the lock between sub-collections, which 
     * prevents concurrent closure-based removal. Sub-classes may override
     * this method to avoid such limitation (e.g. {@code AtomicTableImpl}).
     */
    @SuppressWarnings("unchecked")
    @Override
    public AtomicMapImpl<K,V>[] subViews(int n) {
        MapService<K,V>[] tmp;
        synchronized (lock) {
            tmp = target().subViews(n); // We need sub-views of the real target
        }
        AtomicMapImpl<K,V>[] result = new AtomicMapImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new AtomicMapImpl<K,V>(tmp[i], lock); // Same lock.
        }
        return result;
    }
 
    /** Indicates if the current thread is doing an atomic update. */
    protected final boolean updateInProgress() {
        return updatingThread == Thread.currentThread();
    }

    @Override
    public boolean isEmpty() {
        return targetCopy.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return targetCopy.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return targetCopy.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return targetCopy.get(key);
    }

    @Override
    public V put(K key, V value) {
        synchronized (lock) {
            V v = target().put(key, value);
            if (!updateInProgress()) targetCopy = cloneTarget();
            return v;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (lock) {
            target().putAll(m);
            if (!updateInProgress()) targetCopy = cloneTarget();
        }
      }

    @Override
    public V putIfAbsent(K key, V value) {
        synchronized (lock) {
            V v = target().putIfAbsent(key, value);
            if (!updateInProgress()) targetCopy = cloneTarget();
            return v;
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        synchronized (lock) {
            boolean changed = target().remove(key, value);
            if (changed &&!updateInProgress()) targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        synchronized (lock) {
            boolean changed = target().replace(key, oldValue, newValue);
            if (changed &&!updateInProgress()) targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public V replace(K key, V value) {
        synchronized (lock) {
            V v = target().putIfAbsent(key, value);
            if (!updateInProgress()) targetCopy = cloneTarget();
            return v;
        }
    }

}
