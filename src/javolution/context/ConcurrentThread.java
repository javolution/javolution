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
import javolution.lang.Reflection;

/**
 * <p> This class represents the default {@link ConcurrentExecutor}
 *     implementation used by {@link ConcurrentContext}. Executions
 *     are performed in the same memory area and at the same priority
 *     as the calling thread.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
public class ConcurrentThread extends RealtimeThread implements
        ConcurrentExecutor {

    private volatile Runnable _logic;

    private MemoryArea _memoryArea;

    private int _priority;

    private Status _status;

    private boolean _terminate;

    private String _name;

    private Thread _source;

    /**
     * Default constructor.
     */
    public ConcurrentThread() {
        _name = "ConcurrentThread-" + getCount();
        if (SET_NAME != null) {
            SET_NAME.invoke(this, _name);
        }
        if (SET_DAEMON != null) {
            SET_DAEMON.invoke(this, new Boolean(true));
        }
    }

    private synchronized int getCount() {
        return _Count++;
    }

    private static int _Count;

    private static final Reflection.Method SET_NAME = Reflection
            .getMethod("java.lang.Thread.setName(String)");

    private static final Reflection.Method SET_DAEMON = Reflection
            .getMethod("java.lang.Thread.setDaemon(boolean)");

    /**
     * Executes the concurrent logics sequentially.
     */
    public void run() {
        while (true) { // Main loop.
            synchronized (this) { // Waits for a task.
                try {
                    while ((_logic == null) && !_terminate)
                        this.wait();
                } catch (InterruptedException e) {
                    throw new ConcurrentException(e);
                }
            }
            if (_logic == null)
                break; // Terminates.
            try {
                Thread current = Thread.currentThread();
                if (current.getPriority() != _priority) {
                    current.setPriority(_priority);
                }
                _status.started();
                _memoryArea.executeInArea(_logic);
            } catch (Throwable error) {
                _status.error(error);
            } finally {
                _status.completed();
                ((AllocatorContext) AllocatorContext.current()).deactivate();              
                _status = null;
                _source = null;
                _logic = null; // Last (ready).
            }
        }
    }

    // Implements ConcurrentExecutor
    public boolean execute(Runnable logic, Status status) {
        if (_logic != null)
            return false; // Shortcut to avoid synchronizing.
        synchronized (this) {
            if (_logic != null)
                return false; // Synchronized check.
            _memoryArea = RealtimeThread.getCurrentMemoryArea();
            _source = currentThread();
            if (_source instanceof ConcurrentThread) {
                _source = ((ConcurrentThread) _source)._source;
            }
            _priority = _source.getPriority();
            _status = status;
            _logic = logic; // Must be last.
            this.notify();
            return true;
        }
    }

    // Implements ConcurrentExecutor
    public void terminate() {
        synchronized (this) {
            _terminate = true;
            this.notify();
        }
    }

    /**
     * Returns the name of this concurrent thread as well as its calling source 
     * (in parenthesis).
     * 
     * @return the string representation of this thread.
     */
    public String toString() {
        return _name + "(" + _source + ")";
    }

}
