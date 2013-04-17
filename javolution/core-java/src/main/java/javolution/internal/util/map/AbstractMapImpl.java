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
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.MapService;

/**
 * The parent class for all table implementations.
 */
public abstract class AbstractMapImpl<K, V> implements MapService<K, V>, Serializable {
    
    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public V get(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public CollectionService<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CollectionService<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CollectionService<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(K key, V value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public V replace(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public ComparatorService<K> keyComparator() {
        // TODO Auto-generated method stub
        return null;
    }

    //
    // Entry implementation.
    //

    public static final class EntryImpl<K, V> implements Map.Entry<K, V> {

        final K key;

        V value;

        int hash;

        EntryImpl(K key, V value, int hash) {
            this.key = key;
            this.value = value;
            this.hash = hash;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V previous = this.value;
            this.value = value;
            return previous;
        }

    }    
}
