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
import java.util.NoSuchElementException;

import javolution.util.function.Equality;
import javolution.util.internal.collection.CollectionView;
import javolution.util.service.CollectionService;
import javolution.util.service.TableService;

/**
 * Table view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 */
public abstract class TableView<E> extends CollectionView<E> implements TableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public TableView(TableService<E> target) {
        super(target);
    }

    @Override
    public abstract void add(int index, E element);

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return subList(index, index).addAll(c);
    }

    @Override
    public void addFirst(E element) {
        add(0, element);
    }

    @Override
    public void addLast(E element) {
        add(size(), element);
    }

    @Override
    public abstract void clear();

    @Override
    public final boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReversedTableImpl<E>(this).iterator();
    }

    @Override
    public final E element() {
        return getFirst();
    }

    @Override
    public abstract E get(int index);

    @Override
    public E getFirst() {
        if (size() == 0) emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (size() == 0) emptyError();
        return get(size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object o) {
        Equality<Object> cmp = (Equality<Object>) this.comparator();
        for (int i = 0, n = size(); i < n; i++) {
            if (cmp.areEqual(o, get(i))) return i;
        }
        return -1;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int lastIndexOf(Object o) {
        Equality<Object> cmp = (Equality<Object>) this.comparator();
        for (int i = size() - 1; i >= 0; i--) {
            if (cmp.areEqual(o, get(i))) return i;
        }
        return -1;
    }

    @Override
    public final ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new TableIteratorImpl<E>(this, index);
    }

    @Override
    public final boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public final boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public final boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public final E peek() {
        return peekFirst();
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
    public final E poll() {
        return pollFirst();
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
    public final E pop() {
        return removeFirst();
    }

    @Override
    public final void push(E e) {
        addFirst(e);
    }

    @Override
    public final E remove() {
        return removeFirst();
    }

    @Override
    public abstract E remove(int index);

    @Override
    public final boolean remove(Object o) {
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    public E removeFirst() {
        if (size() == 0) emptyError();
        return remove(0);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    public E removeLast() {
        if (size() == 0) emptyError();
        return remove(size() - 1);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    public abstract E set(int index, E element);

    @Override
    public abstract int size();

    @Override
    public CollectionService<E>[] split(int n) {
        return SubTableImpl.splitOf(this, n); // Sub-views over this.
    }

    @Override
    public TableService<E> subList(int fromIndex, int toIndex) {
        return new SubTableImpl<E>(this, fromIndex, toIndex);
    }

    @Override
    public TableService<E> threadSafe() {
        return new SharedTableImpl<E>(this);
    }

    /** Throws NoSuchElementException */
    protected void emptyError() {
        throw new NoSuchElementException("Empty Table");
    }

    /** Throws IndexOutOfBoundsException */
    protected void indexError(int index) {
        throw new IndexOutOfBoundsException("index: " + index + ", size: "
                + size());
    }

    /** Returns the actual target */
    @Override
    protected TableService<E> target() {
        return (TableService<E>) super.target();
    }
}
