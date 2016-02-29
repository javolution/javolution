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
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * An entry set view over a map.
 */
public final class EntrySetImpl<K, V> extends FastSet<Map.Entry<K, V>> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastMap<K, V> map;

	public EntrySetImpl(FastMap<K, V> map) {
		this.map = map;
	}

	@Override
	public boolean add(Entry<K, V> entry) {
		Entry<K, V> existing = map.getEntry(entry.getKey());
		if (existing != null) {
			if (map.valueEquality().areEqual(existing.getValue(),
					entry.getValue()))
				return false;
			existing.setValue(entry.getValue());
		} else { // No entry with the same key.
			map.put(entry.getKey(), entry.getValue());
		}
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public EntrySetImpl<K, V> clone() {
		return new EntrySetImpl<K, V>(map.clone());
	}

	@Override
	public Order<? super Entry<K, V>> comparator() {
		return new EntryOrder<K, V>(map.keyOrder(), map.valueEquality());
	}

	@Override
	public boolean contains(Object obj) {
		if (!(obj instanceof Entry))
			return false;
		@SuppressWarnings("unchecked")
		Entry<K, V> searched = (Entry<K, V>) obj;
		Entry<K, V> entry = map.getEntry(searched.getKey());
		return map.valueEquality().areEqual(entry.getValue(),
				searched.getValue());
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new EntryIteratorImpl<K,V>(map);
	}

	@Override
	public boolean remove(Object obj) {
		if (contains(obj)) {
			map.remove(((Entry<?, ?>) obj).getKey());
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		return map.size();
	}

	/** The entry order */
	private static class EntryOrder<K, V> implements Order<Entry<K, V>> {
		private final Order<? super K> keyOrder;
		private final Equality<? super V> valueEquality;

		public EntryOrder(Order<? super K> keyOrder,
				Equality<? super V> valueEquality) {
			this.keyOrder = keyOrder;
			this.valueEquality = valueEquality;
		}

		@Override
		public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
			if (left == null)
				return right == null;
			if (right == null)
				return false;
			return keyOrder.areEqual(left.getKey(), right.getKey())
					&& valueEquality
							.areEqual(left.getValue(), right.getValue());
		}

		@Override
		public int compare(Entry<K, V> left, Entry<K, V> right) {
			if (left == null)
				return (right == null) ? 0 : -1;
			if (right == null)
				return 1;
			return keyOrder.compare(left.getKey(), right.getKey());
		}

		@Override
		public int indexOf(Entry<K, V> entry) {
			return entry != null ? keyOrder.indexOf(entry.getKey()) : 0;
		}

		@Override
		public Order<Entry<K, V>> subOrder(Entry<K, V> entry) {
			return entry != null ? new EntryOrder<K, V>(keyOrder.subOrder(entry
					.getKey()), valueEquality) : null;
		}

	}
}
