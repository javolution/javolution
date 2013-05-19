/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.ConcurrentContext;
import javolution.lang.MathLib;

/**
 * Holds the default implementation of ConcurrentContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ConcurrentContextImpl extends ConcurrentContext {

    // Gets all the concurrent threads ready for executions.
    private static final int NB_THREADS = ConcurrentContext.CONCURRENCY.get();

    private static final ConcurrentThreadImpl[] EXECUTORS = new ConcurrentThreadImpl[NB_THREADS];

    static {
        for (int i = 0; i < NB_THREADS; i++) {
            EXECUTORS[i] = new ConcurrentThreadImpl();
            EXECUTORS[i].start();
        }
    }

    private Throwable error;

    private int initiatedCount; // Nbr of concurrent task initiated.

    private int completedCount; // Nbr of concurrent task completed.

    private int concurrency;

    @Override
    protected ConcurrentContext inner() {
        ConcurrentContextImpl ctx = new ConcurrentContextImpl();
        int n = ConcurrentContext.CONCURRENCY.get();
        ctx.concurrency = MathLib.min(n, NB_THREADS); 
        return ctx;
    }

    @Override
    public void execute(Runnable logic) {
        // Find a concurrent thread not busy, limit the search based on concurrency.
        for (int i = NB_THREADS - concurrency; i < NB_THREADS; i++) {
            ConcurrentThreadImpl thread = EXECUTORS[i];
            if (thread.execute(logic, this)) {
                initiatedCount++;
                return;
            }
        }
        try {
            logic.run();
        } catch (Throwable e) {
            error = e;
        }
    }

    @Override
    public synchronized void exit() {
        super.exit();
        try {
            while (initiatedCount != completedCount) {
                this.wait();
            }
        } catch (InterruptedException ex) {
            this.error = ex;
        }
        if (error == null) return; // Everything fine.
        if (error instanceof RuntimeException) throw (RuntimeException) error;
        if (error instanceof Error) throw (Error) error;
        throw new RuntimeException(error);
    }

    @Override
    public void setCurrent() {
        super.setCurrent();
    }

    // Informs this context of the completion of a task (with possible error).
    public synchronized void completed(Throwable error) {
        if (error != null) {
            this.error = error;
        }
        completedCount++;
        this.notify();
    }

}
