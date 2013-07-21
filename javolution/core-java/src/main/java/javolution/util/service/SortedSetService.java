/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

/**
 * The set of related functionalities used to implement sorted set collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface SortedSetService<E> extends SetService<E> {

    /**
     * Returns the first (lowest) element currently in this set.
     */
    E first();

    /**
     * Returns the last (highest) element currently in this set.
     */
    E last();

    /**
     * Returns a view over a portion of this set.
     * 
     * @param fromElement the low endpoint inclusive or {@code null} if none (head set).
     * @param toElement the high endpoint exclusive  or {@code null} if none (tail set).        
     */
    SortedSetService<E> subSet(E fromElement, E toElement);

}
