/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.util.Map.Entry;

import javolution.util.internal.ReadWriteLockImpl;
import javolution.util.internal.map.SharedMapImpl;
import javolution.util.internal.set.sorted.SharedSortedSetImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 *  * A shared view over a map.
 */
public class SharedSortedMapImpl<K, V> extends SharedMapImpl<K, V> implements
        SortedMapService<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSortedMapImpl(SortedMapService<K, V> target) {
        super(target);
    }

    public SharedSortedMapImpl(SortedMapService<K, V> target,
            ReadWriteLockImpl rwLock) {
        super(target, rwLock);
    }

    @Override
    public SortedSetService<Entry<K, V>> entrySet() {
        return new SharedSortedSetImpl<Entry<K, V>>(target().entrySet(), rwLock);
    }

    @Override
    public K firstKey() {
        rwLock.readLock().lock();
        try {
            return target().firstKey();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SortedSetService<K> keySet() {
        return new SharedSortedSetImpl<K>(target().keySet(), rwLock);
    }

    @Override
    public K lastKey() {
        rwLock.readLock().lock();
        try {
            return target().lastKey();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SortedMapService<K, V> subMap(K fromKey, K toKey) {
        rwLock.readLock().lock();
        try {
            return target().subMap(fromKey, toKey);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    private SortedMapService<K, V> target() {
        return (SortedMapService<K, V>) target;
    }

}
