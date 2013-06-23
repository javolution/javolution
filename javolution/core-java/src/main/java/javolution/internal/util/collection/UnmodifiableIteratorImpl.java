/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

/**
 * An iterator which does not allow elements removals.
 */
public final class UnmodifiableIteratorImpl<E> implements Iterator<E> {

    private final Iterator<E> target;

    public UnmodifiableIteratorImpl(Iterator<E> target) {
        this.target = target;
    }

    @Override
    public boolean hasNext() {
        return target.hasNext();
    }

    @Override
    public E next() {
        return target.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

}
