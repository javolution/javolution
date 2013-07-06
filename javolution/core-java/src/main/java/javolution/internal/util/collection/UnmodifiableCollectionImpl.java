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

import javolution.internal.util.UnmodifiableIteratorImpl;
import javolution.util.FastCollection;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    /**
     * Splits the specified collection into unmodifiable sub-collections.
     */
    @SuppressWarnings("unchecked")
    public static <E> UnmodifiableCollectionImpl<E>[] splitOf(
            CollectionService<E> target, int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        UnmodifiableCollectionImpl<E>[] unmodifiables = new UnmodifiableCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            unmodifiables[i] = new UnmodifiableCollectionImpl<E>(tmp[i]);
        }
        return unmodifiables;
    }

    public UnmodifiableCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable Collection");
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        target.forEach(consumer, controller);
    }

    @Override
    public ReadWriteLock getLock() {
        return target.getLock();
    }

   @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIteratorImpl<E>(target.iterator());
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        throw new UnsupportedOperationException("Unmodifiable Collection");
    }

    @Override
    protected CollectionService<E> service() {
        return this;
    }

    @Override
    public UnmodifiableCollectionImpl<E>[] trySplit(int n) {
        return UnmodifiableCollectionImpl.splitOf(target, n);
    }

}
