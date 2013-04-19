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

import javolution.lang.Predicate;
import javolution.util.service.CollectionService;

/**
 * An unmodifiable view over a table.
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
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
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

    private static final long serialVersionUID = 6545160313862150259L;
}
