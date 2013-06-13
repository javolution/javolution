/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import javolution.util.service.CollectionService;

/**
 * <p> An operator upon multiple elements of a collection yielding a result 
 *     of that collection type.</p>
 * 
 * @param <E> The type of elements in the collection operated upon.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see     Operators
 */
public interface CollectionOperator<E> extends Function<CollectionService<E>, E> {
   
}