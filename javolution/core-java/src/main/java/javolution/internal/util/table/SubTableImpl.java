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
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReadWriteLock;

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
    private final TableService<E> target;
    private int toIndex;

    public SubTableImpl(TableService<E> target, int fromIndex, int toIndex) {
        this.target = target;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public boolean add(E element) {
        add(size(), element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size())) indexError(index);
        target.add(index + fromIndex, element);
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
    public void clear() {
        for (int i=size(); --i >= 0;) {
            remove(i);
        }
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        int size = size();
        if (!controller.doReversed()) {
            for (int i = 0; i < size; i++) {
                consumer.accept(get(i));
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = size; --i >= 0;) {
                consumer.accept(get(i));
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size())) indexError(index);
        return target.get(index + fromIndex);
    }

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

    @Override
    public ReadWriteLock getLock() {
        return target.getLock();
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
        if ((index < 0) && (index >= size())) indexError(index);
        toIndex--;
        return target.remove(index + fromIndex);
    }

    @Override
    public E removeFirst() {
        if (size() == 0)
            emptyError();
        return remove(0);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        int size = size();
        boolean removed = false;
        if (!controller.doReversed()) {
            for (int i = 0; i < size; i++) {
                if (filter.test(get(i))) {
                    remove(i--);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = size; --i >= 0;) {
                if (filter.test(get(i))) {
                    remove(i);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return removed;
    }

    @Override
    public E removeLast() {
        if (size() == 0)
            emptyError();
        return remove(size() - 1);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size())) indexError(index);
        return target.set(index + fromIndex, element);
    }

    @Override
    public int size() {
        return toIndex - fromIndex;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return FastTableImpl.splitOf(this, n);
    }
    
    /** Throws NoSuchElementException */
    private void emptyError() {
        throw new NoSuchElementException("Empty Table");
    }

      /** Throws IndexOutOfBoundsException */
    private void indexError(int index) {
        throw new IndexOutOfBoundsException("index: " + index + ", size: "
                + size());
    }
}
