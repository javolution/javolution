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
     * Returns the index of the specified element or -1 if not present.
     */    
    int indexOf(E element);
     
    /** 
     * Removes the specified element if present.
     */    
    boolean remove(E element);
     
    /** 
     * Returns what would be the index of the specified element if it were
     * to be added.
     */    
    int slotOf(E element);

}