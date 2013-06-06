/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.STACK_CONTEXT_TRACKER;
import javolution.internal.context.StackContextImpl;
import javolution.lang.Configurable;
import javolution.lang.Copyable;
import javolution.util.function.Function;

/**
 * <p> A stack memory allocator context (using RTSJ <code>ScopedMemory</code>
 *     or other mechanisms available on the run-time JVM).</p>
 *       
 * <p> Stacks allocations reduce heap memory allocation and often result in 
 *     faster execution time. Although stack allocations are great for 
 *     temporary objects, they cannot be used to store static members.
 *     More generally speaking, logic executing in a stack contexts should 
 *     ensure that stack allocated objects do not escape from
 *     their context scope. If necessary, stack objects can be copied to 
 *     the heap using {@link HeapContext#copy copy} method. When executing 
 *     a {@link Function functor}, the function result is always copied 
 *     to the calling context.
 *     [code]
 *     @StackSafe
 *     public class LargeInteger implements ValueType {
 *         static final Function<LargeInteger, LargeInteger> SQRT = new Function<LargeInteger, LargeInteger>() {
 *             public LargeInteger evaluate(LargeInteger that) {
 *                 LargeInteger result = ZERO;
 *                 LargeInteger k = that.shiftRight(this.bitLength() / 2)); // First approximation.
 *                 while (true) { // Newton Iteration.
 *                     result = (k.plus(this.divide(k))).shiftRight(1);
 *                     if (result.equals(k)) return result; // Exports result.
 *                     k = result;
 *                 }
 *             }
 *         });
 *         public LargeInteger sqrt() {
 *             return StackContext.execute(SQRT, this); // Does not generate garbage on RTSJ platforms!
 *         }
 *     }[/code]</p>
 * 
 * <p> Classes/methods identified as {@link javolution.annotation.StackSafe 
 *     StackSafe} can safely be used in a stack context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class StackContext extends AllocatorContext<StackContext> {
  
    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false);

    /**
     * Default constructor.
     */
    protected StackContext() {
    }

    /**
     * Enters a stack context instance (private since instances are not 
     * configurable).
     */
    private static StackContext enter() {
        StackContext ctx = AbstractContext.current(StackContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return STACK_CONTEXT_TRACKER.getService(
                WAIT_FOR_SERVICE.get(), DEFAULT).inner().enterScope();
    }
    
    /**
     * Executes the specified logic allocating objects on the stack.
     */
    public static void execute(Runnable logic) {
        StackContext ctx = StackContext.enter();
        try {
            ctx.executeInContext(logic);
        } finally {
            ctx.exit();
        }
    }
        
    /**
     * Executes the specified function allocating objects on the stack; the 
     * function result is copied to calling context.
     */
    public static <P,R extends Copyable<R>> R execute(Function<P,R> function, P parameter) {
        StackContext ctx = StackContext.enter();
        try {
            return ctx.executeInContext(function, parameter);
        } finally {
            ctx.exit();
        }
    }        

    /**
     * Evaluates the specified function while allocating on the stack; the 
     * function result is copied to the outer context.
     */
    protected abstract <P,R extends Copyable<R>> R executeInContext(Function<P,R> function, P parameter);

    private static final StackContextImpl DEFAULT = new StackContextImpl();
}
