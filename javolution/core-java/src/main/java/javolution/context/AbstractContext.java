/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.xml.XMLSerializable;

/**
 * <p> This abstract class represents the root class for all contexts. 
 *     Contexts allow for cross cutting concerns (performance, logging, 
 *     security, ...) to be addressed at run-time without polluting the
 *     application code (<a href="http://en.wikipedia.org/wiki/Separation_of_concerns">
 *     Separation of Concerns</a>).</p>
 *     
 * <p> Typically, a context is surrounded by a <code>try, 
 *     finally</code> block statement to ensure correct behavior in case 
 *     of exceptions being raised.[code]
 *     MyContext.enter(); // Enters a new instance of MyContext (OSGi factory produced when possible).
 *     try {              // (equivalent to MyContext.Factory.newMyContext().enterScope())
 *        ...              
 *     } finally {
 *        MyContext.exit();  // Exits (back to previous context).
 *     }
 *     [/code]</p>
 * 
 * <p> Except for {@link ConcurrentContext} instances, there can be only one 
 *     thread executing in any context. Therefore context methods do not 
 *     require synchronization. Nonetheless, because multiple threads 
 *     may execute in a {@link ConcurrentContext} and inherit the contexts of 
 *     their parent thread (the one which entered the concurrent context), 
 *     context instances should provide a thread-safe {@link #shared shared} 
 *     view of themselves (view usable by concurrent threads). </p>
 * 
 * <p> Here are few examples of predefined context.
 *     [code]
 * 
 *     public static LocalParameter<LargeInteger> MODULO = new LocalParameter<LargeInteger>(); 
 *     ...
 *     LocalContext.enter(); 
 *     try {
 *         LocalContext.override(ModuloInteger.MODULO, m); // No impact on other threads!
 *         z = x.times(y); // Multiplication modulo m.
 *     } finally {
 *         LocalContext.exit(); 
 *     }
 *
 *     Complex sum = Complex.ZERO;
 *     StackContext.enter(); // Allocates on the stack (if supported).
 *     try {
 *        for (int i=0; i < n; i++) {
 *           sum = sum.plus(v[i]); // All sums are stack allocated.
 *        }
 *        sum = StackContext.export(sum); // Copies outside of the stack.
 *     } finally {
 *        StackContext.exit(); // Resets stack.
 *     }
 * 
 * 
 *     LogContext.enter();
 *     try {
 *         LogContext.setHeader("My Logger");
 *         LogContext.enableInfo(true);
 *         ... 
 *         LogContext.info("My message"); 
 *     } finally {
 *         LogContext.exit(); // Back to previous LogContext settings. 
 *     }
 * 
 *     [/code]</p>
 * 
 * <p> Threads executing in a {@link ConcurrentContext} inherit the contexts 
 *     of the parent thread (the one which entered the concurrent context), this 
 *     to ensure that the behaviors of the concurrent threads are the same as if 
 *     the concurrent executions were performed by the parent thread. </p>
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class AbstractContext<C extends AbstractContext> implements XMLSerializable {

    /**
     * Holds the last context entered (thread-local).
     */
    private static final ThreadLocal<AbstractContext> CURRENT = new ThreadLocal();
    
    /**
     * Holds the outer context or <code>null</code> if none (root or not attached).
     */
    private AbstractContext outer;

    /**
     * Default constructor. 
     */
    protected AbstractContext() {
    }

    /**
     * Returns the last context entered or <code>null</code> if no context have
     * been entered.
     */
    protected static AbstractContext<?> current() {
        return AbstractContext.CURRENT.get();
    }

    /**
     * Returns the first outer context of specified type or <code>null</code> 
     * if none. If the context found is outer of a {@link ConcurrentContext},
     * then a {@link #shared() shared} view is returned.
     */
    protected static <T extends AbstractContext> T current(Class<T> type) {
        AbstractContext ctx = AbstractContext.CURRENT.get();
        boolean isShared = false;
        while (true) {
            if (ctx == null) return null;
            if (type.isInstance(ctx)) return (T) (isShared ? ctx.shared() : ctx);
            if (ctx instanceof ConcurrentContext) isShared = true;
            ctx = ctx.outer;
        }
    }

    /**
     * Enters the scope of this context. This method sets this context as
     * the current context.
     * 
     * @throws IllegalStateException if this context is currently in use.
     */
    protected void enterScope() throws IllegalStateException {
        if (outer != null)
            throw new IllegalStateException(this + " currently in use");
        outer = AbstractContext.CURRENT.get();
        AbstractContext.CURRENT.set(this);
    }

    /**
     * Exits the scope of this context. 
     * 
     * @throws IllegalStateException if this context is not the current context
     *         in scope.
     */
    protected void exitScope() throws IllegalStateException {
        if (this != AbstractContext.CURRENT.get())
            throw new IllegalStateException(this + " is not the context in scope");
        AbstractContext.CURRENT.set(outer);
        outer = null;
    }

    /**
     * Returns the outer context of this context or <code>null</code> if this 
     * context is root or not attached.
     */
    protected AbstractContext<?> getOuter() {
        return outer;
    }

    /**
     * Returns a shared view of this context (to be inherited by 
     * {@link ConcurrentContext}).
     */
    protected abstract C shared();

}