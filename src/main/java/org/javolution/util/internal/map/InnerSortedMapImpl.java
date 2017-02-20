/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Comparator;
import java.util.Iterator;

import org.javolution.util.FastMap;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayImpl;

/**
 * Sorted map implementation.
 */
public final class InnerSortedMapImpl<K, V> extends FastMap<K, V> implements SparseArrayImpl.Inner<K, FastMap.Entry<K,V>> {

    private static final long serialVersionUID = 0x700L; // Version.
    private FractalTable<Entry<K,V>> entries = new FractalTable<Entry<K,V>>();
    private final Order<? super K> keyOrder;

    public InnerSortedMapImpl(final Order<? super K> keyOrder) {
        this.keyOrder = keyOrder;
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public InnerSortedMapImpl<K, V> clone() {
        InnerSortedMapImpl<K,V> copy = (InnerSortedMapImpl<K,V>)super.clone();
        copy.entries = entries.clone();
        return copy;
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return entries.descendingIterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        int i = insertionIndexOf(fromKey, keyOrder, 0, entries.size());
        return entries.subTable(0, i).descendingIterator();
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        int i = insertionIndexOf(key, keyOrder, 0, entries.size());
        if (i == entries.size()) return null;
        Entry<K,V> previous = entries.get(i);
        return keyOrder.areEqual(key, previous.getKey()) ? previous : null;
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entries.iterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        int i = insertionIndexOf(fromKey, keyOrder, 0, entries.size());
        return entries.subTable(i, entries.size()).iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K,V> putEntry(Entry<? extends K, ? extends V> entry) {
        int i = insertionIndexOf(entry.getKey(), keyOrder, 0, entries.size());
        Entry<K,V> previous = (i != entries.size()) ? entries.get(i) : null;
        if ((previous != null) && keyOrder.areEqual(previous.getKey(), entry.getKey())) 
            return entries.set(i, (Entry<K,V>)entry); 
        entries.add(i, (Entry<K,V>)entry);
        return null;
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        int i = insertionIndexOf(key, keyOrder, 0, entries.size());
        Entry<K,V> previous = (i != entries.size()) ? entries.get(i) : null;
        if ((previous != null) && keyOrder.areEqual(previous.getKey(), key)) 
            return entries.remove(i); 
        return null;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return Equality.DEFAULT;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Only putEntry is supported");
    }

    @Override
    public Order<? super K> keyOrder() {
        return keyOrder;
    }
    
    private int insertionIndexOf(K key, Comparator<? super K> cmp, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        int test = cmp.compare(key, entries.get(start + half).getKey()); 
        if (test == 0) return start + half; // Found.
        return (test < 0) ? insertionIndexOf(key, cmp, start, half) :
             insertionIndexOf(key, cmp, start + half + 1, length - half - 1);
    }

}
