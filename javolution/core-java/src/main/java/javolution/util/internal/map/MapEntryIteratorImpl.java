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
import java.util.Map.Entry;

import javolution.util.FastMap;

/**
 * A generic iterator over the entries of a fast map.
 */
public final class MapEntryIteratorImpl<K, V> implements Iterator<Entry<K, V>> {

	private final FastMap<K, V> map;
	private Entry<K,V> toEntry; // Exclusive.
	private Entry<K,V> current;
	private Entry<K,V> next;

	/** Iterates the whole collection */
	public MapEntryIteratorImpl(FastMap<K, V> map) {
		this.map = map;
	    this.toEntry = null;
		if (!map.isEmpty()) {
		    next = map.firstEntry();
		}
	}

	/** Iterates from the specified key (inclusive). */
	public MapEntryIteratorImpl(FastMap<K, V> map, K fromKey) {
		this.map = map;
		this.toEntry = null;
	    next = map.getEntry(fromKey);
		if (next == null) next = map.getEntryAfter(fromKey);
	}

	/** Iterates from the specified key (inclusive) to the specified key 
	 * (exclusive). */
	public MapEntryIteratorImpl(FastMap<K, V> map, K fromKey, K toKey) {
        this(map, fromKey);
	    toEntry = map.getEntry(toKey);
		if (toEntry == null) toEntry = map.getEntryAfter(toKey);
	}
	
	@Override
	public boolean hasNext() {
		return (next != null);
	}

	@Override
	public Entry<K, V> next() {
		if (next == null)
			throw new IllegalStateException();
		current = next;
		next = map.getEntryAfter(current.getKey());
		if (next == toEntry) next = null;
		return current;
	}

	@Override
	public void remove() {
		if (current == null)
			throw new IllegalStateException();
		map.remove(current.getKey());
		current = null;
	}

}
