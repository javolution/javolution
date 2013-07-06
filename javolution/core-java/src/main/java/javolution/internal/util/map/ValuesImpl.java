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
import java.util.concurrent.locks.ReadWriteLock;

import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * The values view over a map.
 */
public final class ValuesImpl<K, V> implements CollectionService<V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public ValuesImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(V element) {
        throw new UnsupportedOperationException("Cannot add map value without key");
    }

    @Override
    public EqualityComparator<? super V> comparator() {
        return Comparators.STANDARD;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(
            Consumer<? super V> consumer, IterationController controller) {
        if (!controller.doReversed()) {
            for (EntryImpl e = map.firstEntry; e != null; e = e.next) {
                consumer.accept((V) e.value);
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (EntryImpl e = map.lastEntry; e != null; e = e.previous) {
                consumer.accept((V) e.value);
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @Override
    public ReadWriteLock getLock() {
        return map.getLock();
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            EntryImpl current;
            EntryImpl next = map.firstEntry;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V next() {
                current = next;
                if (current == null) throw new NoSuchElementException();
                next = current.next;
                return (V) current.value;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void remove() {
                if (current == null)
                    throw new IllegalStateException();
                map.remove((K)current.key);
                current = null;   
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeIf(
            Predicate<? super V> filter, IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (EntryImpl e = map.firstEntry; e != null; e = e.next) {
                 if (filter.test((V) e.value)) {
                    map.remove((K) e.key);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (EntryImpl e = map.lastEntry; e != null; e = e.previous) {
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


    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<V>[] trySplit(int n) {
        return new CollectionService[] { this }; // No splitting.
    }
}
