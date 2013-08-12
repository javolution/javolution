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

import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A filtered view over a collection.
 */
public class FilteredCollectionImpl<E> extends CollectionView<E> {

    protected class IteratorImpl implements Iterator<E> {

        private boolean ahead; // Indicates if the iterator is ahead (on next element)
        private final Predicate<? super E> filter;
        private E next;
        private final Iterator<E> targetIterator;

        public IteratorImpl(Predicate<? super E> filter) {
            this.filter = filter;
            targetIterator = target.iterator();
        }

        @Override
        public boolean hasNext() {
            if (ahead)
                return true;
            while (targetIterator.hasNext()) {
                next = targetIterator.next();
                if (filter.test(next)) {
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

    protected final Predicate<? super E> filter;
    protected CollectionService<E> target;

    public FilteredCollectionImpl(CollectionService<E> target,
            Predicate<? super E> filter) {
        super(target);
        this.filter = filter;
    }

    @Override
    public boolean add(E element) {
        if (!filter.test(element)) return false;
        return target.add(element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        if (!filter.test((E)o)) return false;
        return target.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        if (!filter.test((E)o)) return false;
        return target.remove(o);
    }

}
