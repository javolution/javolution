/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.Configuration;
import javolution.lang.Reflection;

/**
 * <p> This class represents "worker" threads employed by 
 *     {@link ConcurrentContext} to perform concurrent 
 *     {@link ConcurrentContext#execute executions} on multi-processors
 *     systems.</p>
 *     
 * <p> Instances of this class are created at start-up and maintained
 *     on-standby in order to execute quickly.</p>
 *     
 * <p> The default context for instances of this class is a {@link PoolContext},
 *     unlike normal threads for which the default context is
 *     a {@link HeapContext}.</p>
 *     
 * <p> To avoid thread proliferation, the number of instance of this class 
 *     is voluntarily limited (see <a href=
 *     "{@docRoot}/overview-summary.html#configuration">Javolution 
 *     Configuration</a> for details). Using the default configuration,
 *     only systems with Hyper-Threading or multi-processors have instances 
 *     of this class.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class ConcurrentThread extends Thread {

    /**
     * Holds the maximum number of {@link ConcurrentThread}.
     */
    public static final int MAX = Configuration.concurrency();
    
    /**
     * Holds the concurrent threads instances.
     */
    private static final ConcurrentThread[] INSTANCES 
        = new ConcurrentThread[MAX];
    static {
        for (int i=0; i < MAX; i++) {
            INSTANCES[i] = new ConcurrentThread(i);
            INSTANCES[i].start();
        }
    }
    
    /**
     * Holds the concurrent context this thread belongs to.
     */
    private ConcurrentContext _concurrentContext;

    /**
     * Holds the number of logics to be executed.
     */
    int logicsLength;

    /**
     * Holds this thread's name.
     */
    private final String _name;

    /**
     * Creates a new concurrent thread with the specified index.
     * 
     * @param index the thread index in the range [0 .. {@link #MAX}[
     */
    private ConcurrentThread(int index) {
        _name = "ConcurrentThread-" + index;
        if (SET_DAEMON != null) {
            SET_DAEMON.invoke(this, new Boolean(true));
        }
    }
    private static final Reflection.Method SET_DAEMON 
        = Reflection.getMethod("java.lang.Thread.setDaemon(bool)");
    

    /**
     * Requests for all {@link ConcurrentThread} available to execute the logics
     * of the specified concurrent context.
     *
     * @return the actual number of threads working for this concurrent context.
     */
    static int execute(ConcurrentContext concurrentContext) {
        int concurrency = 0;
        if (MAX > 0) { 
            synchronized (INSTANCES) {
                for (int i=0; i < MAX; i++) {
                    if (INSTANCES[i]._concurrentContext == null) {
                        synchronized (INSTANCES[i]) {
                            INSTANCES[i]._concurrentContext = concurrentContext;
                            INSTANCES[i].notify();
                        }
                        concurrency++;
                    }
                }
            }
        }
        return concurrency;
    }

    /**
     * Overrides parent's <code>run</code> method.
     */
    public void run() {
        PoolContext rootContext = (PoolContext) Context.currentContext();
        while (true) {
           try {
               synchronized (this) {
                    while (_concurrentContext == null) {
                        this.wait();
                    }
               }
               rootContext.setOuter(_concurrentContext);
               while (_concurrentContext.executeNext()) {
                   rootContext.recyclePools();
               }
           } catch (Throwable e) {
               e.printStackTrace();   
           } finally {
               synchronized (INSTANCES) {  
                   _concurrentContext = null; // Thread available again.
               }
           }
        }
    }

    /**
     * Overrides parent's <code>toString</code> method.
     */
    public String toString() {
        return _name;
    }
}