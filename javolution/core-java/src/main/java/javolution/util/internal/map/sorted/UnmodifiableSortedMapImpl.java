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

import javolution.util.internal.map.UnmodifiableMapImpl;
import javolution.util.internal.set.sorted.UnmodifiableSortedSetImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 *  * An unmodifiable view over a map.
 */
public class UnmodifiableSortedMapImpl<K, V> extends UnmodifiableMapImpl<K, V> 
          implements SortedMapService<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.
 
    public UnmodifiableSortedMapImpl(SortedMapService<K, V> target) {
        super(target);
    }

    @Override
    public SortedSetService<Entry<K, V>> entrySet() {
        return new UnmodifiableSortedSetImpl<Entry<K, V>>(target().entrySet());
    }

    @Override
    public K firstKey() {
        return target().firstKey();
    }

    @Override
    public SortedSetService<K> keySet() {
        return new UnmodifiableSortedSetImpl<K>(target().keySet());
    }

    @Override
    public K lastKey() {
        return target().lastKey();
    }

    @Override
    public SortedMapService<K, V> subMap(K fromKey, K toKey) {
        return target().subMap(fromKey, toKey);
    }
    
    private SortedMapService<K, V> target() {
        return (SortedMapService<K, V>) target;
    }
}
