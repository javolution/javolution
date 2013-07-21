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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javolution.internal.util.collection.SplitCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * The entries view over the fast map implementation.
 */
public class FastMapEntrySetImpl<K, V> implements SetService<Map.Entry<K, V>>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public FastMapEntrySetImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(Entry<K, V> entry) {
        int size = map.size();
        V oldValue = map.put(entry.getKey(), entry.getValue());
        return (size != map.size()) 
                || !map.valueComparator.areEqual(entry.getValue(),oldValue);
    }

    @Override
    public void atomic(Runnable update) {
        map.atomic(update);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public EqualityComparator<? super Entry<K, V>> comparator() {
        return new EqualityComparator<Entry<K, V>>() {

            @Override
            public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
                return map.keyComparator
                        .areEqual(left.getKey(), right.getKey())
                        && map.valueComparator.areEqual(left.getValue(),
                                right.getValue());
            }

            @Override
            public int compare(Entry<K, V> left, Entry<K, V> right) {
                return map.keyComparator.compare(left.getKey(), right.getKey());
            }

            @Override
            public int hashCodeOf(Entry<K, V> entry) {
                return map.keyComparator.hashCodeOf(entry.getKey())
                        + map.valueComparator.hashCodeOf(entry.getValue());
            }
        };
    }

    @Override
    public boolean contains(Entry<K, V> entry) {
        V value = map.get(entry.getKey());
        return map.valueComparator.areEqual(entry.getValue(), value);
    }

     @Override
    public void forEach(
            Consumer<? super Entry<K, V>> consumer, IterationController controller) {
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                consumer.accept(e);
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                consumer.accept(e);
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new Iterator<Entry<K, V>>() {
            FastMapEntryImpl<K,V> current;
            FastMapEntryImpl<K,V> next = map.firstEntry;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Entry<K, V> next() {
                current = next;
                if (current == null) throw new NoSuchElementException();
                next = current.next;
                return current;
            }

            @Override
            public void remove() {
                if (current == null)
                    throw new IllegalStateException();
                map.remove(current.key);
                current = null;   
            }
        };
    }

    @Override
    public boolean remove(Entry<K, V> entry) {
        return map.remove(entry.getKey(), entry.getValue());
    }

    @Override
    public boolean removeIf(
            Predicate<? super Entry<K, V>> filter, IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                 if (filter.test(e)) {
                    remove(e);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                if (filter.test((Entry<K, V>) e)) {
                    remove(e);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return removed;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public CollectionService<Entry<K, V>>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }
}
