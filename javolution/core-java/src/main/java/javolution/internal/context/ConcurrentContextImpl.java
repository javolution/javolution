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

/**
 * Holds the default implementation of ConcurrentContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ConcurrentContextImpl extends ConcurrentContext {
         /**
         * Holds the concurrent executors (created during class initialization).
         * The maximum concurrency cannot be changed afterward.
         */
        private static final ConcurrentThread[] _Executors = new ConcurrentThread[((Integer) MAXIMUM_CONCURRENCY.get()).intValue()];

        static {
            for (int i = 0; i < Default._Executors.length; i++) {
                Default._Executors[i] = new ConcurrentThread();
                Default._Executors[i].start();
            }
        }
        /**
         * Holds the concurrency.
         */
        private int _concurrency;
        /**
         * Holds any error occurring during concurrent execution.
         */
        private volatile Throwable _error;
        /**
         * Holds the number of concurrent execution initiated.
         */
        private int _initiated;
        /**
         * Holds the number of concurrent execution completed.
         */
        private int _completed;

        // Implements Context abstract method.
        protected void enterAction() {
            _concurrency = ConcurrentContext.getConcurrency();
        }

        // Implements ConcurrentContext abstract method.
        protected void executeAction(Runnable logic) {
            if (_error != null) {
                return; // No point to continue (there is an error).
            } 
            for (int i = _concurrency; --i >= 0;) {
                if (_Executors[i].execute(logic, this)) {
                    _initiated++;
                    return; // Done concurrently.
                }
            }
            // Execution by current thread.
            logic.run();
        }

        // Implements Context abstract method.
        protected void exitAction() {
            try {
                if (_initiated != 0) {
                    synchronized (this) {
                        while (_initiated != _completed) {
                            try {
                                this.wait();
                            } catch (InterruptedException e) {
                                throw new ConcurrentException(e);
                            }
                        }
                    }
                }
                if (_error != null) {
                    if (_error instanceof RuntimeException) {
                        throw ((RuntimeException) _error);
                    }
                    if (_error instanceof Error) {
                        throw ((Error) _error);
                    }
                    throw new ConcurrentException(_error); // Wrapper.
                }
            } finally {
                _error = null;
                _initiated = 0;
                _completed = 0;
            }
        }

        // Called when a concurrent execution starts.
        void started() {
            AbstractContext.setConcurrentContext(this);
        }

        // Called when a concurrent execution finishes. 
        void completed() {
            synchronized (this) {
                _completed++;
                this.notify();
            }
            AllocatorContext.getCurrentAllocatorContext().deactivate();
        }

        // Called when an error occurs.
        void error(Throwable error) {
            synchronized (this) {
                if (_error == null) {
                    _error = error;
                }
            }
        }
}
