/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.xml.XMLSerializable;
import j2me.lang.IllegalStateException;
import j2me.lang.UnsupportedOperationException;
import j2me.lang.ThreadLocal;

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
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, April 15, 2007
 */
public abstract class Context implements XMLSerializable {

    /**
     * Holds the root context (top context of all threads).
     */
    public static final Context ROOT = new Context() {

        protected void enterAction() {
            throw new UnsupportedOperationException(
                    "Cannot enter the root context");
        }

        protected void exitAction() {
            throw new UnsupportedOperationException(
                "Cannot enter the root context");
        } 

        public String toString() {
            return "Root Context";
        }
    
    };
    
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
     * Default constructor. 
     */
    protected Context() {
        
    }

    /**
     * Returns the current context for the current thread. 
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
     * contexts can be shared by {@link ConcurrentContext concurrent} threads.
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
     * 
     * @param context the concurrent context.
     */
    protected static void setCurrent(ConcurrentContext context) {
        Context.CURRENT.set(context);
    }
    

}