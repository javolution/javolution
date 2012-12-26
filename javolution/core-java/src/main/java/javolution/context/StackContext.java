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
import javolution.text.TypeFormat;

/**
 * <p> This class represents a stack allocator context. Implementations 
 *     may be based on RTSJ <code>ScopedMemory</code> or any other mechanism
 *     specific to the run-time JVM.</p>
 *       
 * <p> Stacks allocations reduce heap memory allocation and often result in 
 *     faster execution time. Although stack allocations are great for 
 *     temporary objects, they cannot be used to store static members.
 *     More generally speaking, methods entering/exiting stack 
 *     contexts should ensure that stack allocated objects do not escape from
 *     their context scope. If necessary, stack objects can be exported using 
 *     the {@link #export} method.
 *     [code]
 *     @StackSafe
 *     public class LargeInteger implements ValueType {
 *         public LargeInteger sqrt() {
 *             StackContext ctx = StackContext.enter(); 
 *             try { 
 *                 LargeInteger result = ZERO;
 *                 LargeInteger k = this.shiftRight(this.bitLength() / 2)); // First approximation.
 *                 while (true) { // Newton Iteration.
 *                     result = (k.plus(this.divide(k))).shiftRight(1);
 *                     if (result.equals(k)) return ctx.export(result); // Exports result.
 *                     k = result;
 *                 }
 *             } finally { 
 *                 ctx.exit(); // Recycles the stack, stack allocated objects are still valid until    
 *             }               // a new stack context is entered.
 *         }
 *     }[/code]
 *     Classes/methods identified as {@link javolution.annotation.StackSafe 
 *     @StackSafe} can be used in a stack context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class StackContext extends AllocatorContext<StackContext> {

    // Start Initialization (forces allocation on the heap for static fields).
    private static final HeapContext INIT_CTX = HeapContext.enter();

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
     * Enters a new stack context instance.
     * 
     * @return the new heap context implementation entered.
     */
    public static StackContext enter() {
        StackContext ctx = AbstractContext.current(StackContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return STACK_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get()).inner().enterScope();
    }

    /**
     * Exports this object (through copy) outside of this stack context.
     * The object is copied to the outer allocator context (which might be 
     * a stack context as well).
     *
     * @param obj the object to export.
     * @return a deep copy of this object allocated in the outer allocator 
     *         context of this statck context.
     */
    public abstract <T extends Copyable> T export(Copyable<T> obj);

    /**
     * Exits the scope of this stack context; the objects allocated while in the 
     * scope of this context remain valid until a new stack context (possibly 
     * the same) is entered.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() {
        super.exit();
    }

    // End of class initialization.
    static {
        INIT_CTX.exit();
    }
}
