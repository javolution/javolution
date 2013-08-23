/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;


/**
 * <p> A function that perform some operation and returns the result of 
 *     that operation.</p>
 * 
 * @param <T> the type of the input parameter of the apply operation.
 * @param <R> the type of the result of the apply operation.
 *                   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see <a href="http://en.wikipedia.org/wiki/Function_(computer_science)">Wikipedia: Function<a>    
 */
public interface Function<T, R> {

    /**
     * Returns the result of applying this function to the specified parameter. 
     * 
     * @param param the parameter object on which the function is performed. 
     * @return the result of the function.
     */
    R apply(T param);

}