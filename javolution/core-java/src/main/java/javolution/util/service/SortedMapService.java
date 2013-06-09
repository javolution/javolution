/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Map.Entry;



/**
 * The set of related functionalities used to implement sorted map.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface SortedMapService<K,V> extends MapService<K,V> {

    /**
     * Returns a view over a portion of this map.
     * 
     * @param fromKey the low endpoint inclusive or {@code null} if none (head map).
     * @param toKey the high endpoint exclusive  or {@code null} if none (tail map).        
     */
    SortedMapService<K,V> subMap(K fromKey, K toKey);

    /**
     * Returns the first (lowest) key currently in this map.
     */
    K firstKey();

    /**
     * Returns the last (highest) element currently in this map.
     */
    K lastKey();   
    
}
