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
     * Executes the specified action on this map in an atomic manner as 
     * far as readers of this collection's are concerned (either readers 
     * see the full result of this action on this collection or nothing).
     *  
     * @param action the action to be executed atomically.
     */
    void atomic(Runnable action);

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
     * Returns the collection view of this map values.
     */
    CollectionService<V> values();

}