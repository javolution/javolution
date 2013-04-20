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
 * The parent class for all table implementations.
 */
public abstract class AbstractMapImpl<K, V> implements MapService<K, V>, Serializable {
    
    /** Returns the entry for the specified key (or <code>null</code> if none). */
    public abstract Map.Entry<K,V> getEntry(K key);

    /** Returns iterator over entries */
    public abstract Iterator<Map.Entry<K,V>> entriesIterator();

    @Override
    public boolean containsKey(K key) {        
        return getEntry(key) != null;
    }

    @Override
    public V get(K key) {
        Entry<K, V> entry = getEntry(key);
        return (entry != null) ? entry.getValue() : null;
    }

    @Override
    public abstract V put(K key, V value);


    @Override
    public abstract V remove(K key);

    @Override
    public abstract int size();

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
 
}
