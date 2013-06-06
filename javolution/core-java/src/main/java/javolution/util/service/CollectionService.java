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

import javolution.util.function.Predicate;


/**
 * The fundamental set of related functionalities required to implement 
 * collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {

    //
    // Basic collection methods.
    // 
 
    /** 
     * Adds the specified element to this collection.
     * 
     * @return <code>true</code> if an element was added as a result of 
     *        this call; <code>false</code> otherwise.
     */
    boolean add(E element);

    /** 
     * Returns an iterator over the collection elements.
     */
    Iterator<E> iterator();
    
    //
    // Closure-based iterations.
    // 
   
    /** 
     * Iterates over this collection elements until the specified predicate 
     * returns <code>false</code>. 
     * 
     * @param pursue a predicate returning {@code false} to stop iterating.
     * @param predicate the predicate being evaluated.
     * @return {@code false} if any predicate evaluation returned {@code false}.
     */
    boolean doWhile(Predicate<? super E> pursue);

    /** 
     * Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if any elements were removed
     */
    boolean removeIf(Predicate<? super E> filter);
    
    //
    // Misc.
    //       

    /** 
     * Splits this collection in <code>n</code> sub-collections.
     * 
     * @param n the number of sub-collection to return.
     * @return the sub-collection or <code>null</code> if the collection 
     *         cannot be split. 
     */
    CollectionService<E>[] trySplit(int n);

    /** 
     * Returns the comparator used for element equality or comparisons.
     */
    ComparatorService<? super E> comparator();
 
 }