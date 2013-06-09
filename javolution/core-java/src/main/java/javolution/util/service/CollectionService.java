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

import javolution.util.function.Consumer;

/**
 * The fundamental set of related functionalities required to implement 
 * collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {
    
    /** 
     * Executes the specified atomic action on this collection.
     * 
     * @param action the action to be executed atomically on this collection.
     * @param update indicates if the specified action may modify the collection.
     */
    void atomic(Runnable action, boolean update);

    /** 
     * Adds the specified element to this collection.
     * 
     * @return <code>true</code> if an element was added as a result of 
     *        this call; <code>false</code> otherwise.
     */
    boolean add(E element);

    /** 
     * Iterates over this collection elements either sequentially or 
     * in parallel. 
     * 
     * @param consumer the consumer of the collection elements.
     */
    void forEach(ConsumerService<? super E> consumer);

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
 
    /** 
     * Returns an iterator over the collection elements.
     */
    Iterator<E> iterator();
    
 }