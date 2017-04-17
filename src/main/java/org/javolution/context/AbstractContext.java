/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

import org.javolution.annotations.Realtime;

/**
 * The parent class for all contexts. Contexts allow for cross cutting concerns (performance, logging, security, ...) 
 * to be addressed at run-time through OSGi published services without polluting the application code 
 * ([Separation of Concerns]).</p>
 *     
 * Context configuration is performed in a `try, finally` block statement and impacts only the current thread and 
 * inner {@link ConcurrentContext concurrent} threads.
 *  
 * ```java
 * LogContext ctx = LogContext.enter(); // Enters a context scope. 
 * try {                             
 *     ctx.setLevel(Level.WARNING); // Local configuration (optional).
 *     ... // Current thread executes using the configured context.
 * } finally {
 *     ctx.exit(); // Reverts to previous settings.
 * }
 * ```
 * 
 * [Separation of Concerns]: "http://en.wikipedia.org/wiki/Separation_of_concerns
 *      
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, March 31, 2017
 */
@Realtime
public abstract class AbstractContext {

    /**
     * Holds the last context entered (thread-local).
     */
    private static final ThreadLocal<AbstractContext> CURRENT = new ThreadLocal<AbstractContext>();

    /**
     * Holds the outer context or {@code null} if none (top context).
     */
    private AbstractContext outer;

    /**
     * Default constructor. 
     */
    protected AbstractContext() {
    }

    /**
     * Returns the current context for the current thread or `null` if this thread has no context (default).
     */
    public static AbstractContext current() {
        return AbstractContext.CURRENT.get();
    }

    /**
     * Returns the current context of specified type.
     * 
     * @param type the type of context to search for.
     * @return the current context of specified type or `null` if none. 
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AbstractContext> T current(Class<T> type) {
        AbstractContext ctx = AbstractContext.CURRENT.get();
        while (ctx != null) {
            if (type.isInstance(ctx))
                return (T) ctx;
            ctx = ctx.outer;
        }
        return null;
    }

    /**
     * Inherits the specified context which becomes the context of the current thread. 
     * This method is particularly useful when creating new threads to make them inherits 
     * from the context stack of the parent thread.
     * 
     * ```java
     * //Spawns a new thread inheriting the context of the current thread.
     * MyThread myThread = new MyThread();
     * myThread.inherited = AbstractContext.current(); 
     * myThread.start(); 
     * ...
     * class MyThread extends Thread {
     *     AbstractContext inherited;
     *     public void run() {
     *         AbstractContext.inherit(inherited); // Sets current context. 
     *         ...
     *     }
     * }
     * ```
     * @param ctx the context to inherit
     */
    public static void inherit(AbstractContext ctx) {
        CURRENT.set(ctx);
    }

    /**
     * Enters the scope of an inner context which becomes the current context; the previous current context becomes 
     * the outer of this context.
     *  
     * @return the inner context entered.
     */
    protected AbstractContext enterInner() {
        AbstractContext inner = inner();
        inner.outer = AbstractContext.CURRENT.get();
        AbstractContext.CURRENT.set(inner);
        return inner;
    }

    /**
     * Exits the scope of this context; the outer of this context becomes the current context.
     * 
     * @throws IllegalStateException if this context is not the current context.
     */
    public void exit() {
        if (this != AbstractContext.CURRENT.get())
            throw new IllegalStateException("This context is not the current context");
        AbstractContext.CURRENT.set(outer);
        outer = null;
    }

    /**
     * Returns the outer context of this context.
     * 
     * @return outer context or {@code null} if this context has no outer context.
     */
    protected AbstractContext getOuter() {
        return outer;
    }

    /**
     * Returns the outer context of this context of specified type.
     * 
     * @param type the class of the outer context to return.
     * @return outer context or {@code null} if this context has no outer context.
     */
    @SuppressWarnings("unchecked")
    protected <T extends AbstractContext> T getOuter(Class<T> type) {
        AbstractContext ctx = outer;
        while (ctx != null) {
            if (type.isInstance(ctx))
                return (T) ctx;
            ctx = ctx.outer;
        }
        return null;
    }

    /**
     * Returns a new inner instance of this context inheriting the properties of this context. 
     * The new instance can be configured independently from its parent.
     *  
     * @return the inner context.
     */
    protected abstract AbstractContext inner();

}