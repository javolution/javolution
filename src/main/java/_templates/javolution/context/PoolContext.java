/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2009 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.context;

import _templates.java.lang.ThreadLocal;
import _templates.javolution.util.FastMap;
import _templates.javolution.util.FastTable;

/**
 * <p> This class represents a shared pool context for object
 *     allocation/recycling. Unlike {@link HeapContext}, objects recycled
 *     can be reused by all threads running in a {@link PoolContext}.[code]
 *         // To avoid each new thread to enter a PoolContext, the default
 *         // allocator context can be configured to PoolContext.class
 *         Thread thread1 = new Thread() {
 *            public void run() {
 *                PoolContext.enter();
 *                try {
 *                    ...
 *                    myObject = myObjectFactory.object(); // Returns an object potentially recycled by thread2.
 *                    ...
 *                } finally {
 *                    PoolContext.exit();
 *                }
 *            }};
 *        Thread thread2 = new Thread() {
 *            public void run() {
 *                PoolContext.enter();
 *                try {
 *                    ...
 *                    myObjectFactory.recycle(myObject); // Recycles an object potentially created by thread1.
 *                    ...
 *                } finally {
 *                    PoolContext.exit();
 *                }
 *            }};
 *     [/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 10, 2009
 */
public class PoolContext extends AllocatorContext {

    /**
     * Holds the factory to allocator mapping (per thread).
     */
    private static final ThreadLocal FACTORY_TO_ALLOCATOR = new ThreadLocal() {

        protected Object initialValue() {
            return new FastMap();
        }
    };
    /**
     * Holds the allocators which have been activated (per thread).
     */
    private static final ThreadLocal ACTIVE_ALLOCATORS = new ThreadLocal() {

        protected Object initialValue() {
            return new FastTable();
        }
    };

    /**
     * Enters a pool context.
     */
    public static void enter() {
         Context.enter(PoolContext.class);
    }

    /**
     * Exits the current pool context.
     *
     * @throws ClassCastException if the context is not a pool context.
     */
    public static void exit() {
         Context.exit(PoolContext.class);
    }

    /**
     * Default constructor.
     */
    public PoolContext() {
    }

    // Overrides.
    protected void deactivate() {
        FastTable allocators = (FastTable) ACTIVE_ALLOCATORS.get();
        for (int i = 0, n = allocators.size(); i < n;) {
            ((Allocator) allocators.get(i++)).user = null;
        }
        allocators.clear();
    }

    // Overrides.
    protected Allocator getAllocator(ObjectFactory factory) {
        final FastMap factoryToAllocator = (FastMap) FACTORY_TO_ALLOCATOR.get();
        PoolAllocator allocator = (PoolAllocator) factoryToAllocator.get(factory);
        if (allocator == null) {
            allocator = new PoolAllocator(factory);
            factoryToAllocator.put(factory, allocator);
        }
        if (allocator.user == null) { // Activate.
            allocator.user = Thread.currentThread();
            FastTable activeAllocators = (FastTable) ACTIVE_ALLOCATORS.get();
            activeAllocators.add(allocator);
        }
        return allocator;
    }

    // Overrides.
    protected void enterAction() {
        getOuter().getAllocatorContext().deactivate();
    }

    // Overrides.
    protected void exitAction() {
        this.deactivate();
    }

    // Holds heap allocator implementation.
    private static final class PoolAllocator extends Allocator {

        private static final FastMap FACTORY_TO_POOL = new FastMap();
        private final ObjectFactory _factory;
        private final FastTable _recycled;

        public PoolAllocator(ObjectFactory factory) {
            _factory = factory;
            synchronized (FACTORY_TO_POOL) {
                FastTable recycled = (FastTable) FACTORY_TO_POOL.get(factory);
                if (recycled == null) {
                    recycled = new FastTable();
                    FACTORY_TO_POOL.put(factory, recycled);
                }
                _recycled = recycled;
            }
        }

        protected Object allocate() {
            if (_recycled.isEmpty()) return _factory.create();
            synchronized (_recycled) {
                return _recycled.removeLast();
            }
        }

        protected void recycle(Object object) {
            if (_factory.doCleanup())
                _factory.cleanup(object);
            synchronized (_recycled) {
               _recycled.addLast(object);
            }
        }

        public String toString() {
            return "Pool allocator for " + _factory.getClass();
        }
    }
}