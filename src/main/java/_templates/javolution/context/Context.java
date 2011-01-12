/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.context;

import _templates.java.lang.IllegalStateException;
import _templates.java.lang.ThreadLocal;
import _templates.java.lang.UnsupportedOperationException;
import _templates.javolution.xml.XMLSerializable;

/**
 * <p> This class represents an execution context; they can be associated to 
 *     particular threads or objects.</p>
 *     
 * <p> Context-aware applications may extend the context base class or any 
 *     predefined contexts in order to facilitate <a 
 *     href="package-summary.html#package_description">
 *     separation of concerns</a>.</p>
 *     
 * <p> The scope of a {@link Context} should be surrounded by a <code>try, 
 *     finally</code> block statement to ensure correct behavior in case 
 *     of exceptions being raised. For example:[code]
 *     LocalContext.enter(); // Current thread enter a local context.
 *     try 
 *         ModuloInteger.setModulus(m); // No impact on other threads!
 *         z = x.times(y); // Multiplication modulo m.
 *     } finally {
 *         LocalContext.exit();
 *     }[/code]</p>
 *
 * <p> Context objects can be inherited by multiple threads (see
 *     {@link ConcurrentContext}}, but only one thread may
 *     {@link #enter(_templates.javolution.context.Context) enter} a particular context 
 *     instance at any given time (and becomes its {@link #getOwner() owner}.
 *     When the owner thread exits its context, the
 *     context is automatically {@link ObjectFactory#recycle(java.lang.Object)
 *     recycled}. Consequently,  whether or not context objects are reused
 *     between multiple threads depends upon the {@link AllocatorContext} policy
 *     with regards to recycling. Threads executing in a {@link PoolContext}
 *     for example, will reuse the same pool of context objects.</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, April 15, 2007
 */
public abstract class Context implements XMLSerializable {

    /**
     * Holds the root context. It is the current context for all newly created
     * threads.
     */
    public static final Context ROOT = new Root();
    /**
     * Holds the current context (thread-local).
     */
    private static final ThreadLocal CURRENT = new ThreadLocal() {

        protected Object initialValue() {
            return ROOT;
        }
    };
    /**
     * Holds the current owner of this context or <code>null</code> if global 
     * context.
     */
    private Thread _owner;
    /**
     * Holds the outer context or <code>null</code> if none (root context).
     */
    private Context _outer;
    /**
     * Holds the factory having produced this context if any (for recycling 
     * purpose upon exit).
     */
    private ObjectFactory _factory;
    /**
     * Holds the inherited allocator context or <code>null</code>
     */
    private AllocatorContext _allocator;

    /**
     * Default constructor. 
     */
    protected Context() {
    }

    /**
     * Returns the current context for the current thread. 
     *
     * @return the current context.
     */
    public static Context getCurrentContext() {
        return (Context) Context.CURRENT.get();
    }

    /**
     * Returns the current owner of this context. The owner of a
     * context is the thread which {@link #enter(Context) entered}
     * the context and has not yet {@link #exit(Context) exited}.
     * A context can only have one owner at any given time, although
     * contexts can be shared by {@link ConcurrentContext concurrent} threads.
     *
     * @return the thread owner of this context or <code>null</code>.
     */
    public final Thread getOwner() {
        return _owner;
    }

    /**
     * Returns the outer context of this context or <code>null</code>
     * if {@link #ROOT} or a default context (not entered).
     *
     * @return the outer context or <code>null</code>.
     */
    public final Context getOuter() {
        return _outer;
    }

    /**
     * Returns the string representation of this context (default 
     * <code>"Instance of " + this.getClass().getName()</code>).
     * 
     * @return the string representation of this context.
     */
    public String toString() {
        return "Instance of " + this.getClass().getName();
    }

    /**
     * The action to be performed after this context becomes the current 
     * context.
     */
    protected abstract void enterAction();

    /**
     * The action to be performed before this context is no more the current 
     * context.
     */
    protected abstract void exitAction();

    /**
     * Enters the specified context.
     *
     * @param context the context being entered.
     * @throws IllegalStateException if this context is currently in use.
     */
    public static final void enter(Context context) {
        if (context._owner != null)
            throw new IllegalStateException("Context is currently in use");
        Context current = Context.getCurrentContext();
        context._outer = current;
        context._owner = Thread.currentThread();
        context._allocator = context instanceof AllocatorContext ? (AllocatorContext) context : current._allocator;
        Context.CURRENT.set(context);
        context.enterAction();
    }
 
    /**
     * Exits the specified context.
     *
     * @param context the context being exited.
     * @throws IllegalStateException if the specified context is not the current context.
     */
    public static final void exit(Context context) {
        if (Context.getCurrentContext() != context)
            throw new IllegalArgumentException("The specified context is not the current context");
        Context.exit(context.getClass());
    }

    /**
     * Enters a context of specified type, this context is
     * {@link ObjectFactory factory} produced and automatically recycled
     * upon {@link #exit exit}. If the specified contextType has no public
     * no-arg constructor accessible, then the object factory for the class
     * should be {@link ObjectFactory#setInstance explicitely set} (typically
     * in a static initializer).  
     *
     * @param contextType the type of context being entered.
     * @see ObjectFactory#getInstance(Class)
     */
    public static final void enter(Class/*<? extends Context>*/ contextType) {
        ObjectFactory factory = ObjectFactory.getInstance(contextType);
        Context context = (Context) factory.object();
        context._factory = factory;
        Context.enter(context);
    }

    /**
     * Exits the current context (the {@link #getOuter outer} context
     * becomes the current context).
     * 
     * @param contextType the type of context being entered.
     * @throws IllegalStateException if this context is the {@link #ROOT} 
     *         context or the current thread is not the context owner.
     */
    public static void exit(Class/*<? extends Context>*/ contextType) {
        Context context = Context.getCurrentContext();
        Context outer = context._outer;
        if (outer == null)
            throw new IllegalStateException("Cannot exit root context");
        if (context._owner != Thread.currentThread())
            throw new IllegalStateException("The current thread is not the context owner");
        if (!contextType.isInstance(context))
            throw new ClassCastException("Current context is an instance of " + context.getClass().getName());
        try {
            context.exitAction();
        } finally {
            Context.CURRENT.set(outer);
            context._outer = null;
            context._owner = null;
            context._allocator = null;
            if (context._factory != null) { // Factory produced.
                context._factory.recycle(context);
                context._factory = null;
            }
        }
    }

    /**
     * Sets the current context, used by {@link ConcurrentContext}
     * exclusively.
     * 
     * @param context the concurrent context.
     */
    protected static void setConcurrentContext(ConcurrentContext context) {
        Context.CURRENT.set(context);
    }

    /**
     * Returns the allocator context used while in this context (shortcut).
     * 
     * @return the allocator context for this context.
     */
    final AllocatorContext getAllocatorContext() {
        return (_allocator == null) ? AllocatorContext.getDefault() : _allocator;
    }

    // Holds the root context definition.
    private static final class Root extends Context {

        protected void enterAction() {
            throw new UnsupportedOperationException(
                    "Cannot enter the root context");
        }

        protected void exitAction() {
            throw new UnsupportedOperationException(
                    "Cannot enter the root context");
        }
    };

 
}