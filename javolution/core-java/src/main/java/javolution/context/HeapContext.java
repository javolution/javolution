/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.internal.context.HeapContextImpl;
import javolution.internal.osgi.JavolutionActivator;

/**
 * <p> This abstract class represents an {@link AllocatorContext} always 
 *     allocating from the heap (default Java allocation). This context 
 *     can be useful when executing in a {@link StackContext} to perform 
 *     allocations on the heap (e.g. to update static variables).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class HeapContext extends AllocatorContext {

    /**
     * Defines the factory service producing {@link HeapContext} implementations.
     */
    public interface Factory {

        /**
         * Returns a new instance of the heap context.
         */
        HeapContext newHeapContext();
    }

    /**
     * Default constructor.
     */
    protected HeapContext() {
    }

    /**
     * Enters a new heap context instance.
     */
    public static void enter() {
        HeapContext.Factory factory = JavolutionActivator.getHeapContextFactory();
        HeapContext ctx = (factory != null) ? factory.newHeapContext()
                : new HeapContextImpl();
        ctx.enterScope();
    }

    /**
     * Exits the heap context.
     *
     * @throws ClassCastException if the current context is not a heap context.
     */
    public static void exit() {
        ((HeapContext) AbstractContext.current()).exitScope();
    }
}