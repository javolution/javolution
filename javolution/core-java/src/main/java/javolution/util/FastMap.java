/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javolution.internal.util.map.BasicMapImpl;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.service.MapService;

/**
 * <p> Hash map with real-time behavior; smooth capacity increase and 
 *     <i>thread-safe</i> behavior without external synchronization when
 *     {@link #shared shared}. The capacity of a fast map is automatically 
 *     adjusted to best fit its size (e.g. when a map is cleared its memory 
 *     footprint is minimal).</p>
 *     <img src="doc-files/map-put.png"/>
 * 
 * <p> The iteration order for a fast map is non-deterministic, 
 *     for a predictable insertion order, the  {@link #ordered ordered}
 *     view can be used (equivalent to {@link LinkedHashMap}).</p> 
 *     
 * <p> Fast maps may use custom {@link #usingKeyComparator key comparator}
 *     or {@link #usingValueComparator value comparator}, <code>null</code> 
 *     keys are supported.
 *     [code]
 *     FastMap<Foo, Bar> identityMap = new FastMap<Foo, Bar>().usingKeyComparator(FastComparator.IDENTITY);
 *     [/code]
 *     Fast maps can advantageously replace any of the standard 
 *     <code>java.util</code> maps. For example:
 *     [code]
 *     Map<Foo, Bar> concurrentHashMap = new FastMap<Foo, Bar>().shared(); // ConcurrentHashMap
 *     Map<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>().ordered(); // LinkedHashMap
 *     Map<Foo, Bar> linkedConcurrentHashMap = new FastMap<Foo, Bar>().ordered().shared(); // Does not exist in java.util !
 *     Map<Foo, Bar> unmodifiableMap = new FastMap<Foo, Bar>().unmodifiable(); // Unmodifiable view.
 *     ...
 *     [/code]</p>
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0.0, December 12, 2012
 */
public class FastMap<K, V> implements Map<K, V> {


    /**
     * Holds map service implementation.
     */
    private final MapService<K, V> service;
    
    /**
     * Creates an empty map whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastMap() {
        service = new BasicMapImpl<K,V>();
    }

    /***************************************************************************
     * Map views.
     */
    
    /**
     * Returns an unmodifiable view of this map.
     * Attempts to modify the map returned or the map elements (keys, values, 
     * entries) will result in an {@link UnsupportedOperationException} being thrown. 
     */
    public FastMap<K, V> unmodifiable() {
        return null; // TODO
    }

    /**
     * Returns a shared view over this map.
     * Multiple threads may concurrently modify this map or the map elements
     * (key, values, entries).
     */
    public FastMap<K, V> shared() {
        return null; // TODO
    }

    /**
     * Returns an ordered view of this map (insertion-order).
     * Closures (and iterations) over the map elements (key, values,
     * entries) have a predictable iteration order which is the order 
     * in which the entries are put.
     */
    public FastMap<K, V> ordered() {
        return null; // TODO
    }

    /**
     * Returns a map using the specified key comparator.
     */
    public FastMap<K, V> usingKeyComparator(FastComparator<K> cmp) {
        return null; // TODO
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return this map's size.
     */
    public int size() {
        return service.size();
    }

    /**
     * Indicates if this map contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Indicates if this map contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the
     *         specified key; <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return service.containsKey((K)key);
    }

    /**
     * Indicates if this map associates one or more keys to the specified value.
     * 
     * @param value the value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value.
     */
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        return service.values().contains((V)value);
    }

    /**
     * Returns the value to which this map associates the specified key.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return service.get((K)key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If this map previously contained a mapping for this key, the old value
     * is replaced.
     * 
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     */
    public V put(K key, V value) {
        return service.put(key, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * If the entry set of the specified map is an instance of 
     * {@link FastCollection}, closure-based iterations are performed 
     * (safe even when the specified map is shared and concurrently 
     * modified).
     * 
     * @param map the mappings to be stored in this map.
     * @throws NullPointerException the specified map is <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> map) {
        Set<?> entries = map.entrySet();
        if (entries instanceof FastCollection) {
            FastCollection<Map.Entry<K, V>> fc =
                    (FastCollection<Map.Entry<K, V>>) entries;
            fc.doWhile(new Predicate<Map.Entry<K, V>>() {

                public Boolean evaluate(Entry<K, V> param) {
                    put(param.getKey(), param.getValue());
                    return true;
                }

            });
        }
        for (Iterator<?> i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry<K, V> e = (Map.Entry<K, V>) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the entry for the specified key if present.
     * 
     * @param key the key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return service.remove((K)key);
    }

    /**
     * Removes all of the mappings from this map; the capacity of the map
     * is then reduced to its minimum (reduces memory footprint).
     */
    public void clear() {
        service.clear();
    }

    /**
     * Returns a {@link FastCollection} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    public KeySet<K> keySet() {
        return new KeySet<K>(this);
    }

    /**
     * Returns a {@link FastCollection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. 
     */
    public Values<V> values() {
        return new Values<V>(this);
    }

    /**
     * Returns a {@link FastCollection} view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. 
     */
    public EntrySet<K, V> entrySet() {
        return new EntrySet<K, V>(this);
    }

    /**
     * Associates the specified value only if the specified key is not already
     * associated. This is equivalent to:[code]
     *   if (!map.containsKey(key))
     *       return map.put(key, value);
     *   else
     *       return map.get(key);[/code]
     * except that for shared maps the action is performed atomically.
     * For shared maps, this method guarantees that if two threads try to 
     * put the same key concurrently only one of them will succeed.
     *
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public V putIfAbsent(K key, V value) {
        return service.putIfAbsent(key, value);
    }

    /**
     * A fast collection view over the map keys.
     */
    public static final class KeySet<K> extends FastCollection<K> implements Set<K> {

        final FastMap<K, ?> that;

        KeySet(FastMap<K, ?> that) {
            this.that = that;
        }

        @Override
        public FastCollection<K> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<K> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<K, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<K> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<K> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<K> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A fast collection view over the map entries.
     */
    public static final class EntrySet<K, V> extends FastCollection<Entry<K, V>> implements Set<Entry<K, V>> {

        final FastMap<K, V> that;

        EntrySet(FastMap<K, V> that) {
            this.that = that;
        }

        @Override
        public FastCollection<Entry<K, V>> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<Entry<K, V>> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<Entry<K, V>, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<Entry<K, V>> predicate) {
            if (that.size() == 0) return;
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<Entry<K, V>> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A fast collection view over the map values.
     */
    public static final class Values<V> extends FastCollection<V> {

        final FastMap<?, V> that;

        Values(FastMap<?, V> that) {
            this.that = that;
        }

        @Override
        public FastCollection<V> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<V> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<V, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<V> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<V> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<V> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
