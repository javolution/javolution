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

import javolution.internal.util.FilteredIteratorImpl;
import javolution.util.FastCollection;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Predicate<? super E> filter;
    private final CollectionService<E> target;

    /**
     * Splits the specified collection into filtered sub-collections.
     */
    @SuppressWarnings("unchecked")
    public static <E> FilteredCollectionImpl<E>[] splitOf(
            CollectionService<E> target, int n, Predicate<? super E> filter) {
        CollectionService<E>[] tmp = target.trySplit(n);
        FilteredCollectionImpl<E>[] filtereds = new FilteredCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            filtereds[i] = new FilteredCollectionImpl<E>(tmp[i], filter);
        }
        return filtereds;
    }

    public FilteredCollectionImpl(CollectionService<E> target,
            Predicate<? super E> filter) {
        this.target = target;
        this.filter = filter;
    }

    @Override
    public boolean add(E element) {
        return filter.test(element) && target.add(element);
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
            @Override
            public void accept(E e) {
                if (filter.test(e)) {
                    consumer.accept(e);
                }
            }
        }, controller);

    }

    @Override
    public Iterator<E> iterator() {
        return new FilteredIteratorImpl<E>(target.iterator(), filter);
    }

    @Override
    public boolean removeIf(final Predicate<? super E> doRemove,
            IterationController controller) {
        return target.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                // Remove only if pass the first filter.
                return filter.test(param) && doRemove.test(param);
            }
        }, controller);
    }

    @Override
    protected FilteredCollectionImpl<E> service() {
        return this;
    }

    @Override
    public FilteredCollectionImpl<E>[] trySplit(int n) {
        return splitOf(target, n, filter);
    }
}
