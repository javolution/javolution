/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.annotation.RealTime;
import javolution.util.function.FullComparator;
import javolution.util.service.SortedTableService;

/**
 * <p> A high-performance sorted table with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p>This class is comparable to {@link FastSortedSet} in performance, 
 *    but it allows for duplicate and implements the {@link java.util.List}
 *    interface.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedTable<E> extends FastTable<E>  {

    private static final long serialVersionUID = 4650179945867476490L;

    /**
      * Creates an empty table maintained sorted using its elements natural order.     
     */
    public FastSortedTable() {
        super((SortedTableService<E>)null); // TBD
    }

    /**
     * Creates an empty table maintained sorted using the specified element comparator.
     */
   public FastSortedTable(FullComparator<? super E> comparator) {
       super((SortedTableService<E>)null); // TBD
   }

   /**
    * Creates a sorted table backed up by the specified service implementation.
    */
   public FastSortedTable(SortedTableService<E> service) {
       super(service);
   }
   
   @Override
   public SortedTableService<E> service() {
       return (SortedTableService<E>) super.service();
   }
 
   /** 
    * Returns the insertion index of the specified element in this table.
    * It is the smallest index of the element if the element was to be
    * added to this sorted table (in the range {@code [0 .. size()]}).
    */
   public final int insertionIndexOf(E element) {
       return service().insertionIndexOf(element);
   }
   
}