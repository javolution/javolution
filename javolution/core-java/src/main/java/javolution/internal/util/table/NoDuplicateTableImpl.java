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
 * A view for which elements are not added if already present.
 */
public final class NoDuplicateTableImpl<E> extends AbstractTableImpl<E>  {

    private final TableService<E> that;

    public NoDuplicateTableImpl(TableService<E> that) {
        this.that = that;
    }

    // 
    // Impacted methods.
    //

    @Override
    public boolean add(E element) {
        if (indexOf(element) >= 0) return false; // Already present.
        return that.add(element);
    }

    @Override
    public void addFirst(E element) {
        if (indexOf(element) < 0) {
            that.addFirst(element);
        }
    }

    @Override
    public void addLast(E element) {
        if (indexOf(element) < 0) {
            that.addLast(element);
        }
    }
    
    @Override
    public E set(int index, E element) {
        if (indexOf(element) < 0) {
            return that.set(index, element);
        }
        return get(index);
    }

    @Override
    public void add(int index, E element) {
        if (indexOf(element) < 0) {
            that.add(index, element);
        }
    }

    @Override
    public TableService<E>[] trySplit(int n) {
        return trySplitDefault(n);
    }    
    
    //
    // If no impact, forwards to inner table.
    // 
  
    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return that.remove(element);
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
    public boolean removeAll(Predicate<? super E> predicate) {
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
    public ComparatorService<? super E> getComparator() {
        return that.getComparator();
    }
    
    @Override
    public void setComparator(ComparatorService<? super E> cmp) {
        that.setComparator(cmp);
    }

    private static final long serialVersionUID = 2896938407273267989L;
 
}
