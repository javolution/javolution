/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Iterator;

import javolution.util.function.Predicate;

/**
 * An iterator for filtered collections.
 */
public class FilteredIteratorImpl<E> implements Iterator<E> {

    private final Iterator<E> target;
    private final Predicate<? super E> filter;
    private E next = null; // The next element for which the predicate is verified. 
    private boolean peekNext; // If the next element has been read in advance.

    public FilteredIteratorImpl(Iterator<E> target, Predicate<? super E> filter) {
        this.target = target;
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        if (peekNext)
            return true;
        while (true) {
            if (!target.hasNext())
                return false;
            next = target.next();
            if (filter.test(next)) {
                peekNext = true;
                return true;
            }
        }
    }

    @Override
    public E next() {
        if (peekNext) { // Usually true (hasNext has been called before). 
            peekNext = false;
            return next;
        }
        while (true) {
            next = target.next();
            if (filter.test(next))
                return next;
        }
    }

    @Override
    public void remove() {
        target.remove();
    }

}
