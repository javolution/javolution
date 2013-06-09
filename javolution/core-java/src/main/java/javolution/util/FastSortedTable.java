/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Comparator;
import java.util.SortedSet;

import javolution.annotation.RealTime;
import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.NoDuplicateTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SortedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.service.ComparatorService;
import javolution.util.service.SetService;
import javolution.util.service.TableService;

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

     /**
      * Creates an empty table ordered on elements natural order.     
     */
    public FastSortedTable() {
        super(...);
    }

    /**
     * Creates an empty table ordered using the specified element comparator.
     */
   public FastSortedTable(ComparatorService<? super E> comparator) {
       super(...);
   }
   
    /**
     * Creates a sorted table backed up by the specified service implementation.
     */
    public FastSortedTable(TableService<E> service) {
        super(service);        
    }
 
}