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
 * A view over a portion of a table. 
 */
public final class SubTableImpl<E> implements TableService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private int fromIndex;
    private final TableService<E> that;
    private int toIndex;

    public SubTableImpl(TableService<E> that, int fromIndex, int toIndex) {
        this.that = that;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public boolean add(E element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void add(int index, E element) {
        // TODO Auto-generated method stub

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
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public EqualityComparator<? super E> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {}

    @Override
    public E get(int index) {
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
    public Iterator<E> iterator() {
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

    @Override
    public E remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E removeFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public E removeLast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E set(int index, E element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }

}
