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
import java.util.concurrent.locks.ReadWriteLock;

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
 * @version 6.0.0, December 12, 2012
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
    public FastMap(EqualityComparator<? super K> keyEquality) {
        service = new FastMapImpl<K, V>(keyEquality);
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
     * Returns an unmodifiable view of this map.
     * Attempts to modify the map returned or the map elements (keys, values or 
     * entries) results in an {@link UnsupportedOperationException} being thrown. 
     */
    public FastMap<K, V> unmodifiable() {
        return new FastMap<K, V>(new UnmodifiableMapImpl<K, V>(service));
    }

    /**
     * Returns a thread-safe view over this map allowing 
     * concurrent read without blocking and concurrent write possibly 
     * blocking. All updates performed on this map are atomic as far as 
     * this map's readers are concerned. To perform complex actions on a 
     * shared map in an atomic manner, the {@link #atomicRead atomicRead} /
     * {@link #atomicWrite atomicWrite} method should be used.
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
     * Executes the specified read action on this map in an atomic 
     * manner. Multiple read actions can be performed concurrently.
     * This method is relevant only for {@link #shared shared} or
     * {@link #parallel parallel} maps.
     * The framework ensures that no read action can be performed if a 
     * write action is in progress or waiting.
     * 
     * @param read the read action to be executed in an atomic manner.
     */
    public void atomicRead(Runnable read) {
        ReadWriteLock rwLock = service().getLock();
        if (rwLock != null) {
            rwLock.readLock().lock();
            try {
                read.run();
            } finally {
                rwLock.readLock().unlock();
            }
        } else { // Not shared or parallel.
            read.run();
        }
    }

    /** 
     * Executes the specified write action on this map in an atomic 
     * manner. As far as readers of this map are concerned, either they
     * see the full result of the action or nothing. This method is relevant 
     * only for {@link #shared shared} or {@link #parallel parallel} maps.
     * The framework ensures that only one write action can be performed at 
     * a time and write action have precedence over read actions.
     * 
     * @param write the write action to be executed in an atomic manner.
     */
    public void atomicWrite(Runnable write) {
        ReadWriteLock rwLock = service().getLock();
        if (rwLock != null) {
            rwLock.writeLock().lock();
            try {
                write.run();
            } finally {
                rwLock.writeLock().unlock();
            }
        } else { // Not shared or parallel.
            write.run();
        }
    }

    /** 
     * Returns an immutable reference over this map. The method should only 
     * be called if this map cannot be modified after the call (for 
     * example if there is no reference to this map after the call).
     */
    public <T extends Map<K,V>> Immutable<T> toImmutable() {
        return new Immutable<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T value() {
                return (T) unmodifiable();
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
