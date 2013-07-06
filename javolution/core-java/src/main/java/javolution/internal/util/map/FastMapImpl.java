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
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;

import javolution.util.function.Comparators;
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
public final class FastMapImpl<K, V> implements MapService<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    EntryImpl firstEntry = null;
    final EqualityComparator<? super K> keyComparator; 
    EntryImpl lastEntry = null;
    private FractalMapImpl fractal = new FractalMapImpl();
    private EntryImpl freeEntry = new EntryImpl();
    private int size;
    
    public FastMapImpl(EqualityComparator<? super K> keyComparator) {
        this.keyComparator = keyComparator;
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
        return new EntrySetImpl<K, V>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        EntryImpl entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        return (V) entry.value;
    }

    @Override
    public ReadWriteLock getLock() {
        return null;
    }

    @Override
    public SetService<K> keySet() {
        return new KeySetImpl<K, V>(this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        EntryImpl tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new EntryImpl();
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
        EntryImpl tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new EntryImpl();
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
        EntryImpl entry = fractal.removeEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return (V) entry.value;  
    }
    
    @Override
    public boolean remove(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        EntryImpl entry = fractal.getEntry(key, hash);
        if (entry == null) return false;
        if (!Comparators.STANDARD.areEqual(entry.value, value)) return false;
        fractal.removeEntry(key, hash);
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return true;  
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(K key, V value) {
        EntryImpl entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        Object oldValue = entry.value;
        entry.value = value;
        return (V) oldValue;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        EntryImpl entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return false;
        if (!Comparators.STANDARD.areEqual(entry.value, oldValue)) return false;
        entry.value = newValue;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public CollectionService<V> values() {
        return new ValuesImpl<K, V>(this);
    }

    private void attachEntry(EntryImpl entry) {
        if (lastEntry != null) {
            lastEntry.next = entry;
            entry.previous = lastEntry;
        }
        lastEntry = entry;
        if (firstEntry == null) {
            firstEntry = entry;
        }
    }

    private void detachEntry(EntryImpl entry) {
        if (entry == firstEntry) {
            firstEntry = entry.next;
        }
        if (entry == lastEntry) {
            lastEntry = entry.previous;
        }
        EntryImpl previous = entry.previous;
        EntryImpl next = entry.next;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }       
    }
    
}
