/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import java.util.EmptyStackException;
import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.FastMap;

/**
 * <p> This class represents a real-time context (thread-based).
 *     Applications do not have direct access to instances of this class.
 *     {@link Context} methods are always static and typically affect
 *     the first outer context of appropriate type (as defined by the method's
 *     class). In some cases (e.g. <code>LogContext</code> below), context
 *     static methods may affect more than one context instance.</p>
 *
 * <p> The scope of a {@link Context} is defined by a <code>try, finally</code>
 *     block statement which starts with a static <code>enter</code> call and
 *     ends with a static <code>exit</code> call. For example:<pre>
 *     LocalContext.enter();
 *     try { // Current thread executes in a local context.
 *         ... 
 *     } finally {
 *         LocalContext.exit();
 *     }</pre>
 *     The class of the enter/exit methods identifies the context type.</p>
 *
 * <p> Because contexts are thread local, entering/exiting a context is 
 *     fast and does not involve any form of synchronization. Nonetheless,
 *     upon thread termination, it is recommended to {@link #clear} 
 *     the top-level context to ensure that all the associated resources 
 *     are immediately released. For example:<pre>
 *          public class MyThread extends Thread {
 *              public void run() {
 *                  try {
 *                      ...
 *                  } finally {  
 *                      Context.clear(); // Finalizes all contexts associated 
 *                  }                    // to this thread.
 *              } 
 *          }</pre></p> 
 *
 * <p> Context-aware applications may extend this base class to address
 *     system-wide concerns, such as logging, security, performance,
 *     and so forth. For example:<pre>
 *     public class LogContext extends Context {
 *         public static enter(Logger logger) { ... }
 *         public static void log(String msg) { ... }
 *         public static void exit() { ... }
 *         ...
 *     }
 *     ...
 *     LogContext.enter(logger_A);
 *     try {
 *         foo(); // Logs foo with logger_A
 *         LogContext.enter(logger_B);
 *         try {
 *             foo(); // Logs foo with logger_A and logger_B
 *         } finally {
 *             LogContext.exit();
 *         }
 *     } finally (
 *         LogContext.exit();
 *     }
 *     ...
 *     void foo() {
 *          LogContext.log("blah, blah, blah");
 *     }
 *     </pre></p>
 *
 * <p> Note: Javolution context programming is somewhat complementary to <a href=
 *    "http://www.javaworld.com/javaworld/jw-01-2002/jw-0118-aspect.html">
 *     aspect-oriented programming</a>. Whereas context programming is dynamic
 *     by nature (thread based); AOP is typically code based (ref.
 *     <a href="http://eclipse.org/aspectj/">AspectJ</a> tool/compiler).
 *     Both can be used in conjunction to insert custom context code
 *     automatically.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class Context {

    /**
     * Holds the current contexts (thread to context mapping).
     * The whole map is replaced when structurally changed 
     * (to avoid synchronizing).
     */
    private static FastMap ThreadToContext = new FastMap(1);

    /**
     * Holds the last context set.
     */
    private static Context Local = new HeapContext();
    static {
        Context.ThreadToContext.put(Context.Local._owner, Context.Local);
    }

    /**
     * Holds the thread owner of this context.
     */
    private Thread _owner;

    /**
     * Holds a shortcut to the pool context for this context or 
     * <code>null</code> if none (heap context). 
     */
    private PoolContext _poolContext;

    /**
     * Holds the outer context of this context or <code>null</code>
     * if none (root context).
     */
    private Context _outer;

    /**
     * Holds the last inner context used by the thread owner of this context.
     */
    private Context _inner;

    /**
     * Default constructor. 
     * This constructor should be called from the thread owner of the context.
     */
    protected Context() {
        _owner = Thread.currentThread();
        if (this instanceof PoolContext) {
            _poolContext = (PoolContext) this;
        }
    }

    /**
     * Clears the current contexts and all its inner contexts. This method 
     * call {@link #dispose} on this context and its inner contexts. 
     */
    public static void clear() {
        Context current = Context.currentContext();
        if (current._outer == null) { // Root context is being cleared.
            synchronized (Context.class) { // Remove thread mapping.
                FastMap tmp = new FastMap(Context.ThreadToContext.size());
                for (Iterator i = ((FastCollection) Context.ThreadToContext
                        .entrySet()).fastIterator(); i.hasNext();) {
                    FastMap.Entry entry = (FastMap.Entry) i.next();
                    Thread thread = (Thread) entry.getKey();
                    if (thread.isAlive() && (thread != current._owner)) {
                        tmp.put(thread, entry.getValue());// Do not copy if not alive.
                    }
                }
                Context.ThreadToContext = tmp;
            }
        } else if (current._outer._inner == current) {
            current._outer._inner = null;
        }

        for (Context ctx = current; ctx != null; ctx = ctx._inner) {
            ctx.dispose();
            ctx._owner = null;
        }
    }

    /**
     * Returns the context for the current thread. The default context 
     * is a {@link HeapContext} for normal threads and a {@link PoolContext}
     * for {@link ConcurrentThread concurrent threads}.  
     *
     * @return the current context (always different from <code>null</code>).
     */
    protected static Context currentContext() {
        Context local = Context.Local;
        return (local._owner == Thread.currentThread()) ? local : getCurrent();
    }

    /**
     * Gets the current context.  
     *
     * @return the current context.
     */
    private static Context getCurrent() {
        Thread thread = Thread.currentThread();
        Object obj = Context.ThreadToContext.get(thread);
        if (obj != null) {
            return (Context) obj;
        }
        // First association (root context).
        if (thread instanceof ConcurrentThread) {
            PoolContext ctx = new PoolContext();
            setCurrent(ctx);
            return ctx;
        } else {
            HeapContext ctx = new HeapContext();
            setCurrent(ctx);
            return ctx;
        }
    }

    /**
     * Sets the current context.  
     *
     * @param ctx the new current context.
     */
    private static void setCurrent(Context ctx) {
        if (Context.ThreadToContext.containsKey(ctx._owner)) {
            Context.ThreadToContext.put(ctx._owner, ctx);
        } else { // Structural modification.
            synchronized (Context.class) {
                FastMap tmp = new FastMap(Context.ThreadToContext.size());
                for (Iterator i = ((FastCollection) Context.ThreadToContext
                        .entrySet()).fastIterator(); i.hasNext();) {
                    FastMap.Entry entry = (FastMap.Entry) i.next();
                    Thread thread = (Thread) entry.getKey();
                    if (thread.isAlive()) { // Do not copy if not alive.
                        tmp.put(thread, entry.getValue());
                    }
                }
                // Add new mapping.
                tmp.put(ctx._owner, ctx);
                Context.ThreadToContext = tmp;
            }
        }
        Context.Local = ctx;
    }

    /**
     * Returns the thread owner of this {@link Context}. The owner of a
     * context is the thread which entered the context.
     * The owner of the {@link #currentContext current} context is always the
     * current thread but the owner of the current outer context might
     * be another thread due to concurrency.
     *
     * @return the thread owner of this context.
     */
    protected final Thread getOwner() {
        return _owner;
    }

    /**
     * Holds the outer context of this {@link Context} or <code>null</code>
     * if none (root context).
     *
     * @return the outer context or <code>null</code> if none (root context).
     */
    protected final Context getOuter() {
        return _outer;
    }

    /**
     * Returns the last inner context used by the owner of this context.
     * This method allows for traversing of the contexts which have 
     * been used by this context's owner.
     *
     * @return the outer context or <code>null</code> if none (deepest context).
     */
    protected final Context getInner() {
        return _inner;
    }

    /**
     * Pushes a context of specified type as the current context.
     * This method tries to reuse contexts from the context stack.
     *
     * @param contextClass the class of the context to be pushed.
     * @return the context pushed or <code>null</code> if none found.
     */
    protected static Context push(Class contextClass) {
        Context current = Context.currentContext();
        Context ctx = current._inner;
        if (contextClass.isInstance(ctx)) {
            // All fields members are correctly set.
            setCurrent(ctx);
            return ctx;
        }
        // Searches inner stack.
        while (ctx != null) {
            ctx = ctx._inner;
            if (contextClass.isInstance(ctx)) { // Found one.
                // Detaches.
                Context next = ctx._inner;
                ctx._outer._inner = next;
                if (next != null) {
                    next._outer = ctx._outer;
                }
                // Reuses.
                push(ctx);
                return ctx;
            }
        }
        return null;
    }

    /**
     * Pushes the specified context as the current context. The previous
     * context becomes the {@link #getOuter} of the specified context.
     *
     * @param  ctx the new current context.
     */
    protected static void push(Context ctx) {
        ctx._outer = Context.currentContext();
        ctx._inner = ctx._outer._inner;
        ctx._outer._inner = ctx;
        if (ctx._inner != null) {
            ctx._inner._outer = ctx;
        }
        setCurrent(ctx);
        if (!(ctx instanceof PoolContext) && !(ctx instanceof HeapContext)) {
            // Inherits pool context.
            ctx._poolContext = ctx._outer._poolContext;
        }
    }

    /**
     * Pops the current context from the context stack. The {@link #getOuter}
     * of the current context becomes the current context.
     *
     * @return the previous {@link #currentContext}.
     * @throws EmptyStackException if the current thread is not the owner
     *         of the current context.
     * @see    #getOwner
     */
    protected static Context pop() {
        Context ctx = Context.currentContext();
        if ((ctx._outer != null)
                && (ctx._outer._owner == Thread.currentThread())) {
            setCurrent(ctx._outer);
            return ctx;
        } else {
            throw new EmptyStackException();
        }
    }

    /**
     * Disposes of this context resources. This method is called by the 
     * {@link #clear} method.
     */
    protected abstract void dispose();

    /**
     * Returns the pool context for this context.
     *
     * @return the pool context for this context or <code>null</code> if 
     *         none (heap context).
     */
    final PoolContext poolContext() {
        return _poolContext;
    }

    /**
     * Sets the outer of this context (used by concurrent threads only).
     * 
     * @param outer the new outer context.
     */
    final void setOuter(Context outer) {
        _outer = outer;
    }
}