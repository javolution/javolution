/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Copyable;
import javolution.osgi.internal.OSGiServices;
import javolution.util.function.Supplier;

/**
 * <p> An {@link AllocatorContext} always allocating from the heap (default).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class HeapContext extends AllocatorContext {

    /**
     * Default constructor.
     */
    protected HeapContext() {}

    /**
      * Enters and returns a new heap context instance (method for internal 
      * usage).
      */
    private static HeapContext enter() {
        HeapContext ctx = AbstractContext.current(HeapContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getHeapContext();
        }
        return (HeapContext) ctx.enterInner();
    }

    /**
     * Executes the specified logic allocating objects on the heap.
     */
    public static void execute(Runnable logic) {
        HeapContext ctx = HeapContext.enter();
        try {
            ctx.executeInContext(logic);
        } finally {
            ctx.exit();
        }
    }

    /**
     * Returns a new instance allocated on the heap and produced by the 
     * specified factory (convenience method).
     */
    public static <T> T allocate(Supplier<T> factory) {
        HeapContext ctx = HeapContext.enter();
        try {
            return ctx.allocateInContext(factory);
        } finally {
            ctx.exit();
        }
    }

    /**
     * Returns a copy of the specified object allocated on the heap
     * (convenience method).
     */
    public static <T> T copy(Copyable<T> obj) {
        HeapContext ctx = HeapContext.enter();
        try {
            return ctx.copyInContext(obj);
        } finally {
            ctx.exit();
        }
    }
}