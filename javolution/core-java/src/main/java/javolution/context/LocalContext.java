/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.internal.context.LocalContextImpl;
import javolution.internal.osgi.JavolutionActivator;

/**
 * <p> This class represents a context to override locally scoped environment
 *     settings. The settings are typically  
 *     {@link LocalParameter} ({@link Configurable}) static fields.
 *     [code]
 *     public static LocalParameter<LargeInteger> MODULO = new LocalParameter<LargeInteger>() { ... }; 
 *     ...
 *     LocalContext.enter(); 
 *     try {
 *         LocalContext.override(ModuloInteger.MODULO, m); // No impact on other threads!
 *         z = x.times(y); // Multiplication modulo m.
 *     } finally {
 *         LocalContext.exit(); // Reverts changes. 
 *     }
 *     }[/code]</p>   
 *     
 * <p> As for any context, local context settings are inherited during 
 *     {@link ConcurrentContext concurrent} executions.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class LocalContext extends AbstractContext {


    /**
     * Defines the factory service producing {@link LocalContext} implementations.
     */
    public interface Factory {

        /**
         * Returns a new instance of the heap context.
         */
        LocalContext newLocalContext();
    }

    /**
     * Default constructor.
     */
    protected LocalContext() {
    }

    /**
     * Enters a new local context instance.
     */
    public static void enter() {
        LocalContext.Factory factory = JavolutionActivator.getLocalContextFactory();
        LocalContext ctx = (factory != null) ? factory.newLocalContext()
                : new LocalContextImpl();
        ctx.enterScope();
    }

    /**
     * Exits the current local context.
     *
     * @throws ClassCastException if the current context is not a local context.
     */
    public static void exit() {
        ((LocalContext) AbstractContext.current()).exitScope();
    }
    
    /**
     * Returns the local value of the specified local parameter (or its default
     * value if not executing within the scope of a local context)
     * 
     * @param  param the local parameter whose current value is returned.
     * @throws IllegalStateException if there is no outer concurrent context.
     */
    public static <T> T valueOf(LocalParameter<T> param) {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx == null)
            return param.getDefault();
        return ctx.getValueOf(param);        
    }
    
    /**
     * Returns the current value of the specified local parameter (or its default
     * value if not executing within the scope of a local context)
     * 
     * @param  param the local parameter whose current value is returned.
     * @throws IllegalStateException if there is no outer local context.
     */
    public static <T> void override(LocalParameter<T> param, T localValue) {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        if (ctx == null)
            return param.getDefault();
        return ctx.getValueOf(param);        
    }
        
}