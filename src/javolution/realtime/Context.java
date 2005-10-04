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
 *     </ul>           
 *     Context-aware applications may extend the context base class or any 
 *     predefined contexts to address system-wide concerns, such as logging,
 *     security, performance, and so forth.</p>
 *     
 * <p> The scope of a {@link Context} should be surrounded by a <code>try, 
 *     finally</code> block statement to ensure correct behavior in case 
 *     of exceptions being raised. For example:<pre>
 *     
 *     LocalContext.enter();
 *     try 
 *         Length.showAs(Unit.FOOT); // Thread-Local setting (no impact on other threads)
 *         ... 
 *     } finally {
 *         LocalContext.exit();
 *     }
 *     
 *     class Calculator {  
 *         PoolContext _pool = new PoolContext();
 *         void execute(Runnable logic) {
 *             _pool.push(); 
 *             try {
 *                 logic.run(); // Executes the logic using this calculator objects.
 *             } finally {
 *                 _pool.pull(); // Recycles used objects all at once.    
 *             }
 *         }
 *     }</pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, September 25, 2005
 */
public abstract class Context {

    /**
     * Holds the context for the current thread.
     */
    private static final ThreadLocal CURRENT = new ThreadLocal() {
        protected Object initialValue() {
            Thread thread = Thread.currentThread();
            Context ctx = (thread instanceof ConcurrentThread) ? 
                    (Context) new PoolContext()
                    : new HeapContext();
            ctx._owner = thread;
            return ctx;
        }
    };

    /**
     * Holds a shortcut to the pool context or 
     * <code>null</code> if none (heap context). 
     */
    PoolContext inheritedPoolContext;

    /**
     * Holds a shortcut to the local context or 
     * <code>null</code> if none (global context). 
     */
    LocalContext inheritedLocalContext;

    /**
     * Holds the current thread owner of this context.
     */
    private Thread _owner;

    /**
     * Holds the current outer context of this context or <code>null</code>
     * if none (root context).
     */
    private Context _outer;

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
     * Returns the context for the current thread. The default context 
     * is a {@link HeapContext} for normal threads and a {@link PoolContext}
     * for {@link ConcurrentThread concurrent threads}.  
     *
     * @return the current context (always different from <code>null</code>).
     */
    public static Context currentContext() {
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
     * Pushes this context as the current context.
     * 
     * @throws IllegalStateException if this context is currently in use.
     */
    public final void push() {
        if (_owner != null)
            throw new IllegalStateException("Context is currently in use");
        Context current = Context.currentContext();
        _outer = current;
        _owner = current._owner;
        Context.CURRENT.set(this);
        inheritedPoolContext = current.inheritedPoolContext;
        inheritedLocalContext = current.inheritedLocalContext;
        enterAction();
    }

    /**
     * Pulls this context out, the context prior from entering this context
     * becomes the current context for the current thread.
     * 
     * @throws IllegalStateException if this context is not used by the 
     *         current thread.
     */
    public final void pull() {
        if (_owner != Thread.currentThread())
            throw new IllegalStateException(
                    "Context is not used by the current thread");
        if ((_outer == null) || (_outer._owner != _owner))
            throw new EmptyStackException();
        try {
            exitAction();
        } finally {
            Context.CURRENT.set(_outer);
            _outer = null;
            _owner = null;
        }
    }

    /**
     * Enters a context of specified class. This method searches the current
     * context for a matching context to reuse; if none found a new instance
     * is created using the specified class no-arg constructor.
     *  
     * @param contextClass the type of context to be entered.
     */
    protected static void enter(Class contextClass) {
        Context current = Context.currentContext();
        Context context = current._inner;
        if (!contextClass.isInstance(context)) { // Search inner stack.
            while (context != null) {
                Context outer = context;
                context = context._inner;
                if (contextClass.isInstance(context)) { // Found one.
                    outer._inner = context._inner; // Detaches.
                    break;
                }
            }
            if (context == null) { // None found.
                try {
                    context = (Context) contextClass.newInstance();
                } catch (InstantiationException e) {
                    throw new JavolutionError(e);
                } catch (IllegalAccessException e) {
                    throw new JavolutionError(e);
                }
            }
            // Attaches as inner of current.
            context._inner = current._inner;
            current._inner = context;
        }
        context._outer = current;
        context._owner = current._owner;
        context.inheritedPoolContext = current.inheritedPoolContext;
        context.inheritedLocalContext = current.inheritedLocalContext;
        Context.CURRENT.set(context);
        context.enterAction();
    }

    /**
     * Exits the current context which must be of specified type.
     * 
     * @param contextClass the type of context to be exited.
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of the specified class. 
     */
    protected static void exit(Class contextClass) {
        Context current = Context.currentContext();
        if (!contextClass.isInstance(current))
            throw new IllegalStateException(
                    "Current context is not an instance of " + contextClass);
        Context outer = current._outer;
        if ((outer == null) || (outer._owner != current._owner))
            throw new EmptyStackException();
        try {
            current.exitAction();
        } finally {
            Context.CURRENT.set(outer);
            current._outer = null;
            current._owner = null;
        }
    }

    /**
     * Sets the outer of this context, used by {@link ConcurrentThread}
     * exclusively.
     * 
     * @param outer the new outer context.
     */
    final void setOuter(Context outer) {
        _outer = outer;
    }
}