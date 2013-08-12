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

import javolution.util.function.Equality;
import javolution.util.internal.map.MapView;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 * A map view over a sorted table of entries.
 */
public class FastSortedMapImpl<K, V> extends MapView<K,V> implements SortedMapService<K,V> {

    private static final long serialVersionUID = 0x600L; // Version.
    final Equality<? super K> keyComparator;
    final Equality<? super V> valueComparator;

    public FastSortedMapImpl(final Equality<? super K> keyComparator,
            final Equality<? super V> valueComparator) {
        super(null);
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
     }

    @Override
    public Comparator<? super K> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

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
    public SortedMapService<K, V> headMap(K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMapService<K, V> subMap(K fromKey, K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMapService<K, V> tailMap(K fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSetService<Map.Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public SortedSetService<K> keySet() {
        return null;
    }

}
