/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.internal.table.sorted.FastSortedTableImpl;

/**
 * A map view over a sorted table of entries.
 */
public class FastSortedMapImpl<K, V> extends SortedMapView<K,V> {
     
    private static final long serialVersionUID = 0x600L; // Version.
    private final Equality<? super K> keyComparator;
    private FastSortedTableImpl<Entry<K,V>> entries 
        = new FastSortedTableImpl<Entry<K,V>>(new EntryComparator());
    private final Equality<? super V> valueComparator;

    public FastSortedMapImpl(final Equality<? super K> keyComparator,
            final Equality<? super V> valueComparator) {
        super(null);
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
     }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return entries.contains(new MapEntryImpl<K,V>((K)key, null));
    }

    @Override
    public K firstKey() {
        return entries.getFirst().getKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        int i = entries.indexOf(new MapEntryImpl<K,V>((K)key, null));
        return (i >= 0) ? entries.get(i).getValue() : null;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entries.iterator();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return keyComparator;
    }

    @Override
    public K lastKey() {
        return entries.getLast().getKey();
    }

    @Override
    public V put(K key, V value) {
        MapEntryImpl<K,V> entry = new MapEntryImpl<K,V>(key, value);
        int i = entries.positionOf(entry);
        if (i < size()) {
            Entry<K,V> e = entries.get(i);
            if (keyComparator().areEqual(key, e.getKey())) { // Entry exists.
                V previous = e.getValue();
                e.setValue(value);
                return previous;
            }
        }    
        entries.add(i, entry);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        int i = entries.indexOf(new MapEntryImpl<K,V>((K)key, null));
        if (i < 0) return null;
        Entry<K,V> e = entries.get(i);
        V previous = e.getValue();
        entries.remove(i);
        return previous;
    }

    @Override
    public Equality<? super V> valueComparator() {
        return valueComparator;
    }

}
