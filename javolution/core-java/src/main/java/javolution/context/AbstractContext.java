/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Parallelizable;
import javolution.lang.Permission;
import javolution.lang.RealTime;

/**
 * <p> The parent class for all contexts. 
 *     Contexts allow for cross cutting concerns (performance, logging, 
 *     security, ...) to be addressed at run-time without polluting the
 *     application code (<a href="http://en.wikipedia.org/wiki/Separation_of_concerns">
 *     Separation of Concerns</a>). The context implementation/behavior is typically 
 *     provided at high-level (OSGi service implementation) and impacts code execution
 *     everywhere. With contexts, you <i>Think Locally, Act Globally!</i></p>
 *     
 * <p> Context configuration is performed sequentially in a {@code try, finally}
 *     block statement and is visible only to the current thread and 
 *     potentially to the concurrent threads of an inner {@link ConcurrentContext} scope.</p> 
 *     [code]
 *     AnyContext ctx = AnyContext.enter(); // Enters a context scope. 
 *     try {                             
 *         ctx.configure(...); // Local configuration (optional).
 *         ... // Thread executes using the configured context.
 *     } finally {
 *         ctx.exit();
 *     }[/code]
 * 
 * <p> When running OSGi, the context implementation is retrieved from published
 *     services (if any). To avoid
 *     <a href="http://wiki.osgi.org/wiki/Avoid_Start_Order_Dependencies">
 *     start order dependencies</a> contexts methods can be configured to block
 *     until an implementation is published. For example, the java option  
 *     <code>-Djavolution.context.SecurityContext#WAIT_FOR_SERVICE=true</code>
 *     causes the security checks to block until a {@link SecurityContext}
 *     implementation is published.</p>
 * 
 * <p> Instead of using dynamic OSGi context implementations, applications may 
 *     also use custom (static) implementations.</p>
 *     [code]
 *     MyContext ctx = AbstractContext.enter(MyContextImpl.class); // Enters custom instance.
 *     try { 
 *         ... // Execution in the scope of MyContextImpl
 *     } finally {
 *         ctx.exit();
 *     }[/code]
 * 
 * <p> Contexts do not pause thread-safety issues (they are {@link Parallelizable 
 *     parallelizable}). They can be inherited by multiple threads but only 
 *     one thread (the entering thread) configure them.</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@RealTime
@Parallelizable(comment="Sequential configuration, parallel use")
public abstract class AbstractContext {

    /**
     * Holds the root context (common instance for all threads).
     */
    private static final AbstractContext ROOT = new AbstractContext() {

        @Override
        protected AbstractContext inner() {
            return this; // Inner of root is itself.
        }};

    /**
     * Holds the last context entered (thread-local).
     */
    private static final ThreadLocal<AbstractContext> CURRENT 
        = new ThreadLocal<AbstractContext>() {
        @Override
        protected AbstractContext initialValue() {
            return ROOT;
        }  
    };

    /**
     * Holds the outer context or <code>null</code> if none (root or not attached).
     */
    private AbstractContext outer;

    /**
     * Default constructor. 
     */
    protected AbstractContext() {}
 
    /**
     * Returns the current context for the current thread or <code>null</code>
     * if this thread has no context (default).
     */
    public static AbstractContext current() {
        return AbstractContext.CURRENT.get();
    }

    /**
     * Returns the current context of specified type or <code>null</code> if none. 
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AbstractContext> T current(Class<T> type) {
        AbstractContext ctx = AbstractContext.CURRENT.get();
        while (true) {
            if (ctx == null)
                return null;
            if (type.isInstance(ctx))
                return (T) ctx;
            ctx = ctx.outer;
        }
    }

    /**
     * <p> Enters the scope of a custom context. This method raises a 
     *    {@link SecurityException} if the permission to enter contexts of 
     *     the specified class is not granted. For example, the following
     *     disallow entering any custom context.</p>
     *[code]
     * SecurityContext ctx = SecurityContext.enter(); 
     * try {
     *     ctx.revoke(new Permission(AbstractContext.class, "enter"));
     *     ... // Cannot enter any custom context.
     * } finally {
     *     ctx.exit(); // Back to previous security settings. 
     * }[/code]     
     *  
     * @param  custom the custom context to enter.
     * @throws IllegalArgumentException if the specified class default constructor
     *         cannot be instantiated.
     * @throws SecurityException if {@code Permission(custom, "enter")} is not granted. 
     * @see    Permission
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractContext> T enter(Class<T> custom) {
        SecurityContext.check(new Permission<T>(custom, "enter"));
        try {
            return (T) custom.newInstance().enterInner();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                    "Cannot instantiate instance of " + custom, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Cannot access " + custom, e);
        }
    }

    /**
     * Inherits the specified context which becomes the context of the current
     * thread. This method is particularly useful when creating new threads to 
     * make them inherits from the context stack of the spawning thread.</p>
     * [code]
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
     * }[/code]
     */
     public static void inherit(AbstractContext ctx) {
         CURRENT.set(ctx);
     }

    /**
     * Enters the scope of an inner context which becomes the current context; 
     * the previous current context becomes the outer of this context. 
     * @see #inner
     */
    protected AbstractContext enterInner() {
        AbstractContext inner = inner();
        inner.outer = AbstractContext.CURRENT.get();
        AbstractContext.CURRENT.set(inner);
        return this;
    }

    /**
     * Exits the scope of this context; the outer of this context becomes  
     * the current context.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    public void exit() {
        if (this != AbstractContext.CURRENT.get())
            throw new IllegalStateException(
                    "This context is not the current context");
        AbstractContext.CURRENT.set(outer);
        outer = null;
    }

    /**
     * Returns the outer context of this context or <code>null</code> if this 
     * context has no outer context (top context).
     */
    protected AbstractContext getOuter() {
        return outer;
    }

    /**
     * Returns a new inner instance of this context inheriting the properties 
     * of this context. The new instance can be configured independently 
     * from its parent. 
     */
    protected abstract AbstractContext inner();

}