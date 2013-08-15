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

import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.service.MapService;

/**
 * An atomic view over a map  (copy-on-write).
 */
public class AtomicMapImpl<K, V> extends MapView<K, V> {

    /** Thread-Safe Iterator. */
    private class IteratorImpl implements Iterator<Entry<K, V>> {
        private Entry<K, V> current;
        private final Iterator<Entry<K, V>> targetIterator = immutable
                .iterator();

        @Override
        public boolean hasNext() {
            return targetIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            current = targetIterator.next();
            return current;
        }

        @Override
        public void remove() {
            if (current == null) throw new IllegalStateException();
            AtomicMapImpl.this.remove(current.getKey());
            current = null;
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.
    protected volatile MapService<K, V> immutable; // The copy used by readers.
    protected transient Thread updatingThread; // The thread executing an update.

    public AtomicMapImpl(MapService<K, V> target) {
        super(target);
        this.immutable = cloneTarget();
    }

    @Override
    public synchronized void clear() {
        clear();
        if (!updateInProgress()) {
            immutable = cloneTarget();
        }
    }

    @Override
    public synchronized AtomicMapImpl<K, V> clone() {
        AtomicMapImpl<K, V> copy = (AtomicMapImpl<K, V>) super.clone();
        copy.updatingThread = null;
        return copy;
    }

    @Override
    public boolean containsKey(Object key) {
        return immutable.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return immutable.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return immutable.get(key);
    }

    @Override
    public boolean isEmpty() {
        return immutable.isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new IteratorImpl();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return immutable.keyComparator();
    }

    @Override
    public synchronized V put(K key, V value) {
        V v = target().put(key, value);
        if (!updateInProgress()) immutable = cloneTarget();
        return v;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        target().putAll(m);
        if (!updateInProgress()) immutable = cloneTarget();
    }

    @Override
    public synchronized V putIfAbsent(K key, V value) {
        V v = target().putIfAbsent(key, value);
        if (!updateInProgress()) immutable = cloneTarget();
        return v;
    }

    @Override
    public synchronized V remove(Object key) {
        V v = target().remove(key);
        if (!updateInProgress()) immutable = cloneTarget();
        return v;
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        boolean changed = target().remove(key, value);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public synchronized V replace(K key, V value) {
        V v = target().replace(key, value);
        if (!updateInProgress()) immutable = cloneTarget();
        return v;
    }

    @Override
    public synchronized boolean replace(K key, V oldValue, V newValue) {
        boolean changed = target().replace(key, oldValue, newValue);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public int size() {
        return immutable.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapService<K, V>[] split(int n) { // Split not supported.
        return new MapService[] { this };
    }

    @Override
    public MapService<K, V> threadSafe() {
        return this;
    }

    @Override
    public synchronized void update(Consumer<MapService<K, V>> action,
            MapService<K, V> view) {
        updatingThread = Thread.currentThread(); // Update in progress.
        try {
            target().update(action, view); // No copy performed.
        } finally {
            updatingThread = null;
            immutable = cloneTarget(); // One single copy !
        }
    }

    @Override
    public Equality<? super V> valueComparator() {
        return immutable.valueComparator();
    }

    /** Returns a clone copy of target. */
    protected MapService<K, V> cloneTarget() {
        try {
            return target().clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Cannot happen since target is Cloneable.");
        }
    }

    /** Indicates if the current thread is doing an atomic update. */
    protected final boolean updateInProgress() {
        return updatingThread == Thread.currentThread();
    }
}
