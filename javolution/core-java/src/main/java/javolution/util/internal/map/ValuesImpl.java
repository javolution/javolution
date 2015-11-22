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
import java.util.Map.Entry;

import javolution.util.FastCollection;
import javolution.util.FastIterator;
import javolution.util.FastMap;
import javolution.util.function.Equality;

/**
 * A collection view over a map values.
 */
public final class ValuesImpl<K, V> extends FastCollection<V> {

	private static class IteratorImpl<K, V> implements FastIterator<V> {
		FastIterator<Map.Entry<K, V>> mapItr;

		public IteratorImpl(FastIterator<Map.Entry<K, V>> mapItr) {
			this.mapItr = mapItr;
		}

		@Override
		public boolean hasNext() {
			return mapItr.hasNext();
		}

		@Override
		public V next() {
			return mapItr.next().getValue();
		}

		@Override
		public void remove() {
			mapItr.remove();
		}

		@Override
		public FastIterator<V> reversed() {
			return new IteratorImpl<K, V>(mapItr.reversed());
		}

		@Override
		public FastIterator<V>[] split(FastIterator<V>[] subIterators) {
			@SuppressWarnings("unchecked")
			FastIterator<Entry<K, V>>[] mapItrs = mapItr
					.split((FastIterator<Entry<K, V>>[]) subIterators);
			int i = 0;
			for (FastIterator<Entry<K, V>> itr : mapItrs)
				if (itr != null)
					subIterators[i++] = new IteratorImpl<K, V>(itr);
			return subIterators;
		}
	}

	private static final long serialVersionUID = 0x700L; // Version.

	private final FastMap<K, V> map;

	public ValuesImpl(FastMap<K, V> map) {
		this.map = map;
	}

	@Override
	public boolean add(V element) {
		throw new UnsupportedOperationException(
				"Values cannot be added directly to maps");
	}

	@Override
	public void clear() {
		map.clear();

	}

	@Override
	public FastCollection<V> clone() {
		return new ValuesImpl<K, V>(map.clone());
	}

	@Override
	public boolean contains(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Equality<? super V> equality() {
		return map.valueEquality();
	}

	@Override
	public FastIterator<V> iterator() {
		return new IteratorImpl<K, V>(map.iterator());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object value) {
		Equality<Object> valueEquality = (Equality<Object>) map.valueEquality();
		for (FastIterator<Entry<K, V>> itr = map.iterator(); itr.hasNext();) {
			Entry<K, V> entry = itr.next();
			if (valueEquality.areEqual(entry.getValue(), value)) {
				itr.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public int size() {
		return map.size();
	}

}
