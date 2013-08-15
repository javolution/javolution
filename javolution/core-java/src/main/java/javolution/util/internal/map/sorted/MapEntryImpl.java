/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.io.Serializable;
import java.util.Map;

/**
 * The sorted map entry implementation (serializable).
 */
public final class MapEntryImpl<K, V> implements Map.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    K key;
    V value;
  
    public MapEntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
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
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }
    
    @Override
    public String toString() {
        return key + "=" + value;
    }

  }
