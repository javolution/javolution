/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.CONCURRENT_CONTEXT_TRACKER;
import javolution.lang.Configurable;
import javolution.text.TypeFormat;

/**
 * <p> A context able to take advantage of concurrent algorithms on 
 *     multi-processors systems.</p>
 *     
 * <p> When a thread enters a concurrent context, it may performs concurrent
 *     executions by calling the {@link #execute(Runnable)} static method.
 *     The logic is then executed by a concurrent thread or by the current 
 *     thread itself if there is no concurrent thread immediately available 
 *     (the number of concurrent threads is limited, see {@link #CONCURRENCY}).
 *     [code]
 *     ConcurrentContext ctx = ConcurrentContext.enter(); 
 *     try { 
 *         ctx.execute(new Runnable() {...}); 
 *         ctx.execute(new Runnable() {...});  
 *     } finally {
 *         ctx.exit(); // Waits for all concurrent executions to complete.
 *                     // Reexports any exception raised during concurrent executions. 
 *     }
 *    [/code] or equivalent shorter notation:
 *    [code]
 *    ConcurrentContext.execute(new Runnable() {...}, new Runnable() {...});
 *    [/code]</p>
 *     
 * <p> Only after all concurrent executions are completed, is the current 
 *     thread allowed to exit the scope of the concurrent context 
 *     (internal synchronization).</p>
 *     
 * <p> Concurrent logics always execute within the same {@link AbstractContext
 *     context} as the calling thread. For example, if the main thread runs in a 
 *     {@link StackContext}, concurrent executions are performed in the
 *     same {@link StackContext} as well.</p>
 *
 * <p> Concurrent contexts ensure the same behavior whether or not the execution
 *     is performed by the current thread or a concurrent thread. Any error or 
 *     runtime exception raised during the concurrent logic executions is 
 *     propagated to the current thread.</p>
 *
 * <p> Concurrent contexts are easy to use, and provide automatic 
 *     load-balancing between processors with almost no overhead. 
 *     Here is a concurrent/recursive quick/merge sort using anonymous inner 
 *     classes (the same method is used for   
 *     <a href="http://javolution.org/doc/benchmark.html">benchmark</a>).
 *     [code]
 *     private void quickSort(final FastTable<? extends Comparable> table) {
 *        final int size = table.size();
 *        if (size < 100) { 
 *            table.sort(); // Direct quick sort.
 *        } else {
 *            // Splits table in two and sort both part concurrently.
 *            final FastTable<? extends Comparable> t1 = new FastTable();
 *            final FastTable<? extends Comparable> t2 = new FastTable();
 *            ConcurrentContext ctx = ConcurrentContext.enter();
 *            try {
 *                ctx.execute(new Runnable() {
 *                    public void run() {
 *                        t1.addAll(table.subList(0, size / 2));
 *                        quickSort(t1); // Recursive.
 *                    }
 *                });
 *                ctx.execute(new Runnable() {
 *                    public void run() {
 *                        t2.addAll(table.subList(size / 2, size));
 *                        quickSort(t2); // Recursive.
 *                    }
 *                });
 *            } finally {
 *                ctx.exit(); // Joins.
 *            }
 *            // Merges results.
 *            for (int i=0, i1=0, i2=0; i < size; i++) {
 *                if (i1 >= t1.size()) {
 *                    table.set(i, t2.get(i2++));
 *                } else if (i2 >= t2.size()) {
 *                    table.set(i, t1.get(i1++));
 *                } else {
 *                    Comparable o1 = t1.get(i1);
 *                    Comparable o2 = t2.get(i2);
 *                    if (o1.compareTo(o2) < 0) {
 *                        table.set(i, o1);
 *                        i1++;
 *                    } else {
 *                        table.set(i, o2);
 *                        i2++;
 *                    }
 *                }
 *            }
 *        }
 *     }[/code]
 *      Here is another example using {@link #execute(java.lang.Runnable[]) 
 *      execute(Runnable ...)} static method 
 *     (Karatsuba recursive multiplication for large integers).
 *      [code]
 *     public LargeInteger multiply(LargeInteger that) {
 *         if (that._size <= 1) {
 *             return multiply(that.longValue()); // Direct multiplication.
 *         } else { // Karatsuba multiplication in O(n^log2(3))
 *             int bitLength = this.bitLength();
 *             int n = (bitLength >> 1) + (bitLength & 1);
 *                 
 *             // this = a + 2^n b,   that = c + 2^n d
 *             LargeInteger b = this.shiftRight(n);
 *             LargeInteger a = this.minus(b.shiftLeft(n));
 *             LargeInteger d = that.shiftRight(n);
 *             LargeInteger c = that.minus(d.shiftLeft(n));
 *             Multiply ac = new Multiply(a, c);
 *             Multiply bd = new Multiply(b, d);
 *             Multiply abcd = new Multiply(a.plus(b), c.plus(d));
 *             ConcurrentContext.execute(ac, bd, abcd); // Convenience method.  
 *             // a*c + ((a+b)*(c+d)-a*c-b*d) 2^n + b*d 2^2n 
 *             return  ac.result.plus(abcd.result.minus(ac.result.plus(bd.result)).shiftWordLeft(n))
 *                 .plus(bd.result.shiftWordLeft(n << 1));
 *         }
 *     }
 *     private static class Multiply implements Runnable {
 *         LargeInteger left, right, result;
 *         Multiply(LargeInteger left, LargeInteger right) {
 *            this.left = left;
 *            this.right = right;
 *         }
 *         public void run() {
 *             result = left.times(right); // Recursive.
 *         }
 *     };
 *    [/code]</p>
 *          
 * <p> Concurrency can be adjusted or disabled. The maximum concurrency 
 *     is defined by the {@link #CONCURRENCY} local parameter. 
 *    [code]
 *    LocalContext ctx = LocalContext.enter(); 
 *    try { 
 *        // Performs analysis sequentially.
 *        ctx.setLocalValue(ConcurrentContext.CONCURRENCY, 0);
 *        runAnalysis();  
 *    } finally {
 *        ctx.exit(); // Back to previous concurrency settings.  
 *    }[/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class ConcurrentContext extends AbstractContext<ConcurrentContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(false) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseBoolean(configuration));
        }

    };

    /**
     * Holds the maximum number of concurrent threads usable 
     * (default <code>Runtime.getRuntime().availableProcessors()</code>).
     * For example, running with the option 
     * <code>-Djavolution.context.ConcurrentContext#CONCURRENCY=0</code>
     * disables concurrency. 
     */
    public static final LocalParameter<Integer> CONCURRENCY = new LocalParameter<Integer>(Runtime.getRuntime().availableProcessors()) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseInt(configuration));
        }

    };

    /**
     * Default constructor.
     */
    protected ConcurrentContext() {
    }

    /**
     * Enters a new concurrent context instance.
     * 
     * @return the new concurrent context implementation entered.
     */
    public static ConcurrentContext enter() {
        return ConcurrentContext.current().inner().enterScope();
    }

    /**
     * Convenience method to executes the specified logics concurrently. 
     * This method is equivalent to:
     * [code]
     *     ConcurrentContext ctx = ConcurrentContext.enter();
     *     try {
     *         ctx.execute(logics[0]);
     *         ctx.execute(logics[1]);
     *         ...
     *     } finally {
     *         ctx.exit();
     *     }
     * [/code]
     * 
     * @param  logics the logics to execute concurrently if possible.
     */
    public static void execute(Runnable... logics) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            for (Runnable logic : logics) {
                ctx.execute(logic);
            }
        } finally {
            ctx.exit();
        }
    }

    /**
     * Executes the specified logic by a concurrent thread if 
     * one available; otherwise the logic is executed by the current thread.
     * Any exception or error occurring during the concurrent execution is
     * propagated to the current thread upon {@link AbstractContext#exit} 
     * of the concurrent context.
     * 
     * @param  logic the logic to be executed concurrently when possible.
     * @throws IllegalStateException if not executing within the scope of 
     *         a concurrent context.
     */
    public abstract void execute(Runnable logic);

    /**
     * Exits the scope of this concurrent context; this method blocks until 
     * all the concurrent executions are completed.
     * 
     * @throws RuntimeException reexports any exception raised during concurrent
     *         executions.
     * @throws Error reexports any error raised during concurrent executions.
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() { // Redefine here for documentation purpose.
        super.exit();
    }

    /**
     * Sets this concurrent context as the current context for the 
     * current thread. This method is particularly useful when users
     * want to create their own threads and make them inherits the 
     * current context stack. That inherited context stack remains 
     * invariant for the child thread even when the parent thread exits
     * contexts scopes or enter new ones.
     * [code]
     * ConcurrentContext ctx = ConcurrentContext.enter();
     * try {
     *     MyThread myThread = new MyThread();
     *     myThread.parentContext = ctx;
     *     myThread.start(); // Autonomous thread inheriting an invariant view of the context stack.
     * } finally {
     *    ctx.exit(); 
     * }
     * ...
     * class MyThread extends Thread {
     *     ConcurrentContext parentContext;
     *     public void run() {
     *         parentContext.setCurrent(); 
     *         ...
     *     }
     * } 
     * [/code]</p>
     */
    public void setCurrent() {
        CURRENT.set(this);
    }

    /**
     * Returns the current concurrent context.
     */
    protected static ConcurrentContext current() {
        ConcurrentContext ctx = AbstractContext.current(ConcurrentContext.class);
        if (ctx != null) return ctx;
        return CONCURRENT_CONTEXT_TRACKER.getService(
                WAIT_FOR_SERVICE.getDefaultValue());
    }
    
 }
