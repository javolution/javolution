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

import javolution.util.FastSet;
import javolution.util.function.Equality;
import javolution.util.service.CollectionService;

/**
 * A view which does not iterate twice over the same elements.
 */
public class DistinctCollectionImpl<E> extends CollectionView<E> {

    /** Peeking ahead iterator. */
    private class IteratorImpl implements Iterator<E> {

        private boolean ahead; 
        private final FastSet<E> iterated = new FastSet<E>(comparator());
        private E next;
        private final Iterator<E> targetIterator = target().iterator();

        @Override
        public boolean hasNext() {
            if (ahead) return true;
            while (targetIterator.hasNext()) {
                next = targetIterator.next();
                if (!iterated.contains(next)) {
                    ahead = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public E next() {
            hasNext(); // Moves ahead.
            ahead = false;
            return next;
        }

        @Override
        public void remove() {
            targetIterator.remove();
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.

    public DistinctCollectionImpl(CollectionService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E element) {
        if (target().contains(element)) return false;
        return target().add(element);
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
    public boolean contains(Object o) {
        return target().contains(o);
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
    public boolean remove(Object o) { // Remove all instances.
        boolean changed = false;
        while (true) {
            if (!remove(o)) return changed;
            changed = true;
        }
    }

}
