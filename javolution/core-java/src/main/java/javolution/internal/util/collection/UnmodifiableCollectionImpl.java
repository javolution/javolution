/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * An unmodifiable view over a collection.
 */
public final class UnmodifiableCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = -5922312619483198062L;
    private final CollectionService<E> target;

    public UnmodifiableCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return target.doWhile(predicate);
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return targetIterator.hasNext();
            }

            @Override
            public E next() {
                return targetIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Unmodifiable");
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return target.comparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null) return null;
        UnmodifiableCollectionImpl<E>[] unmodifiables = new UnmodifiableCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
           unmodifiables[i] = new UnmodifiableCollectionImpl<E>(tmp[i]); 
       }
        return unmodifiables;
    }

    @Override
    public UnmodifiableCollectionImpl<E> service() {
        return this;
    }
}
