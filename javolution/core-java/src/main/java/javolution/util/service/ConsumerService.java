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
 * <p> A consumer of the elements of a collection during closure-based 
 *     iterations.</p>
 *     
 * <p><i> Note: Future versions of this functional interface will have 
 *     a default {@code isParallel} method (once Java 8 is mainstream).</i></p>  
 *          
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @param <E> The type of the collection element consumed.
 * @see CollectionService#forEach(ConsumerService)
 */
public interface ConsumerService<E> {

    /**
     * Identifies consumers which cannot be executed in parallel 
     * (order dependent for example).
     */
    public interface Sequential<E> extends ConsumerService<E> {}

    /**
     * The closure-based iteration controller.
     */
    public interface Controller {
        /**
         * This method should be called if the element being iterated over
         * needs to be removed.
         */
        void remove();

        /**
         * This method should be called if the iteration should be terminated.
         */
        void terminate();
    }

    /**
     * Accepts the specified collection element.
     * 
     * @param e the collection element being iterated over.
     * @param controller the iteration controller. 
     */
    void accept(E e, Controller controller);


}
