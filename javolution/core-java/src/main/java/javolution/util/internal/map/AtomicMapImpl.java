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

import javolution.util.FastMap;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * An sub-map view over a map.
 */
public final class AtomicMapImpl<K, V> extends FastMap<K, V> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastMap<K,V> inner;
	private volatile FastMap<K,V> innerConst; // The copy used by readers.

	public AtomicMapImpl(FastMap<K,V> inner) {
		this.inner = inner;
		this.innerConst = inner.clone();
	}

	@Override
	public synchronized void clear() {
		inner.clear();
		innerConst = inner.clone();
	}

	@Override
	public synchronized AtomicMapImpl<K,V> clone() {
		return new AtomicMapImpl<K,V>(inner.clone());
	}

	@Override
	public boolean containsKey(Object key) {
		return innerConst.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return innerConst.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return innerConst.get(key);
	}

	@Override
	public boolean isEmpty() {
		return innerConst.isEmpty();
	}

	@Override
	public synchronized V put(K key, V value) {
		V previous = inner.put(key, value);
		innerConst = inner.clone();
		return previous;
	}

	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> that) {
		inner.putAll(that);
		innerConst = inner.clone();
	}

	@Override
	public synchronized V remove(Object key) {
		V previous = inner.remove(key);
		innerConst = inner.clone();
		return previous;
	}

	@Override
	public int size() {
		return innerConst.size();
	}

	@Override
	public Order<? super K> comparator() {
		return innerConst.comparator();
	}

	@Override
	public K firstKey() {
		return innerConst.firstKey();
	}

	@Override
	public K lastKey() {
		return innerConst.lastKey();
	}

	@Override
	public Entry<K, V> ceilingEntry(K key) {
		return innerConst.ceilingEntry(key);
	}

	@Override
	public K ceilingKey(K key) {
		return innerConst.ceilingKey(key);
	}

	@Override
	public Entry<K, V> firstEntry() {
		return innerConst.firstEntry();
	}

	@Override
	public Entry<K, V> floorEntry(K key) {
		return innerConst.floorEntry(key);
	}

	@Override
	public K floorKey(K key) {
		return innerConst.floorKey(key);
	}

	@Override
	public Entry<K, V> higherEntry(K key) {
		return innerConst.higherEntry(key);
	}

	@Override
	public K higherKey(K key) {
		return innerConst.higherKey(key);
	}

	@Override
	public Entry<K, V> lastEntry() {
		return innerConst.lastEntry();
	}

	@Override
	public Entry<K, V> lowerEntry(K key) {
		return innerConst.lowerEntry(key);
	}

	@Override
	public K lowerKey(K key) {
		return innerConst.lowerKey(key);
	}

	@Override
	public synchronized Entry<K, V> pollFirstEntry() {
		Entry<K,V> entry = inner.pollFirstEntry();
		if (entry != null) innerConst = inner.clone();
		return entry;
	}

	@Override
	public synchronized Entry<K, V> pollLastEntry() {
		Entry<K,V> entry = inner.pollLastEntry();
		if (entry != null) innerConst = inner.clone();
		return entry;
	}

	@Override
	public synchronized V putIfAbsent(K key, V value) {
		V previous = inner.putIfAbsent(key, value);
		innerConst = inner.clone();
		return previous;
	}

	@Override
	public synchronized boolean remove(Object arg0, Object arg1) {
		boolean changed = inner.remove(arg0, arg1);
		if (changed) innerConst = inner.clone();
		return changed;
	}

	@Override
	public synchronized V replace(K key, V value) {
		V previous = inner.replace(key, value);
		innerConst = inner.clone();
		return previous;
	}

	@Override
	public synchronized boolean replace(K key, V arg1, V arg2) {
		return inner.replace(key, arg1, arg2);
	}

	@Override
	public Entry<K, V> getEntry(K key) {
		return innerConst.getEntry(key);
	}

	@Override
	public synchronized Entry<K, V> removeEntry(K key) {
		Entry<K,V> entry = inner.removeEntry(key);
		if (entry != null) innerConst = inner.clone();
		return entry;
	}

	@Override
	public Equality<? super V> valuesEquality() {
		return innerConst.valuesEquality();
	}
	
}
