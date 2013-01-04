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
import javolution.text.TypeFormat;

/**
 * <p> A context holding local parameters {@link #valueOf values}.
 *     [code]
 *     public static LocalParameter<LargeInteger> MODULO = new LocalParameter<LargeInteger>() { ... }; 
 *     ...
 *     LocalContext ctx = LocalContext.enter(); 
 *     try {
 *         ctx.set(ModuloInteger.MODULO, m); // No impact on other threads!
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
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable(false) {

        @Override
        public void configure(CharSequence configuration) {
            set(TypeFormat.parseBoolean(configuration));
        }

    };

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
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return LOCAL_CONTEXT_TRACKER.getService(
                WAIT_FOR_SERVICE.get()).inner().enterScope();
    }

    /**
     * Returns the local value of the specified parameter (its default
     * value if it is not {@link LocalContext#setLocalValue overriden}).
     * 
     * @param  param the local parameter whose local value is returned.
     */
    public static <T> T valueOf(LocalParameter<T> param) {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        return (ctx != null) ? ctx.get(param) : param.get();
    }

    /**
     * Overrides the value of the specified parameter. 
     * 
     * @param  param the local parameter whose local value is overriden.
     * @param  localValue the new local value.
     * @throws SecurityException if the permission to override the specified 
     *         parameter is not granted.
     */
    public abstract <T> void set(LocalParameter<T> param, T localValue);

    /**
     * Returns the value possibly overriden of the specified parameter 
     * (its default value if not {@link LocalContext#setLocalValue overriden}). 
     * 
     * @param  param the local parameter whose local value is returned.
     */
    protected abstract <T> T get(LocalParameter<T> param);

    /**
     * Exits the scope of this local context; reverts to the local settings 
     * before this context was entered.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() {
        super.exit();
    }

}