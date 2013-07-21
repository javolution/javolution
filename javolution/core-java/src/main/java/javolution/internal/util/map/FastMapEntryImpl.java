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
 * The map entry implementation.
 */
final class FastMapEntryImpl<K, V> implements Map.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    int hash;
    K key;
    FastMapEntryImpl<K, V> next;
    FastMapEntryImpl<K, V> previous;
    V value;

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
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }
    
    @Override
    public String toString() {
        return key + "=" + value;
    }

  }
