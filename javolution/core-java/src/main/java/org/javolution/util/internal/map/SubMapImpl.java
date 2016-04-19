/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.FastMap;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An sub-map view over a map.
 */
public final class SubMapImpl<K, V> extends FastMap<K, V> {

	private static final long serialVersionUID = 0x700L; // Version.

	private final K fromKey; // Inclusive.
	private final K toKey; // Exclusive.
    private final boolean fromInclusive; 
    private final boolean toInclusive;
	private final FastMap<K, V> inner;

	public SubMapImpl(FastMap<K, V> inner, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		this.inner = inner;
		this.fromKey = fromKey;
		this.toKey = toKey;
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}


	@Override
	public Entry<K, V> ceilingEntry(K key) {
		Entry<K,V> entry = inner.ceilingEntry(key);
		if ((entry == null) || !inRange(entry.getKey())) return null;
		return entry;
	}

	@Override
	public void clear() {
        entrySet().clear();
	}

	@Override
	public SubMapImpl<K, V> clone() {
		return new SubMapImpl<K,V>(inner.clone(), fromKey, fromInclusive, toKey, toInclusive);
	}

	@Override
	public Order<? super K> comparator() {
		return inner.comparator();
	}

	@Override
	public Entry<K, V> firstEntry() {
		return fromInclusive ? 
				inner.ceilingEntry(fromKey) : inner.higherEntry(fromKey);
	}

	@Override
	public Entry<K, V> floorEntry(K key) {
		Entry<K,V> entry = inner.floorEntry(key);
		if ((entry == null) || !inRange(entry.getKey())) return null;
		return entry;
	}

	@Override
	public Entry<K, V> getEntry(K key) {		
		if (!inRange(key)) return null;
		return inner.getEntry(key);
	}

	@Override
	public Entry<K, V> higherEntry(K key) {
		Entry<K,V> entry = inner.higherEntry(key);
		if ((entry == null) || !inRange(entry.getKey())) return null;
		return entry;
	}

	private boolean inRange(K key) {
		int i = comparator().compare(fromKey, key);
		if (i > 0) return false;
		if ((i == 0) && !fromInclusive) return false;
		i = comparator().compare(toKey, key);
		if (i < 0) return false;
		if ((i == 0) && !toInclusive) return false;
		return true;
	}

	@Override
	public Entry<K, V> lastEntry() {
		return toInclusive ? 
				inner.floorEntry(fromKey) : inner.lowerEntry(fromKey);
	}


	@Override
	public java.util.Map.Entry<K, V> lowerEntry(K key) {
		Entry<K,V> entry = inner.lowerEntry(key);
		if ((entry == null) || !inRange(entry.getKey())) return null;
		return entry;
	}

	@Override
	public V put(K key, V value) {
		if (!inRange(key))
			throw new IllegalArgumentException("Key not in submap range");
		return inner.put(key, value);
	}

	@Override
	public Entry<K, V> removeEntry(K key) {
		if (!inRange(key)) return null;
		return inner.removeEntry(key);
	}

	@Override
	public int size() {
		return entrySet().size();
	}

	@Override
	public Equality<? super V> valuesEquality() {
		return inner.valuesEquality();
	}
	
}
