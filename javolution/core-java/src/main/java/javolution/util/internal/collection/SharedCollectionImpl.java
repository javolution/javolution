/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Collection;

import javolution.util.FastCollection;
import javolution.util.function.BinaryOperator;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;
import javolution.util.internal.ReadWriteLockImpl;

/**
 * A shared view over a collection (reads-write locks).
 */
public final class SharedCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;
	private final ReadWriteLockImpl lock;

	public SharedCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
		this.lock = new ReadWriteLockImpl();
	}

	public SharedCollectionImpl(FastCollection<E> inner, ReadWriteLockImpl lock) {
		this.inner = inner;
		this.lock = lock;
	}

	@Override
	public boolean add(E element) {
		lock.writeLock.lock();
		try {
			return inner.add(element);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> that) {
		lock.writeLock.lock();
		try {
			return inner.addAll(that);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public E any() {
		lock.readLock.lock();
		try {
			return inner.any();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public void clear() {
		lock.writeLock.lock();
		try {
			inner.clear();
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public SharedCollectionImpl<E> clone() {
		lock.readLock.lock();
		try {
			return new SharedCollectionImpl<E>(inner.clone());
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public FastCollection<E> constant() {
		lock.readLock.lock();
		try {
			return inner.constant();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public boolean contains(final Object searched) {
		lock.readLock.lock();
		try {
			return inner.contains(searched);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> that) {
		lock.readLock.lock();
		try {
			return inner.containsAll(that);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public Equality<? super E> equality() {
		lock.readLock.lock();
		try {
			return inner.equality();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public boolean equals(Object obj) {
		lock.readLock.lock();
		try {
			return inner.equals(obj);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public void forEach(Consumer<? super E> consumer) {
		lock.readLock.lock();
		try {
			inner.forEach(consumer);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public int hashCode() {
		lock.readLock.lock();
		try {
			return inner.hashCode();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		lock.readLock.lock();
		try {
			return inner.isEmpty();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public Iterator<E> iterator() {
		FastCollection<E> innerClone;
		lock.readLock.lock();
		try {
			innerClone = inner.clone();
		} finally {
			lock.readLock.unlock();
		}
		return innerClone.iterator();
	}

	@Override
	public E max() {
		lock.readLock.lock();
		try {
			return inner.max();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public E min() {
		lock.readLock.lock();
		try {
			return inner.min();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public E reduce(BinaryOperator<E> operator) {
		lock.readLock.lock();
		try {
			return inner.reduce(operator);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public boolean remove(Object searched) {
		lock.writeLock.lock();
		try {
			return inner.remove(searched);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> that) {
		lock.writeLock.lock();
		try {
			return inner.removeAll(that);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		lock.writeLock.lock();
		try {
			return inner.removeIf(filter);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> that) {
		lock.writeLock.lock();
		try {
			return inner.retainAll(that);
		} finally {
			lock.writeLock.unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock.lock();
		try {
			return inner.size();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public Object[] toArray() {
		lock.readLock.lock();
		try {
			return inner.toArray();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public <T> T[] toArray(final T[] array) {
		lock.readLock.lock();
		try {
			return inner.toArray(array);
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock.lock();
		try {
			return inner.toString();
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		lock.readLock.lock();
		try {
			FastCollection<E>[] subViews = inner.trySplit(n);
			for (int i = 0; i < subViews.length; i++)
				subViews[i] = new SharedCollectionImpl<E>(subViews[i], lock);
			return subViews;
		} finally {
			lock.readLock.unlock();
		}
	}

	@Override
	public E until(Predicate<? super E> matching) {
		lock.readLock.lock();
		try {
			return inner.until(matching);
		} finally {
			lock.readLock.unlock();
		}
	}

}
