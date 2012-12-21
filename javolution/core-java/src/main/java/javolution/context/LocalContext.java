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
import javolution.text.TypeFormat;

/**
 * <p> This class represents the current context holding {@link LocalParameter 
 *     local parameters} values.
 *     [code]
 *     public static LocalParameter<LargeInteger> MODULO = new LocalParameter<LargeInteger>() { ... }; 
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
     * This parameter cannot be locally overriden.
     */
    public static final LocalParameter<Boolean> WAIT_FOR_SERVICE = new LocalParameter(false) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseBoolean(configuration));
        }

        @Override
        public void checkOverridePermission() throws SecurityException {
            throw new SecurityException(this + " cannot be overriden");
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
                WAIT_FOR_SERVICE.getDefault()).inner().enterScope();
    }
    
    /**
     * Returns the local value of the specified local parameter (its default
     * value if it is not {@link LocalContext#override overriden}).
     * 
     * @param  param the local parameter whose local value is returned.
     */
    public static <T> T valueOf(LocalParameter<T> param) {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        return (ctx != null) ? ctx.getValueOf(param) : param.getDefault();
    }
    
    /**
     * Overrides the local value of the specified local parameter. 
     * 
     * @param  param the local parameter whose local value is overriden.
     * @param  localValue the new local value.
     * @throws SecurityException if <code>param.checkOverridePermission()</code>
     *         raises a security exception.
     */
    public abstract <T> void override(LocalParameter<T> param, T localValue) throws SecurityException;
    

    /**
     * For this context, returns the local value of the specified parameter 
     * or its default value if not {@link LocalContext#override overriden}. 
     * 
     * @param  param the local parameter whose current value is returned.
     */
    protected abstract <T> T getValueOf(LocalParameter<T> param);
                  
}