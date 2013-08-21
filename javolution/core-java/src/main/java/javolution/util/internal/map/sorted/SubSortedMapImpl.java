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
import java.util.NoSuchElementException;

import javolution.util.function.Equality;
import javolution.util.service.SortedMapService;

/**
 * A view over a portion of a sorted map. 
 */
public class SubSortedMapImpl<K, V> extends SortedMapView<K, V> {

    /** Peeking ahead iterator. */
    private class IteratorImpl implements Iterator<Entry<K, V>> {

        private boolean ahead;
        private final Equality<? super K> cmp = keyComparator();
        private Entry<K, V> next;
        private final Iterator<Entry<K, V>> targetIterator = target()
                .iterator();

        @Override
        public boolean hasNext() {
            if (ahead) return true;
            while (targetIterator.hasNext()) {
                next = targetIterator.next();
                if ((from != null) && (cmp.compare(next.getKey(), from) < 0)) continue;
                if ((to != null) && (cmp.compare(next.getKey(), to) >= 0)) break;
                ahead = true;
                return true;
            }
            return false;
        }

        @Override
        public Entry<K, V> next() {
            hasNext(); // Moves ahead.
            ahead = false;
            return next;
        }

        @Override
        public void remove() {
            targetIterator.remove();
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.

    private final K from; // Can be null. 
    private final K to; // Can be null.

    public SubSortedMapImpl(SortedMapService<K, V> target, K from, K to) {
        super(target);
        if ((from != null) && (to != null)
                && (keyComparator().compare(from, to) > 0)) throw new IllegalArgumentException(
                "from: " + from + ", to: " + to); // As per SortedSet contract.
        this.from = from;
        this.to = to;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        Equality<? super K> cmp = keyComparator();
        if ((from != null) && (cmp.compare((K) key, from) < 0)) return false;
        if ((to != null) && (cmp.compare((K) key, to) >= 0)) return false;
        return target().containsKey(key);
    }

    @Override
    public K firstKey() {
        if (from == null) return target().firstKey();
        Iterator<Entry<K, V>> it = iterator();
        if (!it.hasNext()) throw new NoSuchElementException();
        return it.next().getKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        Equality<? super K> cmp = keyComparator();
        if ((from != null) && (cmp.compare((K) key, from) < 0)) return null;
        if ((to != null) && (cmp.compare((K) key, to) >= 0)) return null;
        return target().get(key);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new IteratorImpl();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return target().keyComparator();
    }

    @Override
    public K lastKey() {
        if (to == null) return target().lastKey();
        Iterator<Entry<K, V>> it = iterator();
        if (!it.hasNext()) throw new NoSuchElementException();
        Entry<K, V> last = it.next();
        while (it.hasNext()) {
            last = it.next();
        }
        return last.getKey();
    }

    @Override
    public V put(K key, V value) {
        Equality<? super K> cmp = keyComparator();
        if ((from != null) && (cmp.compare((K) key, from) < 0)) throw new IllegalArgumentException(
                "Key: " + key + " outside of this sub-map bounds");
        if ((to != null) && (cmp.compare((K) key, to) >= 0)) throw new IllegalArgumentException(
                "Key: " + key + " outside of this sub-map bounds");
        return target().put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        Equality<? super K> cmp = keyComparator();
        if ((from != null) && (cmp.compare((K) key, from) < 0)) return null;
        if ((to != null) && (cmp.compare((K) key, to) >= 0)) return null;
        return target().remove(key);
    }

    @Override
    public Equality<? super V> valueComparator() {
        return target().valueComparator();
    }

}
