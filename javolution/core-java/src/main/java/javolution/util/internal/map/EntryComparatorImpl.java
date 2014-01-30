/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.io.Serializable;
import java.util.Map;

import javolution.util.function.Equality;

/**
 * Entry comparator implementation consistent with Map.Entry equals/hashcode
 * contract for standard key and value comparators.
 */
public final class EntryComparatorImpl<K, V> implements
		Equality<Map.Entry<K, V>>, Serializable {

	private static final long serialVersionUID = 0x600L; // Version.
	public final Equality<? super K> keyComparator;
	public final Equality<? super V> valueComparator;

	public EntryComparatorImpl(Equality<? super K> keyComparator,
			Equality<? super V> valueComparator) {
		this.keyComparator = keyComparator;
		this.valueComparator = valueComparator;
	}

	@Override
	public boolean equal(Map.Entry<K, V> left, Map.Entry<K, V> right) {
		return keyComparator.equal(left.getKey(), right.getKey())
				&& valueComparator.equal(left.getValue(), right.getValue());
	}

	@Override
	public int compare(Map.Entry<K, V> left, Map.Entry<K, V> right) {
		int cmp = keyComparator.compare(left.getKey(), right.getKey());
		if (cmp != 0)
			return cmp;
		return valueComparator.compare(left.getValue(), right.getValue());
	}

	@Override
	public int hashOf(Map.Entry<K, V> e) {
		// Consistent with Map.Entry hashcode if the comparators are standard.
		return keyComparator.hashOf(e.getKey())
				^ valueComparator.hashOf(e.getValue());
	}

}
