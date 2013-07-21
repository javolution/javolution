/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import java.util.concurrent.atomic.AtomicBoolean;

import javolution.context.AbstractContext;

/**
 * A worker thread executing in a concurrent context.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class ConcurrentThreadImpl extends Thread { // TODO: Extends RealtimeThread

    private AtomicBoolean isFree = new AtomicBoolean();

    private int priority;

    private Runnable logic;

    private ConcurrentContextImpl inContext;

    /**
     * Default constructor.
     */
    public ConcurrentThreadImpl() {
        super("ConcurrentThread-" + ++count);
        setDaemon(true);
    }

    private static int count;

    /**
     * Executes the specified logic by this thread if ready; returns
     * <code>false</code> if this thread is busy.
     */
    public boolean execute(Runnable logic, ConcurrentContextImpl inContext) {
        if (!isFree.compareAndSet(true, false))
            return false;
        synchronized (this) {
            this.priority = Thread.currentThread().getPriority();
            this.inContext = inContext;
            this.logic = logic;
            this.notify();
            return true;
        }
    }

    @Override
    public synchronized void run() {
        while (true) { // Main loop.
            try {
                while (logic == null) { // Waits for work.
                    this.wait();
                }
                this.setPriority(priority);
                AbstractContext.inherit(inContext);
                logic.run();
                inContext.completed(null);
            } catch (Throwable error) {
                inContext.completed(error);
            }
            logic = null;
            inContext = null;
            AbstractContext.inherit(null);
            isFree.set(true);
        }
    }

}
