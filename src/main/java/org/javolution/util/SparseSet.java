/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Iterator;

import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayDescendingIteratorImpl;
import org.javolution.util.internal.SparseArrayImpl;
import org.javolution.util.internal.SparseArrayIteratorImpl;
import org.javolution.util.internal.set.InnerSortedSetImpl;
import org.javolution.util.internal.set.InnerSparseSetImpl;

/**
* <p> The {@link FastSet} implementation based upon high-performance {@link SparseArray}.</p> 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SparseSet<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Order<? super E> order; 
    SparseArray<Object> array; // Holds elements or inner sub-sets when collision.
    private int size;

    /**
     * Creates an empty set arbitrarily ordered (hash based).
     */
    public SparseSet() {
        this(Order.DEFAULT, SparseArray.empty(), 0);
    }

    /**
     * Creates an empty set ordered using the specified comparator.
     * 
     * @param order the ordering of the set.
     */
    public SparseSet(Order<? super E> order) {
        this(order, SparseArray.empty(), 0);
    }

    /**
     * Creates a sparse set from specified parameters.
     * 
     * @param order the order of the set.
     * @param array the sparse array implementation.
     * @param size the set size. 
     */
    protected SparseSet(Order<? super E> order, SparseArray<Object> array, int size) {
        this.order = order;
        this.array = array;
        this.size = size;
    }        

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(E element) {
        int index = order.indexOf(element);
        Object obj = array.get(index);
        if (obj == null) {
            array = array.set(index, element);
        } else if (isInner(obj)) { 
            boolean added = ((FastSet<E>) obj).add(element);
            if (!added) return false;
        } else if (order.areEqual(element, (E) obj)) {
            return false; // Already present.
        } else { // Collision.
            Order<? super E> subOrder = order.subOrder(element);
            FastSet<E> subSet = (subOrder != null) ? 
                    new InnerSparseSetImpl<E>(subOrder) : new InnerSortedSetImpl<E>(order);
            subSet.add((E) obj);
            subSet.add(element);
            array.set(index, subSet);         
        }
        size++;
        return true;
    }  
  
    @Override
    public void clear() {
        array = SparseArray.empty();
        size = 0;
    }

    @Override
    public SparseSet<E> clone() { 
        SparseSet<E> copy = (SparseSet<E>)super.clone();
        copy.array = array.clone(); // Also clone inner structures.
        return copy;
    }
    
    @Override
    public Order<? super E> order() {
        return order;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object element) {
        int index = order.indexOf((E)element);
        Object obj = array.get(index);
        if (obj == null) return false;
        if (isInner(obj)) return ((FastSet<E>) obj).contains(element);
        return order.areEqual((E) element, (E) obj);
   }

    @Override
    public Iterator<E> descendingIterator() {
        return new SparseArrayDescendingIteratorImpl<E,E>(array) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return new SparseArrayDescendingIteratorImpl<E,E>(array, fromElement, order, false) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new SparseArrayIteratorImpl<E,E>(array) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return new SparseArrayIteratorImpl<E,E>(array, fromElement, order, false) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object element) {
        int index = order.indexOf((E)element);
        Object obj = array.get(index);
        if (isInner(obj)) {
            FastSet<E> subSet = (FastSet<E>) obj;
            if (subSet.remove(element)) {
                size--;
                if (subSet.size() == 1) array.set(index, subSet.first()); 
                return true;                            
            } else return false;
        } 
        if (!order.areEqual((E)element, (E)obj)) return false;
        array = array.set(index, null);
        size--;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    /** Indicates if the specified object is an inner set. */ 
    static final boolean isInner(Object obj) {
        return obj instanceof SparseArrayImpl.Inner; // TODO: Check perfo obj.class == InnerSortedSetImpl || InnerSparseSetImpl
    }
   
}