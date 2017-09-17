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
public interface UnaryOperator<T> extends Function<T,T> {

    /**
     * An unary operator that always returns its input argument.
     */
    public static final UnaryOperator<Object> IDENTITY = new UnaryOperator<Object>() {

        @Override
        public Object apply(Object param) {
            return param;
        }}; 
    
}