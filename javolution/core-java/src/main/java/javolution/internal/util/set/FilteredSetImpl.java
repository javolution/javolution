/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.internal.util.FilteredIteratorImpl;
import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.SetService;

/**
 * A filtered view over a set.
 */
public class FilteredSetImpl<E> implements SetService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Predicate<? super E> filter;
    private final SetService<E> target;

    public FilteredSetImpl(SetService<E> target, Predicate<? super E> filter) {
        this.target = target;
        this.filter = filter;
    }

    @Override
    public boolean add(E element) {
        if (!filter.test(element)) return false;
        return target.add(element);
    }

    @Override
    public void clear() {
        target.removeIf(filter, IterationController.PARALLEL);
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public boolean contains(E e) {
        if (!filter.test(e)) return false;
        return target.contains(e);
    }

    @Override
    public void forEach(
            final Consumer<? super E> consumer, IterationController controller) {
        target.forEach(new Consumer<E>() {

            @Override
            public void accept(E param) {
                if (filter.test(param)) {
                     consumer.accept(param);  
                }
            }}, controller);
    }

    @Override
    public Iterator<E> iterator() {
        return new FilteredIteratorImpl<E>(target.iterator(), filter);
    }

    @Override
    public boolean remove(E e) {
        if (!filter.test(e)) return false;
        return target.remove(e);
    }

    @Override
    public boolean removeIf(
            final Predicate<? super E> aFilter, IterationController controller) {
        return target.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                return filter.test(param) && aFilter.test(param);
            }}, controller);
    }

    @Override
    public int size() {
        final AtomicInteger count = new AtomicInteger();
        this.forEach(new Consumer<E>() {

            @Override
            public void accept(E param) {
                count.incrementAndGet();
            }}, IterationController.PARALLEL);
        
        return count.get();
    }

    @Override
    public FilteredCollectionImpl<E>[] trySplit(int n) {
        return FilteredCollectionImpl.splitOf(target, n, filter);
    }

    @Override
    public void atomic(Runnable update) {
        target.atomic(update);
    }
}
