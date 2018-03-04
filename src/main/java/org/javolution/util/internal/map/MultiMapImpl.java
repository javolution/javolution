/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.annotations.Nullable;
import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * A view for which entries are added even if already there.
 */
public final class MultiMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K, V> inner;

    public MultiMapImpl(AbstractMap<K, V> inner) {
        this.inner = inner;
    }

    @Override
    public AbstractSet<Entry<K, V>> entries() {
        return inner.entries();
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        return inner.getEntry(key);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        return inner.removeEntry(key);
    }

    @Override
    public Order<? super K> keyOrder() {
        return inner.keyOrder();
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return inner.valuesEquality();
    }

    public @Nullable V put(K key, @Nullable V value) {
        inner.addEntry(key, value);
        return null; 
    }

    @Override
    public Entry<K, V> addEntry(K key, V value) {
        return inner.addEntry(key, value);
    }

}
