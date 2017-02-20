/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.javolution.annotations.Parallel;
import org.javolution.context.ConcurrentContext;
import org.javolution.util.FastCollection;
import org.javolution.util.SparseSet;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A view to support parallel processing (methods annotated {@link Parallel}).
 */
public final class ParallelCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.

    public static <E> void forEach(FastCollection<E> that, final Consumer<? super E> consumer) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            final FastCollection<E>[] subViews = that.trySplit(concurrency + 1);
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

    @SuppressWarnings("unchecked")
    public static <E> E reduce(FastCollection<E> that, final BinaryOperator<E> operator) {
        final E[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            final FastCollection<E>[] subViews = that.trySplit(concurrency + 1);
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
        E accumulator = results[0];
        for (int i = 1; i < results.length;)
            accumulator = operator.apply(accumulator, results[i]);
        return accumulator;
    }

    public static <E> boolean removeIf(FastCollection<E> that, final Predicate<? super E> matching) {
        final SparseSet<E> toRemove = new SparseSet<E>();
        // Default equality comparator, assumes that if x.equals(y) then matching.test(x) == matching.

        ParallelCollectionImpl.forEach(that, new Consumer<E>() { // Parallel
            @Override
            public void accept(E param) {
                if (matching.test(param)) {
                    synchronized (toRemove) {
                        toRemove.add(param);
                    }
                }
            }
        });
        that.removeIf(new Predicate<E>() { // Sequential.
            @Override
            public boolean test(E param) {
                return toRemove.contains(param);
            }
        });
        return !toRemove.isEmpty();
    }

    public static <E> boolean until(FastCollection<E> that, final Predicate<? super E> matching) {
        final AtomicBoolean found = new AtomicBoolean(false);
        final Predicate<E> matchingOrFound = new Predicate<E>() {
            @Override
            public boolean test(E param) {
                if (found.get())
                    return true;
                if (!matching.test(param))
                    return false;
                found.set(true);
                return true;
            }
        };
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            final FastCollection<E>[] subViews = that.trySplit(concurrency + 1);
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
        return found.get();
    }

    private final FastCollection<E> inner;

    public ParallelCollectionImpl(FastCollection<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element);
    }

    @Override
    public void clear() {
        removeIf(Predicate.TRUE); // Parallel.
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
        ParallelCollectionImpl.forEach(inner, consumer);
    }

    @Override
    public boolean isEmpty() {
        return until(Predicate.TRUE); // Parallel.
    }

    @Override
    public Iterator<E> iterator() {
        return inner.iterator();
    }

    @Override
    public E reduce(BinaryOperator<E> operator) {
        return ParallelCollectionImpl.reduce(inner, operator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> matching) {
        return ParallelCollectionImpl.removeIf(inner, matching);
    }

    @Override
    public FastCollection<E> sequential() {
        return inner.sequential();
    }

    @Override
    public int size() {
        final AtomicInteger count = new AtomicInteger(0);
        forEach(new Consumer<E>() {// Parallel.
            @Override
            public void accept(E param) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    @Override
    public FastCollection<E>[] trySplit(int n) {
        return inner.trySplit(n);
    }

    @Override
    public boolean until(Predicate<? super E> matching) {
        return ParallelCollectionImpl.until(inner, matching);
    }

}
