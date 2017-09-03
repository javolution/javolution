/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.AbstractMap;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.FilteredCollectionImpl;

/**
 * A filtered view over a map.
 */
public final class FilteredMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K, V> inner;
    private final Predicate<? super Entry<K,V>> filter;

    public FilteredMapImpl(AbstractMap<K, V> inner, Predicate<? super Entry<K,V>> filter) {
        this.inner = inner;
        this.filter = filter;
    }

    @Override
    public void clear() {
        inner.removeIf(filter);
    }

    @Override
    public AbstractMap<K, V> clone() {
        return new FilteredMapImpl<K, V>(inner.clone(), filter);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public FastIterator<Entry<K, V>> descendingIterator() {
        return new FilteredCollectionImpl.IteratorImpl<Entry<K, V>>(inner.descendingIterator(), filter);
    }

    @Override
    public FastIterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new FilteredCollectionImpl.IteratorImpl<Entry<K, V>>(inner.descendingIterator(fromKey), filter);
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        Entry<K,V> entry = inner.getEntry(key);
        return filter.test(entry) ? entry : null;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public FastIterator<Entry<K, V>> iterator() {
        return new FilteredCollectionImpl.IteratorImpl<Entry<K, V>>(inner.iterator(), filter);
    }

    @Override
    public FastIterator<Entry<K, V>> iterator(K fromKey) {
        return new FilteredCollectionImpl.IteratorImpl<Entry<K, V>>(inner.iterator(fromKey), filter);
    }

    @Override
    public V put(K key, V value) {
        if (!filter.test(new Entry<K,V>(key, value))) return null; 
        return inner.put(key, value);
    }    

    @Override
    public Entry<K,V> putEntry(Entry<K, V> entry) {
        return filter.test(entry) ? inner.putEntry(entry) : null;
    }
    
    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K,V> previous = getEntry(key);
        return filter.test(previous) ? removeEntry(key) : null;
    }

    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

    @Override
    public boolean removeIf(final Predicate<? super Entry<K, V>> toRemove) {
        return inner.removeIf(new Predicate<Entry<K, V>>() {

            @Override
            public boolean test(Entry<K, V> param) {
                return filter.test(param) && toRemove.test(param);
            }});
     }

}
