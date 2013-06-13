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

import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.function.CollectionConsumer.Controller;
import javolution.util.service.CollectionService;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> implements CollectionService<E> {

    private final CollectionService<E> target;

    public UnmodifiableCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
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
    public FullComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(final CollectionConsumer<? super E> consumer) {
        target.forEach(new NoRemoveConsumer<E>(consumer));
    }

    // This is a sequential consumer, hence collection.unmodifiable().parallel() 
    // should be preferred to collection.parallel().unmodifiable()
    private static class NoRemoveConsumer<E> implements CollectionConsumer.Sequential<E>,
            Controller {
        private final CollectionConsumer<? super E> actualConsumer;
        private Controller actualController; // State ok, sequential consumer.

        public NoRemoveConsumer(CollectionConsumer<? super E> consumer) {
            actualConsumer = consumer;
        }

        @Override
        public void accept(E e, Controller controller) {
            actualController = controller;
            actualConsumer.accept(e, this);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        @Override
        public void terminate() {
            actualController.terminate();
        };

    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return targetIterator.hasNext();
            }

            @Override
            public E next() {
                return targetIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Unmodifiable");
            }

        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null)
            return null;
        UnmodifiableCollectionImpl<E>[] unmodifiables = new UnmodifiableCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            unmodifiables[i] = new UnmodifiableCollectionImpl<E>(tmp[i]);
        }
        return unmodifiables;
    }

}
