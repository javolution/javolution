/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.NoSuchElementException;

import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An unordered map.
 */
public final class UnorderedMapImpl<K, V> extends FastMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final FastTable<Entry<K, V>> entries = FastTable.newTable();
    private final Equality<? super K> keyEquality;

    public UnorderedMapImpl(Equality<? super K> keyEquality) {
        this.keyEquality = keyEquality;
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public UnorderedMapImpl<K, V> clone() {
        UnorderedMapImpl<K, V> copy = new UnorderedMapImpl<K, V>(keyEquality);
        copy.entries.addAll(entries);
        return copy;
    }

    @Override
    public Order<? super K> comparator() {
        return null;
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> descendingIterator() {
        return ReadOnlyIterator.of(entries.descendingIterator());
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> descendingIterator(K fromKey) {
        int i = indexOf(fromKey);
        if (i < 0)
            throw new NoSuchElementException(fromKey + ": not found");
        return ReadOnlyIterator.of(entries.subTable(0, i + 1).descendingIterator());
    }

    /** Returns the entry with the specified key.*/
    public Entry<K, V> getEntry(K key) {
        int i = indexOf(key);
        if (i < 0)
            return null;
        return entries.get(i);
    }

    /** Returns the index for the specified key.*/
    private int indexOf(K key) {
        for (int i = 0, n = entries.size(); i < n; i++)
            if (keyEquality.areEqual(entries.get(i).getKey(), key))
                return i;
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> iterator() {
        return ReadOnlyIterator.of(entries.iterator());
    }

    @Override
    public ReadOnlyIterator<Entry<K, V>> iterator(K fromKey) {
        int i = indexOf(fromKey);
        if (i < 0)
            throw new NoSuchElementException(fromKey + ": not found");
        return ReadOnlyIterator.of(entries.listIterator(i));
    }

    @Override
    public V put(K key, V value) {
        Entry<K, V> entry = getEntry(key);
        if (entry != null)
            return entry.setValue(value);
        entry = new TrieNodeImpl.EntryNode<K, V>(-1, key, value);
        entries.addFirst(entry);
        return null;
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        int i = indexOf(key);
        return i >= 0 ? entries.remove(i) : null;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return Equality.DEFAULT;
    }
}
