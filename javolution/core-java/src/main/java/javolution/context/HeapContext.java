/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.HEAP_CONTEXT_TRACKER;
import javolution.lang.Configurable;
import javolution.lang.Copyable;
import javolution.text.TypeFormat;
import javolution.util.function.Factory;

/**
 * <p> An {@link AllocatorContext} always allocating from the heap (default). 
 *     This context is useful for {@link javolution.annotation.StackSafe} 
 *     classes to ensure that static fields can be updated even when 
 *     allocations are performed on the stack.
 *     [code]
 *     @StackSafe
 *     public class Text {
 *         private static final FastMap<Text, Text> INTERN_INSTANCES ... 
 *         public Text intern() {
 *             if (!INTERN_INSTANCES.contains(this)) {
 *                  final Text[] internText = new Text[1];
 *                  HeapContext.execute(new Runnable() {
 *                      public void run() {
 *                           internText[0] = Text.copy(); // Heap.
 *                           INTERN_INSTANCES.put(internText[0], internText[0]); 
 *                      }
 *                  });
 *                  return internText[0];
 *              }
 *              ...
 *         }
 *         ...
 *     }[/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see javolution.annotation.StackSafe
 */
public abstract class HeapContext extends AllocatorContext<HeapContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation of this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(false) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseBoolean(configuration));
        }

    };

    /**
     * Default constructor.
     */
    protected HeapContext() {
    }
    
   /**
     * Enters a heap context instance (private since heap context
     * is not configurable).
     */
    private static HeapContext enter() {
        HeapContext ctx = AbstractContext.current(HeapContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return HEAP_CONTEXT_TRACKER.getService(
                WAIT_FOR_SERVICE.getDefaultValue()).inner().enterScope();
    }
    
    /**
     * Executes the specified logic allocating objects on the heap.
     */
    public static void execute(Runnable logic) {
        HeapContext ctx = HeapContext.enter();
        try {
            ctx.executeInContext(logic);
        } finally {
            ctx.exit();
        }
    }

    /**
     * Returns a new instance allocated on the heap and produced by the 
     * specified factory (convenience method).
     */
    public static <T> T allocate(Factory<T> factory) {
        HeapContext ctx = HeapContext.enter();
        try {
            return ctx.allocateInContext(factory);
        } finally {
            ctx.exit();
        }
    }

    /**
     * Returns a copy of the specified object allocated on the heap
     * (convenience method).
     */
    public static <T> T copy(Copyable<T> obj) {
        HeapContext ctx = HeapContext.enter();
        try {
            return ctx.copyInContext(obj);
        } finally {
            ctx.exit();
        }
    }
}