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
import javolution.util.function.Function;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E, R> extends FastCollection<R> {

	private static class IteratorImpl<E, R> implements FastIterator<R> {
		private final FastIterator<E> inner;
		private final Function<? super E, ? extends R> function;

		public IteratorImpl(FastIterator<E> inner,
				Function<? super E, ? extends R> function) {
			this.inner = inner;
			this.function = function;
		}

		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public R next() {
			return function.apply(inner.next());
		}

		@Override
		public void remove() {
			inner.remove();
		}

		@Override
		public FastIterator<R> reversed() {
			return new IteratorImpl<E, R>(inner.reversed(), function);
		}

		@SuppressWarnings("unchecked")
		@Override
		public FastIterator<R>[] split(FastIterator<R>[] subIterators) {
			FastIterator<E>[] inners = inner
					.split((FastIterator<E>[]) subIterators);
			int i = 0;
			for (FastIterator<E> itr : inners)
				if (itr != null)
					subIterators[i++] = new IteratorImpl<E, R>(itr, function);
			return subIterators;
		}
	}

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;
	private final Function<? super E, ? extends R> function;

	public MappedCollectionImpl(FastCollection<E> inner,
			Function<? super E, ? extends R> function) {
		this.inner = inner;
		this.function = function;
	}

	@Override
	public boolean add(R element) {
		throw new UnsupportedOperationException(
				"New elements cannot be added to mapped views");
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
	}

	@Override
	public MappedCollectionImpl<E, R> clone() {
		return new MappedCollectionImpl<E, R>(inner.clone(), function);
	}

	@Override
	public Equality<? super R> equality() {
		return Equality.STANDARD;
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public FastIterator<R> iterator() {
		return new IteratorImpl<E, R>(inner.iterator(), function);
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

}
