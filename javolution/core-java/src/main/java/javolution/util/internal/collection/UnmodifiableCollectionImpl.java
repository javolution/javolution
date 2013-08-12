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

import javolution.util.service.CollectionService;

/**
 * An unmodifiable view over a collection.
 */
public class UnmodifiableCollectionImpl<E> extends CollectionView<E> {

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
    public boolean contains(Object obj) {
        return target().contains(obj);
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> it = target().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read-Only Collection.");
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public int size() {
        return target().size();
    }
    
}
