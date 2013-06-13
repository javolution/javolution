/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;


/**
 * <p> A consumer of the elements of a collection during closure-based 
 *     iterations. Most collection consumers can be executed in parallel; 
 *     if not the {@link Sequential} interface should be implemented.</p>
 *     
 * <p> Note: Future versions of this class may derive from {@link Consumer}.</p>    
 *     
 * @param <E> The type of elements in the collection consumed.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see javolution.util.FastCollection#forEach(CollectionConsumer)
 */
public interface CollectionConsumer<E> {

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
     * @param element the collection element being iterated over.
     * @param controller the iteration controller. 
     */
    void accept(E element, Controller controller);

}
