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

import javolution.util.FastMap;

/**
 * The set of related map functionalities which can be used/reused to implement 
 * map collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see FastMap
 */
public interface MapService<K, V>  {

    //
    // Map interface.
    // 
    
    /** See {@link FastMap#clear} */
    void clear();

    /** See {@link FastMap#containsKey} */
    boolean containsKey(K key);
    
    /** See {@link FastMap#get} */
    V get(K key);

    /** See {@link FastMap#put} */
    V put(K key, V value);
    
    /** See {@link FastMap#remove} */
    V remove(K key);

    /** See {@link FastMap#size} */
    int size();
    
    /** See {@link FastMap#entrySet()} */
    CollectionService<Entry<K,V>> entrySet();

    /** See {@link FastMap#values()} */
    CollectionService<V> values();

    /** See {@link FastMap#keySet()} */
    CollectionService<K> keySet();

    //
    // ConcurrentMap Interface
    //
    
    /** See {@link FastMap#putIfAbsent(Object, Object)} */
    V putIfAbsent(K key, V value);
    
    /** See {@link FastMap#remove(Object, Object)} */
    boolean remove(K key, V value);
    
    /** See {@link FastMap#replace(Object, Object)} */
    V replace(K key, V value);
    
    /** See {@link FastMap#replace(Object, Object, Object)} */
    boolean replace(K key, V oldValue, V newValue);

    
    //
    // Misc.
    //       

    /** Returns the comparator to be used for key comparisons } */
    ComparatorService<K> keyComparator();
        
 }
