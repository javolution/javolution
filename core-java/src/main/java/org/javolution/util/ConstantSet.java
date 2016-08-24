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
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * <p> A set for which immutability is guaranteed by construction
 *     (package private constructor).
 * <pre>{@code
 * // From literal elements.
 * ConstantSet<String> winners = ConstantSet.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // From FastSet instances (same order).
 * ConstantSet<String> caseInsensitiveWinners 
 *     = FastSet.newSet(LEXICAL_CASE_INSENSITIVE, String.class)
 *         .addAll("John Deuff", "Otto Graf", "Sim Kamil").constant();
 * }</pre></p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant(comment = "Immutable")
public final class ConstantSet<E> extends FastSet<E> {

	private static final long serialVersionUID = 0x700L; // Version.

	/**
     * Returns a constant set (hash-ordered) holding the specified 
     * elements. 
     */
	public static <E> ConstantSet<E> of(@SuppressWarnings("unchecked") E... elements) {
    	SparseSet<E> sparse = new SparseSet<E>();
    	for (int i=0; i < elements.length;) 
    		sparse.add(elements[i++]);
    	return new ConstantSet<E>(sparse);
    }

	/** Holds the elements. */
	private final SparseSet<E> sparse;
	
	/** Creates a new instance from the specified sparse set. */
	ConstantSet(SparseSet<E> sparse) {
		this.sparse = sparse;
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	@Override
	public E ceiling(E element) {
		return sparse.ceiling(element);
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	/**  Returns {@code this}.*/
	@Override
	public ConstantSet<E> clone() {
		return this;
	}

	@Override
	public Order<? super E> comparator() {
		return sparse.comparator();
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantSet<E> constant() {
		return this;
	}

	@Override
	public boolean contains(Object obj) {
		return sparse.contains(obj);
	}

	@Override
	public E first() {
		return sparse.first();
	}

	@Override
	public E floor(E element) {
		return sparse.floor(element);
	}

	@Override
	public E higher(E element) {
		return sparse.higher(element);
	}

	@Override
	public boolean isEmpty() {
		return sparse.isEmpty();
	}

	@Override
	public E last() {
		return sparse.last();
	}

	@Override
	public E lower(E element) {
		return sparse.lower(element);
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public E pollFirst() {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public E pollLast() {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public boolean remove(Object obj) {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the set unmodified.
	 * @deprecated Should never be used on immutable set.
	 */
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException(
				"Constant sets cannot be modified.");
	}

	@Override
	public int size() {
		return sparse.size();
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantSet<E> unmodifiable() {
		return this;
	}
	
	@Override
	public void forEach(Consumer<? super E> consumer) { // Optimization.
		sparse.forEach(consumer);
	}

	@Override
	public E until(Predicate<? super E> matching) { // Optimization.
		return sparse.until(matching);
	}

	@Override
	public Iterator<E> iterator() { // Optimization.
		return sparse.iterator();
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		// TODO Auto-generated method stub
		return null;
	}
	
}