/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import javolution.util.FastTable;
import javolution.util.function.Equality;

/**
 * A view over a portion of a table.
 */
public final class SubTableImpl<E> extends FastTable<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final int fromIndex;
	private int toIndex;
	private final FastTable<E> inner;

	public SubTableImpl(FastTable<E> inner, int fromIndex, int toIndex) {
		this.inner = inner;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	@Override
	public boolean add(E element) {
		inner.add(toIndex++, element);
		return true;
	}

	@Override
	public void add(int index, E element) {
		if ((index < 0) && (index > size()))
			indexError(index);
		inner.add(index + fromIndex, element);
		toIndex++;
	}

	@Override
	public void clear() {
		for (int i = toIndex - 1; i >= fromIndex; i--) { 
			// Better to do it from the end (less shift).
			inner.remove(i);
		}
		toIndex = fromIndex;
	}

	@Override
	public SubTableImpl<E> clone() {
		return new SubTableImpl<E>(inner, fromIndex, toIndex);
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public E get(int index) {
		if ((index < 0) && (index >= size()))
			indexError(index);
		return inner.get(index + fromIndex);
	}

	@Override
	public boolean isEmpty() {
		return fromIndex == toIndex;
	}

	@Override
	public E remove(int index) {
		if ((index < 0) && (index >= size()))
			indexError(index);
		toIndex--;
		return inner.remove(index + fromIndex);
	}

	@Override
	public E set(int index, E element) {
		if ((index < 0) && (index >= size()))
			indexError(index);
		return inner.set(index + fromIndex, element);
	}

	@Override
	public int size() {
		return toIndex - fromIndex;
	}
}
