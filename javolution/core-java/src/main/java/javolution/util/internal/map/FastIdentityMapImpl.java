/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.*;

import javolution.util.function.Equalities;
import javolution.util.function.Equality;

public class FastIdentityMapImpl<K, V> extends MapView<K, V> {

    private static final long serialVersionUID = 0x620L; // Version.
    transient IdentityMapEntryImpl<K, V> firstEntry = null;
    transient IdentityMapEntryImpl<K, V> freeEntry = new IdentityMapEntryImpl<K, V>();
    transient IdentityMapEntryImpl<K, V> lastEntry = null;

    static final int EMPTINESS_LEVEL = 2; // Can be 1 (load factor 0.5), 2 (load factor 0.25) or any greater value.
    static final int INITIAL_BLOCK_CAPACITY = 2 << EMPTINESS_LEVEL;
    private IdentityMapEntryImpl[] entries = new IdentityMapEntryImpl[INITIAL_BLOCK_CAPACITY]; // Entries value can be a sub-map.
    
    transient int size;

    @SuppressWarnings("unchecked")
    public FastIdentityMapImpl() {
        super(null);
    }

    @SuppressWarnings("unchecked")
    public IdentityMapEntryImpl addEntry(IdentityMapEntryImpl newEntry, Object key) {
        int hash = System.identityHashCode(key);
        int i = indexOfKey(hash);
        IdentityMapEntryImpl entry = entries[i];
        if (entry != null) return entry; // Entry exists
        entries[i] = newEntry;
        newEntry.key = key;
        newEntry.hash = hash;
        // Check if we need to resize.
        if ((++size << EMPTINESS_LEVEL) > entries.length) {
            resize(entries.length << 1);
        }
        return newEntry;
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        firstEntry = null;
        lastEntry = null;
        entries = new IdentityMapEntryImpl[INITIAL_BLOCK_CAPACITY];
        size = 0;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public FastIdentityMapImpl<K, V> clone() { // Makes a copy.
        FastIdentityMapImpl<K, V> copy = new FastIdentityMapImpl<K, V>();
        copy.putAll(this);
        return copy;
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        IdentityMapEntryImpl<K, V> entry = getEntry(key);
        if (entry == null) return null;
        return entry.value;
    }

    public IdentityMapEntryImpl getEntry(Object key) {
        int hash = System.identityHashCode(key);
        return entries[indexOfKey(hash)];
    }

    private int indexOfKey(int hash) {
        int mask = entries.length - 1;
        int i = hash & mask;
        while (true) {
            IdentityMapEntryImpl entry = entries[i];
            if (entry == null || entry.hash == hash) return i;
            i = (i + 1) & mask;
        }
    }
    
    @Override
    public boolean isEmpty(){
        return size == 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new Iterator<Entry<K, V>>() {
            IdentityMapEntryImpl<K, V> current;
            IdentityMapEntryImpl<K, V> next = firstEntry;

            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public java.util.Map.Entry<K, V> next() {
                if (next == null) throw new NoSuchElementException();
                current = next;
                next = next.next;
                return current;
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                removeEntry(current.key, current.hash);
                detachEntry(current); // Entry is not referenced anymore and will be gc.
                size--;
            }
        };

    }

    @Override
    public Equality<? super K> keyComparator() {
        return Equalities.IDENTITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        IdentityMapEntryImpl<K, V> tmp = addEntry(freeEntry, key);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new IdentityMapEntryImpl<K, V>();
            attachEntry(tmp);
            tmp.value = value;
            return null;
        } else { // Existing entry.
            V oldValue = tmp.value;
            tmp.value = value;
            return oldValue;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(K key, V value) {
        IdentityMapEntryImpl<K, V> tmp = addEntry(freeEntry, key);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new IdentityMapEntryImpl<K, V>();
            attachEntry(tmp);
            size++;
            tmp.value = value;
            return null;
        } else { // Existing entry.
            return tmp.value;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        IdentityMapEntryImpl<K, V> entry = removeEntry(key, System.identityHashCode(key));
        if (entry == null) return null;
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return entry.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object key, Object value) {
        IdentityMapEntryImpl<K, V> entry = getEntry(key);
        if (entry == null) return false;
        if (entry.value != value) return false;
        removeEntry(key, entry.hash);
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return true;
    }

    public IdentityMapEntryImpl removeEntry(Object key, int hash) {
        int i = indexOfKey(hash);
        IdentityMapEntryImpl oldEntry = entries[i];
        if (oldEntry == null) return null; // Entry does not exist.
        entries[i] = null;
        // Since we have made a hole, adjacent keys might have to shift.
        for (;;) {
            // We use a step of 1 (improve caching through memory locality).
            i = (i + 1) & (entries.length - 1);
            IdentityMapEntryImpl entry = entries[i];
            if (entry == null) break; // Done.
            int correctIndex = indexOfKey(entry.hash);
            if (correctIndex != i) { // Misplaced.
                entries[correctIndex] = entries[i];
                entries[i] = null;
            }
        }
        // Check if we need to resize.
        if (((--size << (EMPTINESS_LEVEL + 1)) <= entries.length)
                && (entries.length > INITIAL_BLOCK_CAPACITY)) {
            resize(entries.length >> 1);
        }
        return oldEntry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(K key, V value) {
        IdentityMapEntryImpl<K, V> entry = getEntry(key);
        if (entry == null) return null;
        V oldValue = entry.value;
        entry.value = value;
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        IdentityMapEntryImpl<K, V> entry = getEntry(key);
        if (entry == null || entry.value != oldValue) return false;
        entry.value = newValue;
        return true;
    }

    // The capacity is a power of two such as: 
    //    (count * 2**EMPTINESS_LEVEL) <=  capacity < (count * 2**(EMPTINESS_LEVEL+1))
    // TODO: Use submaps if max capacity reached.
    private void resize(int newCapacity) {
        IdentityMapEntryImpl[] newEntries = new IdentityMapEntryImpl[newCapacity];
        int newMask = newEntries.length - 1;
        for (int i = 0, n = entries.length; i < n; i++) {
            IdentityMapEntryImpl entry = entries[i];
            if (entry == null) continue;
            int newIndex = entry.hash & newMask;
            while (newEntries[newIndex] != null) { // Find empty slot.
                newIndex = (newIndex + 1) & newMask;
            }
            newEntries[newIndex] = entry;
        }
        entries = newEntries;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Equality<? super V> valueComparator() {
        return Equalities.IDENTITY;
    }

    private void attachEntry(IdentityMapEntryImpl<K, V> entry) {
        if (lastEntry != null) {
            lastEntry.next = entry;
            entry.previous = lastEntry;
        }
        lastEntry = entry;
        if (firstEntry == null) {
            firstEntry = entry;
        }
    }

    private void detachEntry(IdentityMapEntryImpl<K, V> entry) {
        if (entry == firstEntry) {
            firstEntry = entry.next;
        }
        if (entry == lastEntry) {
            lastEntry = entry.previous;
        }
        IdentityMapEntryImpl<K, V> previous = entry.previous;
        IdentityMapEntryImpl<K, V> next = entry.next;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }
    }
}
