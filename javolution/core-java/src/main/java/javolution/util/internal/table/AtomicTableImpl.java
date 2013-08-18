/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import javolution.util.internal.collection.AtomicCollectionImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.TableService;

/**
 * An atomic view over a table.
 */
public class AtomicTableImpl<E> extends AtomicCollectionImpl<E> implements
        TableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicTableImpl(TableService<E> target) {
        super(target);
    }

    @Override
    public synchronized void add(int index, E element) {
        target().add(index, element);
        if (!updateInProgress()) immutable = cloneTarget();
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        boolean changed = target().addAll(index, c);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public synchronized void addFirst(E element) {
        target().addFirst(element);
        if (!updateInProgress()) immutable = cloneTarget();
    }

    @Override
    public synchronized void addLast(E element) {
        target().addLast(element);
        if (!updateInProgress()) immutable = cloneTarget();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReversedTableImpl<E>(this).iterator();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E get(int index) {
        return targetView().get(index);
    }

    @Override
    public E getFirst() {
        return targetView().getFirst();
    }

    @Override
    public E getLast() {
        return targetView().getLast();
    }

    @Override
    public int indexOf(Object element) {
        return targetView().indexOf(element);
    }

    @Override
    public ListIterator<E> iterator() {
        return listIterator(0);
    }

    @Override
    public int lastIndexOf(Object element) {
        return targetView().lastIndexOf(element);
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new TableIteratorImpl<E>(this, index); // Iterator view on this.
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public synchronized boolean offerFirst(E e) {
        boolean changed = target().offerFirst(e);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public synchronized boolean offerLast(E e) {
        boolean changed = target().offerLast(e);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public E peekFirst() {
        return targetView().peekFirst();
    }

    @Override
    public E peekLast() {
        return targetView().peekLast();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public synchronized E pollFirst() {
        E e = target().pollFirst();
        if ((e != null) && !updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public synchronized E pollLast() {
        E e = target().pollLast();
        if ((e != null) && !updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public synchronized E remove(int index) {
        E e = target().remove(index);
        if (!updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public synchronized E removeFirst() {
        E e = target().removeFirst();
        if (!updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public synchronized boolean removeFirstOccurrence(Object o) {
        boolean changed = target().removeFirstOccurrence(o);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public synchronized E removeLast() {
        E e = target().removeLast();
        if (!updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public synchronized boolean removeLastOccurrence(Object o) {
        boolean changed = target().removeLastOccurrence(o);
        if (changed && !updateInProgress()) immutable = cloneTarget();
        return changed;
    }

    @Override
    public synchronized E set(int index, E element) {
        E e = target().set(index, element);
        if (!updateInProgress()) immutable = cloneTarget();
        return e;
    }

    @Override
    public CollectionService<E>[] split(int n) {
        return SubTableImpl.splitOf(this, n); // Sub-views over this.
    }

    @Override
    public TableService<E> subList(int fromIndex, int toIndex) {
        return new SubTableImpl<E>(this, fromIndex, toIndex); // View on this.
    }

    @Override
    public TableService<E> threadSafe() {
        return this;
    }

    @Override
    protected TableService<E> targetView() {
        return (TableService<E>) super.targetView();
    }

    /** Returns the actual target */
    @Override
    protected TableService<E> target() {
        return (TableService<E>) super.target();
    }

}
