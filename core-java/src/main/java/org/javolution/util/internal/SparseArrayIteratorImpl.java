/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javolution.util.FastMap;
import org.javolution.util.SparseArray;
import org.javolution.util.function.Order;

/**
 * An iterator over sparse arrays. 
 */
public abstract class SparseArrayIteratorImpl<K, E> implements Iterator<E> {
 
    private SparseArray<Object> array;        
    private Iterator<E> subItr; // Set when iterating internal structures.
    private E next; 
    private int nextIndex;
    private E current;
    private int currentIndex;
 
    @SuppressWarnings("unchecked")
    public SparseArrayIteratorImpl(SparseArray<Object> array) {
        this.array = array;
        nextIndex = (array.get(0) != null) ? 0 : array.next(0);
        Object obj = array.get(nextIndex);
        if (obj instanceof SparseArrayImpl.Inner) {
            subItr = ((SparseArrayImpl.Inner<K,E>)obj).iterator();
        } else {
            next = (E) obj;
        }
    }
    
    @SuppressWarnings("unchecked")
    public SparseArrayIteratorImpl(SparseArray<Object> array, K from, Order<? super K> order, boolean isMap) {
        this.array = array;
        int nextIndex = order.indexOf(from);
        if (array.get(nextIndex) == null) nextIndex = array.next(nextIndex);
        while (true) {
            Object obj = array.get(nextIndex);
            if (obj instanceof SparseArrayImpl.Inner) { 
                 subItr = ((SparseArrayImpl.Inner<K,E>)obj).iterator(from);
            } else if (obj != null) { // An element or entry.
                if (order.compare(from, isMap ? ((FastMap.Entry<K,?>) obj).getKey() : (K) obj) <= 0) {
                    next = (E) obj;
                    break;
                }
            }
            nextIndex = array.next(nextIndex);
            if (nextIndex == 0) break; // None found. 
        }
    }
    
    @Override
    public boolean hasNext() {
        if ((subItr != null) && subItr.hasNext()) return true; 
        return (next != null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
        if ((subItr != null) && !subItr.hasNext())
            subItr = null;
        
        if (subItr != null) {
            current = subItr.next();
            return current;
        } else {
            if (next == null) throw new NoSuchElementException();
            current = next;
            currentIndex = nextIndex;
        }
        
        // Moves to the next entry.
        nextIndex = array.next(nextIndex);
        if (nextIndex == 0) { // Last element.
            next = null;
        } else {  
            Object obj = array.get(nextIndex);
            if (obj instanceof SparseArrayImpl.Inner) {
                subItr = ((SparseArrayImpl.Inner<K, E>)obj).iterator();
            } else {
                next = (E) obj;
            }
        } 
        return current;
    }

    @Override
    public void remove() {
        if (subItr != null) {
            subItr.remove();
            notifyRemoval(null);
            return;
        }
        if (current == null) throw new IllegalStateException();
        SparseArray<Object> newArray = array.set(currentIndex, null);
        if (array != newArray) notifyRemoval(newArray); else notifyRemoval(null);
        current = null;
    }
    
    /** Request to replace the array. **/
    public void notifyRemoval(SparseArray<Object> newArray) {}
    
}
