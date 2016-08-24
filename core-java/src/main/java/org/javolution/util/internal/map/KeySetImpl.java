/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.javolution.util.FastCollection;
import org.javolution.util.FastMap;
import org.javolution.util.FastSet;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * An key set view over a fast map.
 */
public final class KeySetImpl<K, V> extends FastSet<K> {
	
	/** The generic iterator over the map keys */
	private static class IteratorImpl<K, V> extends Iterator<K> {
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
	}
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
	public K ceiling(K element) {
		Entry<K,V> entry = map.ceilingEntry(element);
		return (entry != null) ? entry.getKey() : null;
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
		return map.comparator();
	}
	
	@Override
	public boolean contains(Object key) {
		return map.containsKey(key);
	}
	
	@Override
	public K first() {
		Entry<K,V> entry = map.firstEntry();
		if (entry == null) throw new NoSuchElementException();
		return entry.getKey();
	}

	@Override
	public K floor(K element) {
		Entry<K,V> entry = map.floorEntry(element);
		return (entry != null) ? entry.getKey() : null;
	}

	@Override
	public K higher(K element) {
		Entry<K,V> entry = map.higherEntry(element);
		return (entry != null) ? entry.getKey() : null;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<K> iterator() {
		return new IteratorImpl<K, V>(new EntryIteratorImpl<K,V>(map));
	}

	@Override
	public K last() {
		Entry<K,V> entry = map.lastEntry();
		if (entry == null) throw new NoSuchElementException();
		return entry.getKey();
	}

	@Override
	public K lower(K element) {
		Entry<K,V> entry = map.lowerEntry(element);
		return (entry != null) ? entry.getKey() : null;
	}

	@Override
	public K pollFirst() {
		Entry<K,V> entry = map.pollFirstEntry();
		return (entry != null) ? entry.getKey() : null;
	}

	@Override
	public K pollLast() {
		Entry<K,V> entry = map.pollLastEntry();
		return (entry != null) ? entry.getKey() : null;
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

	@Override
	public boolean removeIf(Predicate<? super K> filter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FastCollection<K>[] trySplit(int n) {
		// TODO Auto-generated method stub
		return null;
	}

}
