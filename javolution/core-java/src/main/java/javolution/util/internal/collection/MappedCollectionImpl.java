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
import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Function;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E, R> extends FastCollection<R>
        implements CollectionService<R> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Function<? super E, ? extends R> function;
    private final CollectionService<E> target;

    public MappedCollectionImpl(CollectionService<E> target,
            Function<? super E, ? extends R> function) {
        this.target = target;
        this.function = function;
    }

    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException(
                "New elements cannot be added to mapped views");
    }

    @Override
    public void atomic(Runnable update) {
        target.atomic(update);
    }
    
    @Override
    public EqualityComparator<? super R> comparator() {
        return Comparators.STANDARD;
    }

    @Override
    public void forEach(final Consumer<? super R> consumer,
            IterationController controller) {

        target.forEach(new Consumer<E>() {
            @Override
            public void accept(E e) {
                consumer.accept(function.apply(e));
            }
        }, controller);

    }

   @Override
    public Iterator<R> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        return new Iterator<R>() {

            @Override
            public boolean hasNext() {
                return targetIterator.hasNext();
            }

            @Override
            public R next() {
                return function.apply(targetIterator.next());
            }

            @Override
            public void remove() {
                targetIterator.remove();
            }

        };
    }

    @Override
    public boolean removeIf(final Predicate<? super R> filter,
            IterationController controller) {

        return target.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {

                return filter.test(function.apply(param));
            }
        }, controller);
    }

    @Override
    protected MappedCollectionImpl<E, R> service() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MappedCollectionImpl<E, R>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        MappedCollectionImpl<E, R>[] mappeds = new MappedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            mappeds[i] = new MappedCollectionImpl<E, R>(tmp[i], function);
        }
        return mappeds;
    }
}
