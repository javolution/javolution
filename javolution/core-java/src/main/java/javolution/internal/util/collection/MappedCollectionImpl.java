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

import javolution.util.FastCollection;
import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.function.Comparators;
import javolution.util.function.Function;
import javolution.util.service.CollectionService;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E, R> extends FastCollection<R>
        implements CollectionService<R> {

    private static final long serialVersionUID = -2593582146510853694L;
    private final CollectionService<E> target;
    private final Function<? super E, ? extends R> function;

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
    public void atomicRead(Runnable action) {
        target.atomicRead(action);
    }

    @Override
    public void atomicWrite(Runnable action) {
        target.atomicWrite(action);        
    }
    
    @Override
    public void forEach(final CollectionConsumer<? super R> consumer) {
        if (consumer instanceof CollectionConsumer.Sequential) {
            target.forEach(new CollectionConsumer.Sequential<E>() {
                @Override
                public void accept(E e, Controller controller) {
                    consumer.accept(function.apply(e), controller);
                }
            });
        } else {
            target.forEach(new CollectionConsumer<E>() {
                @Override
                public void accept(E e, Controller controller) {
                    consumer.accept(function.apply(e), controller);
                }
            });
        }
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
    public FullComparator<? super R> comparator() {
        return Comparators.STANDARD;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<R>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null)
            return null;
        MappedCollectionImpl<E, R>[] mappeds = new MappedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            mappeds[i] = new MappedCollectionImpl<E, R>(tmp[i], function);
        }
        return mappeds;
    }

    @Override
    public MappedCollectionImpl<E, R> service() {
        return this;
    }
}
