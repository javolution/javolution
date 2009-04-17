/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
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
 * <p> This class represents the default allocator context. Allocations are 
 *     performed using the <code>new</code> keyword and explicit object 
 *     {@link ObjectFactory#recycle(Object) recycling} is supported:[code]
 *         char[] buffer = ArrayFactory.CHARS_FACTORY.array(4098); // Possibly recycled.
 *         while (reader.read(buffer) > 0) { ... }
 *         ArrayFactory.CHARS_FACTORY.recycle(buffer); // Explicit recycling.
 *     [/code]</p>
 *
 * <p> It should be noted that object recycling is performed on a thread basis
 *     (for performance reasons) and should only be performed if the object
 *     has been factory produced by the same thread doing the recycling.
 *     It is usually not a problem because recycling is done for temporary
 *     objects within the same method. For example:[code]
 *     public String toString() {
 *         TextBuilder tmp = TextBuilder.newInstance(); // Calls ObjectFactory.object()
 *         try {
 *             tmp.append(...);
 *             ...
 *             return tmp.toString();
 *         } finally {
 *             TextBuilder.recycle(tmp); // Calls ObjectFactory.recycle(...)
 *         }
 *     }[/code]
 *     If allocation/recycling is performed by different threads then
 *     {@link PoolContext} should be employed.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 19, 2009
 */
public class HeapContext extends AllocatorContext {

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
     * Enters a heap context.
     */
    public static void enter() {
         Context.enter(HeapContext.class);
    }

    /**
     * Exits the current heap context.
     * 
     * @throws ClassCastException if the context is not a heap context.
     */
    public static void exit() {
          Context.exit(HeapContext.class);
    }

    /**
     * Default constructor.
     */
    public HeapContext() {
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
        HeapAllocator allocator = (HeapAllocator) factoryToAllocator.get(factory);
        if (allocator == null) {
            allocator = new HeapAllocator(factory);
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
    private static final class HeapAllocator extends Allocator {

        private final ObjectFactory _factory;
        private final FastTable _recycled = new FastTable();

        public HeapAllocator(ObjectFactory factory) {
            _factory = factory;
        }

        protected Object allocate() {
            return _recycled.isEmpty() ? _factory.create() :
                _recycled.removeLast();
        }

        protected void recycle(Object object) {
            if (_factory.doCleanup())
                _factory.cleanup(object);
            _recycled.addLast(object);
        }

        public String toString() {
            return "Heap allocator for " + _factory.getClass();
        }
    }
}