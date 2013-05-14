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
 * The set of related collection functionalities which can be used/reused to 
 * implement collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {

    //
    // Fundamental collections methods.
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
     * 
     * @param predicate the predicate being evaluated.
     * @return <code>true</code> if all the predicate evaluations have returned
     *         <code>true</code>; otherwise returns <code>false</code>  
     */
    boolean doWhile(Predicate<? super E> predicate);

    /** 
     * Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    boolean removeAll(Predicate<? super E> predicate);
    
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
     * Returns the comparator to be used for element ordering/comparisons.
     */
    ComparatorService<? super E> getComparator();

    /** 
     * Sets the comparator to be used for element ordering/comparisons.
     */
    void setComparator(ComparatorService<? super E> cmp);
 
}