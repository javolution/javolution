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
import javolution.JavolutionError;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a heap context for which {@link ObjectFactory
 *     object factories} return new heap allocated objects unless an explicitly 
 *     {@link ObjectPool#recycle(Object) recycled} instance is available.
 *     For example:[code]
 *         ObjectPool<char[]> pool = CHAR_4096_FACTORY.currentPool();
 *         char[] buffer = pool.next(); // May return an explicitly recycled instance.
 *         while (reader.read(buffer) > 0) { ... }
 *         pool.recycle(buffer); // Explicitly recycles the temporary buffer,
 *                               // (for potential reuse by the current thread). 
 *     [/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.8, May 14, 2006
 */
public class HeapContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new HeapContext().getClass();

    /**
     * Holds the XML representation for heap contexts
     * (holds sizes of associated pools).
     */
    protected static final XMLFormat/*<HeapContext>*/ XML = new XMLFormat(CLASS) {
        public void read(InputElement xml, Object obj) throws XMLStreamException {
            final HeapContext ctx = (HeapContext) obj;
            FastTable pools = (FastTable) xml.get("pools");
            ctx.setPools(pools);
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            final HeapContext ctx = (HeapContext) obj;
            xml.add(ctx._pools, "pools");
        }
    };

    /**
     * Holds the pools held by this context.
     */
    final ObjectPool[] _pools = new ObjectPool[ObjectFactory._Instances.length];

    /**
     * Holds the pools currently in use. 
     */
    private final ObjectPool[] _inUsePools = new ObjectPool[ObjectFactory._Instances.length];

    /**
     * Holds the number of pools used.
     */
    private int _inUsePoolsLength;

    /**
     * Default constructor.
     */
    public HeapContext() {
    }

    /**
     * Returns the pools used by this heap context (for example when 
     * an object is explicitely recycled).
     * 
     * @return this context's pools.
     */
    public FastTable getPools() {
        FastTable pools = FastTable.newInstance();
        for (int i = ObjectFactory._Count; i > 0;) {
            ObjectPool pool = _pools[--i];
            if (pool != null) {
                pools.add(pool);
            }
        }
        return pools;
    }
    
    /**
     * Sets the pools for this heap context.
     * 
     * @param pools this context new pools.
     */
    public void setPools(FastTable pools) {
        for (int i = 0;  i < pools.size(); i++) {
            ObjectPool pool = (ObjectPool) pools.get(i);
            _pools[pool.getFactory()._index] = pool;
        }
    }
    
    /**
     * Returns the current heap context or <code>null<code> if the current
     * thread executes within a {@link PoolContext}.  
     *
     * @return the current heap context.
     */
    public static/*HeapContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof PoolContext)
                return null;
            if (ctx instanceof HeapContext)
                return (HeapContext) ctx;
        }
        throw new JavolutionError("No heap context or pool context");
    }

    /**
     * Enters a {@link HeapContext}.
     */
    public static void enter() {
        Context.enter(HeapContext.CLASS);
    }

    /**
     * Exits the current {@link HeapContext}.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of HeapContext. 
     */
    public static void exit() {
        Context.exit(HeapContext.CLASS);
    }

    /**
     * Moves all objects belonging to this pool context to the heap.
     */
    public void clear() {
        super.clear();
        for (int i = ObjectFactory._Count; i > 0;) {
            ObjectPool pool = _pools[--i];
            if (pool != null) {
                pool.clearAll();
            }
        }
        _inUsePoolsLength = 0;
    }

    // Implements Context abstract method.
    protected void enterAction() {
        Context ctx = this.getOuter();
        if (ctx != null) {
            ctx.setPoolsActive(false);
        }
        _poolsShortcut = _pools;
    }

    // Implements Context abstract method.
    protected void exitAction() {
        // Clears used pools.
        for (int i = _inUsePoolsLength; i > 0;) {
            ObjectPool pool = _inUsePools[--i];
            pool._user = null;
            pool._inUse = false;
        }
        _inUsePoolsLength = 0;

        Context ctx = this.getOuter();
        if (ctx != null) {
            ctx.setPoolsActive(true);
        }
    }

    // Overrides. 
    void setPoolsActive(boolean value) {
        Thread user = value ? getOwner() : null;
        for (int i = _inUsePoolsLength; i > 0;) {
            _inUsePools[--i]._user = user;
        }
    }

    // Overrides.
    ObjectPool getPool(int index, boolean activate) {
        ObjectPool pool = _pools[index];
        if (pool == null) {
            _tmpIndex = index;
            MemoryArea.getMemoryArea(this).executeInArea(NEW_POOL);
            pool = _pools[index];
        }
        if (!pool._inUse) { // Marks it in use.
            pool._inUse = true;
            _inUsePools[_inUsePoolsLength++] = pool;
        }
        if (activate) {
            pool._user = getOwner();
        }
        return pool;
    }

    private int _tmpIndex;

    private Runnable NEW_POOL = new Runnable() {
        public void run() {
            _pools[_tmpIndex] = ObjectFactory._Instances[_tmpIndex]
                    .newHeapPool();
        }

    };

}