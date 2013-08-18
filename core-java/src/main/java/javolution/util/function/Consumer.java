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
 * <p> A special type of function which does not return anything.</p>
 * 
 * <p> Note: In future version this interface may derive from 
 *           {@code Function<P, Void>}.</p>
 *           
 * @param <T> The type of input parameter to accept.
 *           
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Consumer<T> {

    /**
     * Accepts an input value.
     */
    void accept(T param);

}