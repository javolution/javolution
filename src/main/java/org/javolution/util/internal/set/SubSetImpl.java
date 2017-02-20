/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javolution.annotations.Parallel;
import org.javolution.util.FastSet;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A sub-set view over a set.
 */
public final class SubSetImpl<E> extends FastSet<E> {

    /** Iterator bounded by the to limit over the sub-set. */
    private class LowerLimitIterator implements Iterator<E> {
        private final Iterator<E> itr;
        boolean currentIsNext;
        E current;

        public LowerLimitIterator(Iterator<E> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            if (currentIsNext)
                return true;
            if (!itr.hasNext())
                return false;
            current = itr.next();
            if (tooLow(current))
                return false;
            currentIsNext = true;
            return true;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            if (currentIsNext)
                throw new IllegalStateException();
            itr.remove();
        }
    }
    /** Iterator bounded by the from limit over the sub-set. */
    private class UpperLimitIterator implements Iterator<E> {
        private final Iterator<E> itr;
        boolean currentIsNext;
        E current;

        public UpperLimitIterator(Iterator<E> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            if (currentIsNext)
                return true;
            if (!itr.hasNext())
                return false;
            current = itr.next();
            if (tooHigh(current))
                return false;
            currentIsNext = true;
            return true;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            currentIsNext = false;
            return current;
        }

        @Override
        public void remove() {
            if (currentIsNext)
                throw new IllegalStateException();
            itr.remove();
        }

    }
    private static final long serialVersionUID = 0x700L; // Version.
    private final FastSet<E> inner;
    private final E from;
    private final Boolean fromInclusive;

    private final E to;

    private final Boolean toInclusive;

    /** Returns a sub-set, there is no bound when inclusive Boolean value is null. */
    public SubSetImpl(FastSet<E> inner, E from, Boolean fromInclusive, E to, Boolean toInclusive) {
        this.inner = inner;
        this.from = from;
        this.fromInclusive = fromInclusive;
        this.to = to;
        this.toInclusive = toInclusive;
    }

    @Override
    public boolean add(E element) {
        if (!inRange(element))
            return false;
        return inner.add(element);
    }

    @Parallel
    @Override
    public void clear() {
        removeIf(Predicate.TRUE);
    }

    @Override
    public SubSetImpl<E> clone() {
        return new SubSetImpl<E>(inner.clone(), from, fromInclusive, to, toInclusive);
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object obj) {
        if (!inRange((E) obj))
            return false;
        return inner.contains(obj);
    }

    @Override
    public Iterator<E> descendingIterator() {
        if (toInclusive == null)
            return new LowerLimitIterator(inner.descendingIterator());
        Iterator<E> itr = new LowerLimitIterator(inner.descendingIterator(to));
        if (!toInclusive && inner.contains(to))
            itr.next(); // Pass element.  
        return itr;
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        if (toInclusive == null)
            return new LowerLimitIterator(inner.descendingIterator(fromElement));
        return (inner.comparator().compare(to, fromElement) <= 0) ? descendingIterator()
                : new LowerLimitIterator(inner.descendingIterator(fromElement));
    }

    private boolean inRange(E e) {
        return !tooHigh(e) && !tooLow(e);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public Iterator<E> iterator() {
        if (fromInclusive == null)
            return new UpperLimitIterator(inner.iterator());
        Iterator<E> itr = new UpperLimitIterator(inner.iterator(from));
        if (!fromInclusive && inner.contains(from))
            itr.next(); // Pass element.  
        return itr;
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        if (fromInclusive == null)
            return new UpperLimitIterator(inner.iterator(fromElement));
        return (inner.comparator().compare(from, fromElement) >= 0) ? iterator()
                : new UpperLimitIterator(inner.iterator(fromElement));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        if (!inRange((E) obj))
            return false;
        return inner.remove(obj);
    }

    @Parallel
    @Override
    public int size() {
        int count = 0;
        for (Iterator<E> itr = iterator(); itr.hasNext(); itr.next())
            count++;
        return count;
    }

    private boolean tooHigh(E e) {
        if (toInclusive == null)
            return false;
        int i = inner.comparator().compare(to, e);
        return (i < 0) || ((i == 0) && !toInclusive);
    }

    private boolean tooLow(E e) {
        if (fromInclusive == null)
            return false;
        int i = inner.comparator().compare(from, e);
        return (i > 0) || ((i == 0) && !toInclusive);
    }

}
