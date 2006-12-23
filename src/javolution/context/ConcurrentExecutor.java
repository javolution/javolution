/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

/**
 * <p> This interface represents an executor (typically a thread)
 *     capable of executing a task concurrently.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
public interface ConcurrentExecutor extends Runnable {

    /**
     * Executes the specified logic concurrently if it can be done immediately.
     *
     * @param logic the logic to be executed.
     * @param status the status object to be notified during the course of the 
     *        concurrent execution.
     * @return <code>true</code> if this concurrent executor will execute
     *         the specified task; <code>false</code> otherwise.
     */
    boolean execute(Runnable logic, Status status);

    /**
     * Terminates this executor once the last pending concurrent execution is
     * completed.
     */
    void terminate();

    /**
     * This interface represents the status notification of a concurrent
     * execution.
     */
    interface Status {

        /**
         * This method is called when the concurrent execution is started.
         */
        void started();

        /**
         * This method is called when the concurrent execution is completed.
         */
        void completed();

        /**
         * This method is called when an exception has been raised during
         * the concurrent execution.
         */
        void error(Throwable error);

    }
}