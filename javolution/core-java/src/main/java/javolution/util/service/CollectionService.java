/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Collection;

import javolution.lang.Predicate;
import javolution.util.FastCollection;

/**
 * The set of related collection functionalities which can be used/reused to 
 * implement generic collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see FastCollection
 */
public interface CollectionService<E> {

    //
    // Collection interface.
    // 
    
    /** See {@link Collection#size}*/
    int size();
    
    /** See {@link Collection#clear} */
    void clear();

    /** See {@link Collection#add} */
    boolean add(E element);

    /** See {@link Collection#contains} */
    boolean contains(E element);

    /** See {@link Collection#remove} */
    boolean remove(E element);
    
    //
    // Closure-based iterations.
    // 
    
    /** See {@link FastCollection#doWhile} */
    void doWhile(Predicate<E> predicate);

    /** See {@link FastCollection#removeAll} */
    boolean removeAll(Predicate<E> predicate);

}
