/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
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
 * @version 3.6, September 24, 2005
 */
public class HeapContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new HeapContext().getClass();

    /**
     * Default constructor.
     */
    public HeapContext() {
    }

    /**
     * Enters a {@link HeapContext}.
     */
    public static void enter() {
        Context.enter(HeapContext.CLASS);
    }

    /**
     * Exits the current {@link HeapContext}.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of HeapContext. 
     */
    public static void exit() {
        Context.exit(HeapContext.CLASS);
    }

    // Implements Context abstract method.
    protected void enterAction() {
        inheritedPoolContext = null; // Overrides inherited.
        PoolContext outer = getOuter().inheritedPoolContext;
        if (outer != null) {
            outer.setInUsePoolsLocal(false);
        }
    }

    // Implements Context abstract method.
    protected void exitAction() {
        PoolContext outer = getOuter().inheritedPoolContext;
        if (outer != null) {
            outer.setInUsePoolsLocal(true);
        }
    }

}