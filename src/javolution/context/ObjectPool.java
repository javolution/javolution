/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.JavolutionError;

/**
 * <p> This abstract class represents an object pool (context-local).</p>
 *     
 * <p> Objects are always allocated in the same memory area as the pool 
 *     object itself. In other words, <code>NoHeapRealtimeThread</code>
 *     executing in <code>ScopedMemory</code> can safely use pools allocated in 
 *     <code>ImmortalMemory</code>.
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, December 20, 2005
 */
public abstract class ObjectPool/*<T>*/ {

    /**
     * Holds the current user of this pool.  
     */
    transient Thread _user;

    /**
     * Indicates if this pool is in use by this context or an inner context.  
     */
    transient boolean _inUse;

    /**
     * Default constructor.
     */
    protected ObjectPool() {
    }

    /**
     * Returns the current user of this pool or <code>null</code> if none.
     * If the user is the current thread, then this pool is the current pool.
     * 
     * @return the pool current user.  
     */
    public final Thread getUser() {
        return _user;
    }

    /**
     * Indicates if this pool is currently active for the current thread.
     * The framework ensures that local pools are visible to their users only
     * (concurrent executions are performed in an inner pool context).
     * Operations on local pools are therefore thread-safe without requiring
     * synchronization.
     * 
     * @return <code>true</code> if this pool is local for the current thread;
     *         <code>false</code> otherwise.
     * @throws JavolutionError if this operation is called upon a pool 
     *         not currently {@link #inUse in use}.
     */
    public final boolean isLocal() {
        if (_user == Thread.currentThread()) return true; // Local pool.
        if (!_inUse) throw new JavolutionError(
            "Access to inner pool or unused pool detected");
        if (_user == null) return false; // Outer pool or heap pool.
        throw new JavolutionError("Concurrent access to local pool detected");
    }

    /**
     * Indicates if this pool is in use. A pool can be in use and not having a
     * current user if the user has entered an inner pool.
     * 
     * @return <code>true</code> if at least one thread has been allocating from 
     *         this pool; <code>false</code> if this pool is not being used at 
     *         all.
     */
    public final boolean inUse() {
        return _inUse;
    }

    /**
     * Returns the number of objects held by this pool.
     * 
     * @return this pool size.
     */
    public abstract int getSize();

    /**
     * Sets the number of objects held by this pool (this method is typically
     * used to preallocate the pool after creation).
     * 
     * @param size this pool size.
     */
    public abstract void setSize(int size);

    /**
     * Returns the next available object from this pool. If there is none,
     * a new object might be allocated.
     * 
     * @return the next available object from this pool.
     */
    public abstract Object/*{T}*/ next();

    /**
     * Explicitly recycles the specified object. Callers should make sure that 
     * the recycled object is not going to be referenced anymore
     * (e.g. temporary object). This method will raise an exception if the 
     * specified object do not belong to the current pool context (or heap for 
     * threads executing in a heap context).
     * 
     * @param obj the object to recycle to this pool.
     * @throws IllegalArgumentException if the specified object belongs to 
     *         to a different pool.
     */
    public abstract void recycle(Object/*{T}*/ obj);

    /////////////////////
    // Control Methods //
    /////////////////////

    /**
     * Recycles all the objects of this pool (all used objects become new).
     * 
     * <p> Note: This method is called upon {@link PoolContext#exit exit}
     *           of a pool context for which this pool has been used.</p>
     */
    protected abstract void recycleAll();

    /**
     * Removes all objects (used and new) from this pool.
     */
    protected abstract void clearAll();

}