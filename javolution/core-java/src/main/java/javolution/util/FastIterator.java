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
 * <p> The iterator used by {@link FastCollection} allowing 
 *     parallel processing and iterations in reverse order.</p>
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
     * Splits this iterators into sub-iterators (iterating 
     * over a partial view of the {@link FastCollection}).
     * If the iterator cannot fully split; the array may hold 
     * {@code null} values. Implementations must ensure that
     * concurrent removal using the sub-iterators is supported
     * when this iterator itself supports elements removal
     * (e.g. using single read-write lock).
     * 
     * @param subIterators the array to hold the subIterators. 
     * @return the specified subIterators array.
     * @throws IllegalArgumentException if {@code  
     *        (subIterators.length == 0)}
     * @see #remove
     */
	@Realtime(limit = CONSTANT)
    FastIterator<E>[] split(FastIterator<E>[] subIterators);
    
    /**
     * Returns an iterator iterating through the same collection 
     * as the one iterated by this iterator but in reverse order.
     *   
     * @return the reversed iterator. 
     */
	@Realtime(limit = CONSTANT)
    FastIterator<E> reversed();
	
}