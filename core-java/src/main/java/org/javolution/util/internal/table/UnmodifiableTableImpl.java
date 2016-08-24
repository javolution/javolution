/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import org.javolution.util.FastCollection;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> extends FastTable<E> {

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
	public int lastIndexOf(Object searched) {
		return inner.lastIndexOf(searched);
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

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		// TODO Auto-generated method stub
		return null;
	}

}
