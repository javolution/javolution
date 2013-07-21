/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.IMMORTAL_CONTEXT_TRACKER;
import javolution.internal.context.ImmortalContextImpl;
import javolution.lang.Copyable;
import javolution.util.function.Supplier;

/**
 * <p> An {@link AllocatorContext} allocating from memory referenceable from all 
 *     Java threads (including real-time threads). Class initialization is 
 *     always performed in immortal memory. </p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class ImmortalContext extends AllocatorContext {

    /**
     * Default constructor.
     */
    protected ImmortalContext() {}

    /**
      * Enters a heap context instance (private since heap context
      * is not configurable).
      */
    private static ImmortalContext enter() {
        ImmortalContext ctx = AbstractContext.current(ImmortalContext.class);
        if (ctx == null) {
            ctx = IMMORTAL_CONTEXT_TRACKER.getService(false, DEFAULT);
        }
        return (ImmortalContext) ctx.enterInner();
    }

    /**
     * Executes the specified logic allocating objects on the heap.
     */
    public static void execute(Runnable logic) {
        ImmortalContext ctx = ImmortalContext.enter();
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
        ImmortalContext ctx = ImmortalContext.enter();
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
        ImmortalContext ctx = ImmortalContext.enter();
        try {
            return ctx.copyInContext(obj);
        } finally {
            ctx.exit();
        }
    }

    private static final ImmortalContextImpl DEFAULT = new ImmortalContextImpl();
}