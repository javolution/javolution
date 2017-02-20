/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.lang;

/**
 * <p> An object whose state cannot change after it is constructed. Maximum reliance on immutable objects is widely 
 *     accepted as a sound strategy for creating simple, reliable code. Immutable objects are particularly useful in
 *     concurrent applications. Since they cannot change state, they cannot be corrupted by thread interference or 
 *     observed in an inconsistent state.</p>
 
 * <p> Immutable objects can be manipulated by value rather than by reference; a JVM implementation may allocate 
 *     instances of this class on the stack. The entire graph of objects reachable from an immutable object must be
 *     immutable as well. 
 * <pre>{@code
 * {@literal@}ReadOnly
 * public class Complex implements Immutable { // Complex numbers can be manipulated by value.
 *     ...
 *     
 *     {@literal@}Override
 *     public boolean equals(Object obj) { ... } // Must compare values.
 * 
 *     {@literal@}Override
 *     public int hashCode() { ... } // Must return value hash code.
 *         
 * }}</pre></p>
 *      
 * <p> <b>Note:</b> "Stack" allocation is not the only optimization that a VM can do on {@link Immutable} objects. 
 *     The VM might decide not to perform any allocation at all and store values directly in registers.</p> 
 *              
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, December 16, 2016
 */
public interface Immutable  { 
    
    /**
     * Returns {@code true} if this object has the same value content as the the one specified; {@code false} otherwise.
     * The default object equals must be overridden to ensure the same behavior whether or not the instance is 
     * allocated on the stack.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns the hash code of the value content of this object.
     */
    @Override
    int hashCode();
    
}