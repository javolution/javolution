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

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Parallel;
import org.javolution.util.FastIterator;
import org.javolution.util.AbstractSet;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A sub-set view over a set.
 */
public final class SubSetImpl<E> extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;
    private final E fromElement; 
    private final E toElement;
    private final boolean fromInclusive;
    private final boolean toInclusive;

    /** Returns a sub-set, there is no bound when null element. */
    public SubSetImpl(AbstractSet<E> inner, @Nullable E fromElement, 
            boolean fromInclusive, @Nullable E toElement, boolean toInclusive) {
        this.inner = inner;
        this.fromElement = fromElement;
        this.fromInclusive = fromInclusive;
        this.toElement = toElement;
        this.toInclusive = toInclusive;
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
        if (!inRange(element)) 
            throw new UnsupportedOperationException("element out of sub-set range");
        return inner.add(element, allowDuplicate);
    }

    @Parallel
    @Override
    public void clear() {
        removeIf(Predicate.TRUE);
    }

    @Override
    public SubSetImpl<E> clone() {
        return new SubSetImpl<E>(inner.clone(), fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public FastIterator<E> descendingIterator(@Nullable E from) {
        if ((from == null) || tooHigh(from))  // Starts from subset higher bound.
            from = toInclusive ? toElement : lower(toElement);
        if (fromElement == null) return inner.descendingIterator(from); // No lower bound.
        E end = fromInclusive ? lower(toElement) : floor(toElement);
        return new IteratorImpl<E>(inner.descendingIterator(from), end);        
    }

    @Override
    public E getAny(E element) {
        if (!inRange(element)) 
            throw new UnsupportedOperationException("element out of sub-set range");
        return inner.getAny(element);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public FastIterator<E> iterator(@Nullable E from) {
        if ((from == null) || tooLow(from))  // Starts from subset lower bound.
            from = fromInclusive ? fromElement : higher(fromElement);
        if (toElement == null) return inner.iterator(from); // No upper bound.
        E end = toInclusive ? higher(toElement) : ceiling(toElement);
        return new IteratorImpl<E>(inner.iterator(from), end);        
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }
    
    @Override
    public E removeAny(E element) {
        if (!inRange(element)) 
            throw new UnsupportedOperationException("element out of sub-set range");
        return inner.removeAny(element);
    }
    
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return inner.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                if (!inRange(param)) return false;
                return filter.test(param);
            }});
    }       

    @Parallel
    @Override
    public int size() {
        int count = 0;
        for (Iterator<E> itr = iterator(); itr.hasNext(); itr.next()) count++;
        return count;
    }
 
    private E ceiling(E element) {
        FastIterator<E> itr = inner.iterator(element);
        if (!itr.hasNext()) return null;
        E next = itr.next();
        if (order().compare(next, element) >= 0) return next;
        if (!itr.hasNext()) return null;
        return itr.next();       
    }

    private E floor(E element) {
        FastIterator<E> itr = inner.descendingIterator(element);
        if (!itr.hasNext()) return null;
        E next = itr.next();
        if (order().compare(next, element) <= 0) return next;
        if (!itr.hasNext()) return null;
        return itr.next();       
    }

    private E higher(E element) {
        FastIterator<E> itr = inner.iterator(element);
        if (!itr.hasNext()) return null;
        E next = itr.next();
        if (order().compare(next, element) > 0) return next;
        if (!itr.hasNext()) return null;
        return itr.next();       
    }

    private boolean inRange(E e) {
        return !tooHigh(e) && !tooLow(e);
    }

    private E lower(E element) {
        FastIterator<E> itr = inner.descendingIterator(element);
        if (!itr.hasNext()) return null;
        E next = itr.next();
        if (order().compare(next, element) < 0) return next;
        if (!itr.hasNext()) return null;
        return itr.next();       
    }

    private boolean tooHigh(E e) {
        if (toElement == null) return false;
        int cmp = order().compare(toElement, e);
        return toInclusive ? cmp < 0 : cmp <= 0;
    }

    private boolean tooLow(E e) {
        if (fromElement == null) return false;
        int cmp = order().compare(fromElement, e);
        return fromInclusive ? cmp > 0 : cmp >= 0;
    }

    /** Iterate up to the specified end element (exclusive). */
    private static class IteratorImpl<E> implements FastIterator<E> {
        private final FastIterator<E> itr; 
        private final E end;  
        private E next; 
        private IteratorImpl(FastIterator<E> itr, E end) {
            this.itr = itr;
            this.end = end;
            next = itr.hasNext() ? itr.next() : end;
        }
        
        @Override
        public boolean hasNext() {
            return next != end;
        }

        @Override
        public boolean hasNext(final Predicate<? super E> matching) {
            while (true) {
                if (next == end) return false;
                if (matching.test(next)) return true;
                next = itr.hasNext() ? itr.next() : end;
            }
        }

        @Override
        public E next() {
            if (next == end) throw new NoSuchElementException();
            E current = next;
            next = itr.hasNext() ? itr.next() : end;
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();            
        }
 
    }

}
