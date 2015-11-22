/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import javolution.util.FastCollection;
import javolution.util.FastIterator;
import javolution.util.function.Equality;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> extends FastCollection<E> {

	public static class IteratorImpl<E> implements FastIterator<E> {
		private final FastIterator<E> inner;

		public IteratorImpl(FastIterator<E> inner) {
			this.inner = inner;
		}

		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public E next() {
			return inner.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Read-Only Iterator.");
		}

		@Override
		public FastIterator<E> reversed() {
			return new IteratorImpl<E>(inner.reversed());
		}

		@Override
		public FastIterator<E>[] split(FastIterator<E>[] subIterators) {
			inner.split(subIterators);
			for (int i = 0, n = subIterators.length; i < n; i++) {
				FastIterator<E> itr = subIterators[i];
				if (itr != null)
					subIterators[i] = new IteratorImpl<E>(itr);
			}
			return subIterators;
		}
	}

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
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public FastIterator<E> iterator() {
		return new IteratorImpl<E>(inner.iterator());
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

}
