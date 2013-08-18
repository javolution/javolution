/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.LOG_N;

import java.util.Comparator;
import java.util.SortedMap;

import javolution.lang.Realtime;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.map.sorted.AtomicSortedMapImpl;
import javolution.util.internal.map.sorted.FastSortedMapImpl;
import javolution.util.internal.map.sorted.SharedSortedMapImpl;
import javolution.util.internal.map.sorted.UnmodifiableSortedMapImpl;
import javolution.util.service.SortedMapService;

/**
 * <p> A high-performance sorted map with {@link Realtime real-time} behavior.</p>
 *     
 * <p> This map provides a total ordering based on the keys natural order or 
 *     using custom {@link #FastSortedMap(Equality) comparators}.</p>
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
        this(Equalities.STANDARD);
    }

    /**
      * Creates an empty sorted map ordered using the specified comparator 
      * for order.
    */
    public FastSortedMap(Equality<? super K> keyComparator) {
        this(keyComparator, Equalities.STANDARD);
    }

    /**
      * Creates an empty sorted map ordered using the specified key comparator 
      * for order and value comparator for values equality.
    */
    public FastSortedMap(Equality<? super K> keyComparator,
            Equality<? super V> valueComparator) {
        super(new FastSortedMapImpl<K, V>(keyComparator, valueComparator));
    }

    /**
     * Creates a sorted map backed up by the specified service implementation.
     */
    protected FastSortedMap(SortedMapService<K, V> service) {
        super(service);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
    public FastSortedMap<K, V> atomic() {
        return new FastSortedMap<K, V>(new AtomicSortedMapImpl<K, V>(service()));
    }

    @Override
    public FastSortedMap<K, V> shared() {
        return new FastSortedMap<K, V>(new SharedSortedMapImpl<K, V>(service()));
    }

    @Override
    public FastSortedMap<K, V> unmodifiable() {
        return new FastSortedMap<K, V>(new UnmodifiableSortedMapImpl<K, V>(
                service()));
    }

    @Override
    public FastSortedSet<Entry<K, V>> entrySet() {
        return new FastSortedSet<Entry<K, V>>(service().entrySet());
    }

    @Override
    public FastSortedSet<K> keySet() {
        return new FastSortedSet<K>(service().keySet());
    }

    /** Returns a view of the portion of this map whose keys range from fromKey, inclusive, to toKey, exclusive. */
    @Override
    public FastSortedMap<K, V> subMap(K fromKey, K toKey) {
        return new FastSortedMap<K, V>(service().subMap(fromKey, toKey));
    }

    /** Returns a view of the portion of this map whose keys are strictly less than toKey. */
    @Override
    public FastSortedMap<K, V> headMap(K toKey) {
        return new FastSortedMap<K, V>(service().subMap(firstKey(), toKey));
    }

    /** Returns a view of the portion of this map whose keys are greater than or equal to fromKey. */
    @Override
    public FastSortedMap<K, V> tailMap(K fromKey) {
        return new FastSortedMap<K, V>(service().subMap(fromKey, lastKey()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

    @Override
    @Realtime(limit = LOG_N)
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    @Realtime(limit = LOG_N)
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    @Realtime(limit = LOG_N)
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    @Realtime(limit = LOG_N)
    public V remove(Object key) {
        return super.remove(key);
    }

    @Override
    @Realtime(limit = LOG_N)
    public V putIfAbsent(K key, V value) {
        return super.putIfAbsent(key, value);
    }

    @Override
    @Realtime(limit = LOG_N)
    public boolean remove(Object key, Object value) {
        return super.remove(key, value);
    }

    @Override
    @Realtime(limit = LOG_N)
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }

    @Override
    @Realtime(limit = LOG_N)
    public V replace(K key, V value) {
        return super.replace(key, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // SortedMap Interface.
    //

    /** Returns the first (lowest) key currently in this map. */
    @Override
    public K firstKey() {
        return service().firstKey();
    }

    /** Returns the last (highest) key currently in this map. */
    @Override
    public K lastKey() {
        return service().lastKey();
    }

    /** Returns the comparator used to order the keys in this map (never null). */
    @Override
    public Comparator<? super K> comparator() {
        return keySet().comparator();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    @Override
    public FastSortedMap<K, V> putAll(FastMap<? extends K, ? extends V> that) {
        return (FastSortedMap<K, V>) super.putAll(that);
    }

    @Override
    protected SortedMapService<K, V> service() {
        return (SortedMapService<K, V>) super.service();
    }
}
