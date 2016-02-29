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
import javolution.util.function.BinaryOperator;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final Predicate<? super E> filter;
	private final FastCollection<E> inner;

	public FilteredCollectionImpl(FastCollection<E> inner,
			Predicate<? super E> filter) {
		this.inner = inner;
		this.filter = filter;
	}

	@Override
	public boolean add(E element) {
		if (!filter.test(element))
			return false;
		return inner.add(element);
	}

	@Override
	public FastCollection<E> clone() {
		return new FilteredCollectionImpl<E>(inner.clone(), filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object searched) {
		if (!filter.test((E) searched))
			return false;
		return inner.contains(searched);
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public Iterator<E> iterator() {
		return new IteratorImpl<E>(inner.iterator(), filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object searched) {
		if (!filter.test((E) searched))
			return false;
		return inner.remove(searched);
	}
	
	@Override
	public FilteredCollectionImpl<E> reversed() { // Optimization.
	    return new FilteredCollectionImpl<E>(inner.reversed(), filter);
	}

	@Override
	public FilteredCollectionImpl<E> parallel() { // Partial support.
	    return new FilteredCollectionImpl<E>(inner.parallel(), filter);
	}
	
	@Override
	public void forEach(final Consumer<? super E> consumer) {
		inner.forEach(new Consumer<E>() {
			@Override
			public void accept(E param) {
				if (filter.test(param)) consumer.accept(param);				
			}});
	}

	@Override
	public boolean removeIf(final Predicate<? super E> toRemove) {
		return inner.removeIf(new Predicate<E>() {
			@Override
			public boolean test(E param) {
				return filter.test(param) && toRemove.test(param);
			}});
	}

	@Override
	public E reduce(BinaryOperator<E> operator) {
		final FractalTable<E> toReduce = new FractalTable<E>();
		inner.forEach(new Consumer<E>() { // Parallel.
			@Override
			public void accept(E param) {
				if (filter.test(param)) {
					synchronized (toReduce) {
						toReduce.add(param);				
					}
				}
			}});		
		return toReduce.reduce(operator); // Sequential.
	}

	/** Default filtered iterator for generic collections **/
	private static class IteratorImpl<E> implements Iterator<E> {

		private final Predicate<? super E> filter;
		private final Iterator<E> inner;
		private boolean onNext;
		private E next;

		public IteratorImpl(Iterator<E> inner, Predicate<? super E> filter) {
			this.inner = inner;
			this.filter = filter;
		}

		@Override
		public boolean hasNext() {
			if (onNext)
				return true;
			// Move to next.
			while (inner.hasNext()) {
				next = inner.next();
				if (!filter.test(next))
					continue; // Ignored
				onNext = true;
				return true;
			}
			return false;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			onNext = false;
			return next;
		}

		@Override
		public void remove() {
			inner.remove();
		}
		
	}

}
