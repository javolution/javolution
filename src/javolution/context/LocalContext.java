/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;
import j2me.io.Serializable;
import javolution.util.FastMap;

/**
 * <p> This class represents a local context; it is used to define locally 
 *     scoped setting through the use of {@link LocalContext.Reference} typically 
 *     wrapped within a static method. For example:[code]
 *     LocalContext.enter();
 *     try {
 *         LargeInteger.setModulus(m); // Performs integer operations modulo m.
 *         Length.showAs(NonSI.INCH); // Shows length in inches.
 *         RelativisticModel.select(); // Uses relativistic physical model.
 *         QuantityFormat.getInstance(); // Returns local format for quantities.
 *         XMLFormat.setFormat(Foo.class, myFormat); // Uses myFormat for instances of Foo.
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
    private static final Class CLASS = new LocalContext().getClass();

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
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof LocalContext)
                return (LocalContext) ctx;
        }
        return null;
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
        // Do nothing.
    }

    // Implements Context abstract method.
    protected void exitAction() {
        _references.clear();
    }
    
    /**
     * <p> This class represents a reference whose setting is local to the current 
     *     {@link LocalContext}; setting outside of any {@link LocalContext} scope 
     *     affects the reference default value (equivalent to {@link #setDefault}).
     *     For example:[code]
     *     public class Foo {
     *         public static final LocalContext.Reference<TextFormat<Foo>> FORMAT 
     *             = new LocalContext.Reference<TextFormat<Foo>>(DEFAULT_FORMAT);
     *             
     *         public Text toString() {
     *              return FORMAT.get().format(this).toString();
     *         }     
     *     }
     *     ...
     *     LocalContext.enter();
     *     try {
     *        Foo.FORMAT.set(localFormat);
     *        ... // This thread displays Foo instances using localFormat. 
     *     } finally {
     *        LocalContext.exit(); // Reverts to previous format.
     *     }[/code]</p>
     *     
     * <p> Accessing/setting a local reference is fast and does not require 
     *     any form of synchronization. Local settings are inherited by 
     *     {@link ConcurrentThread concurrent threads} spawned from within the 
     *     same {@link LocalContext}.</p>
     */
    public static class Reference/*<T>*/implements javolution.lang.Reference/*<T>*/, Serializable {

        /**
         * Holds the default value for this reference.
         */
        private Object/*{T}*/_defaultValue;

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
        public Reference(Object/*{T}*/defaultValue) {
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
        public final Object/*{T}*/get() {
            return (_hasBeenLocallyOverriden) ? retrieveValue() : _defaultValue;
        }

        private Object/*{T}*/retrieveValue() {
            for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
                if (ctx instanceof LocalContext) {
                    LocalContext localContext = (LocalContext) ctx;
                    Object value = localContext._references.get(this);
                    if (value != null) {
                        return (Object/*{T}*/) value;
                    }
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
        public void set(Object/*{T}*/value) {
            LocalContext ctx = (LocalContext) LocalContext.current();
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
        public Object/*{T}*/getDefault() {
            return _defaultValue;
        }

        /**
         * Returns the local (non-inherited) value for this reference.
         *
         * @return the local value or <code>null</code> if none (value to be 
         *         inherited or not set).
         */
        public Object/*{T}*/getLocal() {
            LocalContext ctx = (LocalContext) LocalContext.current();
            return (ctx != null) ? (Object/*{T}*/) ctx._references.get(this)
                    : _defaultValue;
        }

        /**
         * Sets the default value for this reference.
         *
         * @param  defaultValue the root value.
         */
        public void setDefault(Object/*{T}*/defaultValue) {
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
    }
}