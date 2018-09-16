/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.ListIterator;

import org.javolution.util.function.Predicate;

/**
 * A closure-enabled bidirectional list iterator.
 * 
 * ```java
 * public int indexOf(E searched) {
 *     FastListIterator<E> itr = listIterator();
 *     return itr.hasNext(next -> equality().areEqual(next, searched)) ? itr.nextIndex() : -1;
 * }
 * ```
*  Fast list iterators do not allow for modifications of the iterated collection and can be used on 
 * shared collections; the iteration is then performed on a snapshot (clone) of the collection taken when the 
 * iterator is instantiated.
 * 
 * As indicated in the {@link ListIterator} documentation, the iterator has no current element; 
 * its cursor position always lies between the element that would be returned by a call to {@link #previous()}
 * and the element that would be returned by a call to {@link #next()}. 
 * 
 * @param <E> the type of element being iterated
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
public interface FastListIterator<E> extends FastIterator<E>, ListIterator<E> {

    /** Throws {@code UnsupportedOperationException}.*/
    @Override
    @Deprecated
    void add(E arg0);

    /** Indicates if there is a previous element. */
    @Override
    boolean hasPrevious();

    /** 
     * Iterates partially or fully until the specified predicate is verified for the previous {@code non-null} element. 
     * 
     * @param matching the predicate to be verified.
     * @return {@code true} if there is a previous element matching the specified predicate; {@code false} otherwise.
     */
    boolean hasPrevious(Predicate<? super E> matching);

    /** 
     * Returns the next element.
     * @throws NoSuchElementException if the iteration has no next element 
     */
    @Override
    E next();

    /**  Returns the index of the next element or the list size if there is no next element. */
    @Override
    int nextIndex();

    /** 
     * Returns the previous element.
     * @throws NoSuchElementException if the iteration has previous element 
     */
    @Override
    E previous();

    /** Returns the index of the previous element or {@code -1} if there is no previous element. */
    @Override
    int previousIndex();

    /** Throws {@code UnsupportedOperationException}.*/
    @Override
    @Deprecated
    void set(E arg0);

}