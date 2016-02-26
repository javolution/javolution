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
import java.util.NoSuchElementException;

import javolution.util.FastIterator;
import javolution.util.FastTable;
import javolution.util.internal.ReadWriteLockImpl;

/**
 * A generic list iterator over a fast table.
 */
public final class TableIteratorImpl<E> implements FastIterator<E>,
		ListIterator<E> {

	private int currentIndex = -1;
	private int fromIndex; // Inclusive.
	private int toIndex; // Exclusive
	private int nextIndex;
	private final FastTable<E> table;

	public TableIteratorImpl(FastTable<E> table, int fromIndex, int toIndex) {
		this.table = table;
		this.nextIndex = fromIndex;
		this.toIndex = toIndex;
	}

	@Override
	public void add(E e) {
		table.add(nextIndex++, e);
		toIndex++;
		currentIndex = -1;
	}

	@Override
	public boolean hasNext() {
		return nextIndex < toIndex;
	}

	@Override
	public boolean hasPrevious() {
		return nextIndex > fromIndex;
	}

	@Override
	public E next() {
		if (nextIndex >= toIndex)
			throw new NoSuchElementException();
		currentIndex = nextIndex++;
		return table.get(currentIndex);
	}

	@Override
	public int nextIndex() {
		return nextIndex;
	}

	@Override
	public E previous() {
		if (nextIndex <= fromIndex)
			throw new NoSuchElementException();
		currentIndex = --nextIndex;
		return table.get(currentIndex);
	}

	@Override
	public int previousIndex() {
		return nextIndex - 1;
	}

	@Override
	public void remove() {
		if (currentIndex < 0)
			throw new IllegalStateException();
		table.remove(currentIndex);
		toIndex--;
		if (currentIndex < nextIndex) {
			nextIndex--;
		}
		currentIndex = -1;
	}

	@Override
	public FastIterator<E> reversed() {
		int size = table.size();
		return new TableIteratorImpl<E>(table.reversed(), size - toIndex, size - fromIndex);
	}

	@Override
	public void set(E e) {
		if (currentIndex >= 0) {
			table.set(currentIndex, e);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public FastIterator<E> trySplit() {
		int half = (toIndex - fromIndex) >> 1;
		if (half == 0) return null; // Cannot split single element.
		FastIterator<E> tail = new TableIteratorImpl<E>(table, fromIndex + half, toIndex);
		toIndex = fromIndex + half;
		return tail;		
	}

}
