/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.NoSuchElementException;

import org.javolution.util.FastCollection;
import org.javolution.util.FastSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A sequential view which does not iterate twice over the same elements.
 */
public final class DistinctCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;

	public DistinctCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		return inner.contains(element) ? false : inner.add(element);
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
	}

	@Override
	public DistinctCollectionImpl<E> clone() {
		return new DistinctCollectionImpl<E>(inner.clone());
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
		Equality<? super E> equality = equality();
		if (!(equality instanceof Order))
			throw new UnsupportedOperationException(
					"Distinct collections require an ordered equality !");
		@SuppressWarnings("unchecked")
		final FastSet<Object> iterated = FastSet
				.newSet((Order<Object>) equality);
		return new Iterator<E>() {
			Iterator<E> itr = inner.iterator();
			boolean currentIsNext;
			E current;

			@Override
			public boolean hasNext() {
				if (currentIsNext)
					return true;
				while (itr.hasNext()) {
					current = itr.next();
					if (iterated.contains(current))
						continue; // Ignore.
					currentIsNext = true;
					iterated.add(current);
					return true;
				}
				return false;
			}

			@Override
			public E next() {
				if (!hasNext())
					throw new NoSuchElementException();
				currentIsNext = false;
				return current;
			}
		};
	}

	@Override
	public boolean remove(final Object searched) { // Remove all occurrences.
		return inner.removeIf(new Predicate<E>() {
			Equality<? super E> equality = equality();

			@SuppressWarnings("unchecked")
			@Override
			public boolean test(E param) {
				return equality.areEqual((E) searched, param);
			}
		});
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return inner.removeIf(filter);
	}

	@Override
	public DistinctCollectionImpl<E> reversed() { // Optimization.
		return new DistinctCollectionImpl<E>(inner.reversed());
	}

	@SuppressWarnings("unchecked")
	@Override
	public FastCollection<E>[] trySplit(int n) {
		return new FastCollection[] { this }; // Cannot split distinct
												// collections.
	}

}
