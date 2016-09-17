/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Iterator;
import java.util.Map;

import org.javolution.util.FastMap;
import org.javolution.util.FastMap.Entry;
import org.javolution.util.FastSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An entry set view over a map.
 */
public final class EntrySetImpl<K, V> extends FastSet<Entry<K, V>> {

	/** The entry order (must support generic Map.Entry) */
	private static class EntryOrder<K, V> implements Order<Map.Entry<K, V>> {
		private static final long serialVersionUID = 0x700L; // Version.
		private final Order<? super K> keyOrder;
		private final Equality<? super V> valueEquality;

		public EntryOrder(Order<? super K> keyOrder,
				Equality<? super V> valueEquality) {
			this.keyOrder = keyOrder;
			this.valueEquality = valueEquality;
		}

		@Override
		public boolean areEqual(Map.Entry<K, V> left, Map.Entry<K, V> right) {
			if (left == null)
				return right == null;
			if (right == null)
				return false;
			return keyOrder.areEqual(left.getKey(), right.getKey())
					&& valueEquality
							.areEqual(left.getValue(), right.getValue());
		}

		@Override
		public int compare(Map.Entry<K, V> left, Map.Entry<K, V> right) {
			if (left == null)
				return (right == null) ? 0 : -1;
			if (right == null)
				return 1;
			return keyOrder.compare(left.getKey(), right.getKey());
		}

		@Override
		public int indexOf(Map.Entry<K, V> entry) {
			return entry != null ? keyOrder.indexOf(entry.getKey()) : 0;
		}

		@Override
		public Order<Map.Entry<K, V>> subOrder(Map.Entry<K, V> entry) {
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
    public Iterator<Entry<K, V>> iterator() {
        return map.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Map.Entry)) return false;
        Map.Entry<K,V> entry = (Map.Entry<K,V>) obj;
        Entry<K,V> mapEntry = map.getEntry(entry.getKey());
        return map.valuesEquality().areEqual(mapEntry.getValue(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Map.Entry)) return false;
        Map.Entry<K,V> entry = (Map.Entry<K,V>) obj;
        return map.remove(entry.getKey(), entry.getValue());
    }

    @Override
    public Iterator<Entry<K, V>> iterator(Entry<K, V> fromElement) {
        return map.iterator(fromElement.getKey());
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(Entry<K, V> fromElement) {
        return map.descendingIterator(fromElement.getKey());
    }

    @Override
    public FastSet<Entry<K, V>> clone() {
        return new EntrySetImpl<K,V>(map.clone());
    }

    @Override
    public Order<? super Entry<K, V>> comparator() {
        return new EntryOrder<K,V>(map.comparator(), map.valuesEquality());
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return map.descendingIterator();
    }

    @Override
    public boolean add(Entry<K, V> element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

}
