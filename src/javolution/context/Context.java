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
import j2me.lang.IllegalStateException;
import j2me.lang.ThreadLocal;

/**
 * <p> This class represents an execution context; they can be associated to 
 *     particular threads or objects.</p>
 *     
 * <p> This package provides few predefined contexts:<ul>
 *     <li>{@link LocalContext} - To define locally 
 *          scoped setting held by {@link LocalContext.Reference}</li>
 *     <li>{@link PoolContext} - To transparently reuse objects created using
 *          an {@link ObjectFactory} instead of a constructor.</li>
 *     <li>{@link ConcurrentContext} - To take advantage of concurrent 
 *         algorithms on multi-processors systems.</li>
 *     <li>{@link LogContext} - For thread-based or object-based logging
 *         capability.
 *         <i>Note: <code>java.util.logging</code> provides class-based 
 *           logging (based upon class hierarchy).</i></li>
 *     <li>{@link PersistentContext} - To achieve persistency accross 
 *          multiple program execution.</li>
 *     </ul>           
 *     Context-aware applications may extend the context base class or any 
 *     predefined contexts in order to facilitate separation of concern
 *     (e.g. logging, security, performance and so forth).</p>
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
 *     }
 *     
 *     public class Calculator { // Sandbox calculator which does not generate garbage! 
 *         private static final PoolContext _pool = new PoolContext(); // Pool for temporary objects.
 *         public Complex multiply(Vector<Complex> left, Vector<Complex> right) {
 *             PoolContext.enter(_pool); // Ensures that the current thread uses the calculator pool.
 *             try {
 *                 return left.times(right).export(); // Result is exported (out of the pool).
 *             } finally {
 *                 PoolContext.exit(_pool); // Recycles all temporary objects (stack reset).    
 *             }
 *         }
 *     }[/code]</p>
 *     
 * <p> Finally, context instances can be serialized/deserialized in 
 *     {@link javolution.xml.XMLFormat XMLFormat}. For example, 
 *     applications may want to load pool contexts at start-up to limit
 *     the number of object creation at run-time (and therefore reduce
 *     the worst-case execution time).</p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
public abstract class Context extends RealtimeObject implements Serializable {

    /**
     * Holds the root context (top context of all threads).
     */
    public static final HeapContext ROOT = new HeapContext();
    
    /**
     * Holds the current context (thread-local).
     */
    private static final ThreadLocal CURRENT = new ThreadLocal() {
        protected Object initialValue() {
            return ROOT;
        }
    };
   
    /**
     * Holds the current owner of this context or <code>null</code>.
     */
    transient Thread _owner;

    /**
     * Holds the outer context or <code>null</code> if none (root context).
     */
    transient Context _outer;

    /**
     * Default constructor. 
     */
    protected Context() {
    }

    /**
     * Returns the current context for the current thread. The default context 
     * for a new thread is a {@link #ROOT}. {@link ConcurrentExecutor} 
     * have the same context as the calling thread.   
     *
     * @return the current context.
     */
    public static Context current() {
        return (Context) Context.CURRENT.get();
    }

    /**
     * Returns the current owner of this context. The owner of a
     * context is the thread which {@link #enter(Context) entered}
     * the context and has not yet {@link #exit(Context) exited}.
     * A context can only have one owner at any given time, although
     * contexts can be shared by multiple {@link ConcurrentExecutor}).
     *
     * @return the thread owner of this context or <code>null</code>.
     */
    public final Thread getOwner() {
        return _owner;
    }

    /**
     * Holds the outer context of this context or <code>null</code>
     * if {@link #ROOT}.
     *
     * @return the outer context or <code>null</code>.
     */
    public final Context getOuter() {
        return _outer;
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
        context._owner = Thread.currentThread();
        Context.CURRENT.set(context);
        context.enterAction();
    }

    /**
     * Exits the specified context. The {@link #getOuter outer} context
     * becomes the current context.
     * 
     * @param context the context being entered.
     * @throws IllegalArgumentException if the specified context is not the current context.
     * @throws IllegalStateException if this context is not the current context
     *         or does not belong to the current thread.
     */
    public static void exit(Context context) {
        if (context  != Context.current())
            throw new IllegalStateException("The Specified context is not the current context");
       if (context._owner != Thread.currentThread()) // Only possible for ConcurrentContext.
            throw new IllegalStateException("Cannot exit context belonging to another thread");
       exitNoCheck(context);
    }
    static void exitNoCheck(Context context) {
        try {
            context.exitAction();
        } finally {
            Context.CURRENT.set(context._outer);
            context._outer = null;
            context._owner = null;
        }
    }

    /**
     * Sets the current context, used by {@link ConcurrentContext}
     * exclusively.
     */
    static void setCurrent(ConcurrentContext context) {
        Context.CURRENT.set(context);
    }
    
    /**
     * Sets the pools of this context active or inactive.
     * This method is overriden by HeapContext and PoolContext.
     * 
     * @param value indicates if pools should be activated or desactivated.
     */
    void setPoolsActive(boolean value) {
        _outer.setPoolsActive(value);
    }

    /**
     * Returns the local pools for the current thread and for this context.
     * 
     * @return the thread local pools for this context.
     */
    LocalPools getLocalPools() {
        return _outer.getLocalPools();
    }

}