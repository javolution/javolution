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
 * A set view over a table.
 */
public final class SetTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public SetTableImpl(TableService<E> that) {
        this.that = that;
    }

    // 
    // Impacted methods.
    //

    @Override
    public boolean add(E element) {
        ComparatorService<E> cmp = getComparator();
        int i = indexIfSortedOf(element, cmp, 0, size());
        if ((i < size()) && cmp.areEqual(element, get(i))) return false; // Already there.
        that.add(i, element);
        return true;
    }

    @Override
    public int indexOf(E element) {
        ComparatorService<E> cmp = getComparator();
        int i = indexIfSortedOf(element, cmp, 0, size());
        if ((i < size()) && cmp.areEqual(element, get(i))) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(E element) {
        ComparatorService<E> cmp = getComparator();
        int i = indexIfSortedOf(element, cmp, 0, size());
        if ((i < size()) && cmp.areEqual(element, get(i))) {
            while ((++i < size()) && cmp.areEqual(element, get(i))) {
            }
            return --i;
        }
        return -1;
    }
   
    @Override
    public boolean contains(E element) {
        return super.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return super.remove(element);
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
    
    @Override
    public void sort() {
        that.sort();
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
        that.add(index, element);
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
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return that.removeAll(predicate);
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
    public void addFirst(E element) {
        that.addFirst(element);
    }

    @Override
    public void addLast(E element) {
        that.addLast(element);
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
    
    // Utility to find the "should be" position of the specified element.
    public int indexIfSortedOf(E element, ComparatorService<? super E> comparator, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        return (comparator.compare(element, get(start + half)) <= 0)
                ? indexIfSortedOf(element, comparator, start, half)
                : indexIfSortedOf(element, comparator, start + half + 1, length - half - 1);
    }
    
    private static final long serialVersionUID = 6158589683915294638L;
}
