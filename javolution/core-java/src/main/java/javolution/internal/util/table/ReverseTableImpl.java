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
public final class ReverseTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public ReverseTableImpl(TableService<E> that) {
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
        return super.iterator();
    }

    @Override
    public boolean add(E element) {
        return super.add(element);
    }

    @Override
    public void sort() {
        super.sort();
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

    private static final long serialVersionUID = -3471317207858863000L;
}
