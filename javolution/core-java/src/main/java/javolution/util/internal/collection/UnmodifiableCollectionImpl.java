/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.service.CollectionService;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> extends CollectionView<E> {

    /** Read-Only Iterator. */
    private class IteratorImpl implements Iterator<E> {
        private final Iterator<E> targetIterator = target().iterator();

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
            throw new UnsupportedOperationException("Read-Only Collection.");
        }
    }
    
    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableCollectionImpl(CollectionService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public Equality<? super E> comparator() {
        return target().comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return target().contains(obj);
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
   }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public int size() {
        return target().size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] split(int n, boolean updateable) {
        CollectionService<E>[] subTargets = target().split(n, updateable);
        CollectionService<E>[] result = new CollectionService[subTargets.length];
        for (int i = 0; i < subTargets.length; i++) {
            result[i] = new UnmodifiableCollectionImpl<E>(subTargets[i]);
        }
        return result;
    }
    
}
