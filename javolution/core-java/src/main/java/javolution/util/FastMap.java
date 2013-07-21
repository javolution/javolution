/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.RealTime.Limit.LINEAR;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javolution.internal.util.map.FastMapImpl;
import javolution.internal.util.map.SharedMapImpl;
import javolution.internal.util.map.UnmodifiableMapImpl;
import javolution.lang.Immutable;
import javolution.lang.Parallelizable;
import javolution.lang.RealTime;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * <p> A high-performance map with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast maps support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modifications.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications.</li>
 *    <li>{@link #entrySet} - {@link FastSet} view over the map entries allowing 
 *                            new entries to be added.</li>
 *    <li>{@link #keySet} - {@link FastSet} view over the map keys allowing 
 *                           new keys to be added ({@code null} value).</li>
 *    <li>{@link #values} - {@link FastCollection} view over the map values.</li>
 * </ul>      
 * <p> The iteration order over the fast map keys, values or entries is deterministic 
 *     (unlike {@link java.util.HashMap}). It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedMap} subclass. 
 *     This class permits {@code null} keys.</p> 
 *     
 * <p> Fast maps can advantageously replace any of the standard <code>java.util</code> maps.</p> 
 * <p>[code]
 *  FastMap<Foo, Bar> hashMap = new FastMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> concurrentHashMap = new FastMap<Foo, Bar>().shared(); // FastMap implements ConcurrentMap interface.
 *  FastMap<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>(); // Deterministic iteration order (insertion order).
 *  FastMap<Foo, Bar> linkedConcurrentHashMap = new FastMap<Foo, Bar>().shared(); // No equivalent in java.util !
 *  FastMap<Foo, Bar> treeMap = new FastSortedMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> concurrentSkipListMap = new FastSortedMap<Foo, Bar>().shared();
 *  FastMap<Foo, Bar> identityHashMap = new FastMap<Foo, Bar>(Comparators.IDENTITY);
 *  FastMap<String, Bar> lexicalHashMap = new FastMap<String, Bar>(Comparators.LEXICAL);  // Allows for value retrieval using any CharSequence key.
 *  FastMap<String, Bar> fastStringHashMap = new FastMap<String, Bar>(Comparators.LEXICAL_FAST);  // Use constant-time hashcode calculation.
 *  ...                                                                                   
 *  [/code]</p>
 *  
 *  <p> As for fast collections, an {@link javolution.lang.Immutable immutable}. 
 *      reference (or const reference) can be {@link #toImmutable() obtained} when the originator  
 *      guarantees that the map source cannot be modified.</p>      
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0, July 21, 2013
 */
@RealTime
@Parallelizable(mutexFree = false, comment = "When using shared views.")
public class FastMap<K, V> implements Map<K, V>, ConcurrentMap<K, V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the actual map service implementation.
     */
    private final MapService<K, V> service;

    /**
     * Creates an empty fast map.
     */
    public FastMap() {
        this(Comparators.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparator for keys 
     * equality.
     */
    public FastMap(EqualityComparator<? super K> keyEquality) {
        this(keyEquality, Comparators.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparators for keys 
     * equality and values equality.
     */
    public FastMap(EqualityComparator<? super K> keyEquality, 
            EqualityComparator<? super V> valueEquality) {
        service = new FastMapImpl<K, V>(keyEquality, valueEquality);
    }

    /**
     * Creates a map backed up by the specified service implementation.
     */
    protected FastMap(MapService<K, V> service) {
        this.service = service;
    }

    /***************************************************************************
     * Views.
     */

    /**
     * Returns an unmodifiable view over this map. Any attempt to 
     * modify the map through this view will result into 
     * a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastMap<K, V> unmodifiable() {
        return new FastMap<K, V>(new UnmodifiableMapImpl<K, V>(service));
    }

   /**
     * Returns a thread-safe view over this map. The shared view 
     * uses <a href="http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
     * readers–writer locks</a> allowing concurrent read without blocking. 
     * Since only write operation may introduce blocking, in case of 
     * infrequent updates it may be judicious to use {@link #toImmutable()
     * immutable} maps to be replaced at each update rather than shared views.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
     *      Readers–writer lock</a> 
     */
    public FastMap<K, V> shared() {
        return new FastMap<K, V>(new SharedMapImpl<K, V>(service));
    }

    /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    public FastSet<K> keySet() {
        return new FastSet<K>(service.keySet());
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. 
     */
    public FastCollection<V> values() {
        return new FastCollection<V>() {
            private static final long serialVersionUID = 0x600L; // Version.
            CollectionService<V> serviceValues = service.values();

            @Override
            protected CollectionService<V> service() {
                return serviceValues;
            }
        };
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. 
     */
    public FastSet<Entry<K, V>> entrySet() {
        return new FastSet<Entry<K, V>>(service.entrySet());
    }

    /***************************************************************************
     * Map interface.
     */

    @Override
    public int size() {
        return service.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return service.containsKey((K) key);
    }

    @Override
    @RealTime(limit = LINEAR)
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return service.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        return service.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> map) {
        Set<?> entries = map.entrySet();
        entrySet().addAll((Collection<? extends java.util.Map.Entry<K, V>>) entries);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return service.remove((K) key);
    }

    @Override
    public void clear() {
        service.clear();
    }

    /***************************************************************************
     * ConcurrentMap Interface.
     */

    @Override
    public V putIfAbsent(K key, V value) {
        return service.putIfAbsent(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object key, Object value) {
        return service.remove((K) key, (V) value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return service.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return service.replace(key, value);
    }

    /***************************************************************************
     * Misc.
     */

    /** 
     * Executes the specified update in an atomic manner.
     * Either the readers (including closure-based iterations) see the full 
     * effect of the update or nothing.
     * This method is relevant only for {@link #shared shared} maps.
     *  
     * @param update the update action to be executed on this map.
     */
    @Parallelizable(mutexFree = false, comment = "The map is locked during atomic updates")
    public void atomic(Runnable update) {
        service.atomic(update);
     }

    /** 
     * Returns an immutable reference over this collection. The method should 
     * only be called if this collection cannot be modified after this call (for 
     * example if there is no reference left to this collection).
     */
    public <T extends Map<K,V>> Immutable<T> toImmutable() {
        return new Immutable<T>() {
            @SuppressWarnings("unchecked")
            final T value = (T) unmodifiable();
            @Override
            public T value() {
                return value;
            }
            
        };
    }
    
    /**
      * Returns this map service implementation.
      */
    protected MapService<K, V> service() {
        return service;
    }

}
