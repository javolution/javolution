/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import org.javolution.util.FastCollection;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.

	private FastCollection<E> inner;

	public UnmodifiableCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public UnmodifiableCollectionImpl<E> clone() {
		return new UnmodifiableCollectionImpl<E>(inner.clone());
	}

	@Override
	public boolean contains(Object searched) { // Optimization.
		return inner.contains(searched);
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public void forEach(Consumer<? super E> consumer) { // Optimization.
		inner.forEach(consumer);
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return inner.iterator();
	}

	@Override
	public E reduce(BinaryOperator<E> operator) { // Optimization.
		return inner.reduce(operator);
	}

	@Override
	public boolean remove(Object searched) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public UnmodifiableCollectionImpl<E> reversed() { // Optimization.
		return new UnmodifiableCollectionImpl<E>(inner.reversed());
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		FastCollection<E>[] subViews = inner.trySplit(n);
		for (int i = 0; i < subViews.length; i++)
			subViews[i] = new UnmodifiableCollectionImpl<E>(subViews[i]);
		return subViews;
	}

	@Override
	public E until(Predicate<? super E> matching) { // Optimization.
		return inner.until(matching);
	}
	

}
