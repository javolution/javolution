/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.annotation.RealTime.Limit.LOG_N;

import java.util.Comparator;
import java.util.SortedMap;

import javolution.annotation.RealTime;
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
 * @version 6.0.0, December 12, 2012
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
      * for key equality and ordering.
    */
    public FastSortedMap(EqualityComparator<? super K> keyComparator) {
        super(keyComparator); // TODO
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
    public FastSortedSet<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> subMap(K fromKey, K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> headMap(K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @RealTime(limit = LOG_N)
    public FastSortedMap<K, V> tailMap(K fromKey) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public K lastKey() {
        // TODO Auto-generated method stub
        return null;
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
