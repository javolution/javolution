/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.function.Equality;

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
	public Iterator<E> iterator() {
		return new IteratorImpl<E>(inner.iterator());
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public UnmodifiableCollectionImpl<E> reversed() { // Optimization.
	    return new UnmodifiableCollectionImpl<E>(inner.reversed());
	}
	
	@Override
	public FastCollection<E>[] subViews(FastCollection<E>[] subViews) {
		inner.subViews(subViews);
		for (int i = 0; i < subViews.length; i++) {
			FastCollection<E> subView = subViews[i];
			if (subView == null) continue;
			subViews[i] = new UnmodifiableCollectionImpl<E>(subView);
		}
		return subViews;
	}

	/** Default read-only iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {
		private final Iterator<E> inner;

		public IteratorImpl(Iterator<E> inner) {
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

	}

}
