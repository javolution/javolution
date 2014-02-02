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
 * <p> A {@link Constant constant} object which can be manipulated by value
 *     rather than by reference; a JVM implementation may allocate instances 
 *     of this class on the stack. 
 * [code]
 * public class Complex implements ValueType { // Complex numbers can be manipulated by value.
 *     ...
 *     
 *     @Override
 *     public boolean equals(Object obj) { ... } // Must compare values.
 * 
 *     @Override
 *     public int hashCode() { ... } // Must return value hash code.    
 * }
 * [/code]</p>
 *      
 * <p> <b>Note:</b> "Stack" allocation is not the only optimization that a VM 
 *     can do on {@link ValueType}. The VM might decide not to perform any 
 *     allocation at all and store values directly in registers.</p> 
 *              
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 2, 2014
 */
@Constant
public interface ValueType  { 
    
    /**
     * Returns {@code true} if this object has the same value content as the 
     * the one specified; {@code false} otherwise. The default object 
     * equals must be overridden to ensure the same behavior whether or 
     * not the instance is allocated on the stack.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns the hash code of the value content of this object.
     */
    @Override
    int hashCode();
    
}