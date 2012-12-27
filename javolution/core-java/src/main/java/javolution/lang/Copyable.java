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
 * <p> This interface identifies objects for which a deep copy can be performed
 *     (recursive copy). This interface is particularly useful to copy objects 
 *     from one memory space to another (when supported by the JVM). 
 *     [code]
 *     class Foo implements Copyable {...}
 *     ...
 *     StackContext ctx = StackContext.enter(); // Starts stack allocation (if supported by JVM)
 *     try {
 *         Foo foo = calculateFoo(); // All allocations are performed on the stack.
 *         return ctx.export(foo); // Exports through copy outside of the stack.
 *     } finally {
 *         ctx.exit(); // Resets stacks.
 *     }[/code]</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see     javolution.context.StackContext
 */
public interface Copyable<T> {

    /**
     * Returns a deep copy of this object. 
     * 
     * @return an object identical to this object but possibly allocated 
     *         in a different memory space.
     */
    T copy();

}