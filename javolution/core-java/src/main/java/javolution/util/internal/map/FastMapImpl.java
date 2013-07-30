/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.io.Serializable;
import java.util.Map.Entry;

import javolution.util.function.EqualityComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 * The default {@link javolution.util.FastMap FastMap} implementation 
 * based on {@link FractalMapImpl fractal maps}. 
 * This implementation ensures that no more than 3/4 of the map capacity is
 * ever wasted.
 */
public class FastMapImpl<K, V> implements MapService<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    final EqualityComparator<? super K> keyComparator; 
    final EqualityComparator<? super V> valueComparator; 
    FastMapEntryImpl<K,V> firstEntry = null;
    FastMapEntryImpl<K,V> lastEntry = null;
    FractalMapImpl fractal = new FractalMapImpl();
    FastMapEntryImpl<K,V> freeEntry = new FastMapEntryImpl<K,V>();
    int size;
    
    public FastMapImpl(EqualityComparator<? super K> keyComparator, 
            final EqualityComparator<? super V> valueComparator) {
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
    }

    @Override
    public void atomic(Runnable update) {
        update.run();
    }

    @Override
    public void clear() {
        firstEntry = null;
        lastEntry = null;
        fractal = new FractalMapImpl();
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {        
        return fractal.getEntry(key, keyComparator.hashCodeOf(key)) != null;
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new FastMapEntrySetImpl<K, V>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        return entry.value;
    }

    @Override
    public SetService<K> keySet() {
        return new FastMapKeySetImpl<K, V>(this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        FastMapEntryImpl<K,V> tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new FastMapEntryImpl<K,V>();
            attachEntry(tmp);
            size++;
            tmp.hash = hash;
            tmp.key = key;
            tmp.value = value;
            return null;
        } else { // Existing entry.
            V oldValue = (V) tmp.value;
            tmp.value = value;
            return oldValue;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        FastMapEntryImpl<K,V> tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new FastMapEntryImpl<K,V>();
            attachEntry(tmp);
            size++;
            tmp.hash = hash;
            tmp.key = key;
            tmp.value = value;
            return null;
        } else { // Existing entry.
            return (V) tmp.value;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V remove(K key) {
        FastMapEntryImpl<K,V> entry = fractal.removeEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return entry.value;  
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, hash);
        if (entry == null) return false;
        if (!valueComparator.areEqual(entry.value, value)) return false;
        fractal.removeEntry(key, hash);
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return true;  
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(K key, V value) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        V oldValue = entry.value;
        entry.value = value;
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return false;
        if (!valueComparator.areEqual(entry.value, oldValue)) return false;
        entry.value = newValue;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public CollectionService<V> values() {
        return new FastMapValuesImpl<K, V>(this);
    }

    private void attachEntry(FastMapEntryImpl<K,V> entry) {
        if (lastEntry != null) {
            lastEntry.next = entry;
            entry.previous = lastEntry;
        }
        lastEntry = entry;
        if (firstEntry == null) {
            firstEntry = entry;
        }
    }

    private void detachEntry(FastMapEntryImpl<K,V> entry) {
        if (entry == firstEntry) {
            firstEntry = entry.next;
        }
        if (entry == lastEntry) {
            lastEntry = entry.previous;
        }
        FastMapEntryImpl<K,V> previous = entry.previous;
        FastMapEntryImpl<K,V> next = entry.next;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }       
    }
    
}
