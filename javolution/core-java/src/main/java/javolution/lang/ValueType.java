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
 * <p> An object which can be manipulated by value; a JVM implementation may 
 *     allocate instances of this class on the stack.</p>
 *     
 * <p> {@link ValueType} are {@link Immutable} (they have a constant
 *     {@link #value} of the specified parameter type). Most of the time 
 *     the constant value and the value type are the same, but not always
 *     as illustrated below. 
 * [code]
 * public class Complex implements ValueType<Complex> { // Immutable<Complex>
 *     public static class Variable extends Complex { // Still Immutable<Complex>, but mutable Variable !
 *         public Variable(Complex that) { super(that.real, that.imaginary); }
 *         public Variable add(Complex that) {
 *              real += that.real;
 *              imaginary += that.imaginary;
 *              return this;
 *         }
 *          @Override
 *          public Complex value() { // Returns a constant Complex value
 *              return new Complex(real, imaginary); // Cannot return 'this', since this is mutable. 
 *          }
 *      }
 *      double real;
 *      double imaginary;
 *      public Complex(double real, double imaginary) {
 *          this.real = real;
 *          this.imaginary = imaginary;
 *      }
 *      public Complex plus(Complex that) { 
 *          return new Variable(this).add(that);
 *      }
 *      @Override
 *      public Complex value() { 
 *          return this; // Ok to return 'this' (immutable).
 *      }
 *      ... 
 * }
 * // Calculates the sum of an array of complex values. 
 * Complex[] values = ...;
 * 
 * // Standard calculations (stresses GC due to the high number of objects allocated).
 * Complex sum = Complex.ZERO;
 * for (Complex c : values) sum = sum.plus(c);
 * 
 * // Using mutable view, 3x faster and almost no garbage generated !
 * Complex.Variable sum = new Complex.Variable(Complex.ZERO);
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
 * @see <a href="http://en.wikipedia.org/wiki/Reverse_Polish_notation">Reverse Polish Notation</a>
 */
@RealTime
public interface ValueType<T> extends Immutable<T> {}