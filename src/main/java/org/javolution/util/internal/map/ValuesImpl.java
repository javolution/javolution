/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.AbstractCollection;
import org.javolution.util.AbstractMap.Entry;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A collection view over the map values.
 */
public final class ValuesImpl<K, V> extends AbstractCollection<V> {

    /** A generic iterator over the map values. */
    private static class IteratorImpl<K, V> implements FastIterator<V> {
        final FastIterator<Entry<K, V>> entriesItr;

        public IteratorImpl(FastIterator<Entry<K, V>> entriesItr) {
            this.entriesItr = entriesItr;
        }

        @Override
        public boolean hasNext() {
            return entriesItr.hasNext();
        }

        @Override
        public V next() {
            return entriesItr.next().getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext(final Predicate<? super V> matching) {
            return entriesItr.hasNext(new Predicate<Entry<K,V>>() {

                @Override
                public boolean test(Entry<K, V> param) {
                    return matching.test(param.getValue());
                }});
        }
    }

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<Entry<K, V>> entries;
    private final Equality<? super V> equality;

    public ValuesImpl(AbstractSet<Entry<K, V>> entries, Equality<? super V> equality) {
        this.entries = entries;
        this.equality = equality;
    }

    @Override
    public boolean add(V element) {
        throw new UnsupportedOperationException("FastMap.values() does not support adding new values.");
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public AbstractCollection<V> clone() {
        return new ValuesImpl<K, V>(entries.clone(), equality);
    }

    @Override
    public boolean removeIf(final Predicate<? super V> filter) {
        return entries.removeIf(new Predicate<Entry<K,V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param.getValue());
            }});
    }

    @Override
    public FastIterator<V> iterator() {
        return new IteratorImpl<K,V>(entries.iterator());
    }

    @Override
    public FastIterator<V> descendingIterator() {
        return new IteratorImpl<K,V>(entries.descendingIterator());
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Equality<? super V> equality() {
        return equality;
    }

    @Override
    public AbstractCollection<V>[] trySplit(int n) {
        AbstractSet<Entry<K,V>>[] entriesSplit = entries.trySplit(n);
        @SuppressWarnings("unchecked")
        AbstractCollection<V>[] split = new AbstractCollection[entriesSplit.length];
        for (int i=0; i < split.length; i++) split[i] = new ValuesImpl<K,V>(entriesSplit[i], equality);
        return split;
    }


}
