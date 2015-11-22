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
 * <p> Represents an operation upon two operands of the same type, 
 *     producing a result of the same type as the operands.</p>
 *     
 * @param <T> The type of the operands and result of the operator.
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, December 31, 2014
 * @see Operators
 */
public interface BinaryOperator<T> {

    /**
     * Returns the result of applying this operator on the specified parameters. 
     * 
     * @param first the first parameter. 
     * @param second the second parameter. 
     * @return the result of this operator.
     */
    T apply(T first, T second);
    
}