/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.io.Serializable;
import java.util.Map.Entry;

import javolution.util.function.EqualityComparator;
import javolution.util.internal.table.sorted.FastSortedTableImpl;
import javolution.util.internal.table.sorted.SubSortedTableImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;
import javolution.util.service.SortedTableService;

/**
 * A map view over a sorted table of entries.
 */
public class FastSortedMapImpl<K, V> implements SortedMapService<K, V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    final EqualityComparator<? super K> keyComparator;
    final EqualityComparator<? super V> valueComparator;
    final SortedTableService<Entry<K, V>> entries; // Ordered using key comparator.

    public FastSortedMapImpl(final EqualityComparator<? super K> keyComparator,
            final EqualityComparator<? super V> valueComparator) {
        this(keyComparator, valueComparator, new FastSortedTableImpl<Entry<K, V>>(
            new EqualityComparator<Entry<K, V>>() {

                @Override
                public int hashCodeOf(Entry<K, V> entry) {
                    return keyComparator.hashCodeOf(entry.getKey());
                }

                @Override
                public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
                    return keyComparator.areEqual(left.getKey(), right.getKey());
                }

                @Override
                public int compare(Entry<K, V> left, Entry<K, V> right) {
                    return keyComparator.compare(left.getKey(), right.getKey());
                }}));
    }

    private FastSortedMapImpl(final EqualityComparator<? super K> keyComparator,
            final EqualityComparator<? super V> valueComparator, 
            SortedTableService<Entry<K, V>> entries) {
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
        this.entries = entries;
    }
    @Override
    public void atomic(Runnable update) {
        entries.atomic(update);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean containsKey(K key) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, null);
        return entries.indexOf(tmp) >= 0;
    }

    @Override
    public SortedSetService<Entry<K, V>> entrySet() {
        return new FastSortedMapEntrySetImpl<K, V>(this);
    }

    @Override
    public K firstKey() {
        return entries.getFirst().getKey();
    }

    @Override
    public V get(K key) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, null);
        int i = entries.indexOf(tmp);
        return (i >= 0) ? entries.get(i).getValue() : null;
    }

    @Override
    public SortedSetService<K> keySet() {
        return new FastSortedMapKeySetImpl<K, V>(this);
    }

    @Override
    public K lastKey() {
        return entries.getLast().getKey();
    }

    @Override
    public V put(K key, V value) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, value);
        int i = entries.slotOf(tmp);
        if ((i < size()) && entries.comparator().areEqual(entries.get(i), tmp))
            return entries.get(i).setValue(value); // Entry already exists.
        entries.add(i, tmp);
        return null;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, value);
        int size = entries.size();
        int i = entries.addIfAbsent(tmp);
        if (size() == size)
            return entries.get(i).getValue(); // Existing value.
        return null;
    }

    @Override
    public V remove(K key) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, null);
        int i = entries.indexOf(tmp);
        if (i < 0)
            return null; // No mapping.
        V value = entries.get(i).getValue();
        entries.remove(i);
        return value;
    }

    @Override
    public boolean remove(K key, V value) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, null);
        int i = entries.indexOf(tmp);
        if ((i >= 0)
                && valueComparator.areEqual(entries.get(i).getValue(),
                        value)) {
            entries.remove(i);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, value);
        int i = entries.indexOf(tmp);
        if (i >= 0) {
            V oldValue = entries.get(i).getValue();
            entries.add(tmp);
            return oldValue;
        }
        return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        FastSortedMapEntryImpl<K, V> tmp = new FastSortedMapEntryImpl<K, V>(key, newValue);
        int i = entries.indexOf(tmp);
        if ((i >= 0)
                && valueComparator.areEqual(entries.get(i).getValue(),
                        oldValue)) {
            entries.add(tmp);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public FastSortedMapImpl<K, V> subMap(K fromKey, K toKey) {
        int fromIndex = entries.slotOf(new FastSortedMapEntryImpl<K, V>(fromKey, null));
        int toIndex = entries.slotOf(new FastSortedMapEntryImpl<K, V>(toKey, null));
        return new FastSortedMapImpl<K, V>(
                keyComparator, valueComparator,
                new SubSortedTableImpl<Entry<K, V>>(
                entries, fromIndex, toIndex));
    }

    @Override
    public CollectionService<V> values() {
        return new FastSortedMapValuesImpl<K, V>(this);
    }

}
