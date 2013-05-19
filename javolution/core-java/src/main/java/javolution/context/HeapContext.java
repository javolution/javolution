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
import javolution.lang.Copyable;
import javolution.util.function.Supplier;

/**
 * <p> An {@link AllocatorContext} always allocating from the heap (default).</p>
 * 
 * <p> This context is typically used when updating the static fields of 
 *     {@link javolution.annotation.StackSafe} classes.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see javolution.annotation.StackSafe
 */
public abstract class HeapContext extends AllocatorContext<HeapContext> {

    /**
     * Default constructor.
     */
    protected HeapContext() {
    }
    
   /**
     * Enters a heap context instance (private since heap context
     * is not configurable).
     */
    private static HeapContext enter() {
        HeapContext ctx = AbstractContext.current(HeapContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return HEAP_CONTEXT_TRACKER.getService(false).inner().enterScope();
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