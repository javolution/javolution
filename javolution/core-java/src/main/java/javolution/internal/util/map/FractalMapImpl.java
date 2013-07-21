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

/**
 * A fractal-based map with rehash performed only on limited size maps.
 * It is based on a fractal structure with self-similar patterns at any scale
 * (maps holding submaps). At each depth only a part of the hashcode is used
 * starting by the last bits. 
 */
@SuppressWarnings("rawtypes")
final class FractalMapImpl implements Serializable {

    static final int SHIFT = 10; // Number of hashcode bits per depth. 
    static final int EMPTINESS_LEVEL = 2; // Can be 1 (load factor 0.5), 2 (load factor 0.25) or any greater value.
    static final int INITIAL_BLOCK_CAPACITY = 2 << EMPTINESS_LEVEL;
    static final int MAX_BLOCK_CAPACITY = 1 << SHIFT;
    private static final long serialVersionUID = 0x600L; // Version.
    private int count; // Number of entries different from null in this block.
    private FastMapEntryImpl[] entries = new FastMapEntryImpl[INITIAL_BLOCK_CAPACITY]; // Entries value can be a sub-map.
    private final int shift; // Zero if base map.

    public FractalMapImpl() {
        this.shift = 0;
    }

    public FractalMapImpl(int shift) {
        this.shift = shift;
    }

    public void clear() {
        entries = new FastMapEntryImpl[INITIAL_BLOCK_CAPACITY];
        count = 0;
    }

    /** Returns null if no entry with specified key */
    public FastMapEntryImpl getEntry(Object key, int hash) {
        return entries[indexOfKey(key, hash)];
    }

    /** Returns the index of the specified key in the map 
       (points to a null key if key not present). */
    private int indexOfKey(Object key, int hash) {
        int mask = entries.length - 1;
        int i = (hash >> shift) & mask;
        while (true) {
            FastMapEntryImpl entry = entries[i];
            if (entry == null)
                return i;
            if ((entry.hash == hash) && key.equals(entry.key))
                return i;
            i = (i + 1) & mask;
        }
    }

    /** Adds the specified entry if not already present; returns the 
     *  either the specified entry or an existing entry for the specified key. **/
    public FastMapEntryImpl addEntry(FastMapEntryImpl newEntry, Object key, int hash) {
        int i = indexOfKey(key, hash);
        FastMapEntryImpl entry = entries[i];
        if (entry != null) return entry; // Entry exists
        entries[i] = newEntry;
        // Check if we need to resize.
        if ((++count << EMPTINESS_LEVEL) > entries.length) {
            resize(entries.length << 1); 
        }
        return newEntry;        
    }
    
    /** Returns the entry removed or null if none. */
    public FastMapEntryImpl removeEntry(Object key, int hash) {
        int i = indexOfKey(key, hash);
        FastMapEntryImpl oldEntry = entries[i];
        if (oldEntry == null)
            return null; // Entry does not exist.
        entries[i] = null;
        // Since we have made a hole, adjacent keys might have to shift.
        for (;;) {
            // We use a step of 1 (improve caching through memory locality).
            i = (i + 1) & (entries.length - 1);
            FastMapEntryImpl entry = entries[i];
            if (entry == null)
                break; // Done.
            int correctIndex = indexOfKey(entry.key, entry.hash);
            if (correctIndex != i) { // Misplaced.
                entries[correctIndex] = entries[i];
                entries[i] = null;
            }
        }
        // Check if we need to resize.
        if (((--count << (EMPTINESS_LEVEL + 1)) <= entries.length)
                && (entries.length > INITIAL_BLOCK_CAPACITY)) {
            resize(entries.length >> 1);
        }
        return oldEntry;
    }

    // The capacity is a power of two such as: 
    //    (count * 2**EMPTINESS_LEVEL) <=  capacity < (count * 2**(EMPTINESS_LEVEL+1))
    // TODO: Use submaps if max capacity reached.
    private void resize(int newCapacity) {
        FastMapEntryImpl[] newEntries = new FastMapEntryImpl[newCapacity];
        int newMask = newEntries.length - 1;
        for (int i = 0, n = entries.length; i < n; i++) {
            FastMapEntryImpl entry = entries[i];
            if (entry == null)
                continue;
            int newIndex = entry.hash & newMask;
            while (newEntries[newIndex] != null) { // Find empty slot.
                newIndex = (newIndex + 1) & newMask;
            }
            newEntries[newIndex] = entry;
        }
        entries = newEntries;
    }
}
