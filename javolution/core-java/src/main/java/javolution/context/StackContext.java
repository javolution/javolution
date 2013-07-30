/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Copyable;
import javolution.osgi.internal.OSGiServices;
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
 *     the heap using {@link HeapContext#copy HeapContext.copy(...)} method.
 *     When executing a {@link Function function}, the function result is always
 *     copied to the calling context.
 * [code]
 * @RealTime
 * public class LargeInteger implements ValueType {
 *     // Very fast when running on a RTSJ VM (temporary objects on the stack).
 *     public LargeInteger sqrt() { 
 *         return StackContext.execute(SQRT, this); // Execution result exported outside the stack.
 *     }
 *     static final Function<LargeInteger, LargeInteger> SQRT = new Function<LargeInteger, LargeInteger>() {
 *          public LargeInteger apply(LargeInteger that) {
 *                 LargeInteger result = ZERO;
 *                 LargeInteger k = that.shiftRight(this.bitLength() / 2)); // First approximation.
 *                 while (true) { // Newton Iteration.
 *                     result = (k.plus(this.divide(k))).shiftRight(1);
 *                     if (result.equals(k)) return result; 
 *                     k = result;
 *                 }
 *             }
 *          });
 * }[/code]</p>
 * <p> Stack context are great to accelerate calculations when the cost of 
 *     the object creation is prohibitive relatively to the action performed
 *     (operations on complex numbers for examples). Stack contexts
 *     can be be nested (e.g. recursive calculations).</p>
 * 
 * <p> Only classes/methods identified as {@link javolution.lang.RealTime#stackSafe() 
 *     stack-safe} such as {@link javolution.lang.ValueType ValueType} instances 
 *     should be used in a stack context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class StackContext extends AllocatorContext {

    /**
     * Default constructor.
     */
    protected StackContext() {}

    /**
     * Enters and returns a new stack context instance (method for internal 
     * usage).
     */
    private static StackContext enter() {
        StackContext ctx = AbstractContext.current(StackContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getStackContext();
        }
        return (StackContext) ctx.enterInner();
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
    public static <P, R extends Copyable<R>> R execute(Function<P, R> function,
            P parameter) {
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
    protected abstract <P, R extends Copyable<R>> R executeInContext(
            Function<P, R> function, P parameter);

 }
