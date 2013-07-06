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
import java.util.concurrent.locks.ReadWriteLock;

import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A reversed view over a collection (limitation: iterator does not allow for 
 *  modification).
 */
public final class ReversedCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    public ReversedCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(final Consumer<? super E> consumer,
            final IterationController controller) {
        target.forEach(consumer, new IterationController() {

            @Override
            public boolean doReversed() {
                return !controller.doReversed();
            }

            @Override
            public boolean doSequential() {
                return controller.doSequential();
            }

            @Override
            public boolean isTerminated() {
                return controller.isTerminated();
            }
        });
    }

    @Override
    public ReadWriteLock getLock() {
        return target.getLock();
    }

    @Override
    public Iterator<E> iterator() {
        final FastTable<E> reversed = new FastTable<E>();
        target.forEach(new Consumer<E>() {

            @Override
            public void accept(E e) {
                reversed.addFirst(e);
            }
        }, IterationController.SEQUENTIAL);
        return reversed.unmodifiable().iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            final IterationController controller) {
        return target.removeIf(filter, new IterationController() {

            @Override
            public boolean doReversed() {
                return !controller.doReversed();
            }

            @Override
            public boolean doSequential() {
                return controller.doSequential();
            }

            @Override
            public boolean isTerminated() {
                return controller.isTerminated();
            }
        });
    }

    @Override
    protected ReversedCollectionImpl<E> service() {
        return this;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n); // Forwards (view affects iteration controller only).
    }
}
