/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;
import java.util.Iterator;

import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.SetService;
import javolution.util.service.TableService;

/**
 * A set view over a table maintained sorted.
 */
public class SortedTableSetImpl<E> implements SetService<E>, Serializable {
    
    protected final TableService<E> table;

    public SortedTableSetImpl(TableService<E> table) {
        this.table = table;
    }

    @Override
    public boolean add(E element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean doWhile(Predicate<? super E> pursue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ComparatorService<? super E> comparator() {
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
    public boolean contains(E e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(E e) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  Utility to find the "should be" position of the specified element.
     */
    public static <E> int indexIfSortedOf(E element, TableService<E> table, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
       ComparatorService<? super E> comparator = table.comparator();
        return (comparator.compare(element, table.get(start + half)) <= 0)
                ? indexIfSortedOf(element, table, start, half)
                : indexIfSortedOf(element, table, start + half + 1, length - half - 1);
    }
}
