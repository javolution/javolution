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
 * A table view using a custom equality.
 */
public final class CustomEqualityTableImpl<E> extends FastTable<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastTable<E> inner;
	private final Equality<? super E> equality;

	public CustomEqualityTableImpl(FastTable<E> inner,
			Equality<? super E> equality) {
		this.inner = inner;
		this.equality = equality;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	public void add(int index, E element) {
		inner.add(index, element);
	}

	@Override
	public void clear() {
		inner.clear();
	}

	@Override
	public FastTable<E> clone() {
		return new CustomEqualityTableImpl<E>(inner.clone(), equality);
	}

	@Override
	public Equality<? super E> equality() {
		return equality;
	}

	@Override
	public E get(int index) {
		return inner.get(index);
	}

	@Override
	public E remove(int index) {
		return inner.remove(index);
	}

	@Override
	public E set(int index, E element) {
		return inner.set(index, element);
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
