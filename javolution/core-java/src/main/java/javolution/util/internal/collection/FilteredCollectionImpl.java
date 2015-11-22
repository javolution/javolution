/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.NoSuchElementException;

import javolution.util.FastCollection;
import javolution.util.FastIterator;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends FastCollection<E> {

	private static class IteratorImpl<E> implements FastIterator<E> {

		private final Predicate<? super E> filter;
		private final FastIterator<E> inner;
		private boolean onNext;
		private E next;

		public IteratorImpl(FastIterator<E> inner, Predicate<? super E> filter) {
			this.inner = inner;
			this.filter = filter;
		}

		@Override
		public boolean hasNext() {
			if (onNext)
				return true;
			// Move to next.
			while (inner.hasNext()) {
				next = inner.next();
				if (!filter.test(next))
					continue; // Ignored
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

		@Override
		public FastIterator<E> reversed() {
			return new IteratorImpl<E>(inner.reversed(), filter);
		}

		@Override
		public FastIterator<E>[] split(FastIterator<E>[] subIterators) {
			inner.split(subIterators);
			int i = 0;
			for (FastIterator<E> itr : subIterators)
				if (itr != null)
					subIterators[i++] = new IteratorImpl<E>(itr, filter);
			return subIterators;
		}

	}

	private static final long serialVersionUID = 0x700L; // Version.
	private final Predicate<? super E> filter;
	private final FastCollection<E> inner;

	public FilteredCollectionImpl(FastCollection<E> inner,
			Predicate<? super E> filter) {
		this.inner = inner;
		this.filter = filter;
	}

	@Override
	public boolean add(E element) {
		if (!filter.test(element))
			return false;
		return inner.add(element);
	}

	@Override
	public FastCollection<E> clone() {
		return new FilteredCollectionImpl<E>(inner.clone(), filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object searched) {
		if (!filter.test((E) searched))
			return false;
		return inner.contains(searched);
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public FastIterator<E> iterator() {
		return new IteratorImpl<E>(inner.iterator(), filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object searched) {
		if (!filter.test((E) searched))
			return false;
		return inner.remove(searched);
	}

}
