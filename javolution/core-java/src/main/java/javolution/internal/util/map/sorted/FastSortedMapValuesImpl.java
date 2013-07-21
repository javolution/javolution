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
import java.util.Map.Entry;

import javolution.internal.util.collection.SplitCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A value view over a sorted table of entries.
 */
public class FastSortedMapValuesImpl<K, V> implements CollectionService<V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastSortedMapImpl<K, V> map;

    public FastSortedMapValuesImpl(FastSortedMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void atomic(Runnable update) {
        map.entries.atomic(update);
    }

    @Override
    public EqualityComparator<? super V> comparator() {
        return map.valueComparator;
    }

    @Override
    public void forEach(final Consumer<? super V> consumer,
            IterationController controller) {
        map.entries.forEach(new Consumer<Entry<K, V>>() {

            @Override
            public void accept(Entry<K, V> param) {
                consumer.accept(param.getValue());

            }
        }, controller);
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            Iterator<Entry<K, V>> iterator = map.entries.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public V next() {
                return iterator.next().getValue();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public boolean removeIf(final Predicate<? super V> filter,
            IterationController controller) {
        return map.entries.removeIf(new Predicate<Entry<K, V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param.getValue());
            }

        }, controller);
    }

    @Override
    public CollectionService<V>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }
}
