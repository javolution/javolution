/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.Map.Entry;

import javolution.util.FastIterator;
import javolution.util.FastMap;

/**
 * A generic iterator over a fast map.
 */
public final class MapIteratorImpl<K, V> implements FastIterator<Entry<K, V>> {

	private final FastMap<K, V> map;
	private K fromKey;
	private K toKey;
	private boolean reversed;
	private Entry<K, V> last; // Cannot be returned.
	private Entry<K, V> current = null;
	private Entry<K, V> next;

	public MapIteratorImpl(FastMap<K, V> map, K fromKey, K toKey) {
		this(map, fromKey, toKey, false);
	}

	private MapIteratorImpl(FastMap<K, V> map, K fromKey, K toKey,
			boolean reversed) {
		this.map = map;
		this.fromKey = fromKey;
		this.toKey = toKey;
		this.reversed = reversed;
		initialize();
	}

	private void initialize() {
		Entry<K, V> fromKeyEntry = (fromKey != null) ? map.getEntry(fromKey)
				: null;
		Entry<K, V> toKeyEntry = (toKey != null) ? map.getEntry(toKey) : null;
		next = (fromKeyEntry != null) ? fromKeyEntry
				: (fromKey != null) ? nextEntry(fromKey) : null;
		last = (toKeyEntry != null) ? toKeyEntry
				: (toKey != null) ? nextEntry(toKey) : null;		
	}
	
	private Entry<K, V> nextEntry(K key) {
		return reversed ? map.entryBefore(key) : map.entryAfter(key);
	}

	@Override
	public boolean hasNext() {
		return (next != null) && (next != last);
	}

	@Override
	public Entry<K, V> next() {
		if ((next == null) || (next == last))
			throw new IllegalStateException();
		current = next;
		next = nextEntry(current.getKey());
		return current;
	}

	@Override
	public void remove() {
		if (current == null)
			throw new IllegalStateException();
		map.removeEntry(current.getKey());
		current = null;
	}

	@Override
	public MapIteratorImpl<K, V> reversed() {
		reversed = !reversed;
		K tmp = fromKey;
		fromKey = toKey;
		toKey = tmp;
		initialize();
		return this;
	}

	@Override
	public MapIteratorImpl<K, V> trySplit() {
		Entry<K,V> midEntry = map.midEntry(fromKey, toKey);
		if (midEntry == null) return null;
		MapIteratorImpl<K,V> tailIterator = new MapIteratorImpl<K,V>(
				map, midEntry.getKey(), toKey);
		toKey = midEntry.getKey();
		initialize();
		return tailIterator;
	}

}
