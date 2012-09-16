/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.util.FastMap;

/**
 * <p> This class represents a context to define locally scoped environment
 *     settings. This settings are held by {@link LocalContext.Reference} 
 *     and typically wrapped within a static method:[code]
 *     LocalContext.enter();
 *     try {
 *         ModuloInteger.setModulus(m); // Performs integer operations modulo m.
 *         Length.showAs(NonSI.INCH); // Shows length in inches.
 *         RelativisticModel.select(); // Uses relativistic physical model.
 *         ... // Operations performed using local settings.
 *     } finally {
 *         LocalContext.exit(); // Reverts to previous settings.
 *     }[/code]</p>   
 *     
 * <p> Calls to locally scoped methods should be performed either at
 *     start-up (global setting) or within a local context (to avoid 
 *     impacting other threads).</p>
 *     
 * <p> As for any context, local context settings are inherited during 
 *     {@link ConcurrentContext concurrent} executions.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, September 24, 2005
 * @see javolution.util.LocalMap
 */
public class LocalContext extends Context {

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
     * Enters a {@link LocalContext} possibly recycled.
     */
    public static void enter() {
         Context.enter(LocalContext.class);
    }

    /**
     * Exits the current local context.
     * 
     * @throws ClassCastException if the context is not a local context.
     */
    public static void exit() {
        Context.exit(LocalContext.class);
    }

    // Implements Context abstract method.
    protected void enterAction() {
        // Do nothing.
    }

    // Implements Context abstract method.
    protected void exitAction() {
        _references.clear();
    }

    /**
     * <p> This class represents a reference whose setting is local to the current 
     *     {@link LocalContext}. Setting outside of any {@link LocalContext} scope 
     *     affects the reference default value (equivalent to {@link #setDefault}).
     * </p>
     */
    public static class Reference <T>  implements javolution.lang.Reference <T>  {

        /**
         * Holds the default value for this reference.
         */
        private  T  _defaultValue;
        /**
         * Indicates if this reference value has ever been locally overriden 
         * (optimization, most applications use default values).
         */
        private boolean _hasBeenLocallyOverriden;

        /**
         * Default constructor (default referent is <code>null</code>).
         */
        public Reference() {
            this(null);
        }

        /**
         * Creates a local reference having the specified default value.
         * 
         * @param defaultValue the default value or root value of this variable.
         */
        public Reference( T  defaultValue) {
            _defaultValue = defaultValue;
        }

        /**
         * Returns the local value for this reference.
         * The first outer {@link LocalContext} is searched first, then
         * all outer {@link LocalContext} are recursively searched up to the
         * global root context which contains the default value.
         *
         * @return the context-local value.
         */
        public final  T  get() {
            return (_hasBeenLocallyOverriden) ? retrieveValue() : _defaultValue;
        }

        private  T  retrieveValue() {
            for (Context ctx = Context.getCurrentContext(); ctx != null; ctx = ctx.getOuter()) {
                if (ctx instanceof LocalContext) {
                    LocalContext localContext = (LocalContext) ctx;
                    Object value = localContext._references.get(this);
                    if (value != null)
                        return ( T ) value;
                }
            }
            // Not found, returns default value.
            return _defaultValue;
        }

        /**
         * Sets the local value (referent) for this reference.
         *
         * @param value the new local value or <code>null</code> to inherit
         *        the outer value.
         */
        public void set( T  value) {
            LocalContext ctx = Reference.getLocalContext();
            if (ctx != null) {
                FastMap references = ctx._references;
                references.put(this, value);
                _hasBeenLocallyOverriden = true;
                return;
            }
            // No local context, sets default value.
            _defaultValue = value;
        }

        /**
         * Returns the default value for this reference.
         *
         * @return the defaultValue.
         */
        public  T  getDefault() {
            return _defaultValue;
        }

        /**
         * Returns the local (non-inherited) value for this reference.
         *
         * @return the local value or <code>null</code> if none (value to be 
         *         inherited or not set).
         */
        public  T  getLocal() {
            LocalContext ctx = Reference.getLocalContext();
            return (ctx != null) ? ( T ) ctx._references.get(this)
                    : _defaultValue;
        }

        /**
         * Sets the default value for this reference.
         *
         * @param  defaultValue the root value.
         */
        public void setDefault( T  defaultValue) {
            _defaultValue = defaultValue;
        }

        /**
         * Returns the string representation of the current value of this 
         * reference.
         *
         * @return <code>String.valueOf(this.get())</code>
         */
        public String toString() {
            return String.valueOf(this.get());
        }

        // Returns the local context if any.
        private static LocalContext getLocalContext() {
            for (Context ctx = Context.getCurrentContext(); ctx != null; ctx = ctx.getOuter()) {
                if (ctx instanceof LocalContext)
                    return (LocalContext) ctx;
            }
            return null;
        }
    }
}