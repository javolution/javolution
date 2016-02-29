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

import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Order;

/**
 * An key set view over a fast map.
 */
public final class KeySetImpl<K, V> extends FastSet<K> {
	
	private static final long serialVersionUID = 0x700L; // Version.
	private final FastMap<K, V> map;

	public KeySetImpl(FastMap<K, V> map) {
		this.map = map;
	}

	@Override
	public boolean add(K key) {
		if (map.containsKey(key))
			return false;
		map.put(key, null);
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public FastSet<K> clone() {
		return new KeySetImpl<K, V>(map.clone());
	}

	@Override
	public Order<? super K> comparator() {
		return map.keyOrder();
	}

	@Override
	public boolean contains(Object key) {
		return map.containsKey(key);
	}

	@Override
	public Iterator<K> iterator() {
		return new IteratorImpl<K, V>(new EntryIteratorImpl<K,V>(map));
	}
	
	@Override
	public boolean remove(Object key) {
		if (!map.containsKey(key))
			return false;
		map.remove(key);
		return true;
	}
	
	@Override
	public int size() {
		return map.size();
	}

	/** The generic iterator over the map keys */
	private static class IteratorImpl<K, V> implements Iterator<K> {
		final EntryIteratorImpl<K, V> mapItr;

		public IteratorImpl(EntryIteratorImpl<K, V> mapItr) {
			this.mapItr = mapItr;
		}

		@Override
		public boolean hasNext() {
			return mapItr.hasNext();
		}

		@Override
		public K next() {
			return mapItr.next().getKey();
		}

		@Override
		public void remove() {
			mapItr.remove();
		}
	}

}
