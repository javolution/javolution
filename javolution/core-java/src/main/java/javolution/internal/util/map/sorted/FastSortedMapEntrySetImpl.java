/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map.sorted;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javolution.internal.util.collection.SplitCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SortedSetService;

/**
 * The entry set view over the sorted map implementation.
 */
public class FastSortedMapEntrySetImpl<K, V> implements SortedSetService<Entry<K, V>>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastSortedMapImpl<K, V> map;

    public FastSortedMapEntrySetImpl(FastSortedMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(Entry<K, V> e) {
        int size = size();
        map.put(e.getKey(), e.getValue());
        return size() != size;
    }

    @Override
    public void atomic(Runnable update) {
        map.atomic(update);
    }

    @Override
    public void clear() {
        map.entries.clear();
    }

    @Override
    public EqualityComparator<? super Map.Entry<K, V>> comparator() {
        return map.entries.comparator();
    }

    @Override
    public boolean contains(Entry<K, V> entry) {
        return map.entries.indexOf(entry) >= 0;
    }

    @Override
    public Entry<K, V> first() {
        return map.entries.getFirst();
    }

    @Override
    public void forEach(final Consumer<? super Entry<K, V>> consumer,
            IterationController controller) {
        map.entries.forEach(consumer, controller);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map.entries.iterator();
    }

    @Override
    public Entry<K, V> last() {
        return map.entries.getLast();
    }

    @Override
    public boolean remove(Entry<K, V> entry) {
        return map.entries.remove(entry);
    }

    @Override
    public boolean removeIf(final Predicate<? super Entry<K, V>> filter,
            IterationController controller) {
        return map.entries.removeIf(filter, controller);
    }

    @Override
    public int size() {
        return map.entries.size();
    }

    @Override
    public FastSortedMapEntrySetImpl<K, V> subSet(Entry<K, V> fromEntry,
            Entry<K, V> toEntry) {
        return new FastSortedMapEntrySetImpl<K, V>(
                map.subMap(fromEntry.getKey(), toEntry.getKey()));
    }

    @Override
    public CollectionService<Entry<K, V>>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }
}
