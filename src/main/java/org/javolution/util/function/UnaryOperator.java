/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

/**
 * Represents an operation on a single operand that produces a result of the same type as its operand.
 *     
 * @param <T> The type of the operands and result of the operator.
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, December 31, 2014
 */
public interface UnaryOperator<T> extends java.util.function.UnaryOperator<T> {

	/**
     * Returns a unary operator that always returns its input argument.
     *
     * @param <T> the type of the input and output of the operator.
     * @return a unary operator that always returns its input argument.
     */
    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
    
}