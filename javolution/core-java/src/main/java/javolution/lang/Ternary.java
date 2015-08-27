/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> An object composed of three items.</p>
 * 
 * <p> Ternary types may be encountered as the results of methods calls or as 
 *     {@link javolution.util.function functions} parameters.
 * <pre>{@code
 * Ternary<Double, Double, Double> xyz = ...;
 * Ternary<Temperature, Pressure, Humidity> atmosphericConditions() { ... }
 * Ternary<Integer, Integer, Integer> deinterleave3D(int unsigned) { ... } // MathLib
 * }<pre></p>
 * 
 * @param <A> the first type.
 * @param <B> the second type.
 * @param <C> the third type.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 25, 2015
 * @see Binary    
 */
public interface Ternary<A, B, C> {

    /**
     * Returns the instance of the first type.
     */
    A first();

    /**
     * Returns the instance of the second type.
     */
    B second();

    /**
     * Returns the instance of the third type.
     */
    C third();

}