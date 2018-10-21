/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.concurrent.atomic.AtomicInteger;

import org.javolution.annotations.Parallel;
import org.javolution.context.ConcurrentContext;
import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.FastSet;
import org.javolution.util.FastTable;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A view to support parallel processing (methods annotated {@link Parallel}).
 */
public final class ParallelCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.

    private final AbstractCollection<E> inner;

    public ParallelCollectionImpl(AbstractCollection<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Parallel
    public boolean anyMatch(Predicate<? super E> predicate) {
        AnyMatchRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new AnyMatchRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new AnyMatchRunnable<E>(subViews[i], predicate));
            (results[0] = new AnyMatchRunnable<E>(subViews[0], predicate)).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        for (AnyMatchRunnable<E> result : results)
            if (result.matchFound) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Parallel
    public E findAny() {
        AnyRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new AnyRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new AnyRunnable<E>(subViews[i]));
            (results[0] = new AnyRunnable<E>(subViews[0])).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        for (AnyRunnable<E> result : results)
            if (result.found != null) return result.found;
        return null;
    }

    @Parallel
    @Override
    public void clear() {
        removeIf(Predicate.TRUE);
    }

    @Override
    public ParallelCollectionImpl<E> clone() {
        return new ParallelCollectionImpl<E>(inner.clone());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Parallel
    public AbstractCollection<E> collect() {
        CollectRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new CollectRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new CollectRunnable<E>(subViews[i]));
            (results[0] = new CollectRunnable<E>(subViews[0])).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        AbstractCollection<E> collection = results[0].collection;
        for (int i = 1; i < results.length; i++)
            collection.addAll(results[i].collection);
        return collection;
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    @Parallel
    public void forEach(Consumer<? super E> consumer) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(new ForEachRunnable<E>(subViews[i], consumer));
            new ForEachRunnable<E>(subViews[0], consumer).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
    }

    @SuppressWarnings("unchecked")
    @Parallel
    @Override
    public boolean isEmpty() {
        IsEmptyRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new IsEmptyRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new IsEmptyRunnable<E>(subViews[i]));
            (results[0] = new IsEmptyRunnable<E>(subViews[0])).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        for (IsEmptyRunnable<E> result : results)
            if (!result.isEmpty) return false;
        return true;
    }

    @Override
    public FastIterator<E> iterator() {
        return inner.iterator();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return inner.descendingIterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Parallel
    public E reduce(BinaryOperator<E> operator) {
        ReduceRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new ReduceRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new ReduceRunnable<E>(subViews[i], operator));
            (results[0] = new ReduceRunnable<E>(subViews[0], operator)).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        E accumulator = results[0].accumulator;
        for (int i = 1; i < results.length;)
            accumulator = operator.apply(accumulator, results[i].accumulator);
        return accumulator;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Parallel
    public boolean removeIf(Predicate<? super E> filter) {
        RemoveIfRunnable<E>[] results;
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            AbstractCollection<E>[] subViews = inner.trySplit(ctx.getConcurrency() + 1);
            results = new RemoveIfRunnable[subViews.length];
            for (int i = 1; i < subViews.length; i++)
                ctx.execute(results[i] = new RemoveIfRunnable<E>(subViews[i], filter));
            (results[0] = new RemoveIfRunnable<E>(subViews[0], filter)).run(); // Current thread needs to work too!
        } finally {
            ctx.exit(); // Waits for concurrent completion.
        }
        final FastSet<E> toRemove = new FastSet<E>(Order.identity());
        for (RemoveIfRunnable<E> result : results)
            toRemove.addAll(result.toRemove);
        return inner.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                return toRemove.contains(param);
            }
        });
    }

    @Override
    public AbstractCollection<E> sequential() {
        return inner.sequential();
    }

    @Parallel
    @Override
    public int size() {
        final AtomicInteger count = new AtomicInteger(0);
        forEach(new Consumer<E>() {
            @Override
            public void accept(E param) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    @Override
    public AbstractCollection<E>[] trySplit(int n) {
        return inner.trySplit(n);
    }

    private static final class AnyMatchRunnable<E> implements Runnable {
        private final AbstractCollection<E> subView;
        private final Predicate<? super E> predicate;
        private boolean matchFound;

        private AnyMatchRunnable(AbstractCollection<E> subView, Predicate<? super E> predicate) {
            this.subView = subView;
            this.predicate = predicate;
        }

        @Override
        public void run() {
            matchFound = subView.anyMatch(predicate);
        }
    }

    private static final class AnyRunnable<E> implements Runnable {
        private final AbstractCollection<E> subView;
        private E found;

        private AnyRunnable(AbstractCollection<E> subView) {
            this.subView = subView;
        }

        @Override
        public void run() {
            found = subView.findAny();
        }
    }

    private static final class CollectRunnable<E> implements Runnable {
        private final AbstractCollection<E> subView;
        private AbstractCollection<E> collection;

        private CollectRunnable(AbstractCollection<E> subView) {
            this.subView = subView;
        }

        @Override
        public void run() {
            collection = subView.collect();
        }
    }

    private static final class ForEachRunnable<E> implements Runnable, Predicate<E> {
        private final AbstractCollection<E> subView;
        private final Consumer<? super E> consumer;

        private ForEachRunnable(AbstractCollection<E> subView, Consumer<? super E> consumer) {
            this.subView = subView;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            subView.iterator().hasNext(this);
        }

        @Override
        public boolean test(E param) {
            consumer.accept(param);
            return false;
        }
    }

    private static final class IsEmptyRunnable<E> implements Runnable {
        private final AbstractCollection<E> subView;
        private boolean isEmpty;

        private IsEmptyRunnable(AbstractCollection<E> subView) {
            this.subView = subView;
        }

        @Override
        public void run() {
            isEmpty = subView.isEmpty();
        }
    }

    private static final class ReduceRunnable<E> implements Runnable, Predicate<E> {
        private final AbstractCollection<E> subView;
        private final BinaryOperator<E> operator;
        private E accumulator;

        private ReduceRunnable(AbstractCollection<E> subView, BinaryOperator<E> operator) {
            this.subView = subView;
            this.operator = operator;
        }

        @Override
        public void run() {
            subView.iterator().hasNext(this);
        }

        @Override
        public boolean test(E param) {
            accumulator = (accumulator != null) ? operator.apply(accumulator, param) : param;
            return false;
        }
    }

    private static final class RemoveIfRunnable<E> implements Runnable {
        private final AbstractCollection<E> subView;
        private final Predicate<? super E> filter;
        private final FastTable<E> toRemove = new FastTable<E>();

        private RemoveIfRunnable(AbstractCollection<E> subView, Predicate<? super E> filter) {
            this.subView = subView;
            this.filter = filter;
        }

        @Override
        public void run() {
            FastIterator<E> itr = subView.iterator();
            while (itr.hasNext(filter))
                toRemove.add(itr.next());
        }
    }

}
