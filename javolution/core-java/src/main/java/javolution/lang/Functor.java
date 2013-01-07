/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import javolution.util.FastCollection;
import javolution.context.StackContext;

/**
 * <p> A function that perform some operation and returns the result of 
 *     that operation.</p>
 * 
 * <p> This interface is particularly useful when working with 
 *     {@link FastCollection} or {@link StackContext}.</p>
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Functor">Wikipedia: Functor<a>    
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public interface Functor<P, R> {

    /**
     * Returns the result of an evaluation on the specified parameter. 
     * 
     * @param param the parameter object on which the evaluation is performed. 
     * @return the result of the evaluation.
     */
    R evaluate(P param);

}