/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * The set of related map functionalities which can be used/reused to implement 
 * map collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see javolution.util.FastMap#FastMap()
 */
public interface MapService<K, V> {

    /**
     * Removes all of the entries from this map.
     */
    void clear();

    /** 
      * Indicates if the map contains the specified key.
      */
    boolean containsKey(K key);

    /** 
    * Returns the set view of this map entries.
    * Unlike {@link java.util.Map#entrySet()} the view supports adding new 
    * entries to the map.
    */
    SetService<Entry<K, V>> entrySet();

    /** 
     * Returns the value associated to the specified key.
     */
    V get(K key);

    /** 
     * Returns the read-write lock of this map if any; otherwise
     * returns {@code null}. The implementation must give preference to the 
     * waiting writer threads over the readers. That means if we have two 
     * threads waiting to acquire the lock, the one waiting for write lock 
     * will get the lock first. 
     */
    ReadWriteLock getLock();

    /** 
    * Returns the set view of this map keys.
    * Unlike {@link java.util.Map#keySet()} the view supports adding new 
    * keys to the map (associated value is {@code null}).
    */
    SetService<K> keySet();

    /** 
    * Associates the specified value to the specified key; returns 
    * the previously associated value if any.
    */
    V put(K key, V value);

    /** 
     *  Associates the specified key with the specified value unless 
     *  it is already associated.
     */
    V putIfAbsent(K key, V value);

    /**
     * Removes the specified key and returns the previously associated value
     * if any. 
     */
    V remove(K key);

    /** 
     * Removes the entry for a key only if currently mapped to the 
     * specified value.
     */
    boolean remove(K key, V value);

    /** 
     * Replaces the entry for a key only if currently mapped to the
     * specified value.
     */
    V replace(K key, V value);

    /** 
     * Replaces the entry for a key only if currently mapped to the
     * specified value.
     */
    boolean replace(K key, V oldValue, V newValue);

    /**
     * Returns the number of entries in this map.
     */
    int size();

    /** 
     * Returns the collection view of this map values.
     */
    CollectionService<V> values();

}