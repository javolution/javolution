/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.io.Serializable;
import java.util.Map;

import org.javolution.annotations.Nullable;
import org.javolution.util.function.Order;

/**
 * Default entry implementation A collection view over the map values.
 */
public final class EntryImpl<K, V> implements Map.Entry<K, V>, Serializable {
 
    private static final long serialVersionUID = 0x700L; // Version.
    private final K key;
    private @Nullable V value;

    /** Creates an entry from the specified key/value pair.*/
    public EntryImpl(K key, @Nullable V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) { // Default object equality as per Map.Entry contract.
        if (!(obj instanceof Map.Entry))
            return false;
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> that = (Map.Entry<K, V>) obj;
        return Order.STANDARD.areEqual(key, that.getKey()) && Order.STANDARD.areEqual(value, that.getValue());
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public @Nullable V getValue() {
        return value;
    }

    @Override
    public int hashCode() { // Default hash as per Map.Entry contract.
        return Order.STANDARD.indexOf(key) ^ Order.STANDARD.indexOf(value);
    }

    /** 
     * Guaranteed to throw an exception and leave the entry unmodified.
     * @deprecated Modification of an entry value should be performed through the map put(key, value) method.
     */
    @Override
    public @Nullable V setValue(@Nullable V value) {
        throw new UnsupportedOperationException("Entry modification should be performed through the map");
    }    

    @Override
    public String toString() {
        return "(" + key + '=' + value + ')'; // For debug.
    }

    /** 
     * Sets the value of this entry; this method should only be called by the map updateValue method.
     */
    public @Nullable V setValueUnsafe(@Nullable V newValue) {
        V previousValue = value;
        value = newValue;
        return previousValue;
    }    

}
