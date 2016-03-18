/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Map.Entry;

import javolution.util.function.Order;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> 
 *     implementation of {@link FastSet}.</p> 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see SparseMap
 */
public final class SparseSet<E> extends FastSet<E> {
	   
	private static final long serialVersionUID = 0x700L; // Version.
	private static final Object PRESENT = new Object();
	private SparseMap<E,Object> sparse;
	   
	/**
     * Creates an empty set using an arbitrary order (hash based).
     */
    public SparseSet() {
    	this(Order.DEFAULT);
    }
	/**
     * Creates an empty set using the specified order.
     * 
     * @param order the ordering of the map.
     */
    public SparseSet(Order<? super E> order) {
    	sparse = new SparseMap<E, Object>(order);
    }
    
	@Override
	public E first() {
		return sparse.firstKey();
	}
	
	@Override
	public E last() {
		return sparse.lastKey();
	}
	
	@Override
	public Order<? super E> comparator() {
		return sparse.comparator();
	}
	
	@Override
	public int size() {
		return sparse.size();
	}

	@Override
	public void clear() {
		sparse.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object obj) {
		return sparse.containsKey((E) obj); // Cast has no effect here.
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object obj) {
		return sparse.removeEntry((E)obj) != null;
	}

	@Override
	public SparseSet<E> clone() {
		SparseSet<E> copy = (SparseSet<E>) super.clone();
		copy.sparse = sparse.clone();
		return copy;
	}
	
	@Override
	public boolean add(E element) {
		return sparse.put(element, PRESENT) == null;
	}
	@Override
	public boolean isEmpty() {
		return sparse.isEmpty();
	}
	@Override
	public E ceiling(E element) {
		return sparse.ceilingKey(element);
	}
	@Override
	public E floor(E element) {
		return sparse.floorKey(element);
	}
	@Override
	public E higher(E element) {
		return sparse.higherKey(element);
	}
	@Override
	public E lower(E element) {
		return sparse.lowerKey(element);
	}
	@Override
	public E pollFirst() {
		Entry<E,Object> entry = sparse.pollFirstEntry();
		return entry != null ? entry.getKey() : null;
	}
	@Override
	public E pollLast() {
		Entry<E,Object> entry = sparse.pollLastEntry();
		return entry != null ? entry.getKey() : null;
	}

}