/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.io.Serializable;
import java.util.Collection;

import javolution.util.function.Equality;
import javolution.util.function.Splittable;

/**
 * The fundamental set of related functionalities required to implement 
 * fast collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface CollectionService<E> extends Collection<E>,
        Splittable<CollectionService<E>>, Serializable, Cloneable {

    /** 
     * Returns a copy of this collection; updates of the copy should not 
     * impact the original.
     * @throws java.lang.CloneNotSupportedException if the clone operation is not supported by the implementation.
     * @return a clone of the CollectionService
     */
    CollectionService<E> clone() throws CloneNotSupportedException;

    /** 
     * Returns the comparator used for element equality or order if the 
     * collection is sorted.
     * @return the comparator used by the CollectionService
     */
    Equality<? super E> comparator();
    
}