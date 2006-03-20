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
import javolution.lang.Reflection;
import javolution.util.FastMap;
import javolution.xml.XmlElement;
import javolution.xml.XmlException;
import javolution.xml.XmlFormat;

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
 * <p> Finally, the framework guarantees that all pool objects belongs to 
 *     the same memory area (the memory area where the pool context resides).
 *     In other words, pool contexts allocated in ImmortalMemory can safely 
 *     be used by <code>NoHeapRealtimeThread</code> executing in 
 *     <code>ScopedMemory</code>.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public class PoolContext extends Context {

    /**
     * Holds the XML representation for pool contexts. It holds the sizes of 
     * the pool being used in order to repopulate these pools during
     * deserialization.
     */
    protected static final XmlFormat XML = new XmlFormat("javolution.realtime.PoolContext") {
        public void format(Object obj, XmlElement xml) {
            PoolContext ctx = (PoolContext) obj;
            FastMap pools = FastMap.newInstance();
            for (int i = ObjectFactory.Count; --i >= 0;) {
                int size = ctx._pools[i].size();
                if (size > 0) {
                    pools.put(ObjectFactory.INSTANCES[i].getClass(), new Integer(size));
                }
            }
            xml.add(pools);
        }

        public Object parse(XmlElement xml) {
            PoolContext ctx = (PoolContext) xml.object();
            FastMap pools = (FastMap) xml.getNext();
            for (FastMap.Entry e=pools.head(), end = pools.tail(); (e=(FastMap.Entry)e.getNext())!= end;) {
                Class factoryClass = (Class) e.getKey();
                int size = ((Integer)e.getValue()).intValue();
                ObjectFactory factory = ObjectFactory.getInstance(factoryClass);
                if (factory == null) throw new XmlException("Factory for class " + factoryClass + " not found");
                ObjectPool pool = factory.newPool();
                while (pool.size() < size) {
                    pool.next();
                }
                pool.recycleAll();
                ctx._pools[factory._index] = pool;
            }
            return ctx;
        }
    };
    
    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = Reflection
            .getClass("javolution.realtime.PoolContext");

    /**
     * Holds the pools for this context.
     */
    final ObjectPool[] _pools = new ObjectPool[ObjectFactory.MAX];

    /**
     * Holds the pools in use.
     */
    private final ObjectPool[] _inUsePools = new ObjectPool[ObjectFactory.MAX]; 

    /**
     * Holds the number of pools used.
     */
    private int _inUsePoolsLength;

    /**
     * Default constructor.
     */
    public PoolContext() {
        for (int i = _pools.length; i > 0;) {
            _pools[--i] = ObjectPool.NULL;
        }
    }

    /**
     * Returns the current pool context or <code>null<code> if the current
     * thread does not execute within a pool context.  
     *
     * @return the current pool context.
     */
    public static/*PoolContext*/Context current() {
        return Context.current().inheritedPoolContext;
    }

    /**
     * Enters a {@link PoolContext} allocated in the same memory area
     * as the memory area of the current context.
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
    private ObjectPool getPool(final int index) {
        ObjectPool pool = _pools[index];
        if (pool == ObjectPool.NULL) { // Creates pool.
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _pools[index] = ObjectFactory.INSTANCES[index].newPool();
                }});
        }
        pool = _pools[index];
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