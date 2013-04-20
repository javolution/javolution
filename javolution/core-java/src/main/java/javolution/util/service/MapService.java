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
public interface MapService<K, V>  {

    //
    // Map interface.
    // 
    
    /** See {@link java.util.Map#clear} */
    void clear();

    /** See {@link java.util.Map#containsKey} */
    boolean containsKey(K key);
    
    /** See {@link java.util.Map#get} */
    V get(K key);

    /** See {@link java.util.Map#put} */
    V put(K key, V value);
    
    /** See {@link java.util.Map#remove} */
    V remove(K key);

    /** See {@link java.util.Map#size} */
    int size();
    
    /** See {@link java.util.Map#entrySet()} */
    CollectionService<Entry<K,V>> entrySet();

    /** See {@link java.util.Map#values()} */
    CollectionService<V> values();

    /** See {@link java.util.Map#keySet()} */
    CollectionService<K> keySet();

    //
    // ConcurrentMap Interface
    //
    
    /** See {@link java.util.concurrent.ConcurrentMap#putIfAbsent(Object, Object)} */
    V putIfAbsent(K key, V value);
    
    /** See {@link java.util.concurrent.ConcurrentMap#remove(Object, Object)} */
    boolean remove(K key, V value);
    
    /** See {@link java.util.concurrent.ConcurrentMap#replace(Object, Object)} */
    V replace(K key, V value);
    
    /** See {@link java.util.concurrent.ConcurrentMap#replace(Object, Object, Object)} */
    boolean replace(K key, V oldValue, V newValue);

    
    //
    // Misc.
    //       

    /** Returns the comparator to be used for key comparisons and hash-code calculations. */
    ComparatorService<K> keyComparator();

}