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

    protected class IteratorImpl implements Iterator<E> {

        private boolean ahead; // Indicates if the iterator is ahead (on next element)
        private final FastSet<E> iterated;
        private E next;
        private final Iterator<E> targetIterator;

        public IteratorImpl(Equality<? super E> cmp) {
            iterated = new FastSet<E>(cmp);
            targetIterator = target().iterator();
        }

        @Override
        public boolean hasNext() {
            if (ahead) return true;
            while (targetIterator.hasNext()) {
                next = targetIterator.next();
                if (!iterated.contains(next)) {
                    ahead = true;
                    break;
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
    public Iterator<E> iterator() {
        return new IteratorImpl(this.comparator());
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
