/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.LOCAL_CONTEXT_TRACKER;
import javolution.lang.Configurable;

/**
 * <p> A context holding local parameters values.
 *     [code]
 *     public class ModuloInteger {
 *         public static final LocalParameter<LargeInteger> MODULO 
 *             = new LocalParameter<LargeInteger>(ONE.minus());
 *         ...
 *     }     
 *     ...
 *     LocalContext ctx = LocalContext.enter(); 
 *     try {
 *         ctx.override(ModuloInteger.MODULO, m); // No impact on other threads!
 *         z = x.times(y); // Multiplication modulo m (MODULO.get() == m)
 *     } finally {
 *         ctx.exit(); // Reverts changes. 
 *     }
 *     }[/code]</p>   
 *     
 * <p> As for any context, local context settings are inherited during 
 *     {@link ConcurrentContext} executions.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class LocalContext extends AbstractContext<LocalContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false);
    
    /**
     * Default constructor.
     */
    protected LocalContext() {
    }

    /**
     * Enters a new local context instance.
     * 
     * @return the new local context implementation entered.
     */
    public static LocalContext enter() {
        return LocalContext.current().inner().enterScope();
    }

    /**
     * Overrides the local value of the specified parameter. 
     * 
     * @param  param the local parameter whose local value is overridden.
     * @param  localValue the new local value.
     * @throws SecurityException if the permission to override the specified 
     *         parameter is not granted.
     */
    public abstract <T> void override(LocalParameter<T> param, T localValue);

    /**
     * Returns the local value of the specified parameter 
     * (its default value if not {@link LocalContext#setLocalValue overridden}). 
     * 
     * @param  param the local parameter whose local value is returned.
     */
    protected abstract <T> T getLocalValueInContext(LocalParameter<T> param);

    /**
     * Returns the current local context.
     */
    protected static LocalContext current() {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx != null) return ctx;
        return LOCAL_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get());
    }

}