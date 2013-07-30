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
import java.util.Iterator;
import java.util.Map.Entry;

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.internal.collection.SplitCollectionImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.SortedSetService;

/**
 * The key set view over the sorted map implementation.
 */
public class FastSortedMapKeySetImpl<K, V> implements SortedSetService<K>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastSortedMapImpl<K, V> map;

    public FastSortedMapKeySetImpl(FastSortedMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(K key) {
        int size = size();
        map.entries.addIfAbsent(new FastSortedMapEntryImpl<K, V>(key, null));
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
    public EqualityComparator<? super K> comparator() {
        return map.keyComparator;
    }

    @Override
    public boolean contains(K key) {
        return map.entries.indexOf(new FastSortedMapEntryImpl<K, V>(key, null)) >= 0;
    }

    @Override
    public K first() {
        return map.entries.getFirst().getKey();
    }

    @Override
    public void forEach(final Consumer<? super K> consumer,
            IterationController controller) {
        map.entries.forEach(new Consumer<Entry<K, V>>() {

            @Override
            public void accept(Entry<K, V> param) {
                consumer.accept(param.getKey());

            }
        }, controller);
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            Iterator<Entry<K, V>> iterator = map.entries.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                return iterator.next().getKey();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public K last() {
        return map.entries.getLast().getKey();
    }

    @Override
    public boolean remove(K key) {
        return map.entries.remove(new FastSortedMapEntryImpl<K, V>(key, null));
    }

    @Override
    public boolean removeIf(final Predicate<? super K> filter,
            IterationController controller) {
        return map.entries.removeIf(new Predicate<Entry<K, V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param.getKey());
            }

        }, controller);
    }

    @Override
    public int size() {
        return map.entries.size();
    }

    @Override
    public FastSortedMapKeySetImpl<K, V> subSet(K fromKey, K toKey) {
        return new FastSortedMapKeySetImpl<K, V>(map.subMap(fromKey, toKey));
    }

    @Override
    public CollectionService<K>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }
}
