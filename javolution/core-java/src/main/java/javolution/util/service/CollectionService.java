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

import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;

/**
 * The fundamental set of related functionalities required to implement 
 * collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {
    
    /** 
     * Adds the specified element to this collection.
     * 
     * @return <code>true</code> if an element was added as a result of 
     *        this call; <code>false</code> otherwise.
     */
    boolean add(E element);

    /** 
     * Traverses the elements of this collection.
     * 
     * @param consumer the consumer called upopn the element of this collection.
     */
    void forEach(CollectionConsumer<? super E> consumer);

    /** 
     * Returns an iterator over this collection elements.
     */
    Iterator<E> iterator();

    /** 
     * Executes the specified action on this collection in an atomic manner as 
     * far as readers of this collection's are concerned (either readers 
     * see the full result of this action on this collection or nothing).
     *  
     * @param action the action to be executed atomically.
     */
    void atomic(Runnable action);

    /** 
     * Returns the full comparator used for element equality or order.
     */
    FullComparator<? super E> comparator();
 
    /** 
     * Splits this collection in <code>n</code> sub-collections.
     * 
     * @param n the number of sub-collection to return.
     * @return the sub-collection or <code>null</code> if the collection 
     *         cannot be split. 
     */
    CollectionService<E>[] trySplit(int n);
    
 }