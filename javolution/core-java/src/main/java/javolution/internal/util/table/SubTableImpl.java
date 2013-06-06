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
 * A view over a portion of a table. 
 */
public final class SubTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    private int fromIndex;

    private int toIndex;

    public SubTableImpl(TableService<E> that, int fromIndex, int toIndex) {
        this.that = that;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    // 
    // Impacted methods.
    //
    
    @Override
    public int size() {
        return toIndex - fromIndex;
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size())) indexError(index);
        return that.get(index + fromIndex);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size())) indexError(index);
        return that.set(index + fromIndex, element);
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size())) indexError(index);
        that.add(index + fromIndex, element);
    }

    @Override
    public E remove(int index) {
        if ((index < 0) && (index >= size())) indexError(index);
        toIndex--;
        return that.remove(index + fromIndex);
    }
      
    @Override
    public boolean add(E element) {
        return addDefault(element);
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
    public int indexOf(E element) {
        return indexOfDefault(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return lastIndexOfDefault(element);
    }

    @Override
    public void sort() {
        sortDefault();
    }
   
    @Override
    public void clear() {
        removeAllDefault(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                return true;
            }
            
        });
    }

    @Override
    public Iterator<E> iterator() {
        return iteratorDefault();
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
 
    private static final long serialVersionUID = 475452359149518413L;
}
