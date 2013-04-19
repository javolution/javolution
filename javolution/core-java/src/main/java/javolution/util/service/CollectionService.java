/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Iterator;

import javolution.lang.Predicate;


/**
 * The set of related collection functionalities which can be used/reused to 
 * implement generic collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {

    //
    // Collection interface.
    // 
    
    /** See {@link java.util.Collection#size}*/
    int size();
    
    /** See {@link java.util.Collection#clear} */
    void clear();

    /** See {@link java.util.Collection#add} */
    boolean add(E element);

    /** See {@link java.util.Collection#contains} */
    boolean contains(E element);

    /** See {@link java.util.Collection#remove} */
    boolean remove(E element);
    
    /** See {@link java.util.Collection#iterator()} */
    Iterator<E> iterator();
    
    //
    // Closure-based iterations.
    // 
    
    /** 
     * Iterates this collection elements until the specified predicate 
     * returns <code>false</code>. 
     */
    void doWhile(Predicate<E> predicate);

    /** 
     *  Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    boolean removeAll(Predicate<E> predicate);

}
