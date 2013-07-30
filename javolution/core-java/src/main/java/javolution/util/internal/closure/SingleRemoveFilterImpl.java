/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.closure;

import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService.IterationController;

/**
 * The filter to perform single element removal in a collection.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public final class SingleRemoveFilterImpl<E> implements Predicate<E>,
        IterationController {

    private final E element;
    private final EqualityComparator<? super E> equality;
    volatile boolean found;

    public SingleRemoveFilterImpl(E element,
            EqualityComparator<? super E> equality) {
        this.element = element;
        this.equality = equality;
    }

    public boolean isFound() {
        return found;
    }

    @Override
    public boolean doSequential() {
        return false;
    }

    @Override
    public boolean doReversed() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return found;
    }

    @Override
    public boolean test(E param) {
        if (equality.areEqual(element, param)) {
            synchronized (this) { // Ensures removal unicity.
                if (!found) {
                    found = true;
                    return true;
                }
            }
        }
        return false;
    }

}
