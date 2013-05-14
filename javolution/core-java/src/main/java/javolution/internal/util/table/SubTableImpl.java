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
        return super.add(element);
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
        return super.indexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return super.lastIndexOf(element);
    }

    @Override
    public void sort() {
        super.sort();
    }
   
    @Override
    public void clear() {
        super.removeAll(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                return true;
            }
            
        });
    }

    @Override
    public Iterator<E> iterator() {
        return super.iterator();
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        super.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return super.removeAll(predicate);
    }

    @Override
    public E getFirst() {
        return super.getFirst();
    }

    @Override
    public E getLast() {
        return super.getLast();
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
        return super.peekFirst();
    }

    @Override
    public E peekLast() {
        return super.peekLast();
    }
  
    //
    // If no impact, forwards to inner table.
    // 

    @Override
    public ComparatorService<E> getComparator() {
        return that.getComparator();
    }
    
    @Override
    public void setComparator(ComparatorService<E> cmp) {
        that.setComparator(cmp);
    }  
 
    private static final long serialVersionUID = -1596693663009102664L;
}
