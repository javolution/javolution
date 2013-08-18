/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import java.util.Iterator;

/**
 * <p> A function iterating over a collection.</p>
 * 
 * <p> Except for {@link Mutable} instances, iterations are not 
 *     allowed to modify the collection iterated.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Iteration<E>  {

    public interface Mutable<E> extends Iteration<E> {}
    public interface Sequential<E> extends Iteration<E> {}
       
     /** 
     * Runs the iteration using the specified iterator.
     */
    void run(Iterator<E> it);
  
 }