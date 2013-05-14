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
public final class UnmodifiableCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    private final CollectionService<E> that;

    public UnmodifiableCollectionImpl(CollectionService<E> that) {
        this.that = that;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<? super E> predicate) {
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
    public ComparatorService<? super E> getComparator() {
        return that.getComparator();
    }
    
    @Override
    public void setComparator(ComparatorService<? super E> cmp) {
        throw new UnsupportedOperationException("Unmodifiable");
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
