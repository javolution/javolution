/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A reversed view over a collection.
 */
public final class ReversedCollectionImpl<E> extends FastCollection<E> {
	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;

	public ReversedCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
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
	public FastCollection<E> clone() {
		return new ReversedCollectionImpl<E>(inner.clone());
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
		FastTable<E> reversed = FastTable.newTable();
		for (Iterator<E> itr = inner.iterator(); itr.hasNext();)
			reversed.addFirst(itr.next());
		return reversed.iterator();
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
	public FastCollection<E> reversed() { // Optimization.
		return inner;
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