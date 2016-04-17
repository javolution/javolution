/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javolution.context.ConcurrentContext;
import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.BinaryOperator;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A view allowing parallel processing.
 */
public class ParallelCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private static final Object NOT_FOUND = new Object();

	private final FastCollection<E> inner;

	// //////////////////////////////////////////////////////////////////////////
	// Closure operations.
	//

	public ParallelCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	@SuppressWarnings("unchecked")
	public FastCollection<E> all() {
		final FastCollection<E> results[];
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			final FastCollection<E>[] subViews = inner
					.trySplit(concurrency + 1);
			results = new FastCollection[subViews.length];
			for (int i = 1; i < subViews.length; i++) {
				final int j = i;
				ctx.execute(new Runnable() {

					@Override
					public void run() {
						results[j] = subViews[j].all();
					}
				});
			}
			results[0] = subViews[0].all();
		} finally {
			ctx.exit(); // Waits for concurrent completion.
		}
		FastTable<E> mergeResults = FastTable.newTable(equality());
		for (FastCollection<E> result : results)
			mergeResults.addAll(result);
		return mergeResults;
	}

	@Override
	public ParallelCollectionImpl<E> clone() {
		return new ParallelCollectionImpl<E>(inner.clone());
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public void forEach(Consumer<? super E> consumer) {
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			final FastCollection<E>[] subViews = inner
					.trySplit(concurrency + 1);
			for (int i = 1; i < subViews.length; i++) {
				final int j = i;
				ctx.execute(new Runnable() {

					@Override
					public void run() {
						subViews[j].forEach(consumer);
					}
				});
			}
			subViews[0].forEach(consumer);
		} finally {
			ctx.exit(); // Waits for concurrent completion.
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Implements abstract methods.

	@Override
	public Iterator<E> iterator() {
		return inner.iterator();
	}

	@Override
	@SuppressWarnings("unchecked")
	public E reduce(BinaryOperator<E> operator) {
		final E[] results;
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			final FastCollection<E>[] subViews = inner
					.trySplit(concurrency + 1);
			results = (E[]) new Object[subViews.length];
			for (int i = 1; i < subViews.length; i++) {
				final int j = i;
				ctx.execute(new Runnable() {

					@Override
					public void run() {
						results[j] = subViews[j].reduce(operator);
					}
				});
			}
			results[0] = subViews[0].reduce(operator);
		} finally {
			ctx.exit(); // Waits for concurrent completion.
		}
		E accumulator = null;
		for (E e : results)
			accumulator = (accumulator != null) ? ((e != null) ? operator
					.apply(accumulator, e) : accumulator) : e;
		return accumulator;
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		final AtomicBoolean changed = new AtomicBoolean(false);
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			final FastCollection<E>[] subViews = inner
					.trySplit(concurrency + 1);
			for (int i = 1; i < subViews.length; i++) {
				final int j = i;
				ctx.execute(new Runnable() {

					@Override
					public void run() {
						if (subViews[j].removeIf(filter))
							changed.set(true);
					}
				});
			}
			if (subViews[0].removeIf(filter))
				changed.set(true);
		} finally {
			ctx.exit(); // Waits for concurrent completion.
		}
		return changed.get();
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		return inner.trySplit(n);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E until(Predicate<? super E> matching) {
		final AtomicReference<Object> found = new AtomicReference<Object>(
				NOT_FOUND);
		final Predicate<E> matchingOrFound = new Predicate<E>() {

			@Override
			public boolean test(E param) {
				if (found.get() != NOT_FOUND)
					return true; // Terminates, already found.
				if (matching.test(param)) {
					found.set(param);
					return true;
				}
				return false;
			}
		};
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			final FastCollection<E>[] subViews = inner
					.trySplit(concurrency + 1);
			for (int i = 1; i < subViews.length; i++) {
				final int j = i;
				ctx.execute(new Runnable() {

					@Override
					public void run() {
						subViews[j].until(matchingOrFound);
					}
				});
			}
		} finally {
			ctx.exit(); // Waits for concurrent completion.
		}
		return (E) found.get();
	}

}
