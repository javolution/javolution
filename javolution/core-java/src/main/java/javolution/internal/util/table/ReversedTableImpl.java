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
public final class ReversedTableImpl<E> implements TableService<E>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final TableService<E> target;

    public ReversedTableImpl(TableService<E> that) {
        this.target = that;
    }

    @Override
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean add(E element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public void add(int index, E element) {
        // TODO Auto-generated method stub

    }

    @Override
    public E get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E set(int index, E element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E getFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E getLast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addFirst(E element) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addLast(E element) {
        // TODO Auto-generated method stub

    }

    @Override
    public E removeFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E removeLast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E peekFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E peekLast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E pollFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E pollLast() {
        // TODO Auto-generated method stub
        return null;
    }
}
