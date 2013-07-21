/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.RealTime.Limit.LOG_N;

import java.util.Comparator;
import java.util.SortedMap;

import javolution.internal.util.map.sorted.FastSortedMapImpl;
import javolution.internal.util.map.sorted.SharedSortedMapImpl;
import javolution.internal.util.map.sorted.UnmodifiableSortedMapImpl;
import javolution.lang.RealTime;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.service.SortedMapService;

/**
* <p> A high-performance sorted map with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p> This map provides a total ordering based on the keys natural order or 
 *     using custom {@link #FastSortedMap(EqualityComparator) comparators}.</p>
 *        
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class FastSortedMap<K, V> extends FastMap<K, V> implements
        SortedMap<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Creates an empty sorted map ordered on keys natural order.
     */
    public FastSortedMap() {
        this(Comparators.STANDARD);
    }

    /**
      * Creates an empty sorted map ordered using the specified comparator 
      * for order.
    */
    public FastSortedMap(EqualityComparator<? super K> keyComparator) {
        this(keyComparator, Comparators.STANDARD); 
    }

    /**
      * Creates an empty sorted map ordered using the specified key comparator 
      * for order and value comparator for values equality.
    */
    public FastSortedMap(EqualityComparator<? super K> keyComparator, 
            EqualityComparator<? super V> valueComparator) {
        super(new FastSortedMapImpl<K,V>(keyComparator, valueComparator)); 
    }

    /**
     * Creates a sorted map backed up by the specified service implementation.
     */
    protected FastSortedMap(SortedMapService<K, V> service) {
        super(service);
    }

    /***************************************************************************
     * Views.
     */

    @Override
    public FastSortedMap<K, V> unmodifiable() {
        return new FastSortedMap<K, V>(new UnmodifiableSortedMapImpl<K, V>(service()));
    }

    @Override
    public FastSortedMap<K, V> shared() {
        return new FastSortedMap<K, V>(new SharedSortedMapImpl<K, V>(service()));
    }

    @Override
    public FastSortedSet<Entry<K,V>> entrySet() {
        return new FastSortedSet<Entry<K,V>>(service().entrySet());
    }
    
    @Override
    public FastSortedSet<K> keySet() {
        return new FastSortedSet<K>(service().keySet());
    }
    
    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> subMap(K fromKey, K toKey) {
        return new FastSortedMap<K, V>(service().subMap(fromKey, toKey));
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> headMap(K toKey) {
        return new FastSortedMap<K, V>(service().subMap(firstKey(), toKey));
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> tailMap(K fromKey) {
        return new FastSortedMap<K, V>(service().subMap(fromKey, lastKey()));
    }

    /***************************************************************************
     * FastMap operations with different time limit behavior.
     */

    @Override
    @RealTime(limit = LOG_N)
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    @RealTime(limit = LOG_N)
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    @RealTime(limit = LOG_N)
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    @RealTime(limit = LOG_N)
    public V remove(Object key) {
        return super.remove(key);
    }

    @Override
    @RealTime(limit = LOG_N)
    public V putIfAbsent(K key, V value) {
        return super.putIfAbsent(key, value);
    }

    @Override
    @RealTime(limit = LOG_N)
    public boolean remove(Object key, Object value) {
        return super.remove(key, value);
    }

    @Override
    @RealTime(limit = LOG_N)
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }

    @Override
    @RealTime(limit = LOG_N)
    public V replace(K key, V value) {
        return super.replace(key, value);
    }

    /***************************************************************************
     * SortedMap operations.
     */

    @Override
    public K firstKey() {
        return service().firstKey();
    }

    @Override
    public K lastKey() {
        return service().lastKey();
    }

    @Override
    public Comparator<? super K> comparator() {
        return keySet().comparator();
    }

    /***************************************************************************
     * Misc.
     */

    @Override
    protected SortedMapService<K, V> service() {
        return (SortedMapService<K, V>) super.service();
    }
}
