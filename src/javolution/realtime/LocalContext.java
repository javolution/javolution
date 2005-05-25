/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.util.FastMap;

/**
 * <p> This class represents a local context; it is used to define locally 
 *     scoped setting through the use of {@link LocalReference} typically 
 *     wrapped within a static method. For example:<pre>
 *        LargeInteger.setModulus(m); // Performs integer operations modulo m.
 *        Length.showAs(NonSI.INCH); // Shows length in inches.
 *        RelativisticModel.select(); // Uses relativistic physical model.
 *        QuantityFormat.getInstance(); // Returns local format for quantities.
 *     </pre></p>   
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 10, 2004
 */
public final class LocalContext extends Context {

    /**
     * Holds any reference associated to this context (reference to 
     * referent mapping).
     */
    final FastMap _references = new FastMap();

    /**
     * Default constructor.
     */
    LocalContext() {
    }

    /**
     * Enters a {@link LocalContext}.
     */
    public static void enter() {
        LocalContext ctx = (LocalContext) push(LOCAL_CONTEXT_CLASS);
        if (ctx == null) {
            ctx = new LocalContext();
            push(ctx);
        }
    }
    private static final Class LOCAL_CONTEXT_CLASS = new LocalContext().getClass();

    /**
     * Exits the current {@link LocalContext}.
     *
     * @throws ClassCastException if the current context is not a
     *         {@link LocalContext}.
     */
    public static void exit() {
        LocalContext ctx = (LocalContext) pop();
        ctx._references.clear();
    }

    // Implements abstract method.
    protected void dispose() {
        // No resource allocated.
    }
}