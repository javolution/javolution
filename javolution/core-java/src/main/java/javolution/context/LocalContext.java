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
import javolution.internal.context.LocalContextImpl;
import javolution.lang.Configurable;
import javolution.lang.Permission;
import javolution.lang.RealTime;

/**
 * <p> A context holding local parameters values.</p>
 * <p> [code]
 *     public class ModuloInteger {
 *         public static final LocalContext.Parameter<LargeInteger> MODULO 
 *             = new LocalContext.Parameter<LargeInteger>(ONE.minus());
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
public abstract class LocalContext extends AbstractContext {

    /**
     * A local context parameter with {@link Configurable configurable} 
     * default value. The current value can locally {@link LocalContext#override 
     * overridden} when running in the scope of a {@link LocalContext}.</p>
     * 
     * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
     * @version 6.0, July 21, 2013
     */
    @RealTime
    public static class Parameter<T> {

        /**
         * Holds the general permission to override local parameters (action <code>
         * "override"</code>).
         */
        public static final Permission<Parameter<?>> OVERRIDE_PERMISSION = new Permission<Parameter<?>>(
                Parameter.class, "override");

        /**
         * Holds the default value.
         */
        private final Configurable<T> defaultValue;

        /**
         * Holds this instance override permission.
         */
        private final Permission<Parameter<T>> overridePermission;

        /**
         * Creates a local parameter having the specified default value
         * (configurable).
         */
        public Parameter(T defaultValue) {
            this.defaultValue = new Configurable<T>(defaultValue);
            this.overridePermission = new Permission<Parameter<T>>(
                    Parameter.class, "override", this);
        }

        /**
         * Returns the permission to override the current value of this instance.
         * 
         * @see LocalContext#override
         */
        public Permission<Parameter<T>> getOverridePermission() {
            return overridePermission;
        }

        /**
         * Returns this local parameter current value (the default value if not 
         * {@link LocalContext#override overridden}).
         */
        public T get() {
            LocalContext ctx = AbstractContext.current(LocalContext.class);
            return (ctx != null) ? ctx.getLocalValueInContext(this) : getDefault()
                    .get();
        }

        /**
         * Returns this local parameter default value.
         */
        public Configurable<T> getDefault() {
            return defaultValue;
        }
    }

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false);

    /**
     * Default constructor.
     */
    protected LocalContext() {}

    /**
     * Enters a new local context instance.
     * 
     * @return the new local context implementation entered.
     */
    public static LocalContext enter() {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx == null) {
            ctx = LOCAL_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get(), DEFAULT);
        }
        return (LocalContext) ctx.enterInner();
    }

    /**
     * Overrides the local value of the specified parameter. 
     * 
     * @param  param the local parameter whose local value is overridden.
     * @param  localValue the new local value.
     * @throws SecurityException if the permission to override the specified 
     *         parameter is not granted.
     */
    public abstract <T> void override(Parameter<T> param, T localValue);

    /**
     * Returns the local value of the specified parameter 
     * (its default value if not {@link LocalContext#override overridden}). 
     * 
     * @param  param the local parameter whose local value is returned.
     */
    protected abstract <T> T getLocalValueInContext(Parameter<T> param);
    
    private static final LocalContextImpl DEFAULT = new LocalContextImpl();
}