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
import java.util.SortedMap;
import java.util.Map.Entry;

import javolution.annotation.RealTime;
import javolution.util.service.ComparatorService;
import javolution.util.service.MapService;

/**
* <p> A high-performance sorted map with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p> This map provides a total ordering based on the keys natural order or 
 *     using custom {@link #FastSortedMap(ComparatorService) comparators}.</p>
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
     
   /**
    * Creates a sorted map backed up by the specified service implementation.
    */
   public FastSortedMap(MapService<K, V> service) {
       super(service);
   }
   
   

   @Override
   public FastSortedSet<Entry<K, V>> entrySet() {
       return null;
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

    @Override
    public Comparator<? super K> comparator() {
        return keySet().comparator();
    }   
   
}
