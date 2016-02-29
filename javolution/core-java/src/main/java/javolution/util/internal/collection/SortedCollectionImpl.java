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
import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.FastCollection;
import javolution.util.FractalTable;

/**
 * A sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends SequentialCollectionImpl<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final Comparator<? super E> cmp;

	public SortedCollectionImpl(FastCollection<E> inner,
			Comparator<? super E> cmp) {
		super(inner);
		this.cmp = cmp;
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
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return new IteratorImpl<E>(inner, cmp);
	}

	@Override
	public boolean remove(Object searched) { // Optimization.
		return inner.remove(searched);
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}
	
	@Override
	public SortedCollectionImpl<E> reversed() { // Optimization.
	    return new SortedCollectionImpl<E>(inner, new Comparator<E>() {

			@Override
			public int compare(E arg0, E arg1) {
				return cmp.compare(arg1, arg0);
			}});
	}

	/** Default sorted iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {
		private final FastCollection<E> target;
		private final FractalTable<E> elements = new FractalTable<E>();
		private int nextIndex;
		private int currentIndex = -1;
		
		public IteratorImpl(FastCollection<E> target, Comparator<? super E> cmp) {
			this.target = target;
			elements.addAll(target);
			elements.sort(cmp);
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
