/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.javolution.util.FastMap;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An sub-map view over a map.
 */
public final class SubMapImpl<K, V> extends FastMap<K, V> {

    /** Iterator bounded by the to limit over the sub-set. */
    private class LowerLimitIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> itr;
        boolean currentIsNext;
        Entry<K, V> current;

        public LowerLimitIterator(Iterator<Entry<K, V>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            if (currentIsNext)
                return true;
            if (!itr.hasNext())
                return false;
            current = itr.next();
            if (tooLow(current.getKey()))
                return false;
            currentIsNext = true;
            return true;
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            if ((currentIsNext) || (current == null)) throw new IllegalStateException();
            SubMapImpl.this.remove(current.getKey());
            current = null;
        }
    }
    /** Iterator bounded by the from limit over the sub-set. */
    private class UpperLimitIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> itr;
        boolean currentIsNext;
        Entry<K, V> current;

        public UpperLimitIterator(Iterator<Entry<K, V>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            if (currentIsNext)
                return true;
            if (!itr.hasNext())
                return false;
            current = itr.next();
            if (tooHigh(current.getKey()))
                return false;
            currentIsNext = true;
            return true;
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            if ((currentIsNext) || (current == null)) throw new IllegalStateException();
            SubMapImpl.this.remove(current.getKey());
            current = null;
        }
    }
    private static final long serialVersionUID = 0x700L; // Version.
    private final FastMap<K, V> inner;
    private final K from;
    private final Boolean fromInclusive;

    private final K to;

    private final Boolean toInclusive;

    /** Returns a sub-map, there is no bound when inclusive Boolean value is null. */
    public SubMapImpl(FastMap<K, V> inner, K from, Boolean fromInclusive, K to, Boolean toInclusive) {
        this.inner = inner;
        this.from = from;
        this.fromInclusive = fromInclusive;
        this.to = to;
        this.toInclusive = toInclusive;
    }

    @Override
    public void clear() {
        for (Iterator<Entry<K, V>> itr = iterator(); itr.hasNext();) {
            itr.next();
            itr.remove();
        }
    }

    @Override
    public FastMap<K, V> clone() {
        return new SubMapImpl<K, V>(inner.clone(), from, fromInclusive, to, toInclusive);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        if (toInclusive == null)
            return new LowerLimitIterator(inner.descendingIterator());
        Iterator<Entry<K, V>> itr = new LowerLimitIterator(inner.descendingIterator(to));
        if (!toInclusive && inner.containsKey(to))
            itr.next(); // Pass element.  
        return itr;
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromElement) {
        if (toInclusive == null)
            return new LowerLimitIterator(inner.descendingIterator(fromElement));
        return (inner.comparator().compare(to, fromElement) <= 0) ? descendingIterator()
                : new LowerLimitIterator(inner.descendingIterator(fromElement));
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inRange(key) ? inner.getEntry(key) : null;
    }

    private boolean inRange(K e) {
        return !tooHigh(e) && !tooLow(e);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        if (fromInclusive == null)
            return new UpperLimitIterator(inner.iterator());
        Iterator<Entry<K, V>> itr = new UpperLimitIterator(inner.iterator(from));
        if (!fromInclusive && inner.containsKey(from))
            itr.next(); // Pass element.  
        return itr;
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromElement) {
        if (fromInclusive == null)
            return new UpperLimitIterator(inner.iterator(fromElement));
        return (inner.comparator().compare(from, fromElement) >= 0) ? iterator()
                : new UpperLimitIterator(inner.iterator(fromElement));
    }

    @Override
    public V put(K key, V value) {
        return inRange(key) ? inner.put(key, value) : null;
    }

    @Override
    public Entry<K,V> putEntry(Entry<? extends K, ? extends V> entry) {
        return inRange(entry.getKey()) ? inner.putEntry(entry) : null;
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        return inRange(key) ? inner.removeEntry(key) : null;
    }

    @Override
    public int size() {
        int count = 0;
        for (Iterator<Entry<K, V>> itr = iterator(); itr.hasNext(); itr.next())
            count++;
        return count;
    }

    private boolean tooHigh(K e) {
        if (toInclusive == null)
            return false;
        int i = inner.comparator().compare(to, e);
        return (i < 0) || ((i == 0) && !toInclusive);
    }

    private boolean tooLow(K e) {
        if (fromInclusive == null)
            return false;
        int i = inner.comparator().compare(from, e);
        return (i > 0) || ((i == 0) && !toInclusive);
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

}
