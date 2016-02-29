/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import javolution.util.FastMap;
import javolution.util.FractalTable;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * An unordered map (used if no order can be defined).
 */
public final class UnorderedMapImpl<K, V> extends FastMap<K, V> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FractalTable<EntryImpl<K, V>> entries;
	private final Equality<? super K> keyEquality;

	public UnorderedMapImpl(Equality<? super K> keyEquality) {
		this(keyEquality, new FractalTable<>());		
	}

	public UnorderedMapImpl(Equality<? super K> keyEquality,
			 FractalTable<EntryImpl<K, V>> entries) {
		this.keyEquality = keyEquality;
		this.entries = entries;
	}

	@Override
	public Order<? super K> keyOrder() {
		return null;
	}

	@Override
	public Equality<? super V> valueEquality() {
		return Equality.STANDARD;
	}

	@Override
	public FastMap<K, V> clone() {
		return new UnorderedMapImpl<K,V>(keyEquality, entries.clone());
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		for (int i=entries.size(); --i >= 0 ;) {
			EntryImpl<K,V> e = entries.get(i);
			if (keyEquality.areEqual((K)key, e.getKey())) {
			     entries.remove(i);
			     return e.getValue();
			}
		}
		return null;
	}

	@Override
	public EntryImpl<K, V> getEntry(K key) {
		for (int i=entries.size(); --i >= 0 ;) {
			EntryImpl<K,V> e = entries.get(i);
			if (keyEquality.areEqual((K)key, e.getKey()))
			     return e;
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		EntryImpl<K,V> entry = getEntry(key);
		if (entry != null) return entry.setValue(value);
		entries.add(new EntryImpl<K,V>(key, value));
		return null;
	}

	@Override
	public EntryImpl<K, V> firstEntry() {
		return entries.getFirst();
	}

	@Override
	public EntryImpl<K, V> lastEntry() {
		return entries.getLast();
	}

	@Override
	public EntryImpl<K, V> getEntryAfter(K key) {
		for (int i=0; i < entries.size();) {
			EntryImpl<K,V> e = entries.get(i++);
			if (keyEquality.areEqual((K)key, e.getKey()))
			     return i != entries.size() ? entries.get(i) : null;
		}
		return null;
	}

	@Override
	public EntryImpl<K, V> getEntryBefore(K key) {
		for (int i=entries.size(); --i >= 0 ;) {
			EntryImpl<K,V> e = entries.get(i);
			if (keyEquality.areEqual((K)key, e.getKey()))
			     return i != 0 ? entries.get(i-1) : null;
		}
		return null;
	}
	
}
