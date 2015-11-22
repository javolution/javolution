/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.ListIterator;

import javolution.util.FastIterator;
import javolution.util.FastTable;
import javolution.util.function.Equality;
import javolution.util.internal.collection.UnmodifiableCollectionImpl;

/**
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> extends FastTable<E> {

	public static class ListIteratorImpl<E> implements ListIterator<E> {
		private final ListIterator<E> itr;

		public ListIteratorImpl(ListIterator<E> itr) {
			this.itr = itr;
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException("Read-Only Collection.");
		}

		@Override
		public boolean hasNext() {
			return itr.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return itr.hasPrevious();
		}

		@Override
		public E next() {
			return itr.next();
		}

		@Override
		public int nextIndex() {
			return itr.nextIndex();
		}

		@Override
		public E previous() {
			return itr.previous();
		}

		@Override
		public int previousIndex() {
			return itr.previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Read-Only Collection.");
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException("Read-Only Collection.");
		}
	}
	private static final long serialVersionUID = 0x700L; // Version.

	private final FastTable<E> inner;

	public UnmodifiableTableImpl(FastTable<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public UnmodifiableTableImpl<E> clone() {
		return new UnmodifiableTableImpl<E>(inner.clone());
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public E get(int index) {
		return inner.get(index);
	}

	@Override
	public int indexOf(Object searched) {
		return inner.indexOf(searched);
	}

	@Override
	public FastIterator<E> iterator() {
		return new UnmodifiableCollectionImpl.IteratorImpl<E>(inner.iterator());
	}

	@Override
	public int lastIndexOf(Object searched) {
		return inner.lastIndexOf(searched);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListIteratorImpl<E>(inner.listIterator());
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListIteratorImpl<E>(inner.listIterator(index));
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("Read-Only Collection.");
	}

	@Override
	public int size() {
		return inner.size();
	}

}
