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
import javolution.util.FastSet;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.internal.FilteredIteratorImpl;
import javolution.util.service.CollectionService;

/**
 * A view which does not iterate twice over the same elements and which 
 * maintains element unicity.
 */
public final class DistinctCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    public DistinctCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        return contains(element) ? false : target.add(element);
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
            IterationController controller) {
        target.forEach(new Consumer<E>() {
            FastSet<E> iterated = new FastSet<E>(target.comparator()).shared(); // Supports parallel iterations.

            @Override
            public void accept(E e) {
                if (!iterated.add(e))
                    return; // Already iterated over.
                consumer.accept(e);
            }
        }, controller);

    }
 
    @Override
    public Iterator<E> iterator() {
        return new FilteredIteratorImpl<E>(target.iterator(),
                new Predicate<E>() {
                    FastSet<E> iterated = new FastSet<E>(target.comparator());

                    @Override
                    public boolean test(E param) {
                        return iterated.add(param);
                    }
                });
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        return target.removeIf(filter, controller);
    }

    @Override
    protected DistinctCollectionImpl<E> service() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DistinctCollectionImpl<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        DistinctCollectionImpl<E>[] result = new DistinctCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new DistinctCollectionImpl<E>(tmp[i]);
        }
        return result;
    }
}
