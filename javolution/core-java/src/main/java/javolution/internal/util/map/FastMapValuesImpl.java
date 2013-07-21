/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.internal.util.collection.SplitCollectionImpl;
import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * The values view over a map.
 */
public class FastMapValuesImpl<K, V> implements CollectionService<V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public FastMapValuesImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(V element) {
        throw new UnsupportedOperationException("Cannot add map value without key");
    }

    @Override
    public void atomic(Runnable update) {
        map.atomic(update);
    }

    @Override
    public EqualityComparator<? super V> comparator() {
        return Comparators.STANDARD;
    }

    @Override
    public void forEach(
            Consumer<? super V> consumer, IterationController controller) {
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                consumer.accept((V) e.value);
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                consumer.accept((V) e.value);
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            FastMapEntryImpl<K,V> current;
            FastMapEntryImpl<K,V> next = map.firstEntry;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public V next() {
                current = next;
                if (current == null) throw new NoSuchElementException();
                next = current.next;
                return (V) current.value;
            }

            @Override
            public void remove() {
                if (current == null)
                    throw new IllegalStateException();
                map.remove((K)current.key);
                current = null;   
            }
        };
    }

    @Override
    public boolean removeIf(
            Predicate<? super V> filter, IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                 if (filter.test((V) e.value)) {
                    map.remove((K) e.key);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                if (filter.test((V) e.value)) {
                    map.remove((K) e.key);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return removed;
    }

    @Override
    public CollectionService<V>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }
}
