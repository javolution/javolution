/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.Map;

/**
 * The hash map entry implementation (not serializable).
 */
final class MapEntryImpl<K, V> implements Map.Entry<K, V> {

    int hash;
    K key;
    MapEntryImpl<K, V> next;
    MapEntryImpl<K, V> previous;
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
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean equals(Object obj) {
		// As per Map.Entry contract.
    	if (!(obj instanceof Map.Entry)) return false;
    	Map.Entry that = (Map.Entry) obj;
    	return (this.getKey()==null ?
    		      that.getKey()==null : this.getKey().equals(that.getKey()))  &&
    		      (this.getValue()==null ?
    		       that.getValue()==null : this.getValue().equals(that.getValue()));
    }

	@Override
    public int hashCode() {
		// As per Map.Entry contract.
    	return  (getKey()==null   ? 0 : getKey().hashCode()) ^
    		     (getValue()==null ? 0 : getValue().hashCode());
    }

}
