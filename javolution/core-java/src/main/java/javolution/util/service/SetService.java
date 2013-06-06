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
 * The set of related functionalities used to implement set collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface SetService<E> extends CollectionService<E> {

    /**
     * Returns the number of elements in this set.
     */
    int size();

    /**
     * Removes all of the elements from this set.
     */
    void clear();
    
    /**
     * Indicates if this set contains the specified element.
     */
    boolean contains(E e);

    /**
     * Removes the specified element from this set. More formally,
     * removes an element {@code elem} such that
     * {@code getComparator().areEquals(elem, e))}.
     */  
    boolean remove(E e);
                      
}
