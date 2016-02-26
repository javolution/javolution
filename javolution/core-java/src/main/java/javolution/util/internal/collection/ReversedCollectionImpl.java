/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.FastCollection;
import javolution.util.FractalTable;
import javolution.util.function.Equality;

/**
 * A reversed view over a collection (copy-on-write).
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
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return new IteratorImpl<E>(inner);
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public FastCollection<E> reversed() { // Optimization.
	    return inner;
	}

	/** Default reversed iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {
		private final FastCollection<E> target;
		private final FractalTable<E> elements = new FractalTable<E>();
		private int nextIndex;
		private int currentIndex = -1;
		
		public IteratorImpl(FastCollection<E> target) {
			this.target = target;
			elements.addAll(target);
			nextIndex = elements.size() - 1;
		}

		@Override
		public boolean hasNext() {
			return nextIndex >= 0;
		}

		@Override
		public E next() {
			if (nextIndex < 0) throw new NoSuchElementException();
			currentIndex = nextIndex--;
			return elements.get(currentIndex);
		}

		@Override
		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException();
			target.remove(elements.get(currentIndex));
			currentIndex = -1;
		}
	}
	
}