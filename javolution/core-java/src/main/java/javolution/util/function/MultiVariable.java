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
 * <p> An object holding multiple variables; typically used to create 
 *     {@link Function multi-parameters functions}.</p>
 * 
 * <p> Multi-variables may represent an unbounded number of variables.
 *     [code]
 *     MultiVariable<Double, MultiVariable<Integer, Boolean>> tertiaryVariable
 *         = new MultiVariable(2.3, new MultiVariable(57, true));
 *     [/code].</p>
 * 
 * @param <L> the type of the variable on the left.
 * @param <R> the type of the variable on the right.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class MultiVariable<L, R> {

    private final L left;
    private final R right;

    /**
     * Returns a multi-variable holding the specified objects (possibly 
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