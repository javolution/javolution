/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import org.javolution.lang.Constant;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * <p> A map for which immutability is guaranteed by construction 
 *     (package private constructor).
 * <pre>{@code
 * // From literal entries (hash-based default order)
 * ConstantMap<String, Integer> ranking = ConstantMap.of("Oscar Thon", 1, "Yvon Tremblay", 2);
 * 
 * // From FastMap instances (same order & value equality)
 * ConstantMap<String, Integer> winners = ranking.filter(e -> e.getValue() < 3).constant();
 * }</pre></p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant(comment = "Immutable")
public final class ConstantMap<K,V> extends FastMap<K,V> {
    
    private static final long serialVersionUID = 0x700L; // Version.

	/**
     * Returns a constant map (default hash-order) holding the specified key, 
     * values pairs. 
     */
    @SuppressWarnings("unchecked")
	public static <K,V> ConstantMap<K,V> of(K firstKey, V firstValue, Object...others) {
    	SparseMap<K,V> sparse = new SparseMap<K,V>();
    	sparse.put(firstKey, firstValue);
    	for (int i=0; i < others.length; i++) 
    		sparse.put((K)others[i], (V)others[++i]);
    	return new ConstantMap<K,V>(sparse, Equality.DEFAULT);
    }
	
	/** Holds the mapping. */
	private final SparseMap<K,V> sparse;

	/** Holds the value equality. */
	private final Equality<? super V> valuesEquality;
	
	/** Creates a new constant map using the specified sparse map and values
	 *  equality. */
	ConstantMap(SparseMap<K,V> sparse, Equality<? super V> valuesEquality) {
		this.sparse = sparse;
		this.valuesEquality = valuesEquality;
	}

	@Override
	public Entry<K, V> ceilingEntry(K key) {
		return sparse.ceilingEntry(key);
	}

	/** 
	 * Guaranteed to throw an exception and leave the map unmodified.
	 * @deprecated Should never be used on immutable map.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantMap<K,V> clone() {
		return this;
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantMap<K,V> constant() {
		return this;
	}

	@Override
	public Entry<K, V> firstEntry() {
		return sparse.firstEntry();
	}

	@Override
	public Entry<K, V> floorEntry(K key) {
		return sparse.floorEntry(key);
	}
	
	@Override
	public Entry<K, V> getEntry(K key) {
		return sparse.getEntry(key);
	}

	@Override
	public Entry<K, V> higherEntry(K key) {
		return sparse.higherEntry(key);
	}

	@Override
	public Entry<K, V> lastEntry() {
		return sparse.lastEntry();
	}

    @Override
	public Entry<K, V> lowerEntry(K key) {
		return sparse.lowerEntry(key);
	}

	@Override
	public Order<? super K> comparator() {
		return sparse.comparator();
	}

	/** 
	 * Guaranteed to throw an exception and leave the map unmodified.
	 * @deprecated Should never be used on immutable map.
	 */
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException(
				"Constant maps cannot be modified.");
	}
	
	/** 
	 * Guaranteed to throw an exception and leave the map unmodified.
	 * @deprecated Should never be used on immutable map.
	 */
	@Override
	public Entry<K, V> removeEntry(K key) {
		throw new UnsupportedOperationException(
				"Constant maps cannot be modified.");
	}

	@Override
	public int size() {
		return sparse.size();
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantMap<K,V> unmodifiable() {
		return this;
	}

	@Override
	public Equality<? super V> valuesEquality() {
		return valuesEquality;
	}
}