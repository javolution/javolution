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
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A view using a custom comparator for element equality or comparison.
 */
public class ComparatorCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final EqualityComparator<? super E> comparator;
    private CollectionService<E> target;

    public ComparatorCollectionImpl(CollectionService<E> target,
            EqualityComparator<? super E> comparator) {
        this.target = target;
        this.comparator = comparator;
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
        return comparator;
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        target.forEach(consumer, controller);
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        return target.removeIf(filter, controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ComparatorCollectionImpl<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        ComparatorCollectionImpl<E>[] result = new ComparatorCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new ComparatorCollectionImpl<E>(tmp[i], comparator);
        }
        return result;
    }

    @Override
    protected CollectionService<E> service() {
        return this;
    }

}
