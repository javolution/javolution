/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import j2mex.realtime.MemoryArea;
import j2mex.realtime.RealtimeThread;

import javolution.lang.Reflection;
import javolution.realtime.ConcurrentContext.Logic;

/**
 * <p> This class represents "worker" threads employed by 
 *     {@link ConcurrentContext} to perform concurrent execution 
 *     on multi-processors systems.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public class ConcurrentThread extends RealtimeThread {

    private boolean _terminate;
    private Logic _logic;
    private Object[] _args;
    private ConcurrentContext _context;
    private MemoryArea _area;
    private final PoolContext _pool = new PoolContext();
    private final Runnable _runLogic = new Runnable() {
        public void run() {
            _logic.run(_args);
        }
    };

    /**
     * Default constructor.
     */
    public ConcurrentThread() {
        if (SET_DAEMON != null) {
            SET_DAEMON.invoke(this, new Boolean(true));
        }
    }
    private static final Reflection.Method SET_DAEMON 
        = Reflection.getMethod("java.lang.Thread.setDaemon(boolean)");

    /**
     * Overrides parent's <code>run</code> method.
     */
    public final void run() {
        while (true) {
            synchronized (this) {
                while ((_logic == null) && (!_terminate)) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new ConcurrentException(e);
                    }
                }
           }
           if (_logic == null) return; // Terminate.
           try {
               Thread parent = _context.getOwner();
               Thread.currentThread().setPriority(parent.getPriority());
               Context.setCurrent(_pool, _context);
               try {
                   _area.executeInArea(_runLogic);
               } finally {
                   _pool.recyclePools();
               }
           } catch (Throwable error) {
               _context.setError(error);
           } finally {
               _context.decreaseActiveCount();
               synchronized (this) {
                   for (int i=0; i < _args.length;) {
                       _args[i++] = null;
                   }
                   _logic = null;
                   _context = null;
                   _area = null;
                   this.notify();
               }
           }
        }
    }

    /**
     * Overrides parent's <code>toString</code> method.
     */
    public String toString() {
        return "Concurrent-" + super.toString();
    }

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args = _args0;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args0 = new Object[0];
    
    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args1[0] = arg0;
                _args = _args1;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args1 = new Object[1];

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, Object arg1, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args2[0] = arg0;
                _args2[1] = arg1;
                _args = _args2;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args2 = new Object[2];

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, Object arg1, Object arg2, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args3[0] = arg0;
                _args3[1] = arg1;
                _args3[2] = arg2;
                _args = _args3;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args3 = new Object[3];

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, Object arg1, Object arg2, Object arg3, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args4[0] = arg0;
                _args4[1] = arg1;
                _args4[2] = arg2;
                _args4[3] = arg3;
                _args = _args4;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args4 = new Object[4];

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args5[0] = arg0;
                _args5[1] = arg1;
                _args5[2] = arg2;
                _args5[3] = arg3;
                _args5[4] = arg4;
                _args = _args5;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args5 = new Object[5];

    /**
     * Executes the specified logic asynchronously.
     */
    boolean execute(Logic logic, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, ConcurrentContext context, MemoryArea area) {
        synchronized (this) {
            if ((_logic == null) && (!_terminate)) {
                _logic = logic;
                _args6[0] = arg0;
                _args6[1] = arg1;
                _args6[2] = arg2;
                _args6[3] = arg3;
                _args6[4] = arg4;
                _args6[5] = arg5;
                _args = _args6;
                _context = context;
                _area = area;
                this.notify();
                return true;
            } else {
                return false;
            }
        }
    }
    private final Object[] _args6 = new Object[6];

    /**
     * Terminates this thread (called when holder context is disposed).
     */
    public void terminate() {
        synchronized (this) {
            _terminate = true;
            this.notify();
        }
    }
}