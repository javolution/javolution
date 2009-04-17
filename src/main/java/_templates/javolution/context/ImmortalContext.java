/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.context;

import _templates.java.lang.ThreadLocal;
import _templates.javax.realtime.MemoryArea;
import _templates.javolution.util.FastMap;
import _templates.javolution.util.FastTable;

/**
 * <p> This class represents an allocator from immortal memory (RTSJ).</p>
 * 
 * <p> It is typically used to allocate (and recycle) from immortal memory
 *     allowing dynamically created static instances to be accessible by 
 *     <code>NoHeapRealtimeThread</code>:[code] 
 *         public synchronized Text intern() {
 *             if (!INTERN_INSTANCES.containsKey(this)) {
 *                 ImmortalContext.enter(); 
 *                 try { // Forces interned instance to be in immortal memory.
 *                     Text txt = this.copy(); // In ImmortalMemory.
 *                     INTERN_INSTANCES.put(txt, txt);
 *                 } finally {
 *                     ImmortalContext.exit();
 *                 }
 *             }
 *             return (Text) INTERN_INSTANCES.get(str);
 *         }[/code]</p>
 * <p> Because class initialization may occur while running in a non-heap
 *     context, it is recommended to force factory produced constants
 *     to immortal memory:[code]
 *         public class Rational {
 *             public static final Rational ZERO;
 *             public static final Rational ONE;
 *             ...
 *             static { // Forces constants to ImmortalMemory.
 *                 ImmortalContext.enter();
 *                 try {
 *                     ZERO = Rational.valueOf(0, 1); // Factory produced.
 *                     ONE = Rational.valueOf(1, 1); // Factory produced.
 *                 } finally {
 *                     ImmortalContext.exit();
 *                 } 
 *             }
 *        }[/code]</p>
 *        
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 19, 2007
 */
public final class ImmortalContext extends AllocatorContext {

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
     * Enters an immortal memory context.
     */
    public static void enter() {
         Context.enter(ImmortalContext.class);
    }

    /**
     * Exits the current immortal memory context.
     * 
     * @throws ClassCastException if the context is not an immortal context.
     */
    public static void exit() {
        Context.exit(ImmortalContext.class);
    }

    /**
     * Default constructor (private, instances are factory produced).
     */
    private ImmortalContext() {
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
        ImmortalAllocator allocator = (ImmortalAllocator) factoryToAllocator.get(factory);
        if (allocator == null) {
            allocator = new ImmortalAllocator(factory);
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

    // Holds immortal allocator implementation.
    private static final class ImmortalAllocator extends Allocator {

        private static final MemoryArea IMMORTAL = MemoryArea.getMemoryArea("");
        private final ObjectFactory _factory;
        private Object _allocated;

        public ImmortalAllocator(ObjectFactory factory) {
            _factory = factory;
        }
        private final Runnable _allocate = new Runnable() {

            public void run() {
                _allocated = _factory.create();
            }
        };
        private final Runnable _resize = new Runnable() {

            public void run() {
                resize();
            }
        };

        protected Object allocate() {
            IMMORTAL.executeInArea(_allocate);
            return _allocated;
        }

        protected void recycle(Object object) {
            if (_factory.doCleanup())
                _factory.cleanup(object);
            if (queueSize >= queue.length)
                IMMORTAL.executeInArea(_resize);
            queue[queueSize++] = object;
        }

        public String toString() {
            return "Immortal allocator for " + _factory.getClass();
        }
    }

    // Allows instances to be factory produced (private constructor).
    static {
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new ImmortalContext();
            }
        }, ImmortalContext.class);
    }
}