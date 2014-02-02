/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.lang.Constant;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;

/**
 * The default {@link javolution.util.ConstantTable ConstantTable} 
 * implementation.
 */
@Constant
public final class ConstantTableImpl<E> extends TableView<E> {

	/** Internal iterator faster than generic TableIteratorImpl. */
	private class IteratorImpl implements Iterator<E> {
		private int nextIndex;

		@Override
		public boolean hasNext() {
			return nextIndex < elements.length;
		}

		@Override
		public E next() {
			if (nextIndex >= elements.length)
				throw new NoSuchElementException();
			return elements[nextIndex++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Constant tables cannot be modified.");
		}
	}

	private static final long serialVersionUID = 0x610L; // Version.

	private final E[] elements;

	public ConstantTableImpl(E[] elements) {
		super(null); // Root class.
		this.elements = elements;
	}

	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public ConstantTableImpl<E> clone() {
		return this;
	}

	@Override
	public Equality<? super E> comparator() {
		return Equalities.STANDARD;
	}

	@Override
	public E get(int index) {
		return elements[index];
	}

	@Override
	public Iterator<E> iterator() {
		return new IteratorImpl();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public int size() {
		return elements.length;
	}

}
