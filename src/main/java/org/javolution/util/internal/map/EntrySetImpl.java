/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractMap.Entry;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * An entry set view over a map.
 */
public final class EntrySetImpl<K, V> extends AbstractSet<Entry<K, V>> {

    /** The entry order (must support generic Map.Entry) */
    private static class EntryOrder<K, V> implements Order<Entry<K, V>> {
        private static final long serialVersionUID = 0x700L; // Version.
        private final Order<? super K> keyOrder;
        private final Equality<? super V> valueEquality;

        public EntryOrder(Order<? super K> keyOrder, Equality<? super V> valueEquality) {
            this.keyOrder = keyOrder;
            this.valueEquality = valueEquality;
        }

        @Override
        public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
            return keyOrder.areEqual(left.getKey(), right.getKey())
                    && valueEquality.areEqual(left.getValue(), right.getValue());
        }

        @Override
        public int compare(Entry<K, V> left, Entry<K, V> right) {
            return keyOrder.compare(left.getKey(), right.getKey());
        }

        @Override
        public int indexOf(Entry<K, V> entry) {
            return keyOrder.indexOf(entry.getKey());
        }
    }
    
    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K, V> map;

    public EntrySetImpl(AbstractMap<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(Entry<K, V> entry) {
        Entry<K,V> previous = map.getEntry(entry.getKey());
        if ((previous != null) && (previous.equals(entry))) return false;
        map.put(entry.getKey(), entry.getValue());
        return true;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public AbstractSet<Entry<K, V>> clone() {
        return new EntrySetImpl<K, V>(map.clone());
    }

    @Override
    public Order<? super Entry<K, V>> order() {
        return new EntryOrder<K, V>(map.keyOrder(), map.valuesEquality());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Entry))
            return false;
        Entry<K, V> entry = (Entry<K, V>) obj;
        Entry<K, V> mapEntry = map.getEntry(entry.getKey());
        return map.valuesEquality().areEqual(mapEntry.getValue(), entry.getValue());
    }

    @Override
    public FastIterator<Entry<K,V>> descendingIterator() {
        return map.descendingIterator();
    }

    @Override
    public FastIterator<Entry<K, V>> descendingIterator(Entry<K, V> fromElement) {
        return map.descendingIterator(fromElement.getKey());
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public FastIterator<Entry<K, V>> iterator() {
        return map.iterator();
    }

    @Override
    public FastIterator<Entry<K, V>> iterator(Entry<K, V> fromElement) {
        return map.iterator(fromElement.getKey());
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Entry)) return false;
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = (Entry<K, V>) obj;
        return map.remove(entry.getKey(), entry.getValue());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean addMulti(Entry<K, V> entry) {
        return map.addEntry(entry);
    }

    @Override
    public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
        // TODO Auto-generated method stub
        return false;
    }


}
