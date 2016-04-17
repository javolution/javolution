/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import javolution.util.FastCollection.Iterator;
import java.util.Map.Entry;

import javolution.util.FastMap;

/**
 * A generic iterator over the entries of a fast map.
 */
public final class EntryIteratorImpl<K, V> extends Iterator<Entry<K, V>> {

	private final FastMap<K, V> map;
	private Entry<K,V> current;
	private Entry<K,V> next;

	/** Iterates the whole collection */
	public EntryIteratorImpl(FastMap<K, V> map) {
		this.map = map;
	    this.next = map.firstEntry();
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
		next = map.higherEntry(current.getKey());
		return current;
	}

}
