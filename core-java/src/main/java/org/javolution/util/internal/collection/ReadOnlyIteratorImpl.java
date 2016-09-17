/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.util.Iterator;

/**
 * An iterator which does not allow for element removal.
 */
public final class ReadOnlyIteratorImpl<E> implements Iterator<E> {

    /** Returns a read-only iterator (either the one specified or a wrapper over the one specified) */
    public static <E> ReadOnlyIteratorImpl<E> of(Iterator<E> that) {
        return (that instanceof ReadOnlyIteratorImpl) ? (ReadOnlyIteratorImpl<E>) that
                : new ReadOnlyIteratorImpl<E>(that);
    }

    private final Iterator<E> itr;

    private ReadOnlyIteratorImpl(Iterator<E> itr) {
        this.itr = itr;
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public E next() {
        return itr.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Read-Only Iterator.");
    }

}
