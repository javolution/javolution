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

import org.javolution.util.FastMap;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.collection.ReadOnlyIteratorImpl;

/**
 * An unmodifiable view over a map.
 */
public final class UnmodifiableMapImpl<K,V> extends FastMap<K,V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Unmodifiable View.";
   private final FastMap<K,V> inner;
  
    public UnmodifiableMapImpl(FastMap<K,V> inner) {
        this.inner = inner;
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Iterator<Entry<K,V>> iterator() {
        return ReadOnlyIteratorImpl.of(inner.iterator());
    }
    
    @Override
    public Iterator<Entry<K,V>> descendingIterator() {
        return ReadOnlyIteratorImpl.of(inner.descendingIterator());
    }
        
    @Override
    public Iterator<Entry<K,V>> iterator(K fromKey) {
        return ReadOnlyIteratorImpl.of(inner.iterator(fromKey));
    }
    
    @Override
    public Iterator<Entry<K,V>> descendingIterator(K fromKey) {
        return ReadOnlyIteratorImpl.of(inner.descendingIterator(fromKey));
    }
    
    @Override
    public FastMap<K, V> clone() {
        return new UnmodifiableMapImpl<K, V>(inner.clone());
    }

    @Override
    public Order<? super K> comparator() { // Immutable.
        return inner.comparator();
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public Entry<K, V> putEntry(K key, V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Equality<? super V> valuesEquality() { // Immutable.
        return inner.valuesEquality();
    }
    
    @Override
    public UnmodifiableMapImpl<K,V> unmodifiable() {
        return this;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

}
