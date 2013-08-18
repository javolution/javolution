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

import javolution.util.internal.map.AtomicMapImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 * An atomic view over a sorted map  (copy-on-write).
 */
public class AtomicSortedMapImpl<K, V> extends AtomicMapImpl<K, V> implements SortedMapService<K,V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicSortedMapImpl(SortedMapService<K, V> target) {    
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
        return targetView().firstKey();
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
        return targetView().lastKey();
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
    public SortedMapService<K, V> threadSafe() {
        return this;
    }

    @Override
    protected SortedMapService<K,V> targetView() {
        return (SortedMapService<K,V>) super.targetView();
    }

}
