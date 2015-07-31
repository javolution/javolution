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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javolution.util.function.Equality;
import javolution.util.function.Splittable;

/**
 * The set of related map functionalities required to implement fast maps.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see javolution.util.FastMap#FastMap()
 */
public interface MapService<K, V> extends 
        Map<K, V>, ConcurrentMap<K, V>, Splittable<MapService<K, V>>, Serializable, Cloneable {

    /** 
     * Returns a copy of this map; updates of the copy should not 
     * impact the original.
     * @throws java.lang.CloneNotSupportedException if clone is not supported by the implementation
     * @return Clone of the MapService
     */
    MapService<K, V> clone() throws CloneNotSupportedException;

    /**
     * Returns a set view over the entries of this map. The set 
     * support adding/removing entries. Two entries are considered 
     * equals if they have the same key regardless of their values.
     *@return Set containing the Entries of this Map
     */
    @Override
    SetService<Map.Entry<K, V>> entrySet();

    /**
     *  Returns an iterator over this map entries.
     *  @return Iterator over the entries in this map
     */
    Iterator<Entry<K, V>> iterator();

    /** 
    * Returns the key comparator used for key equality or order if the 
    * map is sorted.
    * @return Key Comparator used in this Map
    */
    Equality<? super K> keyComparator();

    /**
     * Returns a set view over the key of this map, the set support 
     * adding new key for which the value is automatically {@code null}.
     * @return SetService containing the KeySet of this Service's Map
     */
    @Override
    SetService<K> keySet();

    /** 
     * Gets the value comparator used for value equality.
     * @return the value comparator used for value equality.
     */
    Equality<? super V> valueComparator();

    /**
     * Returns a collection view over the values of this map, the collection 
     * support value/entry removal but not adding new values.
     * @return CollectionService containing the collection of values of this service's Map
     */
    @Override
    CollectionService<V> values(); 
}