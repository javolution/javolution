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

	private static class LinkedIterator<E> implements FastIterator<E> { // Thread-Safe
		private final FastTable<E> table;
		private final ReadWriteLockImpl lock;
		private int nextIndex;
		private int currentIndex;
		private int fromIndex;
		private int toIndex;
		private LinkedIterator<E> nextIterator;

		public LinkedIterator(FastTable<E> table, int fromIndex, int toIndex,
				ReadWriteLockImpl lock) {
			this.table = table;
			this.lock = lock;
			this.fromIndex = fromIndex;
			this.toIndex = toIndex;
			this.nextIndex = fromIndex;
			this.currentIndex = -1;
		}

		@Override
		public boolean hasNext() {
			lock.readLock.lock();
			try {
				return nextIndex < toIndex;
			} finally {
				lock.readLock.unlock();
			}
		}

		@Override
		public E next() {
			lock.readLock.lock();
			try {
				if (nextIndex >= toIndex)
					throw new NoSuchElementException();
				currentIndex = nextIndex++;
				return table.get(currentIndex);
			} finally {
				lock.readLock.unlock();
			}
		}

		@Override
		public void remove() {
			lock.writeLock.lock();
			try {
				if (currentIndex < 0)
					throw new IllegalStateException();
				table.remove(currentIndex);
				toIndex--;
				currentIndex = -1;
				for (LinkedIterator<E> itr = nextIterator; itr != null; itr = itr.nextIterator)
					itr.shiftLeft();
			} finally {
				lock.writeLock.unlock();
			}
		}

		@Override
		public FastIterator<E> reversed() {
			throw new UnsupportedOperationException(); // Not a list iterator.
		}

		private void shiftLeft() {
			fromIndex--;
			toIndex--;
			currentIndex--;
		}

		@Override
		public FastIterator<E>[] split(FastIterator<E>[] subIterators) {
			subIterators[0] = this; // No splitting.
			return subIterators;
		}
	}

	private int currentIndex = -1;
	private int size;
	private int nextIndex;
	private final FastTable<E> table;

	public TableIteratorImpl(FastTable<E> table, int index) {
		this.table = table;
		this.nextIndex = index;
		this.size = table.size();
	}

	@Override
	public void add(E e) {
		table.add(nextIndex++, e);
		size++;
		currentIndex = -1;
	}

	@Override
	public boolean hasNext() {
		return (nextIndex < size);
	}

	@Override
	public boolean hasPrevious() {
		return nextIndex > 0;
	}

	@Override
	public E next() {
		if (nextIndex >= size)
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
		if (nextIndex <= 0)
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
		size--;
		if (currentIndex < nextIndex) {
			nextIndex--;
		}
		currentIndex = -1;
	}

	@Override
	public FastIterator<E> reversed() {
		return new TableIteratorImpl<E>(table.reversed(), 0);
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
	public FastIterator<E>[] split(FastIterator<E>[] subIterators) {
		if (subIterators.length == 0)
			throw new IllegalArgumentException();
		int rem = size % subIterators.length;
		int subSize = (size - rem) / subIterators.length;
		LinkedIterator<E> previous = null;
		ReadWriteLockImpl lock = new ReadWriteLockImpl();
		int index = 0;
		for (int i = 0; i < subIterators.length; i++) {
			int toIndex = Math.min(size, index + subSize);

			LinkedIterator<E> subIterator = new LinkedIterator<E>(table, index,
					toIndex, lock);
			if (previous != null)
				previous.nextIterator = subIterator;
			previous = subIterator;

			subIterators[i] = subIterator;
			index = toIndex;
		}
		return subIterators;
	}

}
