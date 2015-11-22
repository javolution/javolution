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
import javolution.util.FastTable;
import javolution.util.function.Equality;

/**
 * A view tracking insertion order.
 */
public final class LinkedCollectionImpl<E> extends FastCollection<E> {

	private static class IteratorImpl<E> implements FastIterator<E> {
		private final FastIterator<E> insertionOrdered;
		private final FastCollection<E> collection;
		private E next;

		public IteratorImpl(FastIterator<E> insertionOrdered,
				FastCollection<E> collection) {
			this.insertionOrdered = insertionOrdered;
			this.collection = collection;
		}

		@Override
		public boolean hasNext() {
			return insertionOrdered.hasNext();
		}

		@Override
		public E next() {
			next = insertionOrdered.next();
			return next;
		}

		@Override
		public void remove() {
			insertionOrdered.remove();
			collection.remove(next);
		}

		@Override
		public FastIterator<E> reversed() {
			return new IteratorImpl<E>(insertionOrdered.reversed(), collection);
		}

		@Override
		public FastIterator<E>[] split(FastIterator<E>[] subIterators) {
			insertionOrdered.split(subIterators);
			int i = 0;
			for (FastIterator<E> itr : subIterators)
				if (itr != null)
					subIterators[i++] = new IteratorImpl<E>(itr, collection);
			return subIterators;
		}
	}

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;
	private final FastTable<E> insertionOrdered = FastTable
			.newTable(equality());

	public LinkedCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		boolean added = inner.add(element);
		if (added)
			insertionOrdered.add(element);
		return added;
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
		insertionOrdered.clear();
	}

	@Override
	public LinkedCollectionImpl<E> clone() {
		return new LinkedCollectionImpl<E>(inner.clone());
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
		return new IteratorImpl<E>(insertionOrdered.iterator(), inner);
	}

	@Override
	public boolean remove(Object searched) {
		boolean removed = inner.remove(searched);
		if (removed)
			insertionOrdered.remove(searched);
		return removed;
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

}
