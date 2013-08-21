/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.Map;
import java.util.SortedMap;

/**
 * The set of related functionalities used to implement sorted map.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface SortedMapService<K, V> extends MapService<K, V>,
        SortedMap<K, V> {

    @Override
    SortedSetService<Map.Entry<K, V>> entrySet();

    @Override
    SortedMapService<K, V> headMap(K toKey);

    @Override
    SortedSetService<K> keySet();

    @Override
    SortedMapService<K, V> subMap(K fromKey, K toKey);

    @Override
    SortedMapService<K, V> tailMap(K fromKey);
    
    @Override
    SortedMapService<K, V>[] split(int n, boolean updateable);

}
