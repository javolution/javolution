/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import javolution.context.ConcurrentContext;
import javolution.lang.MathLib;

/**
 * Holds the default implementation of ConcurrentContext.
 */
public final class ConcurrentContextImpl extends ConcurrentContext {

    private int completedCount; // Nbr of concurrent task completed.
    private Throwable error; // Any error raised.

    private int initiatedCount; // Nbr of concurrent task initiated.
    private final ConcurrentContextImpl parent;
    private ConcurrentThreadImpl[] threads;

    /**
     * Default constructor (root).
     */
    public ConcurrentContextImpl() {
        this.parent = null;
        int nbThreads = ConcurrentContext.CONCURRENCY.get();
        threads = new ConcurrentThreadImpl[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            threads[i] = new ConcurrentThreadImpl();
            threads[i].start();
        }
    }

    /**
     * Inner implementation.
     */
    public ConcurrentContextImpl(ConcurrentContextImpl parent) {
        this.parent = parent;
        this.threads = parent.threads; // Inherit threads from parents.
    }

    // Informs this context of the completion of a task (with possible error).
    public synchronized void completed(Throwable error) {
        if (error != null) {
            this.error = error;
        }
        completedCount++;
        this.notify();
    }

    @Override
    public void execute(Runnable logic) {
        // Find a thread not busy.
        for (ConcurrentThreadImpl thread : threads) {
            if (thread.execute(logic, this)) {
                initiatedCount++;
                return;
            }
        }
        // Executes by current thread.
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
        if (error == null)
            return; // Everything fine.
        if (error instanceof RuntimeException)
            throw (RuntimeException) error;
        if (error instanceof Error)
            throw (Error) error;
        throw new RuntimeException(error);
    }

    @Override
    public int getConcurrency() {
        return threads.length;
    }

    @Override
    public void setConcurrency(int concurrency) {
        // The setting of the concurrency can only reduce the number 
        // of threads available in the context.
        int nbThreads = MathLib.min(parent.threads.length, concurrency);
        threads = new ConcurrentThreadImpl[nbThreads];
        for (int i = 0; i < nbThreads; i++) { // Reused from parent threads.
            threads[i] = parent.threads[i];
        }
    }

    @Override
    protected ConcurrentContext inner() {
        return new ConcurrentContextImpl(this);
    }

}
