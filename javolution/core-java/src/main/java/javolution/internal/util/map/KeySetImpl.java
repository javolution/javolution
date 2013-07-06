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

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * The keys view over a map.
 */
public final class KeySetImpl<K, V> implements SetService<K>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public KeySetImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(K key) {
        int size = map.size();
        map.put(key, null);
        return (size != map.size()); 
     }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public EqualityComparator<? super K> comparator() {
        return map.keyComparator;
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(
            Consumer<? super K> consumer, IterationController controller) {
        if (!controller.doReversed()) {
            for (EntryImpl e = map.firstEntry; e != null; e = e.next) {
                consumer.accept((K) e.key);
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (EntryImpl e = map.lastEntry; e != null; e = e.previous) {
                consumer.accept((K) e.key);
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
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            EntryImpl current;
            EntryImpl next = map.firstEntry;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public K next() {
                current = next;
                if (current == null) throw new NoSuchElementException();
                next = current.next;
                return (K) current.key;
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

    @Override
    public boolean remove(K key) {
        int size = map.size();
        map.remove(key);
        return size != map.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeIf(
            Predicate<? super K> filter, IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (EntryImpl e = map.firstEntry; e != null; e = e.next) {
                 if (filter.test((K) e.key)) {
                    map.remove((K) e.key);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (EntryImpl e = map.lastEntry; e != null; e = e.previous) {
                if (filter.test((K) e.key)) {
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
    public int size() {
        return map.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<K>[] trySplit(int n) {
        return new CollectionService[] { this }; // No splitting.
    }
}
