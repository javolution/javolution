/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;
import javolution.lang.Permission;
import javolution.lang.RealTime;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context holding locally scoped {@link Parameter parameters} values.</p>
 * <p> [code]
 *     public class ModuloInteger extends Number {
 *         public static final LocalContext.Parameter<Integer> MODULO 
 *             = new LocalContext.Parameter<Integer>();
 *         public ModuloInteger times(ModuloInteger that) { ... }    
 *     }     
 *     ...
 *     LocalContext ctx = LocalContext.enter(); 
 *     try {
 *         ctx.override(ModuloInteger.MODULO, 13); // Sets local modulo value.
 *         z = x.times(y); // Multiplication modulo 13
 *     } finally {
 *         ctx.exit(); // Reverts to previous modulo setting. 
 *     }
 *     }[/code]</p>
 *     
 * <p> Unlike thread-local objects, local context parameters settings are 
 *     inherited during {@link ConcurrentContext} executions.</p> 
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
     * Default constructor.
     */
    protected LocalContext() {}

    /**
     * Enters and returns a new local context instance.
     */
    public static LocalContext enter() {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getLocalContext();
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
    
}