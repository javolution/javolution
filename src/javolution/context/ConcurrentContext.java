/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2mex.realtime.MemoryArea;
import j2mex.realtime.RealtimeThread;

import javolution.lang.Configurable;
import javolution.lang.Reflection;

/**
 * <p> This class represents a concurrent context; it is used to accelerate 
 *     execution of concurrent algorithms on multi-processors systems.</p>
 *     
 * <p> When a thread enters a concurrent context, it may execute multiple
 *     concurrent {@link Logic logics} by calling any of the 
 *     <code>ConcurrentContext.execute(logic, arg0, arg1, ...)</code> 
 *     static methods. The logic is then executed at the same priority 
 *     as the current thread and in the same memory area by a 
 *     {@link ConcurrentThread} or by the current thread itself if there is
 *     no concurrent thread immediately available (as the number of concurrent
 *     threads is limited, see 
 *     <a href="{@docRoot}/overview-summary.html#configuration">
 *     Javolution Configuration</a> for details).</p>
 *     
 * <p> Only after all concurrent executions are completed, is the current 
 *     thread allowed to exit the scope of the concurrent context 
 *     (internal synchronization).</p>
 *     
 * <p> Concurrent logics always execute within a {@link PoolContext}. 
 *     {@link RealtimeObject Realtime objects} made available outside of the 
 *     logic scope have therefore to be either {@link RealtimeObject#export 
 *     exported} (return value) or {@link RealtimeObject#preserve
 *     preserved} (static).</p>
 *     
 * <p> Concurrent contexts are easy to use, and provide automatic 
 *     load-balancing between processors with almost no overhead. Here is
 *     an example of <b>concurrent/recursive/clean</b> (no garbage generated) 
 *     implementation of the Karatsuba multiplication for large integers:[code]
 *     import javolution.context.PoolContext.Reference;
 *     ...
 *     public LargeInteger multiply(LargeInteger that) {
 *         if (that._size <= 1) {
 *             return multiply(that.longValue()); // Direct multiplication.
 *             
 *         } else { // Karatsuba multiplication in O(n^log2(3))
 *             int bitLength = this.bitLength();
 *             int n = (bitLength >> 1) + (bitLength & 1);
 *             // this = a + 2^n b,   that = c + 2^n d
 *             LargeInteger b = this.shiftRight(n);
 *             LargeInteger a = this.minus(b.shiftLeft(n));
 *             LargeInteger d = that.shiftRight(n);
 *             LargeInteger c = that.minus(d.shiftLeft(n));
 *             Reference<LargeInteger> ac = Reference.newInstance();
 *             Reference<LargeInteger> bd = Reference.newInstance();
 *             Reference<LargeInteger> abcd = Reference.newInstance();
 *             ConcurrentContext.enter();
 *             try { 
 *                 ConcurrentContext.execute(MULTIPLY, a, c, ac);
 *                 ConcurrentContext.execute(MULTIPLY, b, d, bd);
 *                 ConcurrentContext.execute(MULTIPLY, a.plus(b), c.plus(d), abcd);
 *             } finally {
 *                 ConcurrentContext.exit(); // Waits for all concurrent threads to complete.
 *             }
 *             // a*c + ((a+b)*(c+d)-a*c-b*d) 2^n + b*d 2^2n 
 *             return ac.get().plus(abcd.get().minus(ac.get()).minus(bd.get()).shiftLeft(n)).plus(bd.get().shiftLeft(2 * n));
 *         }
 *     }
 *     private static final Logic MULTIPLY = new Logic() {
 *         public void run(Object[] args) {
 *             LargeInteger left = (LargeInteger) args[0];
 *             LargeInteger right = (LargeInteger) args[1];
 *             Reference result = (Reference) args[2];
 *             result.set(left.times(right).export());  // Recursive.
 *         }
 *    };[/code]
 *    
 *    The concurrent logic may have direct access to the class members
 *    (to avoid passing arguments). For example:[code]
 *    import javolution.context.ConcurrentContex.Logic;
 *    ... 
 *    public class Foo {
 *        private Matrix<Complex> A, B;
 *        private Vector<Complex> x, y, Ax, By;
 *        
 *        // Returns z = A*x + B*y
 *       public Vector<Complex> getZ() {
 *           ConcurrentContext.enter();
 *           try {
 *               ConcurrentContext.execute(new Logic() {
 *                    public void run(Object[] arg0) {
 *                        Ax = A.times(x).export(); 
 *                    }
 *                });
 *               ConcurrentContext.execute(new Logic() {
 *                    public void run(Object[] arg0) {
 *                        By = B.times(y).export(); 
 *                    }
 *                });
 *           } finally {
 *               ConcurrentContext.exit();
 *               return Ax.plus(By);
 *           }
 *       }   
 *   }[/code]</p>
 * 
 * <p> Finally, it should be noted that concurrent contexts ensure the same 
 *     behavior whether or not the execution is performed by the current
 *     thread or concurrent threads. In particular, the current {@link Context
 *     context} is inherited by concurrent threads and any exception raised
 *     during the concurrent logic executions is automatically propagated 
 *     to the current thread.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public class ConcurrentContext extends Context {

    /**
     * Holds the maximum number of concurrent thread allowed 
     * (see <a href="{@docRoot}/overview-summary.html#configuration">
     * Javolution Configuration</a> for details).
     */
    public static final Configurable/*<Integer>*/ CONCURRENCY 
        = new Configurable(concurrency());

    /**
     * Indicates if local concurrency is enabled.
     */
    private static final LocalContext.Reference ENABLED 
         = new LocalContext.Reference(new Boolean(true));

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new ConcurrentContext().getClass();

    /**
     * Holds the number of concurrent thread started in this context
     * and not completed yet.
     */
    private int _activeCount;

    /**
     * Holds any error occuring during concurrent execution.
     */
    private Throwable _error;

    /**
     * Holds the concurrent threads or <code>null</code> to inherit from
     * outer context.
     */
    private ConcurrentThread[] _threads;

    /**
     * Indicates if this context is enabled.
     */
    private boolean _isEnabled;

    /**
     * Default constructor. This context has no concurrent thread associated
     * with. The concurrent threads are inherited from outer contexts.
     * If this context is the top-most concurrent context, then new concurrent 
     * threads are created and started the first time that this context is
     * entered. The number of threads created is configurable (see <a href= 
     * "{@docRoot}/overview-summary.html#configuration">Javolution Configuration
     * </a> for details).
     */
    public ConcurrentContext() {
    }

    /**
     * Creates a concurrent context using the specified concurrent threads.
     * 
     * @param threads the concurrent threads available for dispatching.
     */
    public ConcurrentContext(ConcurrentThread[] threads) {
        _threads = threads;
    }

    /**
     * Returns the concurrent threads available to this concurrent 
     * context (inherited from outer concurrent contexts unless specified 
     * at construction). 
     * 
     * @return the concurrent threads available to this context or 
     *         <code>null</code> if none (e.g. context created using 
     *         default constructor and without outer concurrent context).
     */
    public final ConcurrentThread[] getConcurrentThreads() {
        if (_threads != null)
            return _threads;
        for (Context ctx = this.getOuter(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof ConcurrentContext) {
                ConcurrentContext that = (ConcurrentContext) ctx;
                if (that._threads != null)
                    return that._threads;
            }
        }
        return null;
    }

    /**
     * Terminates all the concurrent threads associated to this concurrent 
     * context.
     */
    public void clear() {
        if (_threads != null) {
            for (int i = 0; i < _threads.length; i++) {
                _threads[i].terminate();
            }
            _threads = null;
        }
    }

    /**
     * Returns the current concurrent context or <code>null<code> if the 
     * current thread has not been spawned from a concurrent context.  
     *
     * @return the current concurrent context.
     */
    public static/*ConcurrentContext*/Context current() {
        Context ctx = Context.current();
        while (ctx != null) {
            if (ctx instanceof ConcurrentContext)
                return (ConcurrentContext) ctx;
            ctx = ctx.getOuter();
        }
        return null;
    }

    /**
     * Enters a {@link ConcurrentContext}.
     */
    public static void enter() {
        Context.enter(ConcurrentContext.CLASS);
    }

    /**
     * Exits the current {@link ConcurrentContext}. This method blocks until all
     * concurrent executions within the current context are completed.
     * Errors and exceptions raised in concurrent threads are propagated here.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of ConcurrentContext. 
     * @throws ConcurrentException propagates any error or exception raised
     *         during the execution of a concurrent logic.
     */
    public static void exit() {
        Context.exit(ConcurrentContext.CLASS);
    }

    /**
     * Enables/disables {@link LocalContext local} concurrency.
     * 
     * @param enabled <code>true</code> if concurrency is locally enabled;
     *        <code>false</code> otherwise.
     */
    public static void setEnabled(boolean enabled) {
        ENABLED.set(enabled ? TRUE : FALSE);
    }

    private static final Boolean TRUE = new Boolean(true); // CLDC 1.0

    private static final Boolean FALSE = new Boolean(false); // CLDC 1.0

    /**
     * Indicates if concurrency is {@link LocalContext locally} enabled
     * (default <code>true</code>).
     * 
     * @return <code>true</code> if concurrency is locally enabled;
     *         <code>false</code> otherwise.
     */
    public static boolean isEnabled() {
        return ((Boolean) ENABLED.get()).booleanValue();
    }

    /**
     * Executes the specified logic by a {@link ConcurrentThread} when possible.
     * The specified logic is always executed within a {@link PoolContext}.
     * It inherits the context stack, priority and memory area of the 
     * dispatching thread. Any exception or error during execution is propagated
     * to the current thread upon {@link #exit} of the concurrent context.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     */
    public static void execute(Logic logic) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx.executeByCurrentThread(logic, Logic.NO_ARG);
    }

    /**
     * Executes the specified logic with the specified argument.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the logic argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     */
    public static void execute(Logic logic, Object arg0) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, arg0, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args1[0] = arg0;
        ctx.executeByCurrentThread(logic, ctx._args1);
    }

    private final Object[] _args1 = new Object[1];

    /**
     * Executes the specified logic with the specified two arguments.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the first argument.
     * @param  arg1 the second argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0, Object arg1) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, arg0, arg1, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args2[0] = arg0;
        ctx._args2[1] = arg1;
        ctx.executeByCurrentThread(logic, ctx._args2);
    }

    private final Object[] _args2 = new Object[2];

    /**
     * Executes the specified logic with the specified three arguments.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the first argument.
     * @param  arg1 the second argument.
     * @param  arg2 the third argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0, Object arg1,
            Object arg2) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, arg0, arg1, arg2, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args3[0] = arg0;
        ctx._args3[1] = arg1;
        ctx._args3[2] = arg2;
        ctx.executeByCurrentThread(logic, ctx._args3);
    }

    private final Object[] _args3 = new Object[3];

    /**
     * Executes the specified logic with the specified four arguments.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the first argument.
     * @param  arg1 the second argument.
     * @param  arg2 the third argument.
     * @param  arg3 the fourth argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0, Object arg1,
            Object arg2, Object arg3) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i]
                        .execute(logic, arg0, arg1, arg2, arg3, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args4[0] = arg0;
        ctx._args4[1] = arg1;
        ctx._args4[2] = arg2;
        ctx._args4[3] = arg3;
        ctx.executeByCurrentThread(logic, ctx._args4);
    }

    private final Object[] _args4 = new Object[4];

    /**
     * Executes the specified logic with the specified five arguments.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the first argument.
     * @param  arg1 the second argument.
     * @param  arg2 the third argument.
     * @param  arg3 the fourth argument.
     * @param  arg4 the fifth argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0, Object arg1,
            Object arg2, Object arg3, Object arg4) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, arg0, arg1, arg2, arg3, arg4,
                        ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args5[0] = arg0;
        ctx._args5[1] = arg1;
        ctx._args5[2] = arg2;
        ctx._args5[3] = arg3;
        ctx._args5[4] = arg4;
        ctx.executeByCurrentThread(logic, ctx._args5);
    }

    private final Object[] _args5 = new Object[5];

    /**
     * Executes the specified logic with the specified six arguments.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the first argument.
     * @param  arg1 the second argument.
     * @param  arg2 the third argument.
     * @param  arg3 the fourth argument.
     * @param  arg4 the fifth argument.
     * @param  arg5 the sixth argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0, Object arg1,
            Object arg2, Object arg3, Object arg4, Object arg5) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        if (ctx._isEnabled) {
            ConcurrentThread[] threads = ctx.getConcurrentThreads();
            MemoryArea area = RealtimeThread.getCurrentMemoryArea();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].execute(logic, arg0, arg1, arg2, arg3, arg4,
                        arg5, ctx, area)) {
                    synchronized (ctx) {
                        ctx._activeCount++;
                        return;
                    }
                }
            }
        }
        ctx._args6[0] = arg0;
        ctx._args6[1] = arg1;
        ctx._args6[2] = arg2;
        ctx._args6[3] = arg3;
        ctx._args6[4] = arg4;
        ctx._args6[5] = arg5;
        ctx.executeByCurrentThread(logic, ctx._args6);
    }

    private final Object[] _args6 = new Object[6];

    // Implements Context abstract method.
    protected void enterAction() {
        _error = null;
        _activeCount = 0;
        _isEnabled = ConcurrentContext.isEnabled();
        if (getConcurrentThreads() == null) {
            // No thread associated with or inherited (first time entered).
            MemoryArea contextArea = MemoryArea.getMemoryArea(this);
            contextArea.executeInArea(new Runnable() {
                public void run() {
                    _threads = new ConcurrentThread[((Integer)CONCURRENCY.get()).intValue()];
                    for (int i = 0; i < _threads.length; i++) {
                        _threads[i] = new ConcurrentThread();
                        _threads[i].start();
                    }
                }
            });
        }
    }

    // Implements Context abstract method.
    protected void exitAction() {
        synchronized (this) {
            while (_activeCount > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new ConcurrentException(e);
                }
            }
        }
        // Propagates any concurrent error to current thread.
        if (_error != null) {
            if (_error instanceof ConcurrentException) 
                throw (ConcurrentException)_error;
            throw new ConcurrentException(_error);
        }
    }

    /**
     * Stores the first error occuring during a concurrent execution.
     * 
     * @param error an error raised while executing some concurrent logic.
     */
    synchronized void setError(Throwable error) {
        if (_error == null) { // First error.
            _error = error;
        } // Else ignores subsequent errors.
    }

    synchronized void decreaseActiveCount() {
        _activeCount--;
        this.notify();
    }

    private void executeByCurrentThread(Logic logic, Object[] args) {
        PoolContext.enter();
        try {
            logic.run(args);
        } catch (Throwable error) {
            setError(error);
        } finally {
            PoolContext.exit();
            for (int i = 0; i < args.length;) {
                args[i++] = null;
            }
        }
    }

    /**
     * <p> This abstract class represents some parameterized code which may be
     *     executed concurrently.</p>
     */
    public static abstract class Logic implements Runnable {

        /**
         * Executes this logic with no arguments.
         */
        public final void run() {
            run(NO_ARG);
        }

        private static final Object[] NO_ARG = new Object[0];

        /**
         * Executes this logic with the specified arguments.
         * 
         * @param args the arguments. The number of arguments depends upon
         *        the <code>ConcurrentContext.execute</code> method which 
         *        has been called (e.g. if {@link ConcurrentContext#execute(
         *        ConcurrentContext.Logic, Object, Object)},
         *        has been called, then <code>(args.length == 2)</code>).
         */
        public abstract void run(Object[] args);
    }

    /**
     * Returns the maximum number of concurrent thread.
     * 
     * @return <code>(Number of Processors) - 1</code>
     * @see javolution.context.ConcurrentThread
     */
    private static Integer concurrency() {
        Reflection.Method availableProcessors = Reflection
                .getMethod("java.lang.Runtime.availableProcessors()");
        if (availableProcessors != null) {
            Integer processors = (Integer) availableProcessors.invoke(Runtime
                    .getRuntime());
            return new Integer(processors.intValue() - 1);
        } else {
            return new Integer(0);
        }
    }
}