/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javolution.util.function.Predicate;

/**
 * A closure-enabled collection iterator.
 * 
 * ```java
 * public boolean contains(E searched) {
 *     return iterator().hasNext(next -> equality().areEqual(next, searched));
 * }
 * ```
 * 
 * Fast iterators do not allow for modifications of the iterated collection and can be used on 
 * shared collections; the iteration is then performed on a snapshot (clone) of the collection/view
 * taken when the iterator is instantiated.
 * 
 * @param <E> the type of element being iterated
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
public interface FastIterator<E> extends Iterator<E> {

    /** Indicates if there is a next element. */
    @Override
    boolean hasNext();

    /** 
     * Iterates partially or fully until the specified predicate is verified for the next {@code non-null} element. 
     * 
     * @param matching the predicate to be verified.
     * @return {@code true} if there is a next element matching the specified predicate; {@code false} otherwise.
     */
    boolean hasNext(Predicate<? super E> matching);

    /** 
     * Returns the next element.
     * @throws NoSuchElementException if the iteration has no next element 
     */
    @Override
    E next();
    
    /** Throws {@code UnsupportedOperationException}.*/
    @Override
    @Deprecated
    void remove();
    
}