/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import j2me.lang.IllegalStateException;
import j2me.lang.ThreadLocal;
import j2mex.realtime.MemoryArea;

import java.util.EmptyStackException;
import javolution.JavolutionError;

/**
 * <p> This class represents a real-time context; they are typically 
 *     thread-local but they can also be associated to particular objects.</p>
 *     
 * <p> This package provides few predefined contexts:<ul>
 *     <li>{@link LocalContext} - To define locally 
 *          scoped setting held by {@link LocalReference}</li>
 *     <li>{@link PoolContext} - To transparently reuse objects created using
 *          an {@link ObjectFactory} instead of a constructor.</li>
 *     <li>{@link ConcurrentContext} - To take advantage of concurrent 
 *         algorithms on multi-processors systems.</li>
 *     <li>{@link LogContext} - For thread-based or object-based logging
 *         capability.
 *         <i>Note: <code>java.util.logging</code> provides class-based 
 *           logging (based upon class hierarchy).</i></li>
 *     </ul>           
 *     Context-aware applications may extend the context base class or any 
 *     predefined contexts in order to facilitate separation of concern
 *     (e.g. logging, security, performance and so forth).</p>
 *     
 * <p> The scope of a {@link Context} should be surrounded by a <code>try, 
 *     finally</code> block statement to ensure correct behavior in case 
 *     of exceptions being raised. For example:[code]
 *     LocalContext.enter();
 *     try 
 *         Length.showAs(Unit.FOOT); // Thread-Local setting (no impact on other threads)
 *         ... 
 *     } finally {
 *         LocalContext.exit();
 *     }
 *     
 *     public class Calculator {  
 *         private PoolContext _pool = new PoolContext();
 *         public void execute(Runnable logic) {
 *             PoolContext.enter(_pool); 
 *             try {
 *                 logic.run(); // Executes the logic using this calculator pool.
 *             } finally {
 *                 PoolContext.exit(_pool); // Recycles used objects all at once.    
 *             }
 *         }
 *     }[/code]</p>
 *     
 * <p> Finally, context instances can be serialized/deserialized in 
 *     {@link javolution.xml.XmlFormat xml format}. For example, 
 *     applications may want to load pool contexts at start-up to limit
 *     the number of object creation at run-time (and therefore reducing
 *     the worst-case execution time).</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public abstract class Context {

    /**
     * Holds the context for the current thread.
     */
    private static final ThreadLocal CURRENT = new ThreadLocal() {
        protected Object initialValue() {
            Thread thread = Thread.currentThread();
            Context ctx = new HeapContext();
            ctx._owner = thread;
            return ctx;
        }
    };

    /**
     * Holds a shortcut to the pool context or 
     * <code>null</code> if none (heap context). 
     */
    transient PoolContext inheritedPoolContext;

    /**
     * Holds a shortcut to the local context or 
     * <code>null</code> if none (global context). 
     */
    transient LocalContext inheritedLocalContext;

    /**
     * Holds the current thread owner of this context.
     */
    private transient Thread _owner;

    /**
     * Holds the current outer context of this context or <code>null</code>
     * if none (root context).
     */
    private transient Context _outer;

    /**
     * Holds contexts available for reuse.
     * 
     * @see #enter(Class)
     */
    private Context _inner;

    /**
     * Default constructor. 
     */
    protected Context() {
    }

    /**
     * Returns the current context for the current thread. The default context 
     * is a {@link HeapContext} for normal threads and a {@link PoolContext}
     * for {@link ConcurrentThread concurrent threads}.  
     *
     * @return the current context (always different from <code>null</code>).
     */
    public static Context current() {
        return (Context) Context.CURRENT.get();
    }

    /**
     * Returns the current owner of this context. The owner of a
     * context is the thread which entered the context.
     *
     * @return the thread owner of this context.
     */
    public final Thread getOwner() {
        return _owner;
    }

    /**
     * Holds the outer context of this context or <code>null</code>
     * if none (root context).
     *
     * @return the outer context or <code>null</code> if none (root context).
     */
    public final Context getOuter() {
        return _outer;
    }

    /**
     * Clears this context and releases any associated resource. Dead threads
     * contexts are automatically cleared before finalization.
     */
    public void clear() {
        if (_inner != null)
            _inner.clear();
        _inner = null;
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
    public static void enter(Context context) {
        if (context._owner != null)
            throw new IllegalStateException("Context is currently in use");
        Context current = Context.current();
        context._outer = current;
        context._owner = current._owner;
        Context.CURRENT.set(context);
        context.inheritedPoolContext = current.inheritedPoolContext;
        context.inheritedLocalContext = current.inheritedLocalContext;
        context.enterAction();
    }

    /**
     * Exits the specified context. The {@link #getOuter outer} context
     * becomes the current context.
     * 
     * @param context the context being entered.
     * @throws IllegalStateException if this context is not the current context.
     */
    public static void exit(Context context) {
        if (context._owner != Thread.currentThread())
            throw new IllegalStateException(
                    "Context is not used by the current thread");
        if ((context._outer == null) || (context._outer._owner != context._owner))
            throw new EmptyStackException();
        try {
            context.exitAction();
        } finally {
            Context.CURRENT.set(context._outer);
            context._outer = null;
            context._owner = null;
        }
    }

    /**
     * Enters a context of specified class. This method searches the current
     * context for a matching context to reuse (previously used context of 
     * same class); if none found a new instance is created using the specified
     * class no-arg constructor from the same memory area as the current 
     * context.
     *  
     * @param contextClass the type of context to be entered.
     * @return the context being entered.
     */
    protected static Context enter(Class contextClass) {
       Context current = Context.current();
        
        // Searches inner contexts.
        Context outer = current;
        Context context = current._inner;
        while (context != null) {
            if (contextClass.equals(context.getClass())) { // Found one.
                outer._inner = context._inner; // Detaches.
                break;
            }
            outer = context;
            context = context._inner;
        }
        if (context == null) { // None found.
            try {
                context = (Context) MemoryArea.getMemoryArea(outer).newInstance(contextClass);
            } catch (InstantiationException e) {
                throw new JavolutionError(e);
            } catch (IllegalAccessException e) {
                throw new JavolutionError(e);
            }
        }
        // Attaches as inner of current.
        context._inner = current._inner;
        current._inner = context;

        // Sets outer and owner.
        context._outer = current;
        context._owner = current._owner;
        context.inheritedPoolContext = current.inheritedPoolContext;
        context.inheritedLocalContext = current.inheritedLocalContext;

        // Sets as current.
        Context.CURRENT.set(context);
        context.enterAction();
        return context;
    }

    /**
     * Exits the current context which must be of specified type.
     * 
     * @param contextClass the type of context to be exited.
     * @return the context being exited.
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of the specified class. 
     */
    protected static Context exit(Class contextClass) {
        Context current = Context.current();
        if (!contextClass.isInstance(current))
            throw new IllegalStateException(
                    "Current context is not an instance of " + contextClass);
        Context outer = current._outer;
        if ((outer == null) || (outer._owner != current._owner))
            throw new EmptyStackException();
        try {
            current.exitAction();
            return current;
        } finally {
            Context.CURRENT.set(outer);
            current._outer = null;
            current._owner = null;
        }
    }

    /**
     * Sets the current context, used by {@link ConcurrentThread}
     * exclusively.
     */
    static void setCurrent(PoolContext context, ConcurrentContext outer) {
        ((Context)context)._outer = outer;
        ((Context)context)._owner = Thread.currentThread();
        Context.CURRENT.set(context);
        context.inheritedPoolContext = context;
        context.inheritedLocalContext = outer.inheritedLocalContext;
    }

}