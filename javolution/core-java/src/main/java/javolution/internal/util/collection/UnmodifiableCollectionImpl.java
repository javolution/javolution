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
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    protected final CollectionService<E> that;

    public UnmodifiableCollectionImpl(CollectionService<E> that) {
        this.that = that;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return that.doWhile(predicate);
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
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
                throw new UnsupportedOperationException("Unmodifiable");
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null) return null;
        UnmodifiableCollectionImpl<E>[] unmodifiables = new UnmodifiableCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
           unmodifiables[i] = new UnmodifiableCollectionImpl<E>(tmp[i]); 
       }
        return unmodifiables;
    }
    
    private static final long serialVersionUID = 278852354797494219L;
}
