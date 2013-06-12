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
 * The set of related functionalities used to implement sorted tables collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface SortedTableService<E> extends TableService<E> {
    
    /** 
     * Returns the insertion index of the specified element in this table.
     * It is the smallest index of the element if the element was to be
     * added to this sorted table (in the range {@code [0 .. size()]}).
     */
    int insertionIndexOf(E element);

}