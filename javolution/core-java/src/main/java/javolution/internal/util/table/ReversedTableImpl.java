/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.util.Iterator;

import javolution.util.function.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * A reverse view over a table.
 */
public final class ReversedTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public ReversedTableImpl(TableService<E> that) {
        this.that = that;
    }
    
    // 
    // Impacted methods.
    //
    
    @Override
    public E get(int index) {
        return that.get(size() - 1 - index);
    }

    @Override
    public E set(int index, E element) {
        return that.set(size() - 1 - index, element);
    }

    @Override
    public void add(int index, E element) {
        that.add(size() - 1 - index, element);
    }

    @Override
    public E remove(int index) {
        return that.remove(size() - 1 - index);
    }

    @Override
    public int indexOf(E element) {
        return size() - 1 - that.lastIndexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return  size() - 1 - that.indexOf(element);
    }

    @Override
    public Iterator<E> iterator() {
        return iteratorDefault();
    }

    @Override
    public boolean add(E element) {
        return addDefault(element);
    }

    @Override
    public void sort() {
        sortDefault();
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return doWhileDefault(predicate);
    }
    
    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return removeAllDefault(predicate);
    }

    @Override
    public E getFirst() {
        return getFirstDefault();
    }

    @Override
    public E getLast() {
        return getLastDefault();
    }

    @Override
    public void addFirst(E element) {
        addFirstDefault(element);
    }

    @Override
    public void addLast(E element) {
        addLastDefault(element);
    }

    @Override
    public E removeFirst() {
        return removeFirstDefault();
    }

    @Override
    public E removeLast() {
        return removeLastDefault();
    }

    @Override
    public E pollFirst() {
        return pollFirstDefault();
    }

    @Override
    public E pollLast() {
        return pollLastDefault();
    }

    @Override
    public E peekFirst() {
        return peekFirstDefault();
    }

    @Override
    public E peekLast() {
        return peekLastDefault();
    }    

    @Override
    public TableService<E>[] trySplit(int n) {
        return trySplitDefault(n);
    }
    
    //
    // If no impact, forwards to inner table.
    // 
    
    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
    }
    
    @Override
    public void setComparator(ComparatorService<? super E> cmp) {
        that.setComparator(cmp);
    }
    
    @Override
    public int size() {
        return that.size();
    }

    @Override
    public void clear() {
        that.clear();
    }
    
    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return that.remove(element);
    }

    private static final long serialVersionUID = -3742752971523397049L;
}
