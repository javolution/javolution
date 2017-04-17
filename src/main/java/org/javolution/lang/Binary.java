/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.lang;

/**
 * An object composed of two items.
 * 
 * Binary types may be encountered as the results of methods calls or as 
 * {@link org.javolution.util.function functions} parameters.
 * 
 * ```java
 * E reduce(Function<Binary<E, E>, E> operator) { ... }
 * Binary<Double, Unit<Mass>> getWeight() { ... }
 * Binary<Integer, Integer> deinterleave2D(int interleaved) { ... } // MathLib.
 * ```
 * 
 * @param <A> the first type.
 * @param <B> the second type.
 * 
 * @author <jean-marie@dautelle.com>
 * @version 6.1, February 25, 2015
 * @see Ternary    
 */
public interface Binary<A, B> {

    /**
     * Returns the instance of the first type.
     */
    A first();

    /**
     * Returns the instance of the second type.
     */
    B second();

}