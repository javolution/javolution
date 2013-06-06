/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.SortedMap;

import javolution.annotation.RealTime;
import javolution.util.service.ComparatorService;

/**
* <p> A high-performance sorted map with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p> A {@link FastMap FastMap} providing a total ordering based on keys
 *     natural order or custom {@link #FastSortedMap(ComparatorService) comparators}.</p>
 *        
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSortedMap<K,V> extends FastMap<K,V> implements SortedMap<K,V> {

    /**
     * Creates an empty map ordered on keys natural order.
     */
    public FastSortedMap() {
        this(Comparators.STANDARD);
    }
    
    /**
      * Creates an empty map ordered using the specified key comparator.
    */
   public FastSortedMap(ComparatorService<? super K> keyComparator) {
       super(keyComparator); // TODO
   }
   
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public K firstKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public K lastKey() {
        // TODO Auto-generated method stub
        return null;
    }   
   
}
