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

import javolution.lang.MathLib;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * The default {@link javolution.util.FastTable FastTable} implementation 
 * based on {@link FractalTableImpl fractal tables}. 
 * This implementation ensures that no more than 3/4 of the table capacity is
 * ever wasted.
 */
public final class FastTableImpl<E> implements TableService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final EqualityComparator<? super E> comparator;
    private FractalTableImpl fractal; // Null if empty (capacity 0)
    private int size;
    private int capacity; // Actual memory allocated is usually far less than
                          // capacity since inner fractal tables can be null.

    public FastTableImpl(EqualityComparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if (index == 0) {
            addFirst(element);
        } else if (index == size) {
            addLast(element);
        } else {
            if ((index < 0) || (index > size))
                indexError(index);
            checkUpsize();
            if (index >= (size >> 1)) {
                fractal.shiftRight(element, index, size - index);
            } else {
                fractal.shiftLeft(element, index - 1, index);
                fractal.offset--;
            }
            size++;
        }
    }

    @Override
    public void addFirst(E element) {
        checkUpsize();
        fractal.offset--;
        fractal.set(0, element);
        size++;
    }

    @Override
    public void addLast(E element) {
        checkUpsize();
        fractal.set(size++, element);
    }

    @Override
    public void atomic(Runnable action) {
        action.run();
    }

    @Override
    public void clear() {
        fractal = null;
        capacity = 0;
        size = 0;
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        if (!controller.doReversed()) {
            for (int i = 0; i < size; i++) {
                consumer.accept((E) fractal.get(i));
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = size; --i >= 0;) {
                consumer.accept((E) fractal.get(i));
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size))
            indexError(index);
        return (E) fractal.get(index);
    }

    @Override
    public E getFirst() {
        if (size == 0)
            emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (size == 0)
            emptyError();
        return get(size - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new TableIteratorImpl<E>(this, 0);
    }

    @Override
    public E peekFirst() {
        return (size == 0) ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return (size == 0) ? null : getLast();
    }

    @Override
    public E pollFirst() {
        return (size == 0) ? null : removeFirst();
    }

    @Override
    public E pollLast() {
        return (size == 0) ? null : removeLast();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        if (index == 0)
            return removeFirst();
        if (index == (size - 1))
            return removeLast();
        if ((index < 0) || (index >= size))
            indexError(index);
        E removed = (E) fractal.get(index);
        if (index >= (size >> 1)) {
            fractal.shiftLeft(null, size - 1, size - index - 1);
        } else {
            fractal.shiftRight(null, 0, index);
            fractal.offset++;
        }
        size--;
        checkDownsize();
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeFirst() {
        if (size == 0)
            emptyError();
        E first = (E) fractal.set(0, null);
        fractal.offset++;
        size--;
        checkDownsize();
        return first;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (int i = 0; i < size; i++) {
                if (filter.test((E) fractal.get(i))) {
                    remove(i--);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = size; --i >= 0;) {
                if (filter.test((E) fractal.get(i))) {
                    remove(i);
                    removed = true;
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeLast() {
        if (size == 0)
            emptyError();
        E last = (E) fractal.set(--size, null);
        checkDownsize();
        return last;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size))
            indexError(index);
        return (E) fractal.set(index, element);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TableService<E>[] trySplit(int n) {
        return splitOf(this, n);
    }

    @SuppressWarnings("unchecked")
    static <E> TableService<E>[] splitOf(TableService<E> table, int n) {
        int size = table.size();
        if (n <= 0)
            throw new IllegalArgumentException("Invalid argument n: " + n);
        int length = MathLib.min(n, size);
        if (length < 2)
            return new TableService[] { table }; // No split.
        TableService<E>[] subTables = new TableService[length];
        int div = size / length;
        int start = 0;
        for (int i = 0; i < length - 1; i++) {
            subTables[i] = new SubTableImpl<E>(table, start, start + div);
            start += div;
        }
        subTables[length - 1] = new SubTableImpl<E>(table, start, size - start);
        return subTables;
    }

    /***************************************************************************
     * Private utility methods.    
     */

    private void checkDownsize() {
        if ((capacity > FractalTableImpl.BASE_CAPACITY_MIN)
                && (size <= (capacity >> 2)))
            downsize();
    }

    private void checkUpsize() {
        if (size >= capacity)
            upsize();
    }

    private void downsize() {
        fractal = fractal.downsize(size);
        capacity = fractal.capacity();
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

    private void upsize() {
        fractal = (fractal == null) ? new FractalTableImpl() : fractal.upsize();
        capacity = fractal.capacity();
    }
}
