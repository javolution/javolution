/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import javolution.lang.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * A sorted view over a table.
 */
public final class SortedTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public SortedTableImpl(TableService<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(index);
    }

    @Override
    public E set(int index, E element) {
        return that.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException(
                "Sorted tables don't allow arbitrary insertions (add(E) should be used)");
    }

    @Override
    public E remove(int index) {
        return that.remove(index);
    }

    //
    // Non-abstract methods should forward to the actual table (unless impacted).
    //
    @Override
    public void clear() {
        that.clear();
    }

    @Override
    public E getFirst() {
        return that.getFirst();
    }

    @Override
    public E getLast() {
        return that.getLast();
    }

    @Override
    public boolean add(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        that.add(i, element);
        return true;
    }

    @Override
    public void addFirst(E element) {
        super.addFirst(element);
    }

    @Override
    public void addLast(E element) {
        super.addLast(element);
    }

    @Override
    public E removeFirst() {
        return that.removeFirst();
    }

    @Override
    public E removeLast() {
        return that.removeLast();
    }

    @Override
    public E pollFirst() {
        return that.pollFirst();
    }

    @Override
    public E pollLast() {
        return that.pollLast();
    }

    @Override
    public E peekFirst() {
        return that.peekFirst();
    }

    @Override
    public E peekLast() {
        return that.peekLast();
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return that.removeAll(predicate);
    }

    @Override
    public boolean contains(E element) {
        return super.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return super.remove(element);
    }

    @Override
    public int indexOf(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        if ((i < size()) && comparator().areEqual(element, get(i))) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(E element) {
        int i = indexIfSortedOf(element, comparator(), 0, size());
        if ((i < size()) && comparator().areEqual(element, get(i))) {
            while ((++i < size()) && comparator().areEqual(element, get(i))) {
            }
            return --i;
        }
        return -1;
    }

    @Override
    public void sort() {
        // Do nothing, already sorted.
    }

    // Utility to find the "should be" position of the specified element.
    private int indexIfSortedOf(E element, ComparatorService<? super E> comparator, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        return (comparator.compare(element, get(start + half)) <= 0)
                ? indexIfSortedOf(element, comparator, start, half)
                : indexIfSortedOf(element, comparator, start + half + 1, length - half - 1);
    }

    private static final long serialVersionUID = 3307001131594728496L;
}
