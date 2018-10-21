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
import org.javolution.util.AbstractSet;
import org.javolution.util.AbstractTable;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.set.LinkedSetImpl;

/**
 * An linked view over a map.
 */
public final class LinkedMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private AbstractMap<K,V> inner;
    private final AbstractTable<Entry<K,V>> insertionTable;
 
    public LinkedMapImpl(AbstractMap<K,V> inner) {
        this.inner = inner;
        this.insertionTable = new FastTable<Entry<K,V>>().equality(Equality.identity());
    }

    @Override
    public AbstractSet<Entry<K, V>> entries() {
        return new LinkedSetImpl<Entry<K,V>>(inner.entries(), insertionTable);
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        Entry<K,V> removed = inner.removeEntry(key);
        if (removed != null) insertionTable.remove(removed);
        return removed;
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

    @Override
    public Entry<K,V> addEntry(K key, V value) {
       Entry<K, V> entry = inner.addEntry(key, value);
       insertionTable.add(entry);
       return entry; 
    }

}
