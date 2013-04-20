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

import javolution.util.FastComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.MapService;

/**
 * Parent class to facilitate MapService custom implementations.
 */
public abstract class AbstractMapImpl<K, V> implements MapService<K, V>, Serializable {
    
    @Override
    public abstract int size();    
    
    /** Indicates if this map contains the specified key using the specified hash value. */
    public abstract boolean containsKey(K key, int hash);

    /** Returns the value for the specified key using the specified hash value. */
    public abstract V get(K key, int hash);

    /** Associates the specified key and value using the specified hash value. */
    public abstract V put(K key, V value, int hash);

    /** Removes the specified key using the specified hash value. */
    public abstract V remove(K key, int hash);

    /** Returns the entry for the specified key (or <code>null</code> if none). */    /** Returns iterator over entries */
    public abstract Iterator<Map.Entry<K,V>> entriesIterator();

    @Override
    public boolean containsKey(K key) {
        return containsKey(key, keyComparator().hashCodeOf(key));
    }

    @Override
    public V get(K key) {
        return get(key, keyComparator().hashCodeOf(key));
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, keyComparator().hashCodeOf(key));
    }

    @Override
    public V remove(K key) {
        return remove(key, keyComparator().hashCodeOf(key));
    }

    @Override
    public CollectionService<Entry<K, V>> entrySet() {
        return new EntrySetImpl<K, V>(this);
    }

    @Override
    public CollectionService<V> values() {
        return new ValuesImpl<K, V>(this);
    }

    @Override
    public CollectionService<K> keySet() {
        return new KeySetImpl<K, V>(this);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return (!containsKey(key)) ? put(key, value) : get(key);
    }

    @Override
    public boolean remove(K key, V value) {
        if (containsKey(key) && get(key).equals(value)) {
              remove(key);
              return true;
        } else {
           return false;
        }
    }

    @Override
    public V replace(K key, V value) {
        return (containsKey(key)) ? put(key, value) : null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (containsKey(key) && get(key).equals(oldValue)) {
            put(key, newValue);
            return true;
        } else {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ComparatorService<K> keyComparator() {
        return (ComparatorService<K>) FastComparator.DEFAULT;
    }
 
    private static final long serialVersionUID = 6261409427238347224L;
  }
