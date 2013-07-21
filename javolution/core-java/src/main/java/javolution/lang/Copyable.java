/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> An object for which a completely independent copy can be obtained.
 *     This interface is typically used to ensure "full" immutability of local 
 *     copies or to copy object graphs from one memory space to another 
 *     (when supported by the JVM).</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see     javolution.context.StackContext
 */
public interface Copyable<T> {

    /**
     * Returns an object identical to this but fully independent from it. 
     * No matter what happens to this object (e.g. this object is destroyed), 
     * it will have no impact on the copy. Only objects for which the unicity 
     * has to be maintained may return {@code this}.
     */
    T copy();

}