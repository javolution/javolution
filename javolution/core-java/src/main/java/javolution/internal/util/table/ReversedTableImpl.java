/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.io.Serializable;
import java.util.Iterator;

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.TableService;

/**
 * A reverse view over a table.
 */
public class ReversedTableImpl<E> implements TableService<E>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final TableService<E> target;

    public ReversedTableImpl(TableService<E> that) {
        this.target = that;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void add(int index, E element) {
        target.add(size() - index - 1, element);
    }

    @Override
    public void addFirst(E element) {
        add(0, element);    }

    @Override
    public void addLast(E element) {
        add(size(), element);
    }
    
    @Override
    public void atomic(Runnable update) {
        target.atomic(update);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
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
            }});
        
    }

    @Override
    public E get(int index) {
        return target.get(size() - index - 1);
    }

    @Override
    public E getFirst() {
        return get(0);
    }

    @Override
    public E getLast() {
        return get(size() - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new TableIteratorImpl<E>(this, 0);
    }

    @Override
    public E peekFirst() {
        return (size() == 0) ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return (size() == 0) ? null : getLast();
    }

    @Override
    public E pollFirst() {
        return (size() == 0) ? null : removeFirst();
    }

    @Override
    public E pollLast() {
        return (size() == 0) ? null : removeLast();
    }

    @Override
    public E remove(int index) {
        return target.remove(size() - index - 1) ;
    }

    @Override
    public E removeFirst() {
        return remove(0);
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
            }});
    }

    @Override
    public E removeLast() {
        return remove(size() - 1);
    }

    @Override
    public E set(int index, E element) {
        return target.set(size() - index - 1, element);
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n); // Forwards (view affects iteration controller only).
    }
}
