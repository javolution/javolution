/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.Iterator;

import javolution.util.FastIterator;
import javolution.util.FastMap;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * An sub-map view over a map.
 */
public final class SubMapImpl<K, V> extends FastMap<K, V> {

	private static final long serialVersionUID = 0x700L; // Version.

	private final K fromKey;
	private final K toKey;
	private final FastMap<K, V> inner;

	public SubMapImpl(FastMap<K, V> inner, K fromKey, K toKey) {
		this.inner = inner;
		this.fromKey = fromKey;
		this.toKey = toKey;
	}

	@Override
	public Order<? super K> keyOrder() {
		return inner.keyOrder();
	}

	@Override
	public Equality<? super V> valueEquality() {
		return inner.valueEquality();
	}

	@Override
	public SubMapImpl<K, V> clone() {
		return new SubMapImpl<K,V>(inner.clone(), fromKey, toKey);
	}

	@Override
	public int size() {
		int count = 0;
	    for (Iterator<?> itr = iterator(); itr.hasNext(); itr.next()) count++;
		return count;
	}

	/** Indicates if this map may contain the specified key.*/
	private boolean inScope(K key) {
		if ((fromKey != null) && (keyOrder().compare(fromKey, key) > 0))
			return false;
		if ((toKey != null) && (keyOrder().compare(toKey, key) <= 0))
			return false;
		return true;
	}
	
	@Override
	public Entry<K, V> getEntry(K key) {
		if (!inScope(key)) return null;
		return inner.getEntry(key);
	}

	@Override
	public V put(K key, V value) {
		if (!inScope(key)) throw new IllegalArgumentException("Key not in submap range");
		return inner.put(key, value);
	}

	@Override
	public Entry<K,V> removeEntry(K key) {
		if (!inScope((K)key)) return null;
		return inner.removeEntry(key);
	}

	@Override
	public void clear() {
	    for (Iterator<?> itr = iterator(); itr.hasNext(); itr.next()) 
	    	itr.remove();
	}

	@Override
	public FastIterator<Entry<K, V>> iterator(K from, K to) {
		from = inner.keyOrder().compare(fromKey, from) > 0 ? fromKey : from;
		to = (to == null) || inner.keyOrder().compare(toKey, to) < 0 ? toKey : to;
		return inner.iterator(from, to);
	}
	
}
