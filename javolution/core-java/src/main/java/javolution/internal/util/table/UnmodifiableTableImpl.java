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
 * An unmodifiable view over a table.
 */
public final class UnmodifiableTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public UnmodifiableTableImpl(TableService<E> that) {
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
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    //
    // Non-abstract methods should forward to the actual table (unless impacted).
    //
    @Override
    public void clear() {
        super.clear();
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
        return super.add(element);
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
        return super.removeFirst();
    }

    @Override
    public E removeLast() {
        return super.removeLast();
    }

    @Override
    public E pollFirst() {
        return super.pollFirst();
    }

    @Override
    public E pollLast() {
        return super.pollLast();
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
        return super.removeAll(predicate);
    }

    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return super.remove(element);
    }

    @Override
    public int indexOf(E element) {
        return that.indexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return that.lastIndexOf(element);
    }

    @Override
    public void sort() {
        super.sort();
    }

    @Override
    public ComparatorService<E> comparator() {
        return that.comparator();
    }    

    private static final long serialVersionUID = -2474743491162283998L;
}
