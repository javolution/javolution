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

import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.Equality;

/**
 * A sequential view tracking insertion order.
 */
public final class LinkedCollectionImpl<E> extends SequentialCollectionImpl<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastTable<E> ordered;

	public LinkedCollectionImpl(FastCollection<E> inner) {
		super(inner);
		// This class is thread-safe if inner is thread-safe (ordered itself is thread-safe).
		ordered = new FractalTable<E>().using(FastTable.newTable(inner.equality()); 
    }
	
	private LinkedCollectionImpl(FastCollection<E> inner, FastTable<E> ordered) {
		super(inner);
		this.ordered = ordered;
	}

	@Override
	public boolean add(E element) {
		boolean added = inner.add(element);
		if (added) 
			ordered.add(element);
		return added;
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
		ordered.clear();
	}

	@Override
	public LinkedCollectionImpl<E> clone() {
		return new LinkedCollectionImpl<E>(inner.clone(), ordered.clone());
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
		return new IteratorImpl(); 
	}

	@Override
	public boolean remove(Object searched) {
		boolean removed = inner.remove(searched);
		if (removed) ordered.remove(searched);
		return removed;
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	/** Default linked iterator for generic collections **/
	private class IteratorImpl implements Iterator<E> {
		Iterator<E> orderedIterator;
		E next;

		public IteratorImpl() {
			orderedIterator = ordered.iterator();
		}

		@Override
		public boolean hasNext() {
			return orderedIterator.hasNext();
		}

		@Override
		public E next() {
			next = orderedIterator.next();
			return next;
		}

		@Override
		public void remove() {
			orderedIterator.remove();
			inner.remove(next);
			next = null;
		}
	}

}
