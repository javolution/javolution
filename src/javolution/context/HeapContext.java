/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2me.lang.ThreadLocal;
import j2me.lang.UnsupportedOperationException;
import javolution.JavolutionError;

/**
 * <p> This class represents a heap context. In this context  
 *     {@link ObjectFactory object factories} return new objects from the
 *     heap unless explicitely {@link ObjectFactory#recycle(Object) recycled}
 *     instances are available. For example:[code]
 *         // The default context is a HeapContext.
 *         char[] buffer = CHAR_4096_FACTORY.object(); // Possibly recycled.
 *         while (reader.read(buffer) > 0) { ... }
 *         CHAR_4096_FACTORY.recycle(buffer); // Explicitely recycles the buffer.
 *         ...
 *         static ObjectFactory<char[]> CHAR_4096_FACTORY = new ObjectFactory<char[]>() { 
 *             protected char[] create() {
 *                 return new char[4096];
 *             }
 *         };
 *     [/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
public class HeapContext extends Context {

    /**
     * Holds the context factory.
     */
    private static Factory FACTORY = new Factory() {
        protected Object create() {
            return new HeapContext();
        } 
    };

    /**
     * Holds the thread-local pools (for explicit object recycling).
     */
    private final ThreadLocal _localPools = new ThreadLocal() {
        protected Object initialValue() {
            LocalPools pools = new LocalPools(false);
            pools._owner = Thread.currentThread();
            return pools;
        }
    };

    /**
     * Default constructor.
     */
    public HeapContext() {
    }

    /**
     * Returns the current heap context or <code>null<code> if the current
     * thread executes within a {@link PoolContext}.  
     *
     * @return the current heap context.
     */
    public static/*HeapContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof HeapContext)
                return (HeapContext) ctx;
            if (ctx instanceof PoolContext)
                return null;
        }
        throw new JavolutionError("No heap context or pool context");
    }

    /**
     * Enters a {@link HeapContext} possibly recycled.
     */
    public static void enter() {
        HeapContext ctx = (HeapContext) FACTORY.object();
        ctx._isInternal = true;
        Context.enter(ctx);
    }
    private transient boolean _isInternal;

    /**
     * Exits and recycles the current {@link HeapContext}.
     *
     * @throws UnsupportedOperationException if the current context 
     *         has not been entered using HeapContext.enter() 
     */
    public static void exit() {
        HeapContext ctx = (HeapContext) Context.current();
        if (!ctx._isInternal) throw new UnsupportedOperationException
           ("The context to exit must be specified");
        ctx._isInternal = false;
        Context.exitNoCheck(ctx);
        FACTORY.recycle(ctx);
    }

    /**
     * Clears the pools associated to this context (for the current thread
     * only).
     */
    public void clear() {
        ((LocalPools) _localPools.get()).clear();
    }

    // Implements Context abstract method.
    protected void enterAction() {
        Context outer = this.getOuter();
        outer.getLocalPools().deactivatePools();
        this.getLocalPools().activatePools();
    }

    // Implements Context abstract method.
    protected void exitAction() {
        this.getLocalPools().deactivatePools();
        Context outer = this.getOuter();
        outer.getLocalPools().activatePools();
    }

    final LocalPools getLocalPools() {
        return (LocalPools) _localPools.get();
    }

}