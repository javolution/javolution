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
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * <p> A table for which immutability is guaranteed by construction
 *     (package private constructor).
 * <pre>{@code
 * // From literal elements.
 * ConstantTable<String> winners = ConstantTable.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // From FastTable instances (same elements equality).
 * ConstantTable<String> caseInsensitiveWinners 
 *     = FastTable.newTable(LEXICAL_CASE_INSENSITIVE, String.class)
 *         .addAll("John Deuff", "Otto Graf", "Sim Kamil").constant();
 * }</pre></p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant(comment = "Immutable")
public final class ConstantTable<E> extends FastTable<E> {

	private static final long serialVersionUID = 0x700L; // Version.

	/**
     * Returns a constant table (using default element equality) holding 
     * the specified elements. 
     */
	public static <E> ConstantTable<E> of(@SuppressWarnings("unchecked") E... elements) {
    	return new ConstantTable<E>(elements.clone(), Equality.DEFAULT);
    }

	/** Holds the elements. */
	private final E[] elements;

	/** Holds the equality comparator. */
	private final Equality<? super E> equality;

	/** Creates a new instance from the specified elements and equality. */
	ConstantTable(E[] elements, Equality<? super E> equality) {
		this.elements = elements;
		this.equality = equality;
	}

	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}
	
	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	/**  Returns {@code this}.*/
	@Override
	public ConstantTable<E> clone() {
		return this;
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantTable<E> constant() {
		return this;
	}

	@Override
	public Equality<? super E> equality() {
		return equality;
	}

	@Override
	public void forEach(Consumer<? super E> consumer) { // Optimization.
		for (E e : elements) consumer.accept(e);
	}

	@Override
	public E until(Predicate<? super E> matching) { // Optimization.
		for (E e : elements) if (matching.test(e)) return e;
		return null;
	}

	@Override
	public E get(int index) {
		return elements[index];
	}

	@Override
	public boolean isEmpty() {
		return elements.length == 0;
	}

	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	/** 
	 * Guaranteed to throw an exception and leave the table unmodified.
	 * @deprecated Should never be used on immutable table.
	 */
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public int size() {
		return elements.length;
	}

	/** Returns {@code this}.*/
	@Override
	public ConstantTable<E> unmodifiable() {
		return this;
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		// TODO Auto-generated method stub
		return null;
	}
	
}