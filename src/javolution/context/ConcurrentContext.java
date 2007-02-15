/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2me.lang.ThreadLocal;
import j2me.lang.UnsupportedOperationException;

import javolution.context.ConcurrentExecutor.Status;
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
 * <p> Concurrent logics always execute within the same {@link Context} as 
 *     the calling thread, in other words if the main thread runs in a 
 *     {@link PoolContext}, concurrent executions are performed in the
 *     same {@link PoolContext} as well.</p>
 *     
 * <p> Concurrent contexts are easy to use, and provide automatic 
 *     load-balancing between processors with almost no overhead. Here is
 *     an example of <b>concurrent/recursive/clean</b> implementation of the 
 *     Karatsuba multiplication for large integers:[code]
 *     import javolution.context.PoolContext.Reference;
 *     ...
 *     public LargeInteger multiply(LargeInteger that) {
 *         if (that._size <= 1) {
 *             return multiply(that.longValue()); // Direct multiplication.
 *             
 *         } else { // Karatsuba multiplication in O(n^log2(3))
 *             PoolContext.enter();  // Avoids generating garbage (also faster).
 *             try {
 *                 int bitLength = this.bitLength();
 *                 int n = (bitLength >> 1) + (bitLength & 1);
 *                 
 *                 // this = a + 2^n b,   that = c + 2^n d
 *                 LargeInteger b = this.shiftRight(n);
 *                 LargeInteger a = this.minus(b.shiftLeft(n));
 *                 LargeInteger d = that.shiftRight(n);
 *                 LargeInteger c = that.minus(d.shiftLeft(n));
 *                 Reference<LargeInteger> ac = Reference.newInstance();
 *                 Reference<LargeInteger> bd = Reference.newInstance();
 *                 Reference<LargeInteger> abcd = Reference.newInstance();
 *                 ConcurrentContext.enter();
 *                 try { 
 *                     ConcurrentContext.execute(MULTIPLY, a, c, ac);
 *                     ConcurrentContext.execute(MULTIPLY, b, d, bd);
 *                     ConcurrentContext.execute(MULTIPLY, a.plus(b), c.plus(d), abcd);
 *                 } finally {
 *                     ConcurrentContext.exit(); // Waits for all concurrent threads to complete.
 *                 }
 *                 
 *                 // a*c + ((a+b)*(c+d)-a*c-b*d) 2^n + b*d 2^2n 
 *                 LargeInteger product = ac.get().plus(abcd.get().minus(ac.get()).minus(bd.get()).shiftLeft(n)).plus(bd.get().shiftLeft(2 * n));
 *                 return product.export();
 *             } finally {
 *                 PoolContext.exit();
 *             }    
 *         }
 *     }
 *     private static final Logic MULTIPLY = new Logic() {
 *         public void run() {
 *             LargeInteger left = getArgument(0);
 *             LargeInteger right = getArgument(1);
 *             Reference<LargeInteger> result = getArgument(2);
 *             result.set(left.times(right));  // Recursive.
 *         }
 *    };[/code]
 *    
 *    Here is a concurrent/recursive quick/merge sort using anonymous inner 
 *    classes (same as in Javolution  
 *    <a href="http://javolution.org/doc/benchmark.html">benchmark</a>):[code]
 *    private void quickSort(final FastTable<? extends Comparable> table) {
 *        final int size = table.size();
 *        if (size < 100) { 
 *            table.sort(); // Direct quick sort.
 *        } else {
 *            // Splits table in two and sort both part concurrently.
 *            final FastTable<? extends Comparable> t1 = FastTable.newInstance();
 *            final FastTable<? extends Comparable> t2 = FastTable.newInstance();
 *            ConcurrentContext.enter();
 *            try {
 *                ConcurrentContext.execute(new Logic() {
 *                    public void run() {
 *                        t1.addAll(table.subList(0, size / 2));
 *                        quickSort(t1); // Recursive.
 *                    }
 *                });
 *                ConcurrentContext.execute(new Logic() {
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
 *           }
 *           FastTable.recycle(t1);  
 *           FastTable.recycle(t2);
 *        }
 *     }[/code]
 * 
 * <p> Finally, it should be noted that concurrent contexts ensure the same 
 *     behavior whether or not the execution is performed by the current
 *     thread or concurrent threads. In particular, the current {@link Context
 *     context} is inherited by concurrent threads and any exception raised
 *     during the concurrent logic executions is automatically propagated 
 *     to the current thread.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.1, December 1, 2006
 */
public class ConcurrentContext extends Context {

    /**
     * Holds the context factory.
     */
    private static Factory FACTORY = new Factory() {
        protected Object create() {
            return new ConcurrentContext();
        }
    };

    /**
     * Holds the default number of {@link ConcurrentExecutor concurrent 
     * executors} (see <a href="{@docRoot}/overview-summary.html#configuration">
     * Javolution Configuration</a> for details).
     */
    public static final Configurable/*<Integer>*/CONCURRENCY = new Configurable(
            concurrency());

    /**
     * Indicates if local concurrency is enabled.
     */
    private static final LocalContext.Reference ENABLED = new LocalContext.Reference(
            new Boolean(true));

    /**
     * Holds the thread-local arguments.
     */
    private static final ThreadLocal ARGUMENTS = new ThreadLocal();

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new ConcurrentContext().getClass();

    /**
     * Holds the default executors.
     */
    private static transient ConcurrentExecutor[] _DefaultExecutors;

    /**
     * Holds the number of concurrent thread initiated (might not have 
     * started yet).
     */
    private int _initiatedCount;

    /**
     * Holds the number of concurrent thread execution completed.
     */
    private int _completedCount;

    /**
     * Holds any error occuring during concurrent execution.
     */
    private Throwable _error;

    /**
     * Holds this context executors or <code>null</code> to inherit 
     * concurrent executors from outer concurrent context.
     */
    private ConcurrentExecutor[] _executors;

    /**
     * Indicates if this context is enabled.
     */
    private boolean _isEnabled;

    /**
     * Holds the inherited exectors. 
     */
    private ConcurrentExecutor[] _inheritedExecutors;

    /**
     * Creates a concurrent context inheriting its executors from 
     * the outer concurrent context (or the {@link #getDefaultExecutors() default
     * executors} if none).
     */
    public ConcurrentContext() {
        this(null);
    }

    /**
     * Creates a concurrent context using the specified concurrent executors.
     * 
     * @param executors the concurrent executors available for dispatching.
     */
    public ConcurrentContext(ConcurrentExecutor[] executors) {
        _executors = executors;
    }

    /**
     * Returns the default executors. The number of executors is configurable 
     * (see <a href= 
     * "{@docRoot}/overview-summary.html#configuration">Javolution Configuration
     * </a> for details).
     * 
     * @return the default concurrent executors.
     */
    public static ConcurrentExecutor[] getDefaultExecutors() {
        if (_DefaultExecutors != null)
            return _DefaultExecutors;
        synchronized (CLASS) { // Creates the default.
            if (_DefaultExecutors != null)
                return _DefaultExecutors; // Synchronized check.
            int concurrency = ((Integer) CONCURRENCY.get()).intValue();
            ConcurrentThread[] executors = new ConcurrentThread[concurrency];
            for (int i = 0; i < concurrency; i++) {
                executors[i] = new ConcurrentThread();
                executors[i].start();
            }
            _DefaultExecutors = executors;
            return _DefaultExecutors;
        }
    }

    /**
     * Sets the default concurrent executors.
     * 
     * @param executors the new default concurrent executors.
     */
    public static void setDefaultExecutors(ConcurrentExecutor[] executors) {
        _DefaultExecutors = executors;
    }

    /**
     * Returns the concurrent executors available to this concurrent 
     * context (inherited from outer concurrent contexts).
     * 
     * @return the concurrent executors available to this context.
     */
    final ConcurrentExecutor[] getExecutors() {
        if (_executors != null)
            return _executors;
        for (Context ctx = this.getOuter(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof ConcurrentContext) {
                ConcurrentContext that = (ConcurrentContext) ctx;
                if (that._executors != null)
                    return that._executors;
            }
        }
        return getDefaultExecutors();
    }

    /**
     * Terminates the executors for this concurrent context (if any). 
     */
    public void clear() {
        if (_executors != null) {
            for (int i = 0; i < _executors.length; i++) {
                _executors[i].terminate();
            }
            _executors = null;
        }
    }

    /**
     * Returns the current concurrent context or <code>null</code> if the 
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
     * Enters a {@link ConcurrentContext} possibly recycled. 
     * The new concurrent context is enabled/disabled based upon 
     * the local {@link #isEnabled()} status.
     */
    public static void enter() {
        ConcurrentContext ctx = (ConcurrentContext) FACTORY.object();
        ctx._isInternal = true;
        ctx._isEnabled = ConcurrentContext.isEnabled();
        Context.enter(ctx);
    }

    private transient boolean _isInternal;

    /**
     * Exits and recycles the current {@link ConcurrentContext}.
     *
     * @throws UnsupportedOperationException if the current context 
     *         has not been entered using ConcurrentContext.enter() 
     */
    public static void exit() {
        ConcurrentContext ctx = (ConcurrentContext) Context.current();
        if (!ctx._isInternal)
            throw new UnsupportedOperationException(
                    "The context to exit must be specified");
        ctx._isInternal = false;
        Context.exitNoCheck(ctx);
        FACTORY.recycle(ctx);
    }

    /**
     * Enables/disables {@link LocalContext local} concurrency.
     * 
     * @param enabled <code>true</code> if concurrency is locally enabled;
     *        <code>false</code> otherwise.
     * @see  #enter
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
     * @see  #enter
     */
    public static boolean isEnabled() {
        return ((Boolean) ENABLED.get()).booleanValue();
    }

    /**
     * Executes the specified logic by a {@link ConcurrentExecutor} if 
     * one available. Any exception or error during execution is propagated
     * to the current thread upon {@link #exit} of the concurrent context.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     */
    public static void execute(Logic logic) {
        ConcurrentContext ctx = (ConcurrentContext) current();
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args0;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args1;
        status._args[0] = arg0;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args2;
        status._args[0] = arg0;
        status._args[1] = arg1;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args3;
        status._args[0] = arg0;
        status._args[1] = arg1;
        status._args[2] = arg2;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args4;
        status._args[0] = arg0;
        status._args[1] = arg1;
        status._args[2] = arg2;
        status._args[3] = arg3;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args5;
        status._args[0] = arg0;
        status._args[1] = arg1;
        status._args[2] = arg2;
        status._args[3] = arg3;
        status._args[4] = arg4;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

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
        
        StatusImpl status = (StatusImpl) StatusImpl.FACTORY.object();
        status._args = status._args6;
        status._args[0] = arg0;
        status._args[1] = arg1;
        status._args[2] = arg2;
        status._args[3] = arg3;
        status._args[4] = arg4;
        status._args[5] = arg5;
 
        if ((ctx != null) && (ctx.doExecute(logic, status)))
                return; // Concurrent execution.
        // Else current thread execution.
        ARGUMENTS.set(status._args);
        logic.run();
        StatusImpl.FACTORY.recycle(status);
    }

    // Implements Context abstract method.
    protected void enterAction() {
        _inheritedExecutors = getExecutors();
        _error = null;
        _initiatedCount = 0;
        _completedCount = 0;
    }

    // Implements Context abstract method.
    protected void exitAction() {
        synchronized (this) {
            while (_initiatedCount != _completedCount) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new ConcurrentException(e);
                }
            }
        }
        // Propagates any concurrent error to current thread.
        if (_error != null) {
            if (_error instanceof RuntimeException)
                throw ((RuntimeException) _error);
            if (_error instanceof Error)
                throw ((Error) _error);
            throw new ConcurrentException(_error); // Wrapper.
        }
    }
   
    private boolean doExecute(Logic logic, StatusImpl status) {
        if (_isEnabled) {
            status._context = this;
            ConcurrentExecutor[] executors = _inheritedExecutors;
            for (int i = 0; i < executors.length; i++) {
                if (executors[i].execute(logic, status)) {
                    _initiatedCount++;
                    return true; // Done.
                }
            }
        }
        return false;
    }
    
    /**
     * <p> This abstract class represents some parameterized code which may be
     *     executed concurrently.</p>
     */
    public static abstract class Logic implements Runnable {

        /**
         * Returns the arguments (if any) for this logic execution.
         * 
         * @return an array holding the arguments.
         */
        public Object[] getArguments() {
            return (Object[]) ARGUMENTS.get();
        }

        /**
         * Returns the specified arguments for this logic execution.
         * 
         * @return an array holding the arguments.
         */
        public/*<T>*/Object/*{T}*/getArgument(int i) {
            return (Object/*{T}*/) ((Object[]) ARGUMENTS.get())[i];
        }

        /**
         * Executes this logic, arguments (if any) can be retrieved using  
         * the {@link #getArguments} method.
         */
        public abstract void run();

    }

    /**
     * Returns the maximum number of concurrent thread.
     * 
     * @return <code>(Number of Processors)</code>
     * @see javolution.context.ConcurrentThread
     */
    private static Integer concurrency() {
        Reflection.Method availableProcessors = Reflection
                .getMethod("java.lang.Runtime.availableProcessors()");
        if (availableProcessors != null) {
            Integer processors = (Integer) availableProcessors.invoke(Runtime
                    .getRuntime());
            return new Integer(processors.intValue());
        } else {
            return new Integer(1);
        }
    }

    /**
     * Returns the maximum number of concurrent thread.
     * 
     * @return <code>(Number of Processors) - 1</code>
     * @see javolution.context.ConcurrentThread
     */
    private static class StatusImpl extends RealtimeObject implements Status {

        private static StatusImpl.Factory FACTORY = new Factory() {
            protected Object create() {
                return new StatusImpl();
            }
            protected void cleanup(Object status) {
                ((StatusImpl)status).reset();
            }

        };

        volatile Object[] _args;

        Object[] _args0 = new Object[0];

        Object[] _args1 = new Object[1];

        Object[] _args2 = new Object[2];

        Object[] _args3 = new Object[3];

        Object[] _args4 = new Object[4];

        Object[] _args5 = new Object[5];

        Object[] _args6 = new Object[6];

        ConcurrentContext _context;

        public void started() {
            ARGUMENTS.set(_args);
            Context.setCurrent(_context);
            _context.getLocalPools().activatePools();
        }

        public void completed() {
            _context.getLocalPools().deactivatePools();
 
            // Must be last (as the status may be reused after).
            synchronized (_context) {
                _context._completedCount++;
                _context.notify();
            }
        }

        public void error(Throwable error) {
            synchronized (_context) {
                if (_context._error == null) {
                    _context._error = error;
                }
            }
        }

        void reset() {
            for (int i = 0; i < _args.length; i++) {
                _args[i] = null; // 
            }
            _context = null;
        }
    }
}