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
import java.util.NoSuchElementException;

import javolution.util.FastCollection;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.util.function.Equality;
import javolution.util.function.Order;
import javolution.util.function.Predicate;

/**
 * A view which does not iterate twice over the same elements.
 */
public final class DistinctCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;

	public DistinctCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		return this.contains(element) ? false : inner.add(element);
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
		return new IteratorImpl<E>(inner.iterator(), equality());
	}

	@Override
	public boolean remove(final Object searched) { // Remove all occurrences.
		return removeIf(new Predicate<E>() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean test(E param) {
				return equality().areEqual((E) searched, param);
			}
		});
	}

	@Override
	public DistinctCollectionImpl<E> reversed() { // Optimization.
	    return new DistinctCollectionImpl<E>(inner.reversed());
	}

	/** Default distinct elements iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {

		private final FastCollection<E> iterated;
		private final Iterator<E> inner;
		private boolean onNext;
		private E next;

		public IteratorImpl(Iterator<E> inner, Equality<? super E> equality) {
			this.inner = inner;
			this.iterated = (equality instanceof Order) ? FastSet
					.newSet((Order<? super E>) equality) : FastTable
					.newTable(equality);
		}

		@Override
		public boolean hasNext() {
			if (onNext)
				return true;
			// Move to next.
			while (inner.hasNext()) {
				next = inner.next();
				synchronized (iterated) { // To support split iterators.
					if (iterated.contains(next))
						continue; // Already iterated.
					iterated.add(next);
				}
				onNext = true;
				return true;
			}
			return false;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			onNext = false;
			return next;
		}

		@Override
		public void remove() {
			inner.remove();
		}

	}
}
