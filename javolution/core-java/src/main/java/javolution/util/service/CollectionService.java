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
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;

/**
 * The fundamental set of related functionalities required to implement 
 * collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface CollectionService<E> {

    /**
     * The controller used during closure-based iterations.
     * 
     * @see CollectionService#forEach
     * @see CollectionService#removeIf
     */
    public interface IterationController {
        /** 
         * A sequential {@link CollectionService.IterationController 
         * iteration controller} over all the collection elements in the normal 
         * iterative order.
         */
        public static final IterationController SEQUENTIAL = new IterationController() {

            @Override
            public boolean doReversed() {
                return false;
            }

            @Override
            public boolean doSequential() {
                return true;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }
        };

        /** 
         * A standard {@link CollectionService.IterationController 
         * iteration controller} allowing parallel traversal over all the 
         * collection elements in the normal iterative order.
         */
        public static final IterationController STANDARD = new IterationController() {

            @Override
            public boolean doReversed() {
                return false;
            }

            @Override
            public boolean doSequential() {
                return false;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }
        };

        /** 
         * Indicates if the iterations should be performed in reversed order.
         */
        boolean doReversed();

        /** 
         * Indicates if the iterations should be performed sequentially.
         */
        boolean doSequential();

        /** 
         * Indicates if the iterations should be terminated; this method is 
         * always called after the {@link Consumer#accept(Object) consumer 
         * accept} method.
         */
        boolean isTerminated();

    }

    /** 
     * Adds the specified element to this collection.
     * 
     * @return <code>true</code> if an element was added as a result of 
     *        this call; <code>false</code> otherwise.
     */
    boolean add(E element);

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
    EqualityComparator<? super E> comparator();

    /** 
     * Traverses the elements of this collection.
     * 
     * @param consumer the consumer called upon the elements of this collection.
     * @param controller the iteration controller.
     */
    void forEach(Consumer<? super E> consumer, IterationController controller);

    /** 
     * Returns an iterator over this collection elements.
     */
    Iterator<E> iterator();

    /**
     * Removes from this collection the elements matching the specified
     * predicate.
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @param controller the iteration controller.
     * @return {@code true} if at least one element has been removed;
     *         {@code false} otherwise.
     */
    boolean removeIf(final Predicate<? super E> filter,
            IterationController controller);

    /** 
     * Try to splits this collection in {@code n} sub-collections;
     * if not possible may return an array of length less than 
     * {@code n} (for example of length one if no split). 
     * 
     * @param n the number of sub-collection to return.
     * @return the sub-collections elements.
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    CollectionService<E>[] trySplit(int n);
}