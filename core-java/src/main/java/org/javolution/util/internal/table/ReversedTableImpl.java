/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;

/**
 * A reversed view over a table.
 */
public final class ReversedTableImpl<E> extends FastTable<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastTable<E> inner;

	public ReversedTableImpl(FastTable<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E e) {
		inner.addFirst(e);
		return true;
	}

	@Override
	public void add(int index, E element) {
		inner.add(size() - index - 1, element);
	}

	@Override
	public void clear() {
		inner.clear();
	}

	@Override
	public ReversedTableImpl<E> clone() {
		return new ReversedTableImpl<E>(inner.clone());
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public E get(int index) {
		return inner.get(size() - index - 1);
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public E remove(int index) {
		return inner.remove(size() - index - 1);
	}

	@Override
	public E set(int index, E element) {
		return inner.set(size() - index - 1, element);
	}

	@Override
	public int size() {
		return inner.size();
	}

}
