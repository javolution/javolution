/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.HEAP_CONTEXT_TRACKER;
import javolution.internal.context.HeapContextImpl;
import javolution.lang.Configurable;
import javolution.lang.Copyable;
import javolution.util.function.Supplier;

/**
 * <p> An {@link AllocatorContext} always allocating from the heap (default).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class HeapContext extends AllocatorContext {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false);

    /**
     * Default constructor.
     */
    protected HeapContext() {}

    /**
      * Enters a heap context instance (private since heap context
      * is not configurable).
      */
    private static HeapContext enter() {
        HeapContext ctx = AbstractContext.current(HeapContext.class);
        if (ctx == null) { // Root.
            ctx = HEAP_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get(), DEFAULT);
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

    private static final HeapContextImpl DEFAULT = new HeapContextImpl();
}