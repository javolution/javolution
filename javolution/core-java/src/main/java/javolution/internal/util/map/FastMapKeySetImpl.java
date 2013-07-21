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
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * The keys view over a map.
 */
public class FastMapKeySetImpl<K, V> implements SetService<K>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public FastMapKeySetImpl(FastMapImpl<K, V> map) {
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

    @Override
    public void forEach(
            Consumer<? super K> consumer, IterationController controller) {
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                consumer.accept(e.key);
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                consumer.accept(e.key);
                if (controller.isTerminated())
                    break;
            }
        }
     }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            FastMapEntryImpl<K,V> current;
            FastMapEntryImpl<K,V> next = map.firstEntry;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public K next() {
                current = next;
                if (current == null) throw new NoSuchElementException();
                next = current.next;
                return current.key;
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
    public boolean remove(K key) {
        int size = map.size();
        map.remove(key);
        return size != map.size();
    }

    @Override
    public boolean removeIf(
            Predicate<? super K> filter, IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (FastMapEntryImpl<K,V> e = map.firstEntry; e != null; e = e.next) {
                 if (filter.test(e.key)) {
                    map.remove(e.key);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (FastMapEntryImpl<K,V> e = map.lastEntry; e != null; e = e.previous) {
                if (filter.test(e.key)) {
                    map.remove(e.key);
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
    public CollectionService<K>[] trySplit(int n) {
        return SplitCollectionImpl.splitOf(this, n);
    }

    @Override
    public void atomic(Runnable update) {
        map.atomic(update);   
    }
}
