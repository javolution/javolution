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
 * A reversed view over a collection.
 */
public class ReversedCollectionImpl<E> extends CollectionView<E> {

    /** Reversing Iterator. */
    private class IteratorImpl implements Iterator<E> {

        @SuppressWarnings("unchecked")
        private final E[] elements = (E[]) new Object[size()];
        private int index = 0;
 
        public IteratorImpl() {
            Iterator<E> it = target().iterator();
            while (it.hasNext() && (index < elements.length)) {
                elements[index++] = it.next();
            }
        }

        @Override
        public boolean hasNext() {
            return index > 0;
        }

        @Override
        public E next() {
            return elements[--index];
        }

        @Override
        public void remove() {
            target().remove(elements[index]);
        }

    }

    private static final long serialVersionUID = 0x600L; // Version.

    public ReversedCollectionImpl(CollectionService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E e) {
        return target().add(e);
    }

    @Override
    public void clear() {
        target().clear();
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
    public boolean remove(Object obj) {
        return target().remove(obj);
    }

    @Override
    public int size() {
        return target().size();
    }
    
}
