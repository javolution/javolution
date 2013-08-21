/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.SortedSet;

/**
 * The set of related functionalities used to implement sorted set collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface SortedSetService<E> extends SetService<E>, SortedSet<E> {

    @Override
    SortedSetService<E> headSet(E toElement);

    @Override
    SortedSetService<E> subSet(E fromElement, E toElement);

    @Override
    SortedSetService<E> tailSet(E fromElement);
    
    @Override
    SortedSetService<E>[] split(int n, boolean updateable);

}
