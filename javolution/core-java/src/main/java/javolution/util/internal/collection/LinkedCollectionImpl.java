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
import javolution.util.FastTable;
import javolution.util.function.Equality;

/**
 * A view tracking insertion order.
 */
public final class LinkedCollectionImpl<E> extends FastCollection<E> {

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
	public Iterator<E> iterator() {
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

	/** Default linked iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {
		private final Iterator<E> inner;
		private final FastCollection<E> target;
		private E next;

		public IteratorImpl(Iterator<E> inner,
				FastCollection<E> collection) {
			this.inner = inner;
			this.target = collection;
		}

		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public E next() {
			next = inner.next();
			return next;
		}

		@Override
		public void remove() {
			inner.remove();
			target.remove(next);
		}
	}

}
