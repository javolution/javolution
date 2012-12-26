/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.annotation.StackSafe;

/**
 * <p> This abstract class represents the root class for all contexts. 
 *     Contexts allow for cross cutting concerns (performance, logging, 
 *     security, ...) to be addressed at run-time without polluting the
 *     application code (<a href="http://en.wikipedia.org/wiki/Separation_of_concerns">
 *     Separation of Concerns</a>). 
 *     With contexts, you <i>Think Locally, Act Globally</i>.</p>
 *     
 * <p> Typically, a context is surrounded by a <code>try, 
 *     finally</code> block statement to ensure correct behavior in case 
 *     of exceptions being raised.
 *     [code]
 *     MyContext ctx = MyContext.enter(); // Enters an inner instance 
 *     try {                              // (equivalent to AbstractContext.current(MyContext.class).inner().enterScope())
 *        ctx.memberMethod(...); // Instance configuration.
 *        ...
 *        MyContext.staticMethod(...); // Users methods (works with the last instance entered).
 *        ...
 *     } finally {
 *        ctx.exit();  // Exits context instance.
 *     }
 *     [/code]
 *     When running OSGi, the context implementation is retrieved from published
 *     services (if any). <a href="http://wiki.osgi.org/wiki/Avoid_Start_Order_Dependencies">
 *     To avoid start order dependencies</a> contexts methods can be configured to block
 *     until an implementation is published. For example, the java option  
 *     <code>-Djavolution.context.SecurityContext#WAIT_FOR_SERVICE=true</code>
 *     causes the security checks to block until a {@link SecurityContext}
 *     implementation is published.</p>
 * 
 * <p> Applications may enter specific (static) implementations of any context.
 *     [code]
 *     MyContext ctx = AbstractContext.enter(MyContextImpl.class); // Enter instance of specified class.
 *     try { 
 *        ...
 *     } finally {
 *        ctx.exit();
 *     }
 *     [/code]</p>
 * 
 * <p> Contexts do not pause thread-safety issues. There is at most one thread 
 *     to configure them. Even so they are inherited by potentially multiple threads
 *     (see {@link ConcurrentContext}); these threads cannot modify their states. </p>
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
 *      
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe
public abstract class AbstractContext<C extends AbstractContext> {

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
     * Returns the current context of specified type or <code>null</code> if none. 
     */
    protected static <T extends AbstractContext> T current(Class<T> type) {
        AbstractContext ctx = AbstractContext.CURRENT.get();
        while (true) {
            if (ctx == null) return null;
            if (type.isInstance(ctx))
                return (T) ctx;
            ctx = ctx.outer;
        }
    }

    /**
     * Enters the scope of the specified context implementation
     * (the instance is created using the implementation default constructor).
     * This method raises a {@link SecurityException} if the 
     * permission to enter the specified class (or a parent class) is not
     * granted. 
     * 
     * @param impl the context implementation class.
     * @throws IllegalArgumentException if the specified class default constructor
     *         cannot be instantiated.
     * @throws SecurityException 
     *         if <code>SecurityPermission(impl, "enter")</code> is not granted. 
     */
    public static <T extends AbstractContext> T enter(Class<T> impl) {
        SecurityContext.check(new SecurityPermission(impl, "enter"));
        try {
            return (T) impl.newInstance().enterScope();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Invalid context implementation " + impl, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid context implementation " + impl, e);
        }
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
            throw new IllegalStateException("This context is not the current context");
        AbstractContext.CURRENT.set(outer);
        outer = null;
    }

    /**
     * Enters the scope of this context which becomes the current context; 
     * the previous current context becomes the outer of this context. 
     */
    protected C enterScope() {
        outer = AbstractContext.CURRENT.get();
        AbstractContext.CURRENT.set(this);
        return (C) this;
    }

    /**
     * Returns the outer context of this context or <code>null</code> if this 
     * context has no outer context (top context).
     */
    protected AbstractContext<?> getOuter() {
        return outer;
    }

    /**
     * Returns a new inner instance of this context inheriting the properties 
     * of this context. The new instance can be configured independently 
     * from its parent. 
     */
    protected abstract C inner();

}