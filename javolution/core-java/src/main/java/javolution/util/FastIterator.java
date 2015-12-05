/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;
import java.util.Iterator;
import javolution.lang.Realtime;

/**
 * <p> A high-performance iterator (used by all {@link FastCollection}) 
 *     allowing parallel processing and reverse order iterations.</p>
 * 
 * @param <E> The type of element on which the iterator iterates.
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see FastCollection#parallel
 * @see FastCollection#reversed
 */
public interface FastIterator<E> extends Iterator<E>{

    /**
     * Removes the elements pointed by this iterator. 
     * This operations should allow concurrent removal 
     * for all iterators returned by {@link #split}
     */
	@Override
	@Realtime(limit = CONSTANT)
    void remove();
	
    /**
     * If this iterator can be partitioned, returns an iterator covering 
     * elements, that will, upon return from this method, not be covered by
     * this iterator. Implementations must ensure that concurrent removal 
     * (if supported) is thread-safe. This method should be called before
     * iterating.
     * 
     * @return an iterator covering some portion of the elements, or
     *         {@link null} if this iterator cannot split.}
     * @see #remove
     */
	@Realtime(limit = CONSTANT)
    FastIterator<E> trySplit();
    
    /**
     * Returns an iterator iterating through the same collection 
     * but in reverse order.
     *   
     * @return the reversed iterator (can be {@code this}). 
     */
	@Realtime(limit = CONSTANT)
    FastIterator<E> reversed();
	
}