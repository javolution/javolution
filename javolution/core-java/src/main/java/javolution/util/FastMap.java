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
import java.util.concurrent.ConcurrentMap;

import javolution.internal.util.map.FractalMapImpl;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * <p> A customizable map with real-time behavior; smooth capacity increase and 
 *     <i>thread-safe</i> behavior without external synchronization when
 *     {@link #shared shared}. The map capacity of the default implementation 
 *     is automatically adjusted to best fit its size (e.g. when the map is 
 *     cleared its memory footprint is minimal).</p>
 *     <img src="doc-files/map-put.png"/>
 * 
 * <p> The iteration order for the default implementation is non-deterministic, 
 *     for a predictable insertion order, the  {@link #ordered ordered}
 *     view can be used (equivalent to {@link LinkedHashMap}).</p> 
 *     
 * <p> Fast maps may use custom {@link #setKeyComparator key comparator}, 
 *     <code>null</code> keys are also supported.
 *     [code]
 *     FastMap<Foo, Bar> identityMap = new FastMap<Foo, Bar>().setKeyComparator(FastComparator.IDENTITY);
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
public class FastMap<K, V> implements Map<K, V>, ConcurrentMap<K, V> {

    /**
     * Holds the actual map implementation.
     */
    private final MapService<K, V> impl;

    /**
     * Creates an empty map whose capacity increments or decrements smoothly
     * without large resize/rehash operations.
     */
    public FastMap() {
        impl = new FractalMapImpl<K, V>();
    }

    /**
     * Creates a map backed up by the specified implementation.
     */
    protected FastMap(MapService<K, V> service) {
        this.impl = service;
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
     * Sets the key comparator to be used by this map.
     * If the map is not empty, this operation may result in the rehashing 
     * of all the map's entries.
     */
    public FastMap<K, V> setKeyComparator(Comparators<? super K> cmp) {
        return null; // TODO
    }

    /**
     * Sets the value comparator to be used by this map.
     */
    public FastMap<K, V> setValueComparator(Comparators<? super V> cmp) {
        return null; // TODO
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return this map's size.
     */
    public int size() {
        return impl.size();
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
        return impl.containsKey((K) key);
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
        return impl.values().contains((V) value);
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
        return impl.get((K) key);
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
        return impl.put(key, value);
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
            FastCollection<Map.Entry<K, V>> fc = (FastCollection<Map.Entry<K, V>>) entries;
            fc.doWhile(new Predicate<Map.Entry<K, V>>() {

                public Boolean apply(Entry<K, V> entry) {
                    put(entry.getKey(), entry.getValue());
                    return true;
                }

            });
        } else {
            for (Iterator<?> i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry<K, V> entry = (Map.Entry<K, V>) i.next();
                put(entry.getKey(), entry.getValue());
            }
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
        return impl.remove((K) key);
    }

    /**
     * Removes all of the mappings from this map; the capacity of the map
     * is then reduced to its minimum (reduces memory footprint).
     */
    public void clear() {
        impl.clear();
    }

    /**
     * Returns a {@link FastCollection} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    public KeySet<K> keySet() {
        return new KeySet<K>(impl.keySet());
    }

    /**
     * Returns a {@link FastCollection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. 
     */
    public Values<V> values() {
        return new Values<V>(impl.values());
    }

    /**
     * Returns a {@link FastCollection} view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. 
     */
    public EntrySet<K, V> entrySet() {
        return new EntrySet<K, V>(impl.entrySet());
    }

    /***************************************************************************
     * ConcurrentMap Interface.
     */

    @Override
    public V putIfAbsent(K key, V value) {
        return impl.putIfAbsent(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object key, Object value) {
        return impl.remove((K) key, (V)value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return impl.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return impl.replace(key, value);
    }
    
    /**
     * A fast collection view over the map keys.
     */
    public static final class KeySet<K> extends FastCollection<K> implements
            Set<K> {

        private final CollectionService<K> impl;

        private KeySet(CollectionService<K> service) {
            this.impl = service;
        }

        @Override
        protected CollectionService<K> getService() {
            return impl;
        }

        private static final long serialVersionUID = 5965229814125983593L;
    }

    /**
     * A fast collection view over the map entries.
     */
    public static final class EntrySet<K, V> extends
            FastCollection<Entry<K, V>> implements Set<Entry<K, V>> {

        private final CollectionService<Entry<K, V>> impl;

        private EntrySet(CollectionService<Entry<K, V>> service) {
            this.impl = service;
        }

        @Override
        protected CollectionService<Entry<K, V>> getService() {
            return impl;
        }

        private static final long serialVersionUID = -3956521670593088014L;
    }

    /**
     * A fast collection view over the map values.
     */
    public static final class Values<V> extends FastCollection<V> {

        private final CollectionService<V> impl;

        private Values(CollectionService<V> service) {
            this.impl = service;
        }

        @Override
        protected CollectionService<V> getService() {
            return impl;
        }

        private static final long serialVersionUID = -424158623485419456L;
    }

  
}
