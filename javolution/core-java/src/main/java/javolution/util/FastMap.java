/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javolution.annotation.RealTime;
import javolution.annotation.RealTime.Limit;
import javolution.internal.util.map.FractalMapImpl;
import javolution.util.function.FullComparator;
import javolution.util.function.Comparators;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * <p> A high-performance map with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast maps support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modifications.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications.</li>
 *    <li>{@link #entrySet} - {@link FastSet} view over the map entries.</li>
 *    <li>{@link #keySet} - {@link FastSet} view over the map keys.</li>
 *    <li>{@link #values} - {@link FastCollection} view over the map values.</li>
 * </ul>      
 * <p> The iteration order over the fast map keys, values or entries is deterministic 
 *     (unlike {@link java.util.HashMap}). It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedMap} subclass.</p> 
 *     
 * <p> Fast maps can advantageously replace any of the standard <code>java.util</code> maps. 
 *     [code]
 *     FastMap<Foo, Bar> hashMap = new FastMap<Foo, Bar>(); 
 *     FastMap<Foo, Bar> concurrentHashMap = new FastMap<Foo, Bar>().shared(); // FastMap implements ConcurrentMap interface.
 *     FastMap<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>(); // Deterministic iteration order (insertion order).
 *     FastMap<Foo, Bar> linkedConcurrentHashMap = new FastMap<Foo, Bar>().shared(); // No equivalent in java.util !
 *     FastMap<Foo, Bar> treeMap = new FastSortedMap<Foo, Bar>(); 
 *     FastMap<Foo, Bar> concurrentSkipListMap = new FastSortedMap<Foo, Bar>().shared();
 *     FastMap<Foo, Bar> identityHashMap = new FastMap<Foo, Bar>(Comparators.IDENTITY);
 *     FastMap<String, Bar> lexicalHashMap = new FastMap<String, Bar>(Comparators.LEXICAL);  // Allows for value retrieval using any CharSequence key.
 *     FastMap<String, Bar> fastStringHashMap = new FastMap<String, Bar>(Comparators.LEXICAL_FAST);  // Use constant-time hashcode calculation.
 *     ...                                                                                   
 *     [/code]</p>     
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0.0, December 12, 2012
 */
@RealTime
public class FastMap<K, V> implements Map<K, V>, ConcurrentMap<K, V> {

    /**
     * Holds the actual map implementation.
     */
    private final MapService<K, V> impl;

    /**
     * Creates an empty hash map.
     */
    public FastMap() {
        this(Comparators.STANDARD);
    }
    
    /**
     * Creates an empty hash map using the specified comparator for key 
     * equality only (for sorting the {@link FastSortedMap} subclass should 
     * be used instead).
     */
    public FastMap(FullComparator<? super K> keyEquality) {
        impl = new FractalMapImpl<K, V>(keyEquality);
    }
        
    /**
     * Creates a map backed up by the specified service implementation.
     */
    public FastMap(MapService<K, V> implementation) {
        this.impl = implementation;
    }

    /**
     * Returns this map service implementation.
     */
    public MapService<K, V> service() {
        return impl;
    }    

    /***************************************************************************
     * Views.
     */

    /**
     * Returns an unmodifiable view of this map.
     * Attempts to modify the map returned or the map elements (keys, values or 
     * entries) results in an {@link UnsupportedOperationException} being thrown. 
     */
    public FastMap<K, V> unmodifiable() {
        return null; // TODO
    }

    /**
     * Returns a shared view over this map.
     * Multiple threads may concurrently modify this map or the map elements
     * (key, values or entries). The implementation guarantees that concurrent
     * accesses can always be performed without blocking.
     */
    public FastMap<K, V> shared() {
        return null; // TODO
    }

    /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    public FastSet<K> keySet() {
        return null; // TODO
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. 
     */
    public FastCollection<V> values() {
        return null; // TODO
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. 
     */
    public FastSet<Entry<K, V>> entrySet() {
        return new FastSet<Entry<K, V>>(impl.entrySet());
    }

    /***************************************************************************
     * Map interface.
     */

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return this map's size.
     */
    public int size() {
        return impl.entrySet().size();
    }

    /**
     * Indicates if this map contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings;
     *         <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
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
    public final boolean containsKey(Object key) {
        return impl.containsKey((K) key);
    }

    /**
     * Indicates if this map associates one or more keys to the specified value.
     * 
     * @param value the value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value.
     */
    @RealTime(Limit.LINEAR)
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    /**
     * Returns the value to which this map associates the specified key.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     */
    @SuppressWarnings("unchecked")
    public final V get(Object key) {
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
    public final V put(K key, V value) {
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
        entrySet().addAll((Collection<? extends Entry<K, V>>) map.entrySet()); 
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
    public final V remove(Object key) {
        return impl.remove((K) key);
    }

    /**
     * Removes all of the mappings from this map; the capacity of the map
     * is then reduced to its minimum (reduces memory footprint).
     */
    public void clear() {
        entrySet().clear();
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
    
}
