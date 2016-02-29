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
import javolution.util.FractalTable;
import javolution.util.function.BinaryOperator;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.function.Predicate;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E, R> extends FastCollection<R> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;
	private final Function<? super E, ? extends R> function;

	public MappedCollectionImpl(FastCollection<E> inner,
			Function<? super E, ? extends R> function) {
		this.inner = inner;
		this.function = function;
	}

	@Override
	public boolean add(R element) {
		throw new UnsupportedOperationException(
				"New elements cannot be added to mapped views");
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
	}

	@Override
	public MappedCollectionImpl<E, R> clone() {
		return new MappedCollectionImpl<E, R>(inner.clone(), function);
	}

	@Override
	public Equality<? super R> equality() {
		return Equality.STANDARD;
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<R> iterator() {
		return new IteratorImpl<E, R>(inner.iterator(), function);
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public MappedCollectionImpl<E,R> reversed() { // Optimization.
	    return new MappedCollectionImpl<E,R>(inner.reversed(), function);
	}

	@Override
	public void forEach(final Consumer<? super R> consumer) {
		inner.forEach(new Consumer<E>() {
			@Override
			public void accept(E param) {
				consumer.accept(function.apply(param));				
			}});
	}

	@Override
	public boolean removeIf(final Predicate<? super R> toRemove) {
		return inner.removeIf(new Predicate<E>() {
			@Override
			public boolean test(E param) {
				return toRemove.test(function.apply(param));
			}});
	}

	@Override
	public R reduce(BinaryOperator<R> operator) {
		final FractalTable<R> toReduce = new FractalTable<R>();
		inner.forEach(new Consumer<E>() { // Parallel.
			@Override
			public void accept(E param) {
				R r = function.apply(param);
				synchronized (toReduce) {
					toReduce.add(r);				
				}				
			}});		
		return toReduce.reduce(operator); // Sequential.
	}

	/** Default mapped iterator for generic collections **/
	private static class IteratorImpl<E, R> implements Iterator<R> {
		private final Iterator<E> inner;
		private final Function<? super E, ? extends R> function;

		public IteratorImpl(Iterator<E> inner,
				Function<? super E, ? extends R> function) {
			this.inner = inner;
			this.function = function;
		}

		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public R next() {
			return function.apply(inner.next());
		}

		@Override
		public void remove() {
			inner.remove();
		}

	}

}
