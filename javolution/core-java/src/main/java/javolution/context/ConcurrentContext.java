/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.internal.context.ConcurrentContextImpl;
import javolution.internal.osgi.JavolutionActivator;
import javolution.text.TypeFormat;

/**
 * <p> This class represents a context able to take advantage of concurrent 
 *     algorithms on multi-processors systems.</p>
 *     
 * <p> When a thread enters a concurrent context, it may performs concurrent
 *     executions by calling the {@link #execute(Runnable)} static method.
 *     The logic is then executed by a concurrent thread or by the current 
 *     thread itself if there is no concurrent thread immediately available 
 *     (the number of concurrent threads is limited, see {@link #CONCURRENCY}).
 *     [code]
 *     ConcurrentContext.enter(); 
 *     try { 
 *         ConcurrentContext.execute(new Runnable() {...}); 
 *         ConcurrentContext.execute(...); // Shorter notation if closure are supported (JDK1.8) 
 *     } finally {
 *         ConcurrentContext.exit(); // Waits for all concurrent executions to complete.
 *                                   // Reexports any exception raised during concurrent executions. 
 *     }
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
 *    <a href="http://javolution.org/doc/benchmark.html">benchmark</a>).
 *    [code]
 *    private void quickSort(final FastTable<? extends Comparable> table) {
 *        final int size = table.size();
 *        if (size < 100) { 
 *            table.sort(); // Direct quick sort.
 *        } else {
 *            // Splits table in two and sort both part concurrently.
 *            final FastTable<? extends Comparable> t1 = new FastTable();
 *            final FastTable<? extends Comparable> t2 = new FastTable();
 *            ConcurrentContext.enter();
 *            try {
 *                ConcurrentContext.execute(new Runnable() {
 *                    public void run() {
 *                        t1.addAll(table.subList(0, size / 2));
 *                        quickSort(t1); // Recursive.
 *                    }
 *                });
 *                ConcurrentContext.execute(new Runnable() {
 *                    public void run() {
 *                        t2.addAll(table.subList(size / 2, size));
 *                        quickSort(t2); // Recursive.
 *                    }
 *                });
 *            } finally {
 *                ConcurrentContext.exit();
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
 *     }[/code]</p>
 *     
 * <p> The concurrency can be {@link LocalContext locally} adjusted.
 *    [code]
 *    LocalContext.enter(); 
 *    try { 
 *        // Performs analysis sequentially.
 *        LocalContext.override(ConcurrentContext.CONCURRENCY, 0);
 *        runAnalysis();  
 *     } finally {
 *        LocalContext.exit();    
 *     }[/code]</p>
 * 
 * <p> Finally, because executing in a concurrent context may consume a lot of 
 *     CPU resources, the {@link #ENTER_PERMISSION permission} to enter a 
 *     concurrent context must be granted.
 *     [code]
 *     SecurityContext.enter(); 
 *     try { 
 *        SecurityContext.grant(ConcurrentContext.ENTER_PERMISSION);
 *        ... // Concurrency is authorized.
 *     } finally {
 *        SecurityContext.exit();    
 *     }[/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class ConcurrentContext extends AbstractContext {

    /**
     * Defines the factory service producing {@link ConcurrentContext} implementations.
     */
    public interface Factory {

        /**
         * Returns a new instance of the concurrent context.
         */
        ConcurrentContext newConcurrentContext();
    }

    /**
     * Holds the maximum number of concurent threads (default
     * <code>Runtime.getRuntime().availableProcessors()</code>).
     * A value of <code>0</code> disables concurrency.
     */
    public static final LocalParameter<Integer> CONCURRENCY 
            = new LocalParameter(Runtime.getRuntime().availableProcessors()) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseInt(configuration));
        }
    };

    /**
     * Holds the permission to enter a concurrent context.
     */
    public static final SecurityPermission<ConcurrentContext> ENTER_PERMISSION 
            = new SecurityPermission(ConcurrentContext.class, "enter");

    /**
     * Default constructor.
     */
    protected ConcurrentContext() {
    }

    /**
     * Enters a new concurrent context instance.
     * 
     * @throws SecurityException if the {@link ConcurrentContext#ENTER_PERMISSION
     *         permission} to enter a concurrent context is not granted.
     */
    public static void enter() throws SecurityException {
        ConcurrentContext.Factory factory = JavolutionActivator.getConcurrentContextFactory();
        ConcurrentContext ctx = (factory != null) ? factory.newConcurrentContext()
                : new ConcurrentContextImpl();
        ctx.enterScope();
    }

    /**
     * Exits the concurrent context.
     *
     * @throws ClassCastException if the current context is not a security context.
     */
    public static void exit() {
        ((ConcurrentContext) AbstractContext.current()).exitScope();
    } 

    /**
     * Executes the specified logic by a concurrent thread if 
     * one available; otherwise the logic is executed by the current thread.
     * Any exception or error occurring during the concurrent execution is
     * propagated to the current thread upon {@link #exit} 
     * of the concurrent context.
     * 
     * @param  logic the logic to be executed concurrently when possible.
     * @throws IllegalStateException if there is no outer concurrent context.
     */
    public static void execute(Runnable logic) {
        ConcurrentContext ctx = AbstractContext.current(ConcurrentContext.class);
        if (ctx == null)
            throw new IllegalStateException("Not executing in the scope of a ConcurrentContext");
        ctx.doExecute(logic);        
    }
   
    /**
     * Executes the specified logic concurrently when possible. 
     * 
     * @param  logic the logic to execute.
     */
    protected abstract void doExecute(Runnable logic);

    /**
     * Overrides the parent method {@link AbstractContext#enterScope() } 
     * to check that {@link ConcurrentContext#ENTER_PERMISSION} is granted.
     * 
     * @throws SecurityContext if the permission to enter a concurrent context
     *         is not granted.
     */
    @Override
    protected void enterScope() throws IllegalStateException, SecurityException {
        SecurityContext.check(ConcurrentContext.ENTER_PERMISSION);
        super.enterScope();
    }
       
}