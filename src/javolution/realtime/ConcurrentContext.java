/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.JavolutionError;

/**
 * <p> This class represents a concurrent context; it is used to accelerate
 *     execution of concurrent algorithms on multi-processors systems.</p>
 * <p> When a thread enters a concurrent context, it may execute a 
 *     concurrent {@link Logic logic} by calling any of the 
 *     <code>ConcurrentContext.execute(logic, arg0, arg1, ...)</code> 
 *     static methods.</p>
 * <p> Only after all concurrent executions are completed, is the current 
 *     thread allowed to exit the scope of the concurrent context 
 *     (internal synchronization).</p>
 * <p> The concurrent logics are always executed within a {@link PoolContext}, 
 *     either by a {@link ConcurrentThread} (if one ready) or by the current
 *     thread within an inner pool context. Consequently, {@link Realtime} 
 *     objects made available outside of the logic scope have to be 
 *     {@link RealtimeObject#export exported}.</p>  
 * <p> Concurrent contexts are easy to use, and provide automatic 
 *     load-balancing between processors with almost no overhead. 
 *     To avoid thread proliferation, the maximum concurrency is limited by 
 *     {@link ConcurrentThread#MAX}. Concurrency can also be locally
 *     {@link #setEnabled disabled}.</p>
 * <p> ConcurrentContext are extremely efficient in reducing garbage and 
 *     can often be used in place of pool contexts for this purpose. Here is
 *     an example of <b>concurrent/recursive/clean</b> (no garbage generated) 
 *     implementation of the Karatsuba multiplication for large integers:<pre>
 *     public LargeInteger multiply(LargeInteger that) {
 *         if (that._size <= 1) {
 *             return multiply(that.longValue()); // Direct multiplication.
 *         } else { // Karatsuba multiplication  in O(n<sup>Log(3)</sup>)
 *             int bitLength = this.bitLength();
 *             int n = (bitLength >> 1) + (bitLength & 1);
 *             FastMap results = FastMap.newInstance(3);
 *             ConcurrentContext.enter();
 *             try { // this = a + 2^n b,   that = c + 2^n d
 *                 LargeInteger b = this.shiftRight(n);
 *                 LargeInteger a = this.subtract(b.shiftLeft(n));
 *                 LargeInteger d = that.shiftRight(n);
 *                 LargeInteger c = that.subtract(d.shiftLeft(n));
 *                 ConcurrentContext.execute(MULTIPLY, a, c, "ac", results);
 *                 ConcurrentContext.execute(MULTIPLY, b, d, "bd", results);
 *                 ConcurrentContext.execute(MULTIPLY, a.add(b), c.add(d), "abcd", results);
 *             } finally {
 *                 ConcurrentContext.exit();
 *             }
 *             LargeInteger ac = (LargeInteger) results.get("ac");
 *             LargeInteger bd = (LargeInteger) results.get("bd");
 *             LargeInteger abcd = (LargeInteger) results.get("abcd");
 *             return ac.add(abcd.subtract(ac).subtract(bd).shiftLeft(n)).add(bd.shiftLeft(2 * n));
 *         }
 *     }
 *     private static final Logic MULTIPLY = new Logic() {
 *         public void run(Object[] args) {
 *             LargeInteger left = (LargeInteger) args[0];
 *             LargeInteger right = (LargeInteger) args[1];
 *             LargeInteger product = left.multiply(right); // Recursive.
 *             FastMap results = (FastMap) args[3];
 *             synchronized (results) {
 *                 results.put(args[2], product.export());
 *             }
 *         }
 *    };</pre>
 * 
 * <p> Finally, it should be noted that any exceptions raised during concurrent
 *     logic executions are propagated to the current thread upon 
 *     {@link #exit} of the concurrent context.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class ConcurrentContext extends Context {

    /**
     * Holds the queue size for concurrent logic executions. 
     */
    private static final int QUEUE_SIZE = 256;

    /**
     * Holds the maximum number of arguments. 
     */
    private static final int ARGS_SIZE = 6;

    /**
     * Holds the pending logics.
     */
    private Logic[] _logics = new Logic[QUEUE_SIZE];

    /**
     * Holds the pending logics arguments.
     */
    private Object[][] _args = new Object[QUEUE_SIZE][];

    /**
     * Holds the arguments pool.
     */
    private Object[][][] _argsPool = new Object[QUEUE_SIZE][ARGS_SIZE][];

    /**
     * Holds the number of pending logics.
     */
    private int _logicsCount;

    /**
     * Indicates if local concurrency is enabled.
     */
    private static final LocalContext.Variable ENABLED 
        = new LocalContext.Variable(new Boolean(true));

    /**
     * Holds the concurrency of this context (number of concurrent thread
     * executing).
     */
    private int _concurrency;

    /**
     * Holds the number of threads having completed their execution
     * (including the current thread).
     */
    private int _threadsDone;

    /**
     * Holds any error occuring during concurrent execution.
     */
    private Throwable _error;

    /**
     * Default constructor.
     */
    ConcurrentContext() {
    }

    /**
     * Enables/disables {@link LocalContext local} concurrency.
     * 
     * @param enabled <code>true</code> if concurrency is locally enabled;
     *        <code>false</code> otherwise.
     */
    public static void setEnabled(boolean enabled) {
        ENABLED.setValue(new Boolean(enabled));
    }

    /**
     * Indicates if concurrency is {@link LocalContext locally} enabled
     * (default <code>true</code>).
     * 
     * @return <code>true</code> if concurrency is locally enabled;
     *         <code>false</code> otherwise.
     */
    public static boolean isEnabled() {
        return ((Boolean) ENABLED.getValue()).booleanValue();
    }

    /**
     * Enters a {@link ConcurrentContext}.
     */
    public static void enter() {
        ConcurrentContext ctx = (ConcurrentContext) push(CONCURRENT_CONTEXT_CLASS);
        if (ctx == null) {
            ctx = new ConcurrentContext();
            push(ctx);
        }
    }
    private static final Class CONCURRENT_CONTEXT_CLASS = new ConcurrentContext().getClass();

    /**
     * Executes the specified logic by a {@link ConcurrentThread} when possible.
     * The specified logic is always executed within a {@link PoolContext}
     * and inherits the context of the parent thread. Any exception or error
     * during execution will be propagated to the current thread upon 
     * {@link #exit} of the concurrent context.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     */
    public static void execute(Logic logic) {
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        ctx._args[ctx._logicsCount] = Logic.NO_ARG;
        ctx._logics[ctx._logicsCount++] = logic;
    }

    /**
     * Executes the specified logic with the specified argument.
     * 
     * @param  logic the logic to execute concurrently when possible.
     * @param  arg0 the logic argument.
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @see    #execute(ConcurrentContext.Logic)
     */
    public static void execute(Logic logic, Object arg0) {
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(1);
        args[0] = arg0;
        ctx._logics[ctx._logicsCount++] = logic;
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
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(2);
        args[0] = arg0;
        args[1] = arg1;
        ctx._logics[ctx._logicsCount++] = logic;
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
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(3);
        args[0] = arg0;
        args[1] = arg1;
        args[2] = arg2;
        ctx._logics[ctx._logicsCount++] = logic;
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
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(4);
        args[0] = arg0;
        args[1] = arg1;
        args[2] = arg2;
        args[3] = arg3;
        ctx._logics[ctx._logicsCount++] = logic;
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
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(5);
        args[0] = arg0;
        args[1] = arg1;
        args[2] = arg2;
        args[3] = arg3;
        args[4] = arg4;
        ctx._logics[ctx._logicsCount++] = logic;
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
        ConcurrentContext ctx = (ConcurrentContext) currentContext();
        if (ctx._logicsCount >= QUEUE_SIZE) {
            ctx.flush();
        }
        Object[] args = ctx.getArgs(6);
        args[0] = arg0;
        args[1] = arg1;
        args[2] = arg2;
        args[3] = arg3;
        args[4] = arg4;
        args[5] = arg5;
        ctx._logics[ctx._logicsCount++] = logic;
    }

    /**
     * Exits the {@link ConcurrentContext}. This method blocks until all
     * concurrent executions within the current context are completed.
     * Errors and exceptions raised in concurrent threads are propagated here.
     *
     * @throws ClassCastException if the current context is not a
     *         {@link ConcurrentContext}.
     * @throws ConcurrentException propagates any error or exception raised
     *         during the execution of a concurrent logic.
     */
    public static void exit() throws ConcurrentException {
        ConcurrentContext ctx = (ConcurrentContext) Context.currentContext();
        ctx.flush(); // Executes remaining logics.
        Context.pop();

        // Propagates any concurrent error to current thread.
        if (ctx._error != null) {
            ConcurrentException error = new ConcurrentException(ctx._error);
            ctx._error = null; // Resets error flag.
            throw error;
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

    /**
     * Executes the next pending logic.
     * 
     * @return <code>true</code> if some logics has been executed;
     *         <code>false</code> if there is no pending logic to execute.
     */
    boolean executeNext() {
        int index;
        synchronized (this) {
            if (_logicsCount > 0) {
                index = --_logicsCount;
            } else {
                _threadsDone++;
                this.notify();
                return false;
            }
        }
        try {
            Object[] args = _args[index];
            _logics[index].run(args);
            _logics[index] = null;
            for (int j = args.length; j > 0;) {
                args[--j] = null;
            }
        } catch (Throwable error) {
            setError(error);
        }
        return true;
    }

    /**
     * Executes all pending logics (blocking).
     */
    private void flush() {
        // Current thread enters inner pool context in order to ensure
        // that concurrent threads have no access to its local pools.
        PoolContext.enter();
        try {
            _concurrency = ConcurrentContext.isEnabled() ? ConcurrentThread
                    .execute(this) : 0;
            while (executeNext()) {
                ((PoolContext) Context.currentContext()).recyclePools();
            }
            synchronized (this) {
                while (_threadsDone <= _concurrency) {
                    this.wait();
                } // Exit when _threadsDone = _concurrency + 1 (current thread)
            }
        } catch (InterruptedException e) {
            throw new JavolutionError(e);
        } finally {
            _threadsDone = 0;
            PoolContext.exit();
        }
    }

    /**
     * Gets the next arguments array.
     * 
     * @param  length the array length (> 0).
     * @return the next arguments array available.
     */
    private Object[] getArgs(int length) {
        Object[] args = _argsPool[_logicsCount][length - 1];
        if (args == null) {
            args = new Object[length];
            _argsPool[_logicsCount][length - 1] = args;
        }
        _args[_logicsCount] = args;
        return args;
    }

    /**
     * <p> This abstract class represents a concurrent logic.</p>
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

    // Implements abstract method.
    protected void dispose() {
        _logics = null;
        _args = null;
        _argsPool = null;
    }

}