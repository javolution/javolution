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
import javolution.lang.Configurable;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.text.TypeFormat;

/**
 * <p> A stack memory allocator context (using RTSJ <code>ScopedMemory</code>
 *     or other mechanisms available on the run-time JVM).</p>
 *       
 * <p> Stacks allocations reduce heap memory allocation and often result in 
 *     faster execution time. Although stack allocations are great for 
 *     temporary objects, they cannot be used to store static members.
 *     More generally speaking, logic executing in a stack contexts should 
 *     ensure that stack allocated objects do not escape from
 *     their context scope. If necessary, stack objects can be exported using 
 *     the {@link #export} method.
 *     [code]
 *     @StackSafe
 *     public class LargeInteger implements ValueType {
 *         static final Functor<LargeInteger, LargeInteger> SQRT 
 *                 = new Functor<LargeInteger, LargeInteger>() {
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
 *             return StackContext.evaluate(SQRT, this);
 *         }
 *     }[/code]
 *     Classes/methods identified as {@link javolution.annotation.StackSafe 
 *     @StackSafe} can be used in a stack context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class StackContext extends AllocatorContext<StackContext> {
  
    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable(false) {

        @Override
        public void configure(CharSequence configuration) {
            set(TypeFormat.parseBoolean(configuration));
        }

    };

    /**
     * Default constructor.
     */
    protected StackContext() {
    }

    /**
     * Executes the specified logic allocating objects on the stack.
     */
    public static void execute(Runnable logic) {
        StackContext ctx = AbstractContext.current(StackContext.class);
        if (ctx != null) {
            ctx = STACK_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get());
        }
        ctx.executeInContext(logic);
    }
        
    /**
     * Exports the specified object to the outer allocator conter through 
     * copy (convenience method).
     */
    public static <T> T export(Copyable<T> obj) {
        AbstractContext ctx = AllocatorContext.currentAllocatorContext();
        while (ctx != null) {
            ctx = ctx.getOuter();
            if (ctx instanceof AllocatorContext) 
                return (T) ((AllocatorContext)ctx).copyInContext(obj);
        }
        return HeapContext.copy(obj);
    }

    /**
     * Evaluates the specified function allocating objects on the stack; the 
     * function result is copied to the current context (convenience method).
     */
    public static <P,R extends Copyable> R evaluate(Functor<P,R> function, P parameter) {
        Evaluator<P, R> evaluator = new Evaluator(function, parameter);
        StackContext.execute(evaluator);
        return evaluator.result;
    }

    // Runnable to allocate a new instance on the heap.
    private static class Evaluator<P, R extends Copyable> implements Runnable {

        private final Functor<P,R> function;
        private final P parameter;
        private final AllocatorContext allocatorContext;
        R result;
   
        public Evaluator(Functor<P,R> function, P parameter) {
            this.function = function;
            this.parameter = parameter;
            this.allocatorContext = AllocatorContext.currentAllocatorContext();
        }

        public void run() {
            R r = function.evaluate(parameter);
            result = (R) allocatorContext.copyInContext(r);
        }

    }
}
