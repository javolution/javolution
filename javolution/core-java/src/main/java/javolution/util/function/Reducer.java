/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import java.util.Collection;


/**
 * <p> An operator upon multiple elements of a collection yielding a result 
 *     of that collection type.</p>
 * 
 * @param <E> The type of elements in the collection operated upon.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see     Reducers
 */
public interface Reducer<E> extends Consumer<Collection<E>>, Supplier<E> {

}