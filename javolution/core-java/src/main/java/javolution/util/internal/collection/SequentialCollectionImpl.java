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
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A sequential view over a collection.
 */
public final class SequentialCollectionImpl<E> extends FastCollection<E> implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    public SequentialCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void atomic(Runnable update) {
        target.atomic(update);
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
                return controller.doReversed();
            }

            @Override
            public boolean doSequential() {
                return true;
            }

            @Override
            public boolean isTerminated() {
                return controller.isTerminated();
            }
        });
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            final IterationController controller) {
        return target.removeIf(filter, new IterationController() {

            @Override
            public boolean doReversed() {
                return controller.doReversed();
            }

            @Override
            public boolean doSequential() {
                return true;
            }

            @Override
            public boolean isTerminated() {
                return controller.isTerminated();
            }
        });
    }

    @Override
    protected SequentialCollectionImpl<E> service() {
        return this;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n); // Forwards (view affects iteration controller only).
    }

}
