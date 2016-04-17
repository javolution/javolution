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
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import javolution.util.FastCollection;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equality;
import javolution.util.function.Order;
import javolution.util.function.Predicate;

/**
 * An entry set view over a map.
 */
public final class EntrySetImpl<K, V> extends FastSet<Map.Entry<K, V>> {

	/** The entry order */
	private static class EntryOrder<K, V> implements Order<Entry<K, V>> {
		private static final long serialVersionUID = 0x700L; // Version.
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
	private static final long serialVersionUID = 0x700L; // Version.

	private final FastMap<K, V> map;

	public EntrySetImpl(FastMap<K, V> map) {
		this.map = map;
	}

	@Override
	public boolean add(Entry<K, V> entry) {
		Entry<K, V> existing = map.getEntry(entry.getKey());
		if (existing != null) {
			if (map.valuesEquality().areEqual(existing.getValue(),
					entry.getValue()))
				return false;
			existing.setValue(entry.getValue());
		} else { // No entry with the same key.
			map.put(entry.getKey(), entry.getValue());
		}
		return true;
	}

	@Override
	public Entry<K, V> ceiling(Entry<K, V> element) {
		return map.ceilingEntry(element.getKey());
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
		return new EntryOrder<K, V>(map.comparator(), map.valuesEquality());
	}

	@Override
	public boolean contains(Object obj) {
		if (!(obj instanceof Entry))
			return false;
		@SuppressWarnings("unchecked")
		Entry<K, V> searched = (Entry<K, V>) obj;
		Entry<K, V> entry = map.getEntry(searched.getKey());
		if (entry == null) return false;
 		return map.valuesEquality().areEqual(entry.getValue(),
				searched.getValue());
	}

	@Override
	public Entry<K, V> first() {
		Entry<K,V> entry = map.firstEntry();
		if (entry == null) throw new NoSuchElementException();
		return entry;
	}

	@Override
	public Entry<K, V> floor(Entry<K, V> element) {
		return map.floorEntry(element.getKey());
	}

	@Override
	public Entry<K, V> higher(Entry<K, V> element) {
		return map.higherEntry(element.getKey());
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new EntryIteratorImpl<K,V>(map);
	}

	@Override
	public Entry<K, V> last() {
		Entry<K,V> entry = map.lastEntry();
		if (entry == null) throw new NoSuchElementException();
		return entry;
	}

	@Override
	public Entry<K, V> lower(Entry<K, V> element) {
		return map.lowerEntry(element.getKey());
	}

	@Override
	public Entry<K, V> pollFirst() {
		return map.pollFirstEntry();
	}

	@Override
	public Entry<K, V> pollLast() {
		return map.pollLastEntry();
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

	@Override
	public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FastCollection<Entry<K, V>>[] trySplit(int n) {
		// TODO Auto-generated method stub
		return null;
	}
}
