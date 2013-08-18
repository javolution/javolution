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

import javolution.util.internal.map.SharedMapImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 * A shared view over a sorted map.
 */
public class SharedSortedMapImpl<K, V> extends SharedMapImpl<K, V> implements SortedMapService<K,V> {
    
    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedMapImpl(SortedMapService<K, V> target) {
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
        lock.readLock.lock();
        try {
            return target().firstKey();
        } finally {
            lock.readLock.unlock();
        }
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
        lock.readLock.lock();
        try {
            return target().lastKey();
        } finally {
            lock.readLock.unlock();
        }
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
    protected SortedMapService<K,V> target() {
        return (SortedMapService<K,V>) super.target();
    }
}
