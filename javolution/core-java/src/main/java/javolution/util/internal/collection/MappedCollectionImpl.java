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
		return Equality.DEFAULT;
	}

	@Override
	public void forEach(final Consumer<? super R> consumer) { // Optimization.
		inner.forEach(new Consumer<E>() {
			@Override
			public void accept(E param) {
				consumer.accept(function.apply(param));
			}});
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<R> iterator() {
		return new Iterator<R>() {
			Iterator<E> itr = inner.iterator();

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public R next() {
				return function.apply(itr.next());
			}
		};
	}

	@Override
	public boolean removeIf(final Predicate<? super R> toRemove) {
		return inner.removeIf(new Predicate<E>() {
			@Override
			public boolean test(E param) {
				return toRemove.test(function.apply(param));
			}
		});
	}

	@Override
	public MappedCollectionImpl<E, R> reversed() { // Optimization.
		return new MappedCollectionImpl<E, R>(inner.reversed(), function);
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FastCollection<R>[] trySplit(int n) {
		FastCollection[] subViews = inner.trySplit(n);
		for (int i = 0; i < subViews.length; i++)
			subViews[i] = new MappedCollectionImpl(subViews[i], function);
		return subViews;
	}

	@Override
	public R until(Predicate<? super R> matching) { // Optimization.
		return function.apply(inner.until(new Predicate<E>() {
			@Override
			public boolean test(E param) {
				return matching.test(function.apply(param));
			}}));
	}
	

}
