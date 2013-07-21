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

import javolution.internal.util.UnmodifiableIteratorImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * An unmodifiable view over a table.
 */
public class UnmodifiableTableImpl<E> implements TableService<E>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final TableService<E> target;

    public UnmodifiableTableImpl(TableService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void addFirst(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void addLast(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void atomic(Runnable update) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
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
    public E get(int index) {
        return target.get(index);
    }

    @Override
    public E getFirst() {
        return target.getFirst();
    }

    @Override
    public E getLast() {
        return target.getLast();
    }

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIteratorImpl<E>(target.iterator());
    }

    @Override
    public E peekFirst() {
        return target.peekFirst();
    }

    @Override
    public E peekLast() {
        return target.peekLast();
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public UnmodifiableCollectionImpl<E>[] trySplit(int n) {
        return UnmodifiableCollectionImpl.splitOf(target, n);
    }
    
    protected TableService<E> target() {
        return target;
    }
}
