/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.context.SecurityContext.Permission;
import javolution.lang.Configurable;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context holding locally scoped {@link Parameter parameters} values.</p>
 * <p> For example, when performing modulo arithmetics the actual modulo 
 *     being used is usually the same for most operations and does not need 
 *     to be specified for each operation.
 * [code]
 * import javolution.context.LocalContext.Parameter;
 * public class ModuloInteger extends Number {
 *     public static final Parameter<Integer> MODULO = new Parameter<Integer>() {
 *          protected Integer getDefault() { return -1; }
 *     }; 
 *     public ModuloInteger times(ModuloInteger that) { ... }    
 * }     
 * LocalContext ctx = LocalContext.enter(); 
 * try {
 *     ctx.supersede(ModuloInteger.MODULO, 13); // Sets local modulo value.
 *     x = a.times(b).plus(c.times(d)); // Operations modulo 13
 *     ...
 * } finally {
 *     ctx.exit(); // Reverts to previous modulo setting. 
 * }[/code]</p>
 *     
 * <p> As for any context, local context settings are inherited during 
 *     {@link ConcurrentContext} executions.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class LocalContext extends AbstractContext {

    /**
     * A {@link Configurable configurable} parameter whose value can 
     * be locally superseded within the scope of {@link LocalContext}.</p>
     */
    public static abstract class Parameter<T> extends Configurable<T> {

        /**
         * Holds the general permission to supersede any parameter value 
         * (action "supersede").
         */
        public static final Permission<Parameter<?>> SUPERSEDE_PERMISSION = new Permission<Parameter<?>>(
                Parameter.class, "supersede");

        /**
         * Holds this instance supersede permission.
         */
        private final Permission<Parameter<T>> supersedePermission;

        /**
         * Creates a new parameter (configurable).
         */
        public Parameter() {
            this.supersedePermission = new Permission<Parameter<T>>(
                    Parameter.class, "supersede", this);
        }

        /**
         * Returns the permission to locally supersede the current value 
         * of this instance.
         */
        public Permission<Parameter<T>> getSupersedePermission() {
            return supersedePermission;
        }

        /**
         * Returns the current parameter value (the default value if not 
         * reconfigured nor {@link LocalContext#supersede superseded}).
         */
        public T get() {
            LocalContext ctx = current(LocalContext.class);
            return (ctx != null) ? ctx.getValue(this, super.get()) : super.get();
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
        LocalContext ctx = current(LocalContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getLocalContext();
        }
        return (LocalContext) ctx.enterInner();
    }

    /**
     * Supersedes the value of the specified parameter. 
     * 
     * @param  param the local parameter whose local value is overridden.
     * @param  localValue the new local value.
     * @throws SecurityException if the permission to override the specified 
     *         parameter is not granted.
     * @throws NullPointerException if the specified local value is {@code null}.
     */
    public abstract <T> void supersede(Parameter<T> param, T localValue);

    /**
     * Returns the local value of the specified parameter or the specified 
     * default value if not {@link LocalContext#supersede superseded}. 
     * 
     * @param param the local parameter whose local value is returned.
     * @param defaultValue the parameter value if not superseded.
     */
    protected abstract <T> T getValue(Parameter<T> param, T defaultValue);
    
}