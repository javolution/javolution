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

import javolution.util.function.FullComparator;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * A sorted view over a table.
 */
public final class SortedTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public SortedTableImpl(TableService<E> that, boolean allowDuplicate) {
        this.that = that;
    }

    // 
    // Impacted methods.
    //

    @Override
    public boolean add(E element) {
        int i = SortedTableImpl.indexIfSortedOf(element, this, 0, size());
        that.add(i, element);
        return true;
    }

    @Override
    public int indexOf(E element) {
        FullComparator<? super E> cmp = comparator();
        int i = SortedTableImpl.indexIfSortedOf(element, this, 0, size());
        if ((i < size()) && cmp.areEqual(element, get(i))) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(E element) {
        FullComparator<? super E> cmp = comparator();
        int i = SortedTableImpl.indexIfSortedOf(element, this, 0, size());
        if ((i < size()) && cmp.areEqual(element, get(i))) {
            while ((++i < size()) && cmp.areEqual(element, get(i))) {
            }
            return --i;
        }
        return -1;
    }
   
    @Override
    public boolean contains(E element) {
        return containsDefault(element);
    }

    @Override
    public boolean remove(E element) {
        return removeDefault(element);
    }

    @Override
    public TableService<E>[] trySplit(int n) {
        return trySplit(n);
    }
        
    @Override
    public void sort() {
        // Do nothing, already sorted.
    }
    
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException(
                "Sorted view do not allow elements to be inserted at specific positions.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException(
                "Sorted view do not allow elements to be inserted at specific positions.");
    }

    @Override
    public void addFirst(E element) {
        throw new UnsupportedOperationException(
                "Sorted view do not allow elements to be inserted at specific positions.");
    }

    @Override
    public void addLast(E element) {
        throw new UnsupportedOperationException(
                "Sorted view do not allow elements to be inserted at specific positions.");
    }
    
    //
    // If no impact, forwards to inner table.
    // 

    @Override
    public FullComparator<? super E> comparator() {
        return that.comparator();
    }
    
    @Override
    public void setComparator(FullComparator<? super E> cmp) {
        that.setComparator(cmp);
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
    public E remove(int index) {
        return that.remove(index);
    }

    @Override
    public void clear() {
        that.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return that.iterator();
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return that.doWhile(predicate);
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return that.removeIf(predicate);
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
    
    /**
     *  Utility to find the "should be" position of the specified element.
     */
    public static <E> int indexIfSortedOf(E element, TableService<E> table, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
       FullComparator<? super E> comparator = table.comparator();
        return (comparator.compare(element, table.get(start + half)) <= 0)
                ? indexIfSortedOf(element, table, start, half)
                : indexIfSortedOf(element, table, start + half + 1, length - half - 1);
    }
    
    private static final long serialVersionUID = 5579306002545263788L;
}
