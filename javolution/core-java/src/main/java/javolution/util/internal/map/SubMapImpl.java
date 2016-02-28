/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.NoSuchElementException;

import javolution.util.FastMap;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * An sub-map view over a map.
 */
public final class SubMapImpl<K, V> extends FastMap<K, V> {

	private static final long serialVersionUID = 0x700L; // Version.

	private final K fromKey; // Inclusive.
	private final K toKey; // Exclusive.
    private final boolean isFromSet; 
    private final boolean isToSet;
	private final FastMap<K, V> inner;

	public SubMapImpl(FastMap<K, V> inner, K fromKey, boolean isFromSet, 
			K toKey, boolean isToSet) {
		this.inner = inner;
		this.fromKey = fromKey;
		this.toKey = toKey;
		this.isFromSet = isFromSet;
		this.isToSet = isToSet;
	}


	@Override
	public SubMapImpl<K, V> clone() {
		return new SubMapImpl<K,V>(inner.clone(), fromKey, isFromSet, toKey, isToSet);
	}

	@Override
	public Entry<K, V> getEntry(K key) {
		if (isTooSmall(key) || isTooLarge(key)) return null;
		return inner.getEntry(key);
	}

	private boolean isTooSmall(K key) {
		return isFromSet && keyOrder().compare(fromKey, key) > 0;
	}
	private boolean isTooLarge(K key) {
		return isToSet && keyOrder().compare(toKey, key) <= 0;
	}

	@Override
	public Order<? super K> keyOrder() {
		return inner.keyOrder();
	}

	@Override
	public V put(K key, V value) {
		if (isTooSmall(key) || isTooLarge(key))
			throw new IllegalArgumentException("Key not in submap range");
		return inner.put(key, value);
	}

	@Override
	public Equality<? super V> valueEquality() {
		return inner.valueEquality();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		if (isTooSmall((K)key) || isTooLarge((K)key)) return null;
		return inner.remove(key);
	}

	@Override
	public Entry<K, V> firstEntry() {
		Entry<K,V> first = inner.getEntry(fromKey);
		if (first != null) return first;
		first = inner.getEntryAfter(fromKey);
		if ((first == null) || isTooLarge(first.getKey()))
				throw new NoSuchElementException();
		return first;
	}

	@Override
	public java.util.Map.Entry<K, V> lastEntry() {
		Entry<K,V> last = inner.getEntryBefore(toKey);
		if ((last == null) || isTooSmall(last.getKey()))
				throw new NoSuchElementException();
		return last;
	}

	@Override
	public java.util.Map.Entry<K, V> getEntryAfter(K key) {
		Entry<K,V> after = inner.getEntryAfter(key);
		if ((after == null) || isTooLarge(after.getKey())) return null;
		return after;
	}

	@Override
	public java.util.Map.Entry<K, V> getEntryBefore(K key) {
		Entry<K,V> before = inner.getEntryAfter(key);
		if ((before == null) || isTooSmall(before.getKey())) return null;
		return before;
	}
	
}
