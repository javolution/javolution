/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context able to take advantage of concurrent algorithms on 
 *     multi-processors systems.</p>
 *     
 * <p> When a thread enters a concurrent context, it may performs concurrent
 *     executions by calling the {@link #execute(Runnable)} static method.
 *     The logic is then executed by a concurrent thread or by the current 
 *     thread itself if there is no concurrent thread immediately available 
 *     (the number of concurrent threads is limited, see {@link #CONCURRENCY}).
 * [code]
 * ConcurrentContext ctx = ConcurrentContext.enter(); 
 * try { 
 *     ctx.execute(new Runnable() {...}); 
 *     ctx.execute(new Runnable() {...});  
 * } finally {
 *     ctx.exit(); // Waits for all concurrent executions to complete.
 *                 // Re-exports any exception raised during concurrent executions. 
 * }[/code]</p>
 * 
 * <p> or equivalent shorter notation:
 * [code]
 * ConcurrentContext.execute(new Runnable() {...}, new Runnable() {...});[/code]</p>
 *     
 * <p> Only after all concurrent executions are completed, is the current 
 *     thread allowed to exit the scope of the concurrent context 
 *     (internal synchronization).</p>
 *     
 * <p> Concurrent logics always execute within the same {@link AbstractContext
 *     context} as the calling thread.</p>
 *
 * <p> Concurrent contexts ensure the same behavior whether or not the execution
 *     is performed by the current thread or a concurrent thread. Any error or 
 *     runtime exception raised during the concurrent logic executions is 
 *     propagated to the current thread.</p>
 *
 * <p> Concurrent contexts are easy to use, and provide automatic 
 *     load-balancing between processors with almost no overhead. 
 *     Here is a concurrent/recursive quick/merge sort using anonymous inner 
 *     classes.
 * [code]
 * static void concurrentSort(final FastTable<? extends Comparable> table) {
 *     final int size = table.size();
 *     if (size < 100) { 
 *         table.sort(); // Direct quick sort.
 *     } else {
 *         // Splits table in two and sort both part concurrently.
 *         final FastTable<? extends Comparable> t1 = new FastTable();
 *         final FastTable<? extends Comparable> t2 = new FastTable();
 *         ConcurrentContext ctx = ConcurrentContext.enter();
 *         try {
 *             ctx.execute(new Runnable() {
 *                 public void run() {
 *                     t1.addAll(table.subList(0, size / 2));
 *                     concurrentSort(t1); // Recursive.
 *                 }
 *             });
 *             ctx.execute(new Runnable() {
 *                 public void run() {
 *                     t2.addAll(table.subList(size / 2, size));
 *                     concurrentSort(t2); // Recursive.
 *                 }
 *             });
 *         } finally {
 *           ctx.exit(); // Joins.
 *         }
 *         // Merges results.
 *         for (int i=0, i1=0, i2=0; i < size; i++) {
 *             if (i1 >= t1.size()) {
 *                 table.set(i, t2.get(i2++));
 *             } else if (i2 >= t2.size()) {
 *                 table.set(i, t1.get(i1++));
 *             } else {
 *                 Comparable o1 = t1.get(i1);
 *                 Comparable o2 = t2.get(i2);
 *                 if (o1.compareTo(o2) < 0) {
 *                     table.set(i, o1);
 *                     i1++;
 *                  } else {
 *                     table.set(i, o2);
 *                     i2++;
 *                  }
 *             }
 *         }
 *     }
 * }[/code]</p>
 *     
 * <p> Here is another example using {@link #execute(java.lang.Runnable[]) 
 *     execute(Runnable ...)} static method 
 *    (Karatsuba recursive multiplication for large integers).
 * [code]
 *  public LargeInteger times(LargeInteger that) {
 *      if (that._size <= 1) {
 *          return times(that.longValue()); // Direct multiplication.
 *      } else { // Karatsuba multiplication in O(n^log2(3))
 *          int bitLength = this.bitLength();
 *          int n = (bitLength >> 1) + (bitLength & 1);
 *                 
 *          // this = a + 2^n b,   that = c + 2^n d
 *          LargeInteger b = this.shiftRight(n);
 *          LargeInteger a = this.minus(b.shiftLeft(n));
 *          LargeInteger d = that.shiftRight(n);
 *          LargeInteger c = that.minus(d.shiftLeft(n));
 *          Multiply ac = new Multiply(a, c);
 *          Multiply bd = new Multiply(b, d);
 *          Multiply abcd = new Multiply(a.plus(b), c.plus(d));
 *          ConcurrentContext.execute(ac, bd, abcd); // Convenience method.  
 *          // a*c + ((a+b)*(c+d)-a*c-b*d) 2^n + b*d 2^2n 
 *          return  ac.result.plus(abcd.result.minus(ac.result.plus(bd.result)).shiftWordLeft(n))
 *              .plus(bd.result.shiftWordLeft(n << 1));
 *      }
 *  }
 *  private static class Multiply implements Runnable {
 *      LargeInteger left, right, result;
 *      Multiply(LargeInteger left, LargeInteger right) {
 *         this.left = left;
 *         this.right = right;
 *      }
 *      public void run() {
 *         result = left.times(right); // Recursive.
 *      }
 *  }[/code]</p>
 *          
 * <p> Concurrency can be adjusted or disabled. The default concurrency 
 *     is defined by the {@link #CONCURRENCY} configurable. 
 * [code]
 * ConcurrentContext ctx = ConcurrentContext.enter(); 
 * try { 
 *    ctx.setConcurrency(0); // Disables concurrency
 *    runAnalysis();         // Performs analysis sequentially.
 * } finally {
 *    ctx.exit(); // Back to previous concurrency settings.  
 * }[/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class ConcurrentContext extends AbstractContext {

    /**
     * Holds the maximum concurrency  
     * (default {@code Runtime.getRuntime().availableProcessors() - 1}).
     * The maximum concurrency is configurable. For example, the JVM option 
     * {@code -Djavolution.context.ConcurrentContext#CONCURRENCY=0}
     * disables concurrency. 
     */
    public static final Configurable<Integer> CONCURRENCY = new Configurable<Integer>() {
        @Override
        protected Integer getDefault() {
            return Runtime.getRuntime().availableProcessors() - 1;
        }

        @Override
        protected Integer initialized(Integer value) {
            return MathLib.min(value, 65536); // Hard-limiting
        }

        @Override
        protected Integer reconfigured(Integer oldCount, Integer newCount) {
            throw new UnsupportedOperationException(
                    "Concurrency reconfiguration not supported.");
        }
    };

    /**
     * Default constructor.
     */
    protected ConcurrentContext() {}

    /**
     * Enters and returns a new concurrent context instance.
     */
    public static ConcurrentContext enter() {
        ConcurrentContext ctx = current(ConcurrentContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getConcurrentContext();
        }
        return (ConcurrentContext) ctx.enterInner();
    }

    /**
     * Convenience method to executes the specified logics concurrently. 
     * This method is equivalent to:
     * [code]
     * ConcurrentContext ctx = ConcurrentContext.enter();
     * try {
     *     ctx.execute(logics[0]);
     *     ctx.execute(logics[1]);
     *     ...
     * } finally {
     *     ctx.exit();
     * }[/code]
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
     * propagated to the current thread upon exit of the concurrent context.
     * 
     * @param  logic the logic to be executed concurrently when possible.
     */
    public abstract void execute(Runnable logic);

    /**
     * Sets the maximum concurrency. Setting a value greater than the 
     * {@link #getConcurrency() current concurrency} has no effect 
     * (concurrency can only be reduced).
     */
    public abstract void setConcurrency(int concurrency);

    /**
     * Returns the current concurrency which is basically the number of 
     * concurrent threads authorized to do concurrent work (on top of all
     * others threads of course).
     */
    public abstract int getConcurrency();

    /**
     * Exits the scope of this concurrent context; this method blocks until 
     * all the concurrent executions are completed.
     * 
     * @throws RuntimeException re-exports any exception raised during concurrent
     *         executions.
     * @throws Error re-exports any error raised during concurrent executions.
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() { // Redefine here for documentation purpose.
        super.exit();
    }
}
