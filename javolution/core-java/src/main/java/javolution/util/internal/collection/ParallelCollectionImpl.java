/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import javolution.context.ConcurrentContext;
import javolution.util.FastCollection;
import javolution.util.FastIterator;
import javolution.util.function.BinaryOperator;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;

/**
 * A view allowing parallel bulk operations.
 */
public class ParallelCollectionImpl<E> extends FastCollection<E> {

	private static class ForEachRunnable<E> implements Runnable {
		private final Consumer<? super E> consumer;
		private final FastIterator<E> iterator;

		public ForEachRunnable(Consumer<? super E> consumer,
				FastIterator<E> iterator) {
			this.consumer = consumer;
			this.iterator = iterator;
		}

		@Override
		public void run() {
			if (iterator == null)
				return;
			while (iterator.hasNext()) {
				consumer.accept(iterator.next());
			}
		}

	}

	private static class ReduceRunnable<E> implements Runnable {
		private final BinaryOperator<E> operator;
		private final FastIterator<E> iterator;
		private E result;

		public ReduceRunnable(BinaryOperator<E> operator,
				FastIterator<E> iterator) {
			this.operator = operator;
			this.iterator = iterator;
		}

		@Override
		public void run() {
			if (iterator == null)
				return;
			E result = null;
			while (iterator.hasNext()) {
				E next = iterator.next();
				if (next == null)
					continue;
				result = (result != null) ? operator.apply(result, next) : next;
			}
		}
	}

	private static class RemoveIfRunnable<E> implements Runnable {
		private final Predicate<? super E> filter;
		private final FastIterator<E> iterator;
		private boolean result;

		public RemoveIfRunnable(Predicate<? super E> filter,
				FastIterator<E> iterator) {
			this.filter = filter;
			this.iterator = iterator;
		}

		@Override
		public void run() {
			if (iterator == null)
				return;
			while (iterator.hasNext()) {
				if (filter.test(iterator.next())) {
					iterator.remove();
					result = true;
				}
			}
		}
	}

	private static final long serialVersionUID = 0x700L; // Version.

	protected final FastCollection<E> inner;

	public ParallelCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	public FastCollection<E> clone() {
		return new ParallelCollectionImpl<E>(inner.clone());
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
	public void forEach(Consumer<? super E> consumer) {
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			FastIterator<E> itr = iterator();
			@SuppressWarnings("unchecked")
			FastIterator<E>[] subIterators = itr
					.split((FastIterator<E>[]) new FastIterator[concurrency + 1]);
			for (int i = 0; i < concurrency; i++) {
				ctx.execute(new ForEachRunnable<E>(consumer, subIterators[i]));
			}
			new ForEachRunnable<E>(consumer, subIterators[concurrency]).run(); // Current
																				// thread.
		} finally {
			ctx.exit();
		}
	}

	@Override
	public FastIterator<E> iterator() {
		return inner.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E reduce(BinaryOperator<E> operator) {
		ReduceRunnable<E>[] runnables;
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			FastIterator<E> itr = iterator();
			final FastIterator<E>[] subIterators = itr
					.split((FastIterator<E>[]) new FastIterator[concurrency + 1]);
			runnables = new ReduceRunnable[concurrency + 1];
			for (int i = 0; i < runnables.length; i++) {
				runnables[i] = new ReduceRunnable<E>(operator, subIterators[i]);
				if (i < concurrency)
					ctx.execute(runnables[i]);
				else
					runnables[i].run(); // Current thread.
			}
		} finally {
			ctx.exit();
		}
		E result = runnables[0].result;
		for (int i = 1; i < runnables.length; i++) {
			E next = runnables[i].result;
			if (result == null)
				result = next;
			else if (next != null)
				result = operator.apply(result, next);
		}
		return result;

	}

	@Override
	public boolean remove(Object searched) { // Optimization.
		return inner.remove(searched);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		RemoveIfRunnable<E>[] runnables;
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int concurrency = ctx.getConcurrency();
			FastIterator<E> itr = iterator();
			final FastIterator<E>[] subIterators = itr
					.split((FastIterator<E>[]) new FastIterator[concurrency + 1]);
			runnables = new RemoveIfRunnable[concurrency + 1];
			for (int i = 0; i < runnables.length; i++) {
				runnables[i] = new RemoveIfRunnable<E>(filter, subIterators[i]);
				if (i < concurrency)
					ctx.execute(runnables[i]);
				else
					runnables[i].run(); // Current thread.
			}
		} finally {
			ctx.exit();
		}
		for (RemoveIfRunnable<E> runnable : runnables) {
			if (runnable.result)
				return true;
		}
		return false;
	}

}
