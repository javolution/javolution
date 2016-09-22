/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Iterator;

/**
 * <p> An iterator which does not allow for collection modification (the {@link #remove} method throws 
 *     {@link UnsupportedOperationException}).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
public abstract class ReadOnlyIterator<E> implements Iterator<E> {

    /** 
     * Returns a read-only iterator from the specified iterator.
     * 
     * @param that the iterator to convert.
     * @return a read-only iterator wrapping the iterator specified or {@code this} if the specified 
     *         iterator is a read-only iterator.
     */
    public static <E> ReadOnlyIterator<E> of(final Iterator<E> that) {
        if (that instanceof ReadOnlyIterator) return (ReadOnlyIterator<E>)that;
        return new ReadOnlyIterator<E>() {

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public E next() {
                return that.next();
            }};
    }

    /** 
     * Guaranteed to throw an exception and leave the collection unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException("Read-Only Iterator");
    }

}