/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

/**
 * <p> This class represents a heap context; it is used to allocate objects
 *     from the heap exclusively.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class HeapContext extends Context {

    /**
     * Default constructor.
     */
    HeapContext() {
    } 

    /**
     * Enters a {@link HeapContext}.
     */
    public static void enter() {
        HeapContext ctx = (HeapContext) push(HeapContext.class);
        if (ctx == null) {
            ctx = new HeapContext();
            push(ctx);
        }
        PoolContext outer = ctx.getOuter().poolContext();
        if (outer != null) {
            outer.setInUsePoolsLocal(false);
        }
    }

    /**
     * Exits the current {@link HeapContext}.
     *
     * @throws ClassCastException if the current context is not a heap context.
     * @throws UnsupportedOperationException if the current context is the root 
     *         context.
     */
    public static void exit() {
        HeapContext ctx = (HeapContext) pop();
        PoolContext outer = ctx.getOuter().poolContext();
        if (outer != null) {
            outer.setInUsePoolsLocal(true);
        }
    }

    // Implements abstract method.
    protected void dispose() {
        // No resource allocated.
    }
}