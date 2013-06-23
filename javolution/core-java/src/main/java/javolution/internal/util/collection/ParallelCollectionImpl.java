/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

import javolution.context.ConcurrentContext;
import javolution.util.FastCollection;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A parallel view over a collection.
 */
public final class ParallelCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    public ParallelCollectionImpl(CollectionService<E> target) {
        this.target = new SharedCollectionImpl<E>(target); // Ensures the target collection is shared.
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void atomic(Runnable action) {
        target.atomic(action);
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(final Consumer<? super E> consumer,
            final IterationController controller) {
        if (controller.doSequential()) {
            target.forEach(consumer, controller); // Sequential.
            return;
        }
        CollectionService<E>[] split = target
                .trySplit(ConcurrentContext.CONCURRENCY.get());
        if (split.length == 0)
            return;
        if (split.length == 1) {
            split[0].forEach(consumer, controller);
        }

        // Parallelization.
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            for (int i = 0; i < split.length; i++) {
                final CollectionService<E> subcollection = split[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        subcollection.forEach(consumer, controller);
                    }
                });
            }
        } finally {
            ctx.exit();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter,
            final IterationController controller) {
        if (controller.doSequential())
            return target.removeIf(filter, controller); // Sequential.
        CollectionService<E>[] split = target
                .trySplit(ConcurrentContext.CONCURRENCY.get());
        if (split.length == 1)
            return split[0].removeIf(filter, controller);

        // Parallelization.
        ConcurrentContext ctx = ConcurrentContext.enter();
        final boolean[] atLeastOneRemoved = new boolean[1];
        try {
            for (int i = 0; i < split.length; i++) {
                final CollectionService<E> subcollection = split[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (subcollection.removeIf(filter, controller)) {
                            atLeastOneRemoved[0] = true;
                        }
                    }
                });
            }
        } finally {
            ctx.exit();
        }
        return atLeastOneRemoved[0];
    }

    @Override
    protected ParallelCollectionImpl<E> service() {
        return this;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n); // Forwards (view affects iteration controller only).
    }

}
