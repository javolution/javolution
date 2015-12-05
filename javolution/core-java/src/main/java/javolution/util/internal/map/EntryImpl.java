/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.io.Serializable;
import java.util.Map.Entry;

import javolution.util.function.Equality;

/**
 * The default entry implementation.
 */
public final class EntryImpl<K, V> implements Entry<K, V>, Serializable {
	private static final long serialVersionUID = 0x700L; // Version.
	private final K key;
	private V value;

	public EntryImpl(K key, V value) {
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
	public V setValue(V newValue) {
		V oldValue = value;
		this.value = newValue;
		return oldValue;
	}

	@Override
	public boolean equals(Object obj) { // As per Map.Entry contract.
		if (!(obj instanceof Entry))
			return false;
		@SuppressWarnings("unchecked")
		Entry<K, V> that = (Entry<K, V>) obj;
		return Equality.STANDARD.areEqual(key, that.getKey())
				&& Equality.STANDARD.areEqual(value, that.getValue());
	}

	@Override
	public int hashCode() { // As per Map.Entry contract.
		return (key == null ? 0 : key.hashCode())
				^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public String toString() {
		return "(" + key + '=' + value + ')'; // For debug.
	}

}
