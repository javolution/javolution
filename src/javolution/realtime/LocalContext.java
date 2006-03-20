/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.lang.Reflection;
import javolution.util.FastMap;

/**
 * <p> This class represents a local context; it is used to define locally 
 *     scoped setting through the use of {@link LocalReference} typically 
 *     wrapped within a static method. For example:[code]
 *     LocalContext.enter();
 *     try {
 *         LargeInteger.setModulus(m); // Performs integer operations modulo m.
 *         Length.showAs(NonSI.INCH); // Shows length in inches.
 *         RelativisticModel.select(); // Uses relativistic physical model.
 *         QuantityFormat.getInstance(); // Returns local format for quantities.
 *         XmlFormat.setFormat(Foo.class, myFormat); // Uses myFormat for instances of Foo.
 *     } finally {
 *         LocalContext.exit(); // Reverts to previous settings.
 *     }[/code]</p>   
 *     
 * <p> Calls to locally scoped methods should be performed either at
 *     start-up (global setting) or within a local context (to avoid 
 *     impacting other threads).</p>
 *     
 * <p> Finally, local settings are inherited by {@link ConcurrentThread 
 *     concurrent threads} spawned while in a local context scope.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, September 24, 2005
 * @see javolution.util.LocalMap
 */
public class LocalContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = Reflection
            .getClass("javolution.realtime.LocalContext");

    /**
     * Holds any reference associated to this context (reference to 
     * referent mapping).
     */
    final FastMap _references = new FastMap();

    /**
     * Default constructor.
     */
    public LocalContext() {
    }

    /**
     * Returns the current local context or <code>null<code> if the current
     * thread does not execute within a local context (global context).  
     *
     * @return the current local context.
     */
    public static/*LocalContext*/Context current() {
        return Context.current().inheritedLocalContext;
    }

    /**
     * Enters a {@link LocalContext}.
     */
    public static void enter() {
        Context.enter(LocalContext.CLASS);
    }

    /**
     * Exits the current {@link LocalContext}.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of LocalContext. 
     */
    public static void exit() {
        Context.exit(LocalContext.CLASS);
    }

    /**
     * Removes all local settings for this context.
     */
    public void clear() {
        super.clear();
        _references.clear();
    }

    // Implements Context abstract method.
    protected void enterAction() {
        inheritedLocalContext = this; // Overrides inherited.
    }

    // Implements Context abstract method.
    protected void exitAction() {
        _references.clear();
    }
}