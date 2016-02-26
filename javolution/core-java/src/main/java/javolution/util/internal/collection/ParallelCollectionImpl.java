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

import javolution.context.ConcurrentContext;
import javolution.util.FastCollection;
import javolution.util.FastTable;
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
		private final Iterator<E> iterator; // Shared

		public ForEachRunnable(Consumer<? super E> consumer,
				Iterator<E> iterator) {
			this.consumer = consumer;
			this.iterator = iterator;
		}

		@Override
		public void run() {
			while (iterator.hasNext()) {
			   consumer.accept(iterator.next());
			}
		}

	}

	private static class ReduceRunnable<E> implements Runnable {
		private final BinaryOperator<E> operator;
		private final Iterator<E> iterator;
		private E result;

		public ReduceRunnable(BinaryOperator<E> operator,
				Iterator<E> iterator) {
			this.operator = operator;
			this.iterator = iterator;
		}

		@Override
		public void run() {
			if (!iterator.hasNext()) return;
			result = iterator.next();
			while (iterator.hasNext()) {
				result = operator.apply(result, iterator.next());
			}
		}
	}

	private static class RemoveIfRunnable<E> implements Runnable {
		private final Predicate<? super E> filter;
		private final Iterator<E> iterator;
		private boolean result;

		public RemoveIfRunnable(Predicate<? super E> filter,
				Iterator<E> iterator) {
			this.filter = filter;
			this.iterator = iterator;
		}

		@Override
		public void run() {
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
			int n = ctx.getConcurrency() + 1;
			@SuppressWarnings("unchecked")
			FastCollection<E>[] subViews = new FastCollection[n];
			inner.subViews(subViews);
			for (FastCollection<E> subView : subViews) {
				if (subView == null) continue;
				ctx.execute(new ForEachRunnable<E>(consumer, subView.iterator()));
			}			
		} finally {
			ctx.exit(); // Joins.
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		return inner.iterator();
	}

	@Override
	public E reduce(BinaryOperator<E> operator) {
		FastTable<ReduceRunnable<E>> runnables = FastTable.newTable();
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int n = ctx.getConcurrency() + 1;
			@SuppressWarnings("unchecked")
			FastCollection<E>[] subViews = new FastCollection[n];
			inner.subViews(subViews);
			for (FastCollection<E> subView : subViews) {
				if (subView == null) continue;
				ReduceRunnable<E> r = new ReduceRunnable<E>(operator, subView.iterator()); 
				ctx.execute(r);
				runnables.add(r);
			}			
		} finally {
			ctx.exit();
		}
		Iterator<ReduceRunnable<E>> itr = runnables.iterator();
		if (!itr.hasNext()) return null;
		E accumulator = itr.next().result;
		while (itr.hasNext()) {
			accumulator = operator.apply(accumulator, itr.next().result);
		}
		return accumulator;
	}

	@Override
	public boolean remove(Object searched) { // Optimization.
		return inner.remove(searched);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		FastTable<RemoveIfRunnable<E>> runnables = FastTable.newTable();
		ConcurrentContext ctx = ConcurrentContext.enter();
		try {
			int n = ctx.getConcurrency() + 1;
			@SuppressWarnings("unchecked")
			FastCollection<E>[] subViews = new FastCollection[n];
			inner.subViews(subViews);
			for (FastCollection<E> subView : subViews) {
				if (subView == null) continue;
				RemoveIfRunnable<E> r = new RemoveIfRunnable<E>(filter, subView.iterator()); 
				ctx.execute(r);
				runnables.add(r);
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
