/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> An object holding multiple variables; typically used to create 
 *     {@link Functor multifunctors}.</p>
 * 
 * <p> Multivariable may be composed to represent an unbounded number of 
 *     variables. For example.
 *     [code]
 *     MultiVariable<Double, MultiVariable<Integer, Boolean>> tertiaryVariable
 *         = new MultiVariable(2.3, new MultiVariable(57, true));
 *     [/code].</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public class MultiVariable<L, R> {

    private final L left;
    private final R right;

    /**
     * Returns a multi-variables holding the specified objects (possibly 
     * multi-variables themselves). 
     */
    public MultiVariable(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the variable on the left.
     */
    public L getLeft() {
        return left;
    }

    /**
     * Returns the variable on the right.
     */
    public R getRight() {
        return right;
    }
}