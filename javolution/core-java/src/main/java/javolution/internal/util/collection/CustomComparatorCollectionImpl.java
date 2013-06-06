/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.io.Serializable;
import java.util.Iterator;

import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A view using a custom comparator for element equality or comparison.
 */
public class CustomComparatorCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    protected final CollectionService<E> that;
    protected final ComparatorService<? super E> comparator;

    public CustomComparatorCollectionImpl(CollectionService<E> that, ComparatorService<? super E> comparator) {
        this.that = that;
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
        return that.add(element);
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return that.doWhile(predicate);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return that.removeIf(filter);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return thatIterator.hasNext();
            }

            @Override
            public E next() {
                return thatIterator.next();
            }

            @Override
            public void remove() {
                thatIterator.remove();
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return comparator;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null) return null;
        CustomComparatorCollectionImpl<E>[] customs = new CustomComparatorCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
           customs[i] = new CustomComparatorCollectionImpl<E>(tmp[i], comparator); 
       }
        return customs;
    }
    
    private static final long serialVersionUID = -291533989999887947L;
}
