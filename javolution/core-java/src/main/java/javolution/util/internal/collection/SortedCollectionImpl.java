/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Comparator;

import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;

	private final Comparator<? super E> cmp;

	public SortedCollectionImpl(FastCollection<E> inner,
			Comparator<? super E> cmp) {
		this.inner = inner;
		this.cmp = cmp;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
	}

	@Override
	public SortedCollectionImpl<E> clone() {
		return new SortedCollectionImpl<E>(inner.clone(), cmp);
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
		final FastTable<E> sorted = FastTable.newTable(equality());
		sorted.addAll(inner);
		sorted.sort(cmp);
		return sorted.iterator();
	}

	@Override
	public boolean remove(Object searched) { // Optimization.
		return inner.remove(searched);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return inner.removeIf(filter);
	}

	@Override
	public SortedCollectionImpl<E> reversed() { // Optimization.
		return new SortedCollectionImpl<E>(inner, new Comparator<E>() {

			@Override
			public int compare(E left, E right) {
				return cmp.compare(right, left);
			}
		});
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		return inner.trySplit(n);
	}

}
