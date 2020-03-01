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
import org.javolution.util.FastMap;
import org.javolution.util.FastMap.EntryWithoutValue;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.set.SubSetImpl;

/**
 * An sub-map view over a map.
 */
public final class SubMapImpl<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractMap<K,V> inner;
    private final K fromKey; 
    private final K toKey;
    private final boolean fromInclusive;
    private final boolean toInclusive;

    /** Returns a sub-set, there is no bound when null element. */
    public SubMapImpl(AbstractMap<K,V> inner, @Nullable K fromKey, 
            boolean fromInclusive, @Nullable K toKey, boolean toInclusive) {
        this.inner = inner;
        this.fromKey = fromKey;
        this.fromInclusive = fromInclusive;
        this.toKey = toKey;
        this.toInclusive = toInclusive;
    }

    @Override
    public SubSetImpl<Entry<K, V>> entries() {
        return new SubSetImpl<Entry<K, V>>(inner.entries(), new EntryWithoutValue<>(fromKey), fromInclusive,
                new EntryWithoutValue<>(toKey), toInclusive);
    }

    @Override
    public Entry<K, V> getEntry(K key) {
        if (!inRange(key)) 
            throw new UnsupportedOperationException("key out of sub-map range");
        return inner.getEntry(key);
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        if (!inRange(key)) 
            throw new UnsupportedOperationException("key out of sub-map range");
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

    private boolean inRange(K key) {
        return !tooHigh(key) && !tooLow(key);
    }

    private boolean tooHigh(K key) {
        if (toKey == null) return false;
        int cmp = keyOrder().compare(toKey, key);
        return toInclusive ? cmp < 0 : cmp <= 0;
    }

    private boolean tooLow(K key) {
        if (fromKey == null) return false;
        int cmp = keyOrder().compare(fromKey, key);
        return fromInclusive ? cmp > 0 : cmp >= 0;
    }

    @Override
    public Entry<K,V> addEntry(K key, V value) {
        if (!inRange(key)) 
            throw new UnsupportedOperationException("key out of sub-map range");
        return inner.addEntry(key, value);
    }
    
}
