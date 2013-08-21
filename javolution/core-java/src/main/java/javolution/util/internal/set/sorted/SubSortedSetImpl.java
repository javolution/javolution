/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set.sorted;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.function.Equality;
import javolution.util.service.CollectionService;
import javolution.util.service.SortedSetService;

/**
 * A view over a portion of a sorted set. 
 */
public class SubSortedSetImpl<E> extends SortedSetView<E> {

    /** Peeking ahead iterator. */
    private class IteratorImpl implements Iterator<E> {

        private boolean ahead;
        private final Equality<? super E> cmp = comparator();
        private E next;
        private final Iterator<E> targetIterator = target().iterator();

        @Override
        public boolean hasNext() {
            if (ahead) return true;
            while (targetIterator.hasNext()) {
                next = targetIterator.next();
                if ((from != null) && (cmp.compare(next, from) < 0)) continue;
                if ((to != null) && (cmp.compare(next, to) >= 0)) break;
                ahead = true;
                return true;
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
    private final E from; // Can be null.
    private final E to; // Can be null.

    public SubSortedSetImpl(SortedSetService<E> target, E from, E to) {
        super(target);
        if ((from != null) && (to != null)
                && (comparator().compare(from, to) > 0)) throw new IllegalArgumentException(
                "from: " + from + ", to: " + to); // As per SortedSet contract.
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean add(E e) {
        Equality<? super E> cmp = comparator();
        if ((from != null) && (cmp.compare(e, from) < 0)) throw new IllegalArgumentException(
                "Element: " + e + " outside of this sub-set bounds");
        if ((to != null) && (cmp.compare(e, to) >= 0)) throw new IllegalArgumentException(
                "Element: " + e + " outside of this sub-set bounds");
        return target().add(e);
    }

    @Override
    public Equality<? super E> comparator() {
        return ((CollectionService<E>)target()).comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        Equality<? super E> cmp = comparator();
        if ((from != null) && (cmp.compare((E) obj, from) < 0)) return false;
        if ((to != null) && (cmp.compare((E) obj, to) >= 0)) return false;
        return target().contains(obj);
    }

    @Override
    public E first() {
        if (from == null) return target().first();
        Iterator<E> it = iterator();
        if (!it.hasNext()) throw new NoSuchElementException();
        return it.next();
    }

    @Override
    public boolean isEmpty() {
        return iterator().hasNext();
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
    }

    @Override
    public E last() {
        if (to == null) return target().last();
        Iterator<E> it = iterator();
        if (!it.hasNext()) throw new NoSuchElementException();
        E last = it.next();
        while (it.hasNext()) {
            last = it.next();
        }
        return last;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        Equality<? super E> cmp = comparator();
        if ((from != null) && (cmp.compare((E) obj, from) < 0)) return false;
        if ((to != null) && (cmp.compare((E) obj, to) >= 0)) return false;
        return target().remove(obj);
    }

    @Override
    public int size() { // Unfortunately, no choice other than counting.
        int count = 0;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

}
