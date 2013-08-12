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

import javolution.util.function.Consumer;
import javolution.util.function.Equality;

/**
 * The fundamental set of related functionalities required to implement 
 * fast collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface CollectionService<E> extends Collection<E>, Serializable,
        Cloneable {

    /** 
     * Returns a copy of this collection; updates of the copy should not 
     * impact the original.
     */
    CollectionService<E> clone() throws CloneNotSupportedException;

    /** 
     * Returns the comparator used for element equality or order if the 
     * collection is sorted.
     */
    Equality<? super E> comparator();

    /** 
     * Executes the specified (read-only) action on this collection.
     *       
     * @param action the read-only action.
     * @param view the view handle to be passed to the action.
     * @throws UnsupportedOperationException if the action tries to update this 
     *         collection.
     */
    void perform(Consumer<Collection<E>> action, CollectionService<E> view);

    /** 
     * Returns {@code n} sub-views over distinct parts of this collections.
     * If {@code n == 1} or if this collection cannot be split, 
     * this method returns an array holding a single element.
     * If {@code n > this.size()} this method may return empty views.
     *  
     * @param n the number of sub-views to return.
     * @return the sub-views.
     * @throws IllegalArgumentException if {@code n <= 1}
     */
    CollectionService<E>[] subViews(int n);

    /** 
     * Executes the specified update action on this collection.
     *       
     * @param action the action authorized to update this collection.
     * @param view the view handle to be passed to the action.
     */
    void update(Consumer<Collection<E>> action, CollectionService<E> view);

}