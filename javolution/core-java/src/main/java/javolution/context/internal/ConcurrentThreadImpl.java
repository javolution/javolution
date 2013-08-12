/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.realtime.RealtimeThread;

import javolution.context.AbstractContext;

/**
 * A worker thread executing in a concurrent context.
 */
public class ConcurrentThreadImpl extends RealtimeThread { 

    private static int count;
    private ConcurrentContextImpl context;
    private AtomicBoolean isBusy = new AtomicBoolean();
    private Runnable logic;
    private int priority;

    /**
     * Default constructor.
     */
    public ConcurrentThreadImpl() {
        this.setName("ConcurrentThread-" + ++count);
        setDaemon(true);
    }

    /**
     * Executes the specified logic by this thread if ready; returns
     * {@code false} if this thread is busy.
     */
    public boolean execute(Runnable logic, ConcurrentContextImpl inContext) {
        if (!isBusy.compareAndSet(false, true))
            return false;
        synchronized (this) {
            this.priority = Thread.currentThread().getPriority();
            this.context = inContext;
            this.logic = logic;
            this.notify();
        }
        return true;
    }

    @Override
    public void run() {
        while (true) { // Main loop.
            try {
                synchronized (this) {
                    while (logic == null) this.wait();
                }
                this.setPriority(priority);
                AbstractContext.inherit(context);
                logic.run();
                context.completed(null);
            } catch (Throwable error) {
                context.completed(error);
            }
            // Clean up.
            logic = null;
            context = null;
            AbstractContext.inherit(null);
            isBusy.set(false);
        }
    }

}
