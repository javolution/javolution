/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

/**
 * <p> This class represents a pool context; it is used to recycle objects 
 *     transparently, reduce memory allocation and avoid garbage collection.</p>
 *     
 * <p> Threads executing in a pool context may allocate objects from 
 *     the context's pools (also called "stack") through an 
 *     {@link ObjectFactory}. Allocated objects are recycled automatically 
 *     upon {@link PoolContext#exit exit}. This recycling is almost 
 *     instantaneous and has no impact on performance.</p>  
 *     
 * <p> Objects allocated within a pool context should not be directly 
 *     referenced outside of the context unless they are  
 *     {@link RealtimeObject#export exported} (e.g. result being returned)
 *     or {@link RealtimeObject#preserve preserved} (e.g. shared static 
 *     instance). If this simple rule is followed, then pool context are
 *     completely safe. In fact, pool contexts promote the use of immutable
 *     objects (as their allocation cost is then negligible with no adverse 
 *     effect on garbarge collection) and often lead to safer, faster and
 *     more robust applications.</p>
 *     
 * <p> Upon thread termination, pool objects associated to a thread are 
 *     candidate for garbage collection (the "export rule" guarantees that these
 *     objects are not referenced anymore). They will be collected after 
 *     the thread finalization. It is also possible to move all pools' objects 
 *     to the heap directly (for early garbage collection) by calling the 
 *     {@link PoolContext#clear} method.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, September 24, 2005
 */
public class PoolContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new PoolContext(0).getClass();

    /**
     * Holds the pools for this context.
     */
    final ObjectPool[] _pools;

    /**
     * Holds the pools in use.
     */
    private final ObjectPool[] _inUsePools;

    /**
     * Holds the number of pools used.
     */
    private int _inUsePoolsLength;

    /**
     * Default constructor.
     */
    public PoolContext() {
        this(ObjectFactory.MAX);
    }

    /**
     * Creates of pool context of specified capacity.
     * 
     * @param capacity the maximum number of pool.
     */
    private PoolContext(int capacity) {
        _pools = new ObjectPool[capacity];
        _inUsePools = new ObjectPool[capacity];
        for (int i = _pools.length; i > 0;) {
            _pools[--i] = ObjectPool.NULL;
        }
    }

    /**
     * Enters a {@link PoolContext}.
     */
    public static void enter() {
        Context.enter(PoolContext.CLASS);
    }

    /**
     * Exits the current {@link PoolContext}.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of PoolContext. 
     */
    public static void exit() {
        Context.exit(PoolContext.CLASS);
    }

    /**
     * Returns the current pool context or <code>null<code> if the current
     * thread does not execute within a pool context.  
     *
     * @return the current pool context.
     */
    public static PoolContext currentPoolContext() {
        return Context.currentContext().inheritedPoolContext;
    }

    /**
     * Moves all objects belonging to this pool context to the heap.
     */
    public void clear() {
        super.clear();
        for (int i = ObjectFactory.Count; i > 0;) {
            ObjectPool pool = _pools[--i];
            if (pool != ObjectPool.NULL) {
                pool.clearAll();
            }
        }
        _inUsePoolsLength = 0;
    }

    // Implements Context abstract method.
    protected void enterAction() {
        inheritedPoolContext = this; // Overrides inherited.
        PoolContext outer = getOuter().inheritedPoolContext;
        if (outer != null) {
            outer.setInUsePoolsLocal(false);
        }
    }

    // Implements Context abstract method.
    protected void exitAction() {
        recyclePools();
        PoolContext outer = getOuter().inheritedPoolContext;
        if (outer != null) {
            outer.setInUsePoolsLocal(true);
        }
    }

    /**
     * Sets the pool currently being used as local or non-local.
     *
     * @param areLocal <code>true</code> if this context is the current 
     *        pool context; <code>false</code> otherwise.
     */
    void setInUsePoolsLocal(boolean areLocal) {
        Thread user = areLocal ? getOwner() : null;
        for (int i = _inUsePoolsLength; i > 0;) {
            _inUsePools[--i].user = user;
        }
    }

    /**
     * Returns the pool from the specified factory and marks it as local. 
     *
     * @param index the factory index for the pool to return.
     * @return the corresponding pool marked as local. 
     */
    ObjectPool getLocalPool(int index) {
        ObjectPool pool = _pools[index];
        return (pool.user != null) ? pool : getLocalPool2(index);
    }

    private ObjectPool getLocalPool2(int index) {
        ObjectPool pool = getPool(index);
        pool.user = getOwner();
        return pool;
    }

    /**
     * Returns the pool from the specified factory in this context.
     * The pool returned is marked "in use" and its outer is set.
     *
     * @param index the factory index of the pool to return.
     * @return the corresponding pool. 
     */
    private ObjectPool getPool(int index) {
        ObjectPool pool = _pools[index];
        if (pool == ObjectPool.NULL) { // Creates pool.
            pool = ObjectFactory.INSTANCES[index].newPool();
            _pools[index] = pool;
        }
        if (!pool.inUse) { // Marks it used and set its outer.
            pool.inUse = true;
            _inUsePools[_inUsePoolsLength++] = pool;
            PoolContext outerPoolContext = this.getOuter().inheritedPoolContext;
            if (outerPoolContext != null) {
                synchronized (outerPoolContext) { // Not local.
                    pool.outer = outerPoolContext.getPool(index);
                }
            } else {
                pool.outer = null;
            }
        }
        return pool;
    }

    /**
     * Recycles pools.
     */
    void recyclePools() {
        // Recycles pools and reset pools used.
        for (int i = _inUsePoolsLength; i > 0;) {
            ObjectPool pool = _inUsePools[--i];
            pool.recycleAll();
            pool.user = null;
            pool.inUse = false;
        }
        _inUsePoolsLength = 0;
    }
}