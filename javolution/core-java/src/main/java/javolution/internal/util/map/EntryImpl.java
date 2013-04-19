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
import java.util.Map;

/**
 * The parent class for all table implementations.
 */

public final class EntryImpl<K, V> implements Map.Entry<K, V>, Serializable {

    final K key;

    V value;

    int hash;

    EntryImpl(K key, V value, int hash) {
        this.key = key;
        this.value = value;
        this.hash = hash;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V previous = this.value;
        this.value = value;
        return previous;
    }

    private static final long serialVersionUID = -840205841264586393L;

}
