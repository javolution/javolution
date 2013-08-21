/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.util.Comparator;
import java.util.Map;

import javolution.util.internal.map.UnmodifiableMapImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 *  * An unmodifiable view over a map.
 */
public class UnmodifiableSortedMapImpl<K, V> extends UnmodifiableMapImpl<K, V> implements SortedMapService<K,V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableSortedMapImpl(SortedMapService<K, V> target) {
        super(target);
    }

    @Override
    public Comparator<? super K> comparator() {
        return target().keyComparator();
    }

    @Override
    public SortedSetService<Map.Entry<K, V>> entrySet() {
        return new SubSortedMapImpl<K,V>(this, null, null).entrySet();
    }

    @Override
    public K firstKey() {
        return target().firstKey();
    }

    @Override
    public SortedMapService<K, V> headMap(K toKey) {
        return new SubSortedMapImpl<K,V>(this, null, toKey);
    }

    @Override
    public SortedSetService<K> keySet() {
        return new SubSortedMapImpl<K,V>(this, null, null).keySet();
    }

    @Override
    public K lastKey() {
        return target().lastKey();
    }

    @Override
    public SortedMapService<K, V> subMap(K fromKey, K toKey) {
        return new SubSortedMapImpl<K,V>(this, fromKey, toKey);
    }

     
    @Override
    public SortedMapService<K, V> tailMap(K fromKey) {
        return new SubSortedMapImpl<K,V>(this, fromKey, null);
    }
    
    @Override
    protected SortedMapService<K,V> target() {
        return (SortedMapService<K,V>) super.target();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public SortedMapService<K,V>[] split(int n, boolean updateable) {
        SortedMapService<K,V>[] subTargets = target().split(n, updateable);
        SortedMapService<K,V>[] result = new SortedMapService[subTargets.length];
        for (int i = 0; i < subTargets.length; i++) {
            result[i] = new UnmodifiableSortedMapImpl<K,V>(subTargets[i]);
        }
        return result;
    }
 }
