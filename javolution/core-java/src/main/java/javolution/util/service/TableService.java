/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import java.util.List;
import java.util.Deque;
import java.util.RandomAccess;

/**
 * The set of related functionalities used to implement tables collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface TableService<E> extends CollectionService<E>, List<E>, Deque<E>, RandomAccess {


    @Override
    TableService<E> subList(int fromIndex, int toIndex);

}
