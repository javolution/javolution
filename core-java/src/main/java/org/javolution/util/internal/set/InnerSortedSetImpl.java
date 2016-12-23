/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;

import org.javolution.util.FastSet;
import org.javolution.util.FractalTable;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayImpl;

/**
 * Sorted set inner implementation. 
 */
public final class InnerSortedSetImpl<E> extends FastSet<E> implements SparseArrayImpl.Inner<E,E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private FractalTable<E> elements;
    private final Order<? super E> order;

    public InnerSortedSetImpl(Order<? super E> order) {
        this.order = order;
        this.elements = new FractalTable<E>();
    }

    @Override
    public boolean add(E element) {
        int i = elements.insertionIndexOf(element, order);
        if (indexOf(element, i) >= 0) return false; // Already present.
        elements.add(i, element);
        return true;
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public InnerSortedSetImpl<E> clone() {
        InnerSortedSetImpl<E> copy = (InnerSortedSetImpl<E>)super.clone();
        copy.elements = elements.clone();
        return copy;
    }

    @Override
    public Order<? super E> order() {
        return order;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        return indexOf((E)obj, elements.insertionIndexOf((E)obj, order)) >= 0;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return elements.descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        int i = elements.insertionIndexOf(fromElement, order);
        return elements.subTable(0, i).descendingIterator();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        int i = elements.insertionIndexOf(fromElement, order);
        return elements.subTable(i, elements.size()).iterator();
    }

    @Override
    public boolean remove(Object obj) {
        @SuppressWarnings("unchecked")
        int i = indexOf((E)obj, elements.insertionIndexOf((E) obj, order));
        if (i < 0) return false;
        elements.remove(i);
        return true;
    }

    @Override
    public int size() {
        return elements.size();
    }
    

    /** Returns the index of the element equals to the one specified starting from the specified position
     *  or {@code -1} if not found.*/
    private int indexOf(E element, int fromIndex) {
        while (true) {
            if (fromIndex >= size()) return -1;
            E next = elements.get(fromIndex);
            if (order.compare(element, next) != 0) return -1;
            if (order.areEqual(element, next)) return fromIndex;
            fromIndex++;
        }
    }
}