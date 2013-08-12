/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javolution.util.function.Equality;

/**
 * The set of related map functionalities which can be used/reused to implement 
 * map collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see javolution.util.FastMap#FastMap()
 */
public interface MapService<K, V> extends Map<K, V>, ConcurrentMap<K, V>,
        Serializable, Cloneable {

    /** 
     * Returns a copy of this map; updates of the copy should not 
     * impact the original.
     */
    MapService<K, V> clone() throws CloneNotSupportedException;

    /**
     * Returns a set view over the entries of this map. The set 
     * support adding/removing entries. Two entries are considered 
     * equals if they have the same key regardless of their values.
     */
    @Override
    SetService<Map.Entry<K, V>> entrySet();

    /** 
    * Returns the key comparator used for key equality or order if the 
    * map is sorted.
    */
    Equality<? super K> keyComparator();

    /**
     * Returns a set view over the key of this map, the set support 
     * adding new key for which the value is automatically {@code null}.
     */
    @Override
    SetService<K> keySet();

    /** 
     * Returns {@code n} sub-views over distinct parts of this map.
     * If {@code n == 1} or if this map cannot be split, 
     * this method returns an array holding a single element.
     * If {@code n > this.size()} this method may return empty views.
     *  
     * @param n the number of sub-views to return.
     * @return the sub-views.
     * @throws IllegalArgumentException if {@code n <= 1}
     */
    MapService<K, V>[] subViews(int n);


    /** 
    * Returns the value comparator used for value equality.
    */
    Equality<? super V> valueComparator();

    /**
     * Returns a collection view over the values of this map, the collection 
     * support value/entry removal but not adding new values.
     */
    @Override
    CollectionService<V> values();
 
}