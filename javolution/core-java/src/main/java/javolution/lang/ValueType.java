/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;


/**
 * <p> An {@link Immutable immutable} object which can be manipulated by value; 
 *     a JVM implementation may allocate instances of this class on the stack.</p>
 *     
 * <p> {@link ValueType ValueType} extends {@link Immutable} with the additional
 *     constraint that its value is itself (the {@link #value value} method 
 *     returns {@code this}).
 * [code]
 * public final class Complex implements ValueType<Complex> { // Complex is immutable.
 *     public class Variable implements Immutable<Complex> { // Variable has immutable value.
 *         private Variable() {} // Do not provide access outside.
 *         public Variable add(Complex that) {
 *              real += that.real;
 *              imaginary += that.imaginary;
 *              return this;
 *         }
 *         @Override
 *         public Complex value() { return new Complex(real, imaginary); }
 *     }
 *     private double real, imaginary;
 *     public Complex(double real, double imaginary) {
 *         this.real = real;
 *         this.imaginary = imaginary;
 *     }
 *     public Complex plus(Complex that) { 
 *         return new Complex(this.real + that.real, this.imaginary + this.imaginary);
 *     }
 *     @Override
 *     public Complex value() { return this; } // As per ValueType contract.
 *     public Variable newVariable() { return new Complex(real, imaginary).new Variable(); }
 * }
 * // Calculates the sum of an array of complex values. 
 * Complex[] values = ...;
 * 
 * // Standard calculations (stresses GC due to the high number of objects allocated).
 * Complex sum = Complex.ZERO;
 * for (Complex c : values) sum = sum.plus(c);
 * 
 * // Using variables, 2-3x faster and almost no garbage generated !
 * Complex.Variable sum = Complex.ZERO.newVariable();
 * for (Complex c : values) sum.add(c); 
 * [/code]</p>
 *      
 * <p> <b>Note:</b> "Stack" allocation is not the only optimization that a VM 
 *     can do on {@link ValueType}. The VM might decide not to perform any 
 *     allocation at all and store values directly in registers.</p> 
 *              
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @param <T> The type of the immutable constant value.
 */
public interface ValueType<T> extends Immutable<T> { 
    
    /**
     * Returns {@code this}.
     */
    @Override
    T value();

}